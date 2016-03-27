package com.way.firupgrade;

/**
 * @author way
 * @since 2014/10/29
 */
public abstract class LocaleHandler {
    protected String value;

    public abstract String getDialogTitle();

    public abstract String getProgressDialogTitle();

    public abstract String getProgressDialogMessage();

    public abstract String getToastMessage();

    public abstract String getToastNetErrorMessage();

}
