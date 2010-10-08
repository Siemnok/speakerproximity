package net.androcom.dev.speakerproximity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class CalibrationActivity extends Activity {

	private SPApp						app;

	private Thread						thread;

	/** used for preference reading **/
	private SharedPreferences			prefs;

	/** used for preference editing **/
	private SharedPreferences.Editor	prefsEditor;

	private float						actualProximityValue;

	private final int					STATE_INIT					= 1;
	private final int					STATE_INIT_SHOW				= 2;
	private final int					STATE_SENSOR_TOGGLE			= 3;
	private final int					STATE_SENSOR_TOGGLE_SHOW	= 4;
	private final int					STATE_WAIT					= 10;
	private int							actualState;

	private float						value1, value2;
	private int							valueCount;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * use this XML layout as the main layout for the preference application
		 **/
		setContentView(R.layout.calibrate);
		setTitle(R.string.calibration_header);
		app = SPApp.getInstance();
		/** get the preference viewer **/
		prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		/** get the preference writer **/
		prefsEditor = prefs.edit();
		/** get the sensor service reference from the system **/
		app.setSensorManager((SensorManager) getApplicationContext()
				.getSystemService(Context.SENSOR_SERVICE));
		app.setProximityListener(new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// nothing to do, we don't care (yet?)
			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				/** local temp vars **/
				final float proxVal = event.values[0];
				final int sensorType = event.sensor.getType();
				final String sensorName = event.sensor.getName();

				SPApp.log("SensorEvent: Type[" + sensorType + "] Name["
						+ sensorName + "] value(0)[" + proxVal + "]");
				if (sensorType == Sensor.TYPE_PROXIMITY) {
					actualProximityValue = proxVal;
					proxEvent();
				}
			}
		});
		// start calibration wizard
		startWizard();
	}

	private void updatePreference() {
		prefsEditor.putString("calibration", prefs.getFloat("calibration_init",
				-1.0f)
				+ ";"
				+ prefs.getFloat("calibration_covered", -1.0f)
				+ ";"
				+ prefs.getFloat("calibration_uncovered", -1.0f));
		prefsEditor.commit();
	}

	private void updateInit(float init) {
		prefsEditor.putFloat("calibration_init", init);
		prefsEditor.commit();
		SPApp.log("calibration_init=" + init);
	}

	private void showInit() {
		final TextView version = (TextView) findViewById(R.id.calibration_do_show_init);
		version.setText(getString(R.string.calibration_do_show_init, prefs
				.getFloat("calibration_init", -1.0f)));
	}

	private void updateCovered(float covered) {
		prefsEditor.putFloat("calibration_covered", covered);
		prefsEditor.commit();
		SPApp.log("calibration_covered=" + covered);
	}

	private void showCovered() {
		final TextView version = (TextView) findViewById(R.id.calibration_do_show_covered);
		version.setText(getString(R.string.calibration_do_show_covered, prefs
				.getFloat("calibration_covered", -1.0f)));
	}

	private void updateUncovered(float uncovered) {
		prefsEditor.putFloat("calibration_uncovered", uncovered);
		prefsEditor.commit();
		SPApp.log("calibration_uncovered=" + uncovered);
	}

	private void showUncovered() {
		final TextView version = (TextView) findViewById(R.id.calibration_do_show_uncovered);
		version.setText(getString(R.string.calibration_do_show_uncovered, prefs
				.getFloat("calibration_uncovered", -1.0f)));
	}

	private void showError(String msg) {
		final TextView version = (TextView) findViewById(R.id.calibration_error);
		version.setText(msg);
	}

	private void showToggle() {
		final TextView version = (TextView) findViewById(R.id.calibration_do_toggle);
		version.setText(getString(R.string.calibration_do_toggle, valueCount));
	}

	private void init() {
		app.setInCalibration(true);
		/** register the Proximitysensor **/
		if (!app.registerProximityListener()) {
			SPApp.log("No proximity sensor available");
			showError("No proximity sensor available");
		}
	}

	private void unregister() {
		app.setInCalibration(false);
		app.getSensorManager().unregisterListener(app.getProximityListener());
		updatePreference();
		SPApp.log("Calibration ended");
	}

	private void startWizard() {
		actualState = STATE_INIT;
		init();
	}

	private void proxEvent() {
		switch (actualState) {
			case STATE_INIT:
				actualState = STATE_WAIT;
				updateInit(actualProximityValue);
				actualState = STATE_INIT_SHOW;
				showInit();
				valueCount = 6;
				showToggle();
				value1 = -1;
				value2 = -1;
				actualState = STATE_SENSOR_TOGGLE;
				break;
			case STATE_SENSOR_TOGGLE:
				if (valueCount >= 1) {
					if (value1 == -1) {
						value1 = actualProximityValue;
					} else if (value2 == -1) {
						value2 = actualProximityValue;
					} else if (value1 == actualProximityValue
							|| value2 == actualProximityValue) {
						// do nothing
					} else {
						showError("more than 2 different values, can't calibrate");
						unregister();
						break;
					}
					showToggle();
					valueCount--;
					showError("actual=" + actualProximityValue + " value1="
							+ value1 + " value2=" + value2);
				} else {
					actualState = STATE_SENSOR_TOGGLE_SHOW;
					proxEvent();
				}
				break;
			case STATE_SENSOR_TOGGLE_SHOW:
				if (value1 < value2) {
					updateCovered(value1);
					updateUncovered(value2);
				} else {
					updateCovered(value2);
					updateUncovered(value1);
				}
				showCovered();
				showUncovered();
				showError("value1=" + value1 + " value2=" + value2);
				unregister();
				finish();
				break;
			default:
				break;
		}
	}
}
