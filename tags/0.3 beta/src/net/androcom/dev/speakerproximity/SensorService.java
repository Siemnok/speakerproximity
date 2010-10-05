/****
 * Created by Michel Racic (http://www.2030.tk)
 * 
 * This is the service that starts the magic
 */
package net.androcom.dev.speakerproximity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class SensorService extends Service {
	private static Context ctx;
	private static TelephonyManager telephony;
	private MyPhoneStateListener phoneListener;
	
	@Override
    public void onStart(Intent intent, int startId) {
		/** Acquire the applications context to work with **/
		ctx = getApplicationContext();
		/** check if listener object exists and create it if not **/
		if(phoneListener == null) {
			phoneListener=new MyPhoneStateListener(ctx, this);
		}
		/** check if TelephonyManager object exists and create it if not **/
	    if(telephony == null) {
	    	telephony = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
	    }
	    /** register our phonestatelistener to the systems TelephonyManager class **/ 
	    telephony.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// we don't need binding for now
		return null;
	}
}
