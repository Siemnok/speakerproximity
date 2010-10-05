/****
 * Created by Michel Racic (http://www.2030.tk)
 * 
 * This is the preference screen that loads the preferences from XML
 */
package net.androcom.dev.speakerproximity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferenceScreen extends PreferenceActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
