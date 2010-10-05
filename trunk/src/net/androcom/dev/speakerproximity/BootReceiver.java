/****
 * Created by Michel Racic (http://www.2030.tk)
 * 
 * This is the boot broadcast receiver
 */
package net.androcom.dev.speakerproximity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context ctx, Intent intent) {
		/** reset the headsetConnected variable as we just booted the phone and don't want an old state **/
		SPApp.getInstance().setHeadsetConnected(false);
		SPApp.log("HeadsetConnected variable has ben reset to false");
	}
}
