/****
 * Created by Michel Racic (http://www.2030.tk)
 * 
 * This is the application logic class that handles the actual magic ;-)
 */

package net.androcom.dev.speakerproximity;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MyPhoneStateListener extends PhoneStateListener {

	/** used to Store the calling Context **/
	private final Context					ctx;
	private final SPApp						app;

	/** used for preference reading **/
	private final SharedPreferences			prefs;

	/** used for preference editing **/
	private final SharedPreferences.Editor	prefsEditor;

	/** used for the sensor management **/
	private final SensorService				sensorService;

	/** used for the audio routing **/
	private final AudioManager				audiomanager;

	/** used for turning screen off or on **/
	private final PowerManager				pm;
	private final PowerManager.WakeLock		wl;

	/** temporary variables **/
	private boolean							phoneWasCovered					= false;
	private int								conference						= -1;

	/** Constants **/
	private final static String				LAST_STATE						= "LastState";
	private final static String				LAST_PROXIMITY_STATE			= "LastProximityState";
	private final static String				LAST_CONFERENCE_STATE			= "LastConferenceState";
	private final static String				SPEAKER_SETTING_BEFORE			= "SpeakerSettingBefore";
	public static final int					PROXIMITY_SCREEN_OFF_WAKE_LOCK	= 32;

	public MyPhoneStateListener(Context context, SensorService sensorService) {
		super();
		/** Save the calling context **/
		ctx = context;
		/** Save the application instance **/
		app = SPApp.getInstance();
		/** save the service reference **/
		this.sensorService = sensorService;
		/** get the preference viewer **/
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		/** get the preference writer **/
		prefsEditor = prefs.edit();
		/** get the sensor service reference from the system **/
		app.setSensorManager((SensorManager) ctx
				.getSystemService(Context.SENSOR_SERVICE));
		/** get the audio service reference from the system **/
		audiomanager = (AudioManager) ctx
				.getSystemService(Context.AUDIO_SERVICE);
		/** get the powermanager service reference from the system **/
		pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PROXIMITY_SCREEN_OFF_WAKE_LOCK, "SpeakerProximity");
		app.setProximityListener(new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// nothing to do, we don't care (yet?)
			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				/** Detect duplicate events in row **/
				if (prefs.getFloat(LAST_PROXIMITY_STATE, -1) == event.values[0]) {
					return;
				} else {
					prefsEditor.putFloat(LAST_PROXIMITY_STATE, event.values[0]);
					prefsEditor.commit();
				}
				/** local temp vars **/
				final float proxVal = event.values[0];
				final int sensorType = event.sensor.getType();
				final String sensorName = event.sensor.getName();

				SPApp.log("SensorEvent: Type[" + sensorType + "] Name["
						+ sensorName + "] value(0)[" + proxVal + "]");
				final boolean headsetOff = prefs.getBoolean("headset", true) ? (!app
						.isHeadsetConnected() && !audiomanager
						.isWiredHeadsetOn())
						: true;
				SPApp.log("HeadsetSetting=" + prefs.getBoolean("headset", true)
						+ " isHeadsetConnected()=" + app.isHeadsetConnected()
						+ " isWiredHeadsetOn()="
						+ audiomanager.isWiredHeadsetOn()
						+ " headtes_off_eval=" + headsetOff);

				/** get sensor calibration values **/
				final float init = prefs.getFloat("calibration_init", -1.0f);
				final float covered = prefs.getFloat("calibration_covered",
						-1.0f);
				final float uncovered = prefs.getFloat("calibration_uncovered",
						-1.0f);

				if (sensorType == Sensor.TYPE_PROXIMITY && headsetOff) {
					if (proxVal == init && !phoneWasCovered
							&& !prefs.getBoolean("speakerStart", false)) {
						SPApp.log("ProximityEvent[" + proxVal + "]");
						phoneWasCovered = true;
						return;
					} else if (proxVal == covered) {
						audiomanager.setSpeakerphoneOn(false);
						SPApp.log("Covered ProximityEvent[" + proxVal + "]");
					} else if (proxVal == uncovered) {
						audiomanager.setSpeakerphoneOn(true);
						SPApp.log("Free ProximityEvent[" + proxVal + "]");
					} else {
						SPApp
								.log("ProximityEvent[" + proxVal
										+ "] not handled");
					}
				} else {
					SPApp.log("headset is in use");
				}
			}
		});

	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		SPApp
				.log("active="
						+ prefs.getBoolean("active", false)
						+ " Has proximity sensor="
						+ ((app.getSensorManager().getDefaultSensor(
								Sensor.TYPE_PROXIMITY) == null) ? "no proximity sensor detected"
								: app.getSensorManager().getDefaultSensor(
										Sensor.TYPE_PROXIMITY).getName()));

		/** Detect duplicate events in row **/
		if ((state == prefs.getInt(LAST_STATE, -1))) {
			return;
		} else if (!prefs.getBoolean("active", false)) {
			return;
		}

		/** save the actual state as last state **/
		prefsEditor.putInt(LAST_STATE, state);
		prefsEditor.commit();

		/** detect which state the call has and if we have to do some action **/
		switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				app.setInCall(false);
				app.getSensorManager().unregisterListener(
						app.getProximityListener());

				/**
				 * Handling conference mode
				 */
				if (app.getOrientationListener() != null) {
					app.getSensorManager().unregisterListener(
							app.getOrientationListener());
				}

				/** restore speaker state from beginning of the call **/
				audiomanager.setSpeakerphoneOn(prefs.getBoolean(
						SPEAKER_SETTING_BEFORE, false));
				phoneWasCovered = false;
				SPApp.log("Phone gets IDLE");

				/**
				 * handling the screnn off stuff, taken from
				 * http://proximitytoolextension.googlecode.com
				 **/
				if (wl.isHeld()) {
					wl.release();
				}

				/**
				 * handling headset changes, taken from
				 * http://proximitytoolextension.googlecode.com
				 **/
				if (app.getHeadSetPlugReceiver() != null) {
					ctx.unregisterReceiver(app.getHeadSetPlugReceiver());
				}
				if (app.getBluetoothConnectReceiver() != null) {
					ctx.unregisterReceiver(app.getBluetoothConnectReceiver());
				}
				if (app.getBluetoothDisconnectReceiver() != null) {
					ctx
							.unregisterReceiver(app
									.getBluetoothDisconnectReceiver());
				}

				/** stop myself as I'm not in duty anymore **/
				sensorService.stopSelf();

				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				app.setInCall(true);
				/** save speaker state to restore after the call finished **/
				prefsEditor.putBoolean(SPEAKER_SETTING_BEFORE, audiomanager
						.isSpeakerphoneOn());
				prefsEditor.commit();

				/** register the Proximitysensor **/
				if (!app.registerProximityListener()) {
					SPApp.log("No proximity sensor available");
				}
				SPApp.log("Phone is picked up");

				/**
				 * handling the screnn off stuff, taken from
				 * http://proximitytoolextension.googlecode.com
				 **/
				if (!wl.isHeld()) {
					wl.acquire();
				}

				/**
				 * handling headset changes, taken from
				 * http://proximitytoolextension.googlecode.com
				 **/
				if (prefs.getBoolean("headset", true)) {
					app.setHeadSetPlugReceiver(new HeadSetPlugReceiver());
					ctx
							.registerReceiver(
									app.getHeadSetPlugReceiver(),
									new IntentFilter(
											android.content.Intent.ACTION_HEADSET_PLUG));
				}

				/**
				 * handling conference mode disable this app if device is flat
				 * on a table with it's screen
				 **/
				if (prefs.getBoolean("conferenceCall", true)) {
					app.setOrientationListener(new SensorEventListener() {
						@Override
						public void onAccuracyChanged(Sensor sensor,
								int accuracy) {
							// we don't need to do something here

						}

						@Override
						public void onSensorChanged(SensorEvent event) {
							// float x = event.values[0]; // not needed
							// float y = event.values[1]; // not needed
							final float z = event.values[2];
							conference = (z < -8 && z > -10) ? 1 : 0;

							/** Detect duplicate events in row **/
							final int lastConf = prefs.getInt(
									LAST_CONFERENCE_STATE, -1);
							if (lastConf != -1 && conference != -1
									&& lastConf == conference) {
								return;
							} else {
								prefsEditor.putInt(LAST_CONFERENCE_STATE,
										conference);
								prefsEditor.commit();
							}
							if ((conference == 1) ? true : false) {
								// upsidedown and flat
								SPApp.log("changed to upsidedown and flat "
										+ "event[0]=" + event.values[0]
										+ " event[1]=" + event.values[1]
										+ " event[2]=" + event.values[2]);
								app.unregisterProximityListener();
							} else {
								// not upsidedown and flat
								SPApp.log(" not anymore upsidedown and flat "
										+ "event[0]=" + event.values[0]
										+ " event[1]=" + event.values[1]
										+ " event[2]=" + event.values[2]);
								app.registerProximityListener();
							}
						}
					});
					app.getSensorManager().registerListener(
							app.getOrientationListener(),
							app.getSensorManager().getDefaultSensor(
									Sensor.TYPE_ACCELEROMETER),
							SensorManager.SENSOR_DELAY_UI);
				}
				break;
		}
	}
}
