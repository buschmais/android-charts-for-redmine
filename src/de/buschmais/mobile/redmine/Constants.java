package de.buschmais.mobile.redmine;

/**
 * Class to keep all constant values.
 */
public final class Constants
{
    /**
     * Hidden private constructor.
     */
    private Constants() {}
    
    /** Settings key for the redmine URL */
    public static final String SETTINGS_KEY_REDMINE_URL = "redminUrl";
    
    /** Settings key for the user name */
    public static final String SETTINGS_KEY_USERNAME = "username";

    /** A prefix for intents. */
    private static final String INTENT_PREFIX = ".intent.";
    
    /** A key for intents to store projects in them. */
    public static final String INTENT_KEY_PROJECT = Constants.class.getPackage().getName() + INTENT_PREFIX + "project";

    /** A key for intents to store issues in them. */
    public static final String INTENT_KEY_ISSUES = Constants.class.getPackage().getName() + INTENT_PREFIX + "issues";

    /** A key for intents to store issues in them. */
    public static final String INTENT_KEY_ASSIGNEE = Constants.class.getPackage().getName() + INTENT_PREFIX + "assignee";
    
    /** A prefix for bundles. */
    private static final String BUNDLE_PREFIX = ".bundle.";
    
    /** A key for bundles to store an issue in them. */
    public static final String BUNDLE_KEY_ISSUE = Constants.class.getPackage().getName() + BUNDLE_PREFIX + "issue";
    
    /** A key for bundles to store issues in them. */
    public static final String BUNDLE_KEY_ISSUES = Constants.class.getPackage().getName() + BUNDLE_PREFIX + "issues";

    /** A key for bundles to store issues in them. */
    public static final String BUNDLE_KEY_PASSWORD = Constants.class.getPackage().getName() + BUNDLE_PREFIX + "password";
}
