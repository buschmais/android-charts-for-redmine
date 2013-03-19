package de.buschmais.mobile.redmine.exception;

/**
 * An exception that indicates if the provided credentials are invalid.
 */
public class InvalidCredentialsException extends Exception
{
    /** serial version UID */
    private static final long serialVersionUID = 4231628767535152996L;

    public InvalidCredentialsException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }

    public InvalidCredentialsException(String detailMessage)
    {
        super(detailMessage);
    }

}
