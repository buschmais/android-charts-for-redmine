package de.buschmais.mobile.redmine.dao;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.buschmais.mobile.redmine.data.Issue;
import de.buschmais.mobile.redmine.data.Issue.Priority;
import de.buschmais.mobile.redmine.data.Issue.Status;
import de.buschmais.mobile.redmine.data.Project;
import de.buschmais.mobile.redmine.data.User;
import de.buschmais.mobile.redmine.exception.InvalidCredentialsException;
import de.buschmais.mobile.redmine.exception.InvalidURLException;
import de.buschmais.mobile.redmine.exception.TechnicalError;


/**
 * Data access object for a Redmine system.
 */
@SuppressLint({"UseSparseArrays", "SimpleDateFormat"})
public final class Redmine
{
    /** The log tag. */
    private static final String TAG = Redmine.class.getSimpleName();
    
    /** The cache refresh interval: 1 min */
    private static final long CACHE_REFRESH_INTERVAL = 1 * 60 * 1000;
    
    /** The cache of projects */
    private static final HashMap<Integer, Project> projectCache = new HashMap<Integer, Project>();
    /** The time the cache was refreshed */ 
    private static Date lastProjectCacheRefresh = new Date(0);
    
    /** The cache for issues */
    private static final HashMap<Integer, CacheEntry<List<Issue>>> issueCache = new HashMap<Integer, Redmine.CacheEntry<List<Issue>>>();
    
    /**
     * Get the list of all available projects at the given Redmine url. The user name and password are used to 
     * authenticate at the system. <br />
     * <b>Note:</b> this is a network operation which should not be done within the UI thread
     * 
     * @param urlString the URL
     * @param username the user name to be used
     * @param password the password to be used
     * @return a list of projects which might be empty, but never {@code null}
     * @throws InvalidURLException if the URL could not be created and thus is invalid
     * @throws TechnicalError if there was a technical problem (see included exception for more details)
     * @throws InvalidCredentialsException if the provided user credentials are wrong
     */
    public static List<Project> getAllProjects(String urlString, String username, String password) throws InvalidCredentialsException, TechnicalError, InvalidURLException
    {
        Log.d(TAG, "getAllProjects()");
        
        // see if we can collect the items from the cache
        long now = System.currentTimeMillis();
        if ((now - CACHE_REFRESH_INTERVAL) < lastProjectCacheRefresh.getTime())
        {
            Log.d(TAG, "No cache refresh necessary for project cache.");
        }
        else
        {
            Log.d(TAG, "Refreshing project cache.");
            List<Project> projects = collectAllProjects(urlString, username, password);
            
            projectCache.clear();
            
            for (Project p : projects)
            {
                projectCache.put(Integer.valueOf(p.getId()), p);
            }

            Log.d(TAG, "Project cache refresh finished, replaced " + projects.size() + " values.");
            
            lastProjectCacheRefresh = new Date(System.currentTimeMillis());
        }
        return new ArrayList<Project>(projectCache.values());
    }
    
    /**
     * Get the list of all issues for the given project using the given Redmine url. The user name and password
     * are used to authenticate at the system. <br/>
     * <b>Note:</b> this is a network operation which should not be done within the UI thread
     * 
     * @param project the project to retrieve the issues for
     * @param urlString the URL to the Redmine system
     * @param username the user name to be used
     * @param password the password to be used
     * @return a list of isses, never {@code null}
     * @throws InvalidURLException if the URL could not be created and thus is invalid
     * @throws TechnicalError if there was a technical problem (see included exception for more details)
     * @throws InvalidCredentialsException if the provided user credentials are wrong
     */
    public static List<Issue> getIssues(Project project, String urlString, String username, String password) throws InvalidCredentialsException, TechnicalError, InvalidURLException
    {
        Log.d(TAG, "getIssues()");
        
        int projectId = project.getId();
        
        CacheEntry<List<Issue>> ce = issueCache.get(projectId);
        long now = System.currentTimeMillis();
        if (ce == null)
        {
            Log.d(TAG, "No issue cache entry found for project: " + project);
            
            List<Issue> issues = collectIssues(project, urlString, username, password);
            
            ce = new CacheEntry<List<Issue>>(new Date(now), issues);
            issueCache.put(Integer.valueOf(project.getId()), ce);
            
            Log.d(TAG, "Created new cache entry for '" + project.getId() + "'.");
        }
        else
        {
            if ((now - CACHE_REFRESH_INTERVAL) < ce.getLastRefresh().getTime())
            {
                Log.d(TAG, "No cache refresh necessary for issues of project: " + project);
            }
            else
            {
                Log.d(TAG, "Updating cache issues cache for project: " + project);
                
                List<Issue> issues = collectIssues(project, urlString, username, password);
                
                ce.update(new Date(now), issues);
            }
        }
        
        return ce.getObject();
    }
    
