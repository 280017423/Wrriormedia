package com.warriormedia.app.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.warriormedia.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base dialog fragment for all your dialogs, styleable and same design on Android 2.2+.
 *
 * @author zou.sq
 */
public abstract class BaseDialogFragment extends DialogFragment implements DialogInterface.OnShowListener {

    protected int mRequestCode;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        Dialog dialog = new Dialog(getActivity(), R.style.Dialog);
        if (args != null) {
            dialog.setCanceledOnTouchOutside(
                    args.getBoolean(BaseDialogBuilder.ARG_CANCELABLE_ON_TOUCH_OUTSIDE));
        }
        dialog.setOnShowListener(this);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity(), inflater, container);
        return build(builder).create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            mRequestCode = getTargetRequestCode();
        } else {
            Bundle args = getArguments();
            if (args != null) {
                mRequestCode = args.getInt(BaseDialogBuilder.ARG_REQUEST_CODE, 0);
            }
        }
    }

    /**
     * Customized dialogs need to be set up via provided builder.
     *
     * @param initialBuilder Provided builder for setting up customized dialog
     * @return Updated builder
     */
    protected abstract Builder build(Builder initialBuilder);

    @Override
    public void onDestroyView() {
        // bug in the compatibility library
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public void showAllowingStateLoss(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onShow(DialogInterface dialog) {
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        for (ISimpleDialogCancelListener listener : getCancelListeners()) {
            listener.onCancelled(mRequestCode);
        }
    }

    /**
     * Get dialog cancel listeners.
     * There might be more than one cancel listener.
     *
     * @return Dialog cancel listeners
     * @since 2.1.0
     */
    protected List<ISimpleDialogCancelListener> getCancelListeners() {
        return getDialogListeners(ISimpleDialogCancelListener.class);
    }

    /**
     * Utility method for acquiring all listeners of some type for current instance of DialogFragment
     *
     * @param listenerInterface Interface of the desired listeners
     * @return Unmodifiable list of listeners
     * @since 2.1.0
     */
    @SuppressWarnings("unchecked")
    protected <T> List<T> getDialogListeners(Class<T> listenerInterface) {
        final Fragment targetFragment = getTargetFragment();
        List<T> listeners = new ArrayList<>(2);
        if (targetFragment != null && listenerInterface.isAssignableFrom(targetFragment.getClass())) {
            listeners.add((T) targetFragment);
        }
        if (getActivity() != null && listenerInterface.isAssignableFrom(getActivity().getClass())) {
            listeners.add((T) getActivity());
        }
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Custom dialog builder
     */
    protected static class Builder {

        private final Context mContext;

        private final ViewGroup mContainer;

        private final LayoutInflater mInflater;

        private CharSequence mTitle = null;

        private CharSequence mPositiveButtonText;

        private View.OnClickListener mPositiveButtonListener;

        private CharSequence mNegativeButtonText;

        private View.OnClickListener mNegativeButtonListener;

        private CharSequence mMessage;

        public Builder(Context context, LayoutInflater inflater, ViewGroup container) {
            this.mContext = context;
            this.mContainer = container;
            this.mInflater = inflater;
        }

        public LayoutInflater getLayoutInflater() {
            return mInflater;
        }

        public Builder setTitle(int titleId) {
            this.mTitle = mContext.getText(titleId);
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.mTitle = title;
            return this;
        }

        public Builder setPositiveButton(int textId, final View.OnClickListener listener) {
            mPositiveButtonText = mContext.getText(textId);
            mPositiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, final View.OnClickListener listener) {
            mPositiveButtonText = text;
            mPositiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId, final View.OnClickListener listener) {
            mNegativeButtonText = mContext.getText(textId);
            mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, final View.OnClickListener listener) {
            mNegativeButtonText = text;
            mNegativeButtonListener = listener;
            return this;
        }

        public Builder setMessage(int messageId) {
            mMessage = mContext.getText(messageId);
            return this;
        }

        public Builder setMessage(CharSequence message) {
            mMessage = message;
            return this;
        }

        public View create() {
            LinearLayout content = (LinearLayout) mInflater.inflate(R.layout.view_dialog_layout, mContainer, false);
            TextView vTitle = (TextView) content.findViewById(R.id.tv_dialog_layout_title);
            TextView vMessage = (TextView) content.findViewById(R.id.tv_dialog_layout_msg);
            Button vPositiveButton = (Button) content.findViewById(R.id.btn_dialog_layout_sure);
            Button vNegativeButton = (Button) content.findViewById(R.id.btn_dialog_layout_cancel);

            set(vTitle, mTitle);
            set(vMessage, mMessage);
            set(vPositiveButton, mPositiveButtonText, mPositiveButtonListener);
            set(vNegativeButton, mNegativeButtonText, mNegativeButtonListener);
            return content;
        }

        private void set(Button button, CharSequence text, View.OnClickListener listener) {
            set(button, text);
            if (listener != null) {
                button.setOnClickListener(listener);
            }
        }

        private void set(TextView textView, CharSequence text) {
            if (text != null) {
                textView.setText(text);
            } else {
                textView.setVisibility(View.GONE);
            }
        }
    }
}