package de.buschmais.mobile.redmine.exception;

/**
 * An exception indicating that the an URL is invalid.
 */
public class InvalidURLException extends Exception
{
    /** serial version UID */
    private static final long serialVersionUID = 7133224883395560261L;

    public InvalidURLException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }

}
