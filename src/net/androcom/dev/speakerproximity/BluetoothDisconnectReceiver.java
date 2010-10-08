/****
 * Created by Michel Racic (http://www.2030.tk)
 * 
 * This is the bluetooth disconnect broadcast receiver
 */
package net.androcom.dev.speakerproximity;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BluetoothDisconnectReceiver extends BroadcastReceiver {
	/** Which audio class to listen for, we only want headsets **/
	int	remoteAudioClass	= BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
									| BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES
									| BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				"headset", true)) {
			return;
		}
		final BluetoothDevice btDevice = intent.getExtras().getParcelable(
				BluetoothDevice.EXTRA_DEVICE);
		final int btDevClass = btDevice.getBluetoothClass().getDeviceClass();
		if ((btDevClass & remoteAudioClass) != 0) {
			SPApp.getInstance().registerProximityListener();
			SPApp.getInstance().setHeadsetConnected(false);
			SPApp.log("Bluetooth headset has been disconnected");
		}
	}
}
