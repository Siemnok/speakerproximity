/****
 * Created by Michel Racic (http://www.2030.tk)
 * 
 * This is the call event receiver to start the sensor service
 */

package net.androcom.dev.speakerproximity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class CallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		/** Check if Application is set to be active **/
		if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				"active", false)) {
			return;
		}

		/** Start the service that handles the InCall logic **/
		context.startService(new Intent(context, SensorService.class));

	}

}
