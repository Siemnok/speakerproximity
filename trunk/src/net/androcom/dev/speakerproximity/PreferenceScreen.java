/****
 * Created by Michel Racic (http://www.2030.tk)
 * 
 * This is the preference screen that loads the preferences from XML
 */
package net.androcom.dev.speakerproximity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferenceScreen extends PreferenceActivity {

	private CalibrationPreference	calpref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		calpref = (CalibrationPreference) findPreference("calibration");
		calpref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						startActivityForResult(new Intent(getBaseContext(),
								CalibrationActivity.class), 1);
						return true;
					}
				});
		if (PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getString("calibration",
				"not calibrated;not calibrated;not calibrated").equals(
				"not calibrated;not calibrated;not calibrated")) {
			startActivityForResult(new Intent(getBaseContext(),
					CalibrationActivity.class), 1);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** hack to refresh the values on the preference screen **/
		// TODO find a proper method for this
		finish();
		final ComponentName comp = new ComponentName(this.getPackageName(),
				SpeakerProximity.class.getName());
		startActivity(new Intent().setComponent(comp));
		SPApp.log("activity result: requestCode=" + requestCode
				+ " resultCode=" + resultCode);
	}

}
