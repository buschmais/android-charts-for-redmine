package de.buschmais.mobile.redmine.exception;

/**
 * If a technical error occurs, something the user can not handle by himself.
 */
public class TechnicalError extends Exception
{
    /** serial version UID */
    private static final long serialVersionUID = -6796527954235449378L;

    public TechnicalError(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }

}
