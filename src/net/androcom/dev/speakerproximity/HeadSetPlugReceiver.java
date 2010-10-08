/****
 * Created by Michel Racic (http://www.2030.tk)
 * 
 * This is the Headset receiver too handle a wired headset
 */
package net.androcom.dev.speakerproximity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HeadSetPlugReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getExtras().getInt("state") == 0) {
			SPApp.getInstance().registerProximityListener();
			SPApp.getInstance().setHeadsetConnected(false);
		} else {
			SPApp.getInstance().unregisterProximityListener();
			SPApp.getInstance().setHeadsetConnected(true);
		}
	}
}
