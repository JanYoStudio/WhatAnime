package pw.janyo.whatanime.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pw.janyo.whatanime.R;
import pw.janyo.whatanime.util.Settings;

/**
 * Created by myste.
 */

public class SettingsFragment extends PreferenceFragment {
    private Preference resultNumberPreference;
    private Preference similarityPreference;
    private Preference licensePreference;
    private Preference janyoInfoPreference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initialization();
        monitor();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initialization() {
        resultNumberPreference = findPreference(getString(R.string.key_result_number));
        similarityPreference = findPreference(getString(R.string.key_similarity));
        licensePreference = findPreference(getString(R.string.key_license));
        janyoInfoPreference = findPreference(getString(R.string.key_info_janyo));

        resultNumberPreference.setSummary(R.string.summary_result_number);
    }

    @SuppressWarnings("ConstantConditions")
    private void monitor() {
        resultNumberPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit, new TextInputLayout(getActivity()), false);
                final TextInputLayout textInputLayout = view.findViewById(R.id.layout);
                textInputLayout.setHint(getString(R.string.title_result_number));
                textInputLayout.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                textInputLayout.getEditText().setText(String.valueOf(Settings.INSTANCE.getResultNumber()));
                new AlertDialog.Builder(getActivity())
                        .setTitle(" ")
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Settings.INSTANCE.setResultNumber(Integer.parseInt(textInputLayout.getEditText().getText().toString()));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return false;
            }
        });
        similarityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit, new TextInputLayout(getActivity()), false);
                final TextInputLayout textInputLayout = view.findViewById(R.id.layout);
                textInputLayout.setHint(getString(R.string.title_similarity));
                textInputLayout.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                textInputLayout.getEditText().setText(String.valueOf(Settings.INSTANCE.getSimilarity() * 100));
                new AlertDialog.Builder(getActivity())
                        .setTitle(" ")
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                float similarity = Float.parseFloat(textInputLayout.getEditText().getText().toString());
                                Settings.INSTANCE.setSimilarity(similarity / 100f);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return false;
            }
        });
        licensePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View view_license = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_license, new NestedScrollView(getActivity()), false);
                TextView text_license_point1 = view_license.findViewById(R.id.license_point1);
                TextView text_license_point2 = view_license.findViewById(R.id.license_point2);
                TextView text_license_point3 = view_license.findViewById(R.id.license_point3);
                VectorDrawableCompat point = VectorDrawableCompat.create(getResources(), R.drawable.ic_point, null);
                point.setBounds(0, 0, point.getMinimumWidth(), point.getMinimumHeight());
                text_license_point1.setCompoundDrawables(point, null, null, null);
                text_license_point2.setCompoundDrawables(point, null, null, null);
                text_license_point3.setCompoundDrawables(point, null, null, null);
                new AlertDialog.Builder(getActivity())
                        .setTitle(" ")
                        .setView(view_license)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return false;
            }
        });
        janyoInfoPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View view_info = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_info, new NestedScrollView(getActivity()), false);
                new AlertDialog.Builder(getActivity())
                        .setTitle(" ")
                        .setView(view_info)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return false;
            }
        });
    }
}
