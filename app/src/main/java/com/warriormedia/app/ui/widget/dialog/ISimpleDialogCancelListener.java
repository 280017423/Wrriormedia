package com.warriormedia.app.ui.widget.dialog;

/**
 * Implement this interface in Activity or Fragment to react when the dialog is cancelled.
 * This listener is common for all types of dialogs.
 *
 * @author zou.sq
 */
public interface ISimpleDialogCancelListener {

    void onCancelled(int requestCode);
}
