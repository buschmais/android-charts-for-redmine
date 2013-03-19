package de.buschmais.mobile.redmine.util;

import android.os.AsyncTask;

/**
 * A helper class to allow {@link AsyncTask}s to return an error in their {@link AsyncTask#doInBackground()} method.
 *
 * @param <T> the type of the result
 */
public class AsyncTaskResult<T>
{
    /** The actual result value */
    private T t;
    /** The error - if any */
    private Exception e;
    
    /**
     * Create a new task result using the given result.
     * 
     * @param result
     */
    public AsyncTaskResult(T result)
    {
        t = result;
    }
    
    /**
     * Create a new task result using the given error.
     * @param error
     */
    public AsyncTaskResult(Exception error)
    {
        e = error;
    }
    
    /**
     * Returns if this result is in error state. If the returned value is {@code true}, the error
     * could be accessed by {@link AsyncTaskResult#getError()}.
     * 
     * @return {@code true} if there is an error state, {@code false} otherwise
     */
    public boolean isError()
    {
        return (e != null);
    }
    
    /**
     * Returns the error of this result (if any).
     * @return an exception or {@code null} if none did happen
     */
    public Exception getError()
    {
        return e;
    }
    
    /**
     * Returns the result or {@code null} if an error occured. If there was an error, the 
     * exception to this error can be accessed by {@link AsyncTaskResult#getError()}.
     * @return {@code null} or the actual result
     */
    public T getResult()
    {
        return t;
    }
}
