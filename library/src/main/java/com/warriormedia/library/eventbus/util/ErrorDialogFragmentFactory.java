package com.warriormedia.library.eventbus.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Factory to allow injecting a more complex exception mapping; typically you would subclass one of {@link Honeycomb} or
 * {@link Support}.
 */
public abstract class ErrorDialogFragmentFactory<T> {
    protected final ErrorDialogConfig config;

    protected ErrorDialogFragmentFactory(ErrorDialogConfig config) {
        this.config = config;
    }

    /**
     * Prepares the fragment's arguments and creates the fragment. May be overridden to provide custom error fragments.
     */
    protected T prepareErrorFragment(com.warriormedia.library.eventbus.util.ThrowableFailureEvent event, boolean finishAfterDialog,
                                     Bundle argumentsForErrorDialog) {
        if (event.isSuppressErrorUi()) {
            // Show nothing by default
            return null;
        }
        Bundle bundle;
        if (argumentsForErrorDialog != null) {
            bundle = (Bundle) argumentsForErrorDialog.clone();
        } else {
            bundle = new Bundle();
        }

        if (!bundle.containsKey(com.warriormedia.library.eventbus.util.ErrorDialogManager.KEY_TITLE)) {
            String title = getTitleFor(event, bundle);
            bundle.putString(com.warriormedia.library.eventbus.util.ErrorDialogManager.KEY_TITLE, title);
        }
        if (!bundle.containsKey(com.warriormedia.library.eventbus.util.ErrorDialogManager.KEY_MESSAGE)) {
            String message = getMessageFor(event, bundle);
            bundle.putString(com.warriormedia.library.eventbus.util.ErrorDialogManager.KEY_MESSAGE, message);
        }
        if (!bundle.containsKey(com.warriormedia.library.eventbus.util.ErrorDialogManager.KEY_FINISH_AFTER_DIALOG)) {
            bundle.putBoolean(com.warriormedia.library.eventbus.util.ErrorDialogManager.KEY_FINISH_AFTER_DIALOG, finishAfterDialog);
        }
        if (!bundle.containsKey(com.warriormedia.library.eventbus.util.ErrorDialogManager.KEY_EVENT_TYPE_ON_CLOSE)
                && config.defaultEventTypeOnDialogClosed != null) {
            bundle.putSerializable(com.warriormedia.library.eventbus.util.ErrorDialogManager.KEY_EVENT_TYPE_ON_CLOSE, config.defaultEventTypeOnDialogClosed);
        }
        if (!bundle.containsKey(com.warriormedia.library.eventbus.util.ErrorDialogManager.KEY_ICON_ID) && config.defaultDialogIconId != 0) {
            bundle.putInt(com.warriormedia.library.eventbus.util.ErrorDialogManager.KEY_ICON_ID, config.defaultDialogIconId);
        }
        return createErrorFragment(event, bundle);
    }

    /**
     * Returns either a new Honeycomb+ or a new support library DialogFragment.
     */
    protected abstract T createErrorFragment(com.warriormedia.library.eventbus.util.ThrowableFailureEvent event, Bundle arguments);

    /**
     * May be overridden to provide custom error title.
     */
    protected String getTitleFor(com.warriormedia.library.eventbus.util.ThrowableFailureEvent event, Bundle arguments) {
        return config.resources.getString(config.defaultTitleId);
    }

    /**
     * May be overridden to provide custom error messages.
     */
    protected String getMessageFor(com.warriormedia.library.eventbus.util.ThrowableFailureEvent event, Bundle arguments) {
        int msgResId = config.getMessageIdForThrowable(event.throwable);
        return config.resources.getString(msgResId);
    }

    public static class Support extends ErrorDialogFragmentFactory<Fragment> {

        public Support(ErrorDialogConfig config) {
            super(config);
        }

        protected Fragment createErrorFragment(com.warriormedia.library.eventbus.util.ThrowableFailureEvent event, Bundle arguments) {
            com.warriormedia.library.eventbus.util.ErrorDialogFragments.Support errorFragment = new com.warriormedia.library.eventbus.util.ErrorDialogFragments.Support();
            errorFragment.setArguments(arguments);
            return errorFragment;
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class Honeycomb extends ErrorDialogFragmentFactory<android.app.Fragment> {

        public Honeycomb(ErrorDialogConfig config) {
            super(config);
        }

        protected android.app.Fragment createErrorFragment(com.warriormedia.library.eventbus.util.ThrowableFailureEvent event, Bundle arguments) {
            com.warriormedia.library.eventbus.util.ErrorDialogFragments.Honeycomb errorFragment = new com.warriormedia.library.eventbus.util.ErrorDialogFragments.Honeycomb();
            errorFragment.setArguments(arguments);
            return errorFragment;
        }

    }
}