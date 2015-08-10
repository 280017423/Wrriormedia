package com.warriormedia.app.ui.widget.dialog;

/**
 * Implement this interface in Activity or Fragment to react to negative dialog buttons.
 *
 * @author zou.sq
 * @since 2.1.0
 */
public interface INegativeButtonDialogListener {

    void onNegativeButtonClicked(int requestCode);
}
