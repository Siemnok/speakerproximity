/****
 * Created by Michel Racic (http://www.2030.tk)
 * 
 * This is the bluetooth connect broadcast receiver
 */
package net.androcom.dev.speakerproximity;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BluetoothConnectReceiver extends BroadcastReceiver {
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
		if(btDevice != null) {
			final BluetoothClass btClass = btDevice.getBluetoothClass(); 
			if (btClass != null) {
				final int btDevClass = btClass.getDeviceClass();
				if ((btDevClass & remoteAudioClass) != 0) {
					SPApp.getInstance().unregisterProximityListener();
					SPApp.getInstance().setHeadsetConnected(true);
					SPApp.log("Bluetooth headset has been connected");
				}
			}
		}
	}
}