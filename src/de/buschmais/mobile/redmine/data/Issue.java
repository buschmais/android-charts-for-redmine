package de.buschmais.mobile.redmine.data;

import java.io.Serializable;
import java.util.Date;

import de.buschmais.mobile.redmine.R;

/**
 * Object representing an "issue" in Redmine.
 */
public class Issue implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 7806815857828468517L;
    
    private final int id;
    private final String subject;
    private final String description;
    private final Date created;
    private final Date updated;
    private final Status status;
    private final Priority priority;
    private final User author;
    private User assignee;
    
    /**
     * Creates a new issues using the given fields.
     * @param id the id of the issue
     * @param subject the subject of the issue
     * @param description the description of the issue
     * @param created the time of creation
     * @param updated when was the issue updated
     * @param status the status of the issue
     * @param priority the priority of the issue
     * @param author the author of the issue
     */
    public Issue(int id, String subject, String description, Date created, Date updated, Status status,
            Priority priority, User author)
    {
        super();
        this.id = id;
        this.subject = subject;
        this.description = description;
        this.created = created;
        this.updated = updated;
        this.status = status;
        this.priority = priority;
        this.author = author;
    }

    public int getId()
    {
        return id;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getDescription()
    {
        return description;
    }

    public Date getCreated()
    {
        return created;
    }

    public Date getUpdated()
    {
        return updated;
    }

    public Status getStatus()
    {
        return status;
    }

    public Priority getPriority()
    {
        return priority;
    }
    
    public User getAuthor()
    {
        return author;
    }
    
    public void setAssignee(User assignee)
    {
        this.assignee = assignee;
    }
    
    public User getAssignee()
    {
        return assignee;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Issue other = (Issue) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "Issue [id=" + id + ", subject=" + subject + ", description=" + description + ", created=" + created
                + ", updated=" + updated + ", status=" + status + ", priority=" + priority + ", author=" + author
                + ", assignee=" + assignee + "]";
    }

    /** 
     * The issue status as defined in the Redmine system.
     */
    public enum Status
    {
        New(1, R.string.issues_status_new, R.color.color_status_new), 
        In_Progress(2, R.string.issues_status_in_progress, R.color.color_status_in_progress), 
        Resolved(3, R.string.issues_status_resolved, R.color.color_status_resolved), 
        Feedback(4, R.string.issues_status_feedback, R.color.color_status_feedback), 
        Closed(5, R.string.issues_status_closed, R.color.color_status_closed), 
        Rejected(6, R.string.issues_status_rejected, R.color.color_status_rejected), 
        Unknown(-1, R.string.issues_status_unknown, R.color.color_status_new);
        
        private int id;
        private int stringResourceId;
        private int colorResourceId;
        
        private Status(int id, int resourceId, int colorResourceId)
        {
            this.id = id;
            this.stringResourceId = resourceId;
            this.colorResourceId = colorResourceId;
        };
        
        public int getId()
        {
            return id;
        }
        
        public int getStringResourceId()
        {
            return stringResourceId;
        }
        
        public int getColorResoureId()
        {
            return colorResourceId;
        }
        
        /**
         * Get the {@link Status} for the given status id. If the id is unknown, {@link Status#Unknown} is returned.
         * @param anId
         * @return 
         */
        public static Status getStatus(int anId)
        {
            int length = Status.values().length;
            for (int i = 0; i < length; i++)
            {
                Status s = Status.values()[i];
                if (s.getId() == anId)
                {
                    return s;
                }
            }
            
            return Unknown;
        }
    }
    
    /** 
     * The issue priority as defined in the Redmine system.
     */
    public enum Priority
    {
        Low(3), Normal(4), High(5), Urgent(6), Immediate(7), Unknown(-1);
        
        private int id;
        
        private Priority(int id)
        {
            this.id = id;
        };
        
        public int getId()
        {
            return id;
        }
        
        /**
         * Get the {@link Priority} for the given priority id. If the id is unknown, {@link Priority#Unknown} is returned.
         * @param anId
         * @return 
         */
        public static Priority getPriority(int anId)
        {
            int length = Priority.values().length;
            for (int i = 0; i < length; i++)
            {
                Priority p = Priority.values()[i];
                if (p.getId() == anId)
                {
                    return p;
                }
            }
            
            return Unknown;
        }
    }
}
