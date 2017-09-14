package pw.janyo.whatanime.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pw.janyo.whatanime.R;
import pw.janyo.whatanime.util.Settings;
import vip.mystery0.tools.Logs.Logs;

/**
 * Created by myste.
 */

public class SettingsFragment extends PreferenceFragment
{
	private static final String TAG = "SettingsFragment";
	private Settings settings;
	private Preference resultNumberPreference;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		settings = Settings.getInstance(getActivity());
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		initialization();
		monitor();
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	private void initialization()
	{
		resultNumberPreference = findPreference(getString(R.string.key_result_number));
		resultNumberPreference.setSummary(R.string.summary_result_number);
	}

	@SuppressWarnings("ConstantConditions")
	private void monitor()
	{
		resultNumberPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit, new TextInputLayout(getActivity()), false);
				final TextInputLayout textInputLayout = view.findViewById(R.id.layout);
				textInputLayout.setHint(getString(R.string.title_result_number));
				textInputLayout.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
				textInputLayout.getEditText().setText(String.valueOf(settings.getResultNumber()));
				new AlertDialog.Builder(getActivity())
						.setTitle(" ")
						.setView(view)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialogInterface, int i)
							{
								Logs.i(TAG, "onClick: " + textInputLayout.getEditText().getText().toString());
								settings.setResultNumber(Integer.parseInt(textInputLayout.getEditText().getText().toString()));
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.show();
				return false;
			}
		});
	}
}
