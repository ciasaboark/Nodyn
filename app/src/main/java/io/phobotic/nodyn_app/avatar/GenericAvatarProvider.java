package io.phobotic.nodyn_app.avatar;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.helper.ColorHelper;

public class GenericAvatarProvider extends AvatarProvider {
    private static final String PREF_KEY = "generic_avatar_provider_source";
    private static final String NAME = "Generic Provider";

    @Override
    public String fetchUserAvatar(@NotNull Context context, @NotNull User user, int size) {
        String source = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        source = prefs.getString(PREF_KEY, "");

        String username = user.getUsername() == null ? "" : user.getUsername();
        source = source.replaceAll("%u", username);

        String name = user.getName() == null ? "" : user.getName();
        source = source.replaceAll("%n", name);

        String employeeID = user.getEmployeeNum() == null ? "" : user.getEmployeeNum();
        source = source.replaceAll("%i", employeeID);

        String email = user.getEmail() == null ? "" : user.getEmail();
        source = source.replaceAll("%e", email);

        String notes = user.getNotes() == null ? "" : user.getNotes();
        source = source.replaceAll("%o", notes);

        source = source.replaceAll("%%", "%");

        return source;
    }

    @Override
    public String getRequiredField() {
        return null;
    }

    @Override
    public boolean isUniversal() {
        return false;
    }

    @Nullable
    @Override
    public Drawable getIconDrawable(@NotNull Context context) {
        Drawable d = context.getDrawable(R.drawable.web);
        int c = Color.parseColor("#2196F3");
        d.setTint(c);

        return d;
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @Nullable
    @Override
    public DialogFragment getConfigurationDialogFragment(Context context) {
        return new ConfigurationDialogFragment();
    }

    public static class ConfigurationDialogFragment extends DialogFragment {
        public ConfigurationDialogFragment() {
            //required empty constructor
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_configure_generic_avatar_provider, null);
            final EditText source = (EditText) rootView.findViewById(R.id.edit_text);

            AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.cloud_sync)
                    .setTitle("Generic Avatar Provider")
                    .setView(rootView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            prefs.edit().putString(PREF_KEY, source.getText().toString()).apply();
                            dialog.dismiss();
                        }
                    });

            return b.create();
        }
    }
}
