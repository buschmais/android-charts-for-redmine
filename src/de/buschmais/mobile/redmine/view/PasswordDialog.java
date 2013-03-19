package de.buschmais.mobile.redmine.view;

import static de.buschmais.mobile.redmine.Constants.BUNDLE_KEY_PASSWORD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import de.buschmais.mobile.redmine.R;

/**
 * A dialog that lets the user enter a password.
 */
public class PasswordDialog extends DialogFragment
{
    /** The tag that can be used within fragment transactions. */
    public static final String FRAGMENT_TRANSACTION_TAG = PasswordDialog.class.getName();
    
    private OnPasswordDialogClosedListener listener;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            listener = (OnPasswordDialogClosedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Ensure the attaching activity implements '"
                    + OnPasswordDialogClosedListener.class.getName() + "'.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        String password = null;
        
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(BUNDLE_KEY_PASSWORD))
        {
            password = getArguments().getString(BUNDLE_KEY_PASSWORD, "");
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_password, null);

        final EditText passwordEditText = (EditText) view.findViewById(R.id.editTextPassword);
        passwordEditText.setText(password);

        final CheckBox tempStoreCheckBox = (CheckBox) view.findViewById(R.id.checkBoxTempStore);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_password_title)
                .setPositiveButton(R.string.dialog_password_ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (listener != null)
                        {
                            listener.onPositiveButtonClicked(passwordEditText.getText().toString(),
                                    tempStoreCheckBox.isChecked());
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_password_cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (listener != null)
                        {
                            listener.onNegativeButtonClicked();
                        }
                    }
                })
                .setView(view)
                .create();
    }

    /**
     * Listener that will be informed which button at the dialog was clicked.
     */
    public interface OnPasswordDialogClosedListener
    {
        /**
         * The positive button of the dialog was clicked.
         * 
         * @param password the value for the password the user has entered
         * @param tempStorePassword the value for temporarily-store-the-password
         */
        void onPositiveButtonClicked(String password, boolean tempStorePassword);

        /**
         * The user has clicked the negative button.
         */
        void onNegativeButtonClicked();
    }
}