    /**
     * Invalidates the internal cache and removes all objects from it.
     */
    public static void invalidateCache()
    {
        Log.d(TAG, "invalidateCache()");
        
        lastProjectCacheRefresh = new Date(0);
        projectCache.clear();
        issueCache.clear();
    }
    
    /**
     * Collect the list of available projects from the back end system using the given credentials.
     * 
     * @see Redmine#getAllProjects(String, String, String)
     * @param urlString
     * @param username
     * @param password
     * @return
     * @throws InvalidURLException 
     * @throws TechnicalError 
     * @throws InvalidCredentialsException 
     */
    private static List<Project> collectAllProjects(String urlString, String username, String password) throws InvalidCredentialsException, TechnicalError, InvalidURLException
    {
        Log.d(TAG, "collectAllProjects()");
        
        String completeUrl = urlString + "/projects.json?limit=100";
        
        String result = collectRedmineContent(completeUrl, username, password);
        List<Project> projects = new ArrayList<Project>();
        
        try
        {
            JSONObject resultObject = new JSONObject(result);
            JSONArray projectJsonArray = resultObject.getJSONArray("projects");
            for (int i = 0; i < projectJsonArray.length(); i++)
            {
                JSONObject projectJson = projectJsonArray.getJSONObject(i);
                
                int id = projectJson.getInt("id");
                String name = projectJson.getString("name");
                String identifier = projectJson.getString("identifier");
                String description = projectJson.getString("description");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date created = new Date();
                Date updated = new Date();
                created = sdf.parse(projectJson.getString("created_on"));
                updated = sdf.parse(projectJson.getString("updated_on"));
                
                projects.add(new Project(id, name, identifier, description, created, updated));
            }
        }
        catch (ParseException e)
        {
            throw new TechnicalError("Unable to parse date.", e);
        }
        catch (JSONException e)
        {
            throw new TechnicalError("Unable to parse result: " + result, e);
        }
        
        return projects;
    }
    
    /**
     * Collect the list of issues for the given project.
     * 
     * @see Redmine#getIssues(Project, String, String, String)
     * @param project
     * @param urlString
     * @param username
     * @param password
     * @return
     * @throws InvalidURLException 
     * @throws TechnicalError 
     * @throws InvalidCredentialsException 
     */
    private static List<Issue> collectIssues(Project project, String urlString, String username, String password) throws InvalidCredentialsException, TechnicalError, InvalidURLException
    {
        Log.d(TAG, "collectIssues()");
        
        String completeUrl = urlString + "/issues.json?limit=100&status_id=*&project_id=" + project.getId();
        
        List<Issue> issues = new ArrayList<Issue>();
        String result = collectRedmineContent(completeUrl, username, password);
        
        try
        {
            JSONObject resultObject = new JSONObject(result);
            JSONArray issueJsonArray = resultObject.getJSONArray("issues");
            for (int i = 0; i < issueJsonArray.length(); i++)
            {
                JSONObject issueJson = issueJsonArray.getJSONObject(i);
                
                int id = issueJson.getInt("id");
                String subject = issueJson.getString("subject");
                String description = issueJson.getString("description");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date created = sdf.parse(issueJson.getString("created_on"));
                Date updated =  sdf.parse(issueJson.getString("updated_on"));
                
                JSONObject statusJson = issueJson.getJSONObject("status");
                int statusId = statusJson.getInt("id");
                Status status = Status.getStatus(statusId);
                
                JSONObject priorityJson = issueJson.getJSONObject("priority");
                int priorityId = priorityJson.getInt("id");
                Priority priority = Priority.getPriority(priorityId);
                
                JSONObject authorJson = issueJson.getJSONObject("author");
                int authorId = authorJson.getInt("id");
                String authorName = authorJson.getString("name");
                User author = new User(authorId, authorName);
                
                Issue issue = new Issue(id, subject, description, created, updated, status, priority, author);
                issues.add(issue);
                
                try
                {
                    JSONObject assigneeJson = issueJson.getJSONObject("assigned_to");
                    int assigneeId = assigneeJson.getInt("id");
                    String assigneeName = assigneeJson.getString("name");
                    User assignee = new User(assigneeId, assigneeName);
                    
                    issue.setAssignee(assignee);
                }
                catch (JSONException e)
                {
                    // do nothing
                }
            }
        }
        catch (ParseException e)
        {
            throw new TechnicalError("Unable to parse date.", e);
        }
        catch (JSONException e)
        {
            throw new TechnicalError("Unable to parse result: " + result, e);
        }
        
        return issues;
    }
    
    /**
     * Collect the content from the provided URL and return it as {@link String}. The user name and password are used
     * for authentication. 
     * @param completeUrl the complete URL string, with any URL parameter needed
     * @param username the user name
     * @param password the password
     * @return the response of the request as {@link String}
     * @throws InvalidCredentialsException if authorization fails
     * @throws TechnicalError if a technical error occurs
     * @throws InvalidURLException if the provided URL is wrong
     */
    private static String collectRedmineContent(String completeUrl, String username, String password) throws InvalidCredentialsException, TechnicalError, InvalidURLException
    {
        Log.d(TAG, "collectRedmineContent() " + completeUrl);
        
        URL url;
        try
        {
            url = new URL(completeUrl);
        }
        catch (Exception e)
        {
            throw new InvalidURLException("Unable to parse: " + completeUrl, e);
        }
        
        StringBuilder result = new StringBuilder();
        BufferedReader br = null;
        HttpURLConnection connection;
        try{
            connection = (HttpURLConnection) url.openConnection();
            
            String userpass = username + ":" + password;
            String basicAuth = "Basic " + Base64.encodeToString(userpass.getBytes(), Base64.URL_SAFE);
            connection.setRequestProperty("Authorization", basicAuth);
            
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "response code: " + responseCode);
            
            if (responseCode == 401)
            {
                // bad authorization
                throw new InvalidCredentialsException("Unable to authorize at " + completeUrl);
            }
            else if ((responseCode / 100) != 2)
            {
                // some thing different than OK was returned
                throw new TechnicalError("Bad response code (" + responseCode + ") connecting to " + completeUrl, null);
            }
            
            InputStream inputStream = connection.getInputStream();
            
            br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            
            String readLine = null;
            while ((readLine = br.readLine()) != null)
            {
                result.append(readLine);
                result.append("\n");
            }
        }
        catch (IOException e)
        {
            throw new TechnicalError("IO error", e);
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Unable to close buffered reader", e);
                }
            }
        }
        
        return result.toString();
    }
    
    /**
     * Helper class for a cache entry which holds its own last refresh time.
     *
     * @param <T> the type of the actual cached object
     */
    private static class CacheEntry<T>
    {
        private Date lastRefresh;
        private T t;
        
        public CacheEntry(Date refreshTime, T object)
        {
            t = object;
            lastRefresh = refreshTime;
        }

        public Date getLastRefresh()
        {
            return lastRefresh;
        }

        public T getObject()
        {
            return t;
        }
        
        public void update(Date lastRefresh, T object)
        {
            this.lastRefresh = lastRefresh;
            this.t = object;
        }
    }
}
