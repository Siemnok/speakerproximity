/****
 * Created by Michel Racic (http://www.2030.tk)
 * 
 * This is the base GUI class that handles the settings stuff
 */

package net.androcom.dev.speakerproximity;

import net.androcom.dev.speakerproximity.log.SendLogActivity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SpeakerProximity extends ActivityGroup {

	private final String	paypalDonateUrl	= "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=SJDNMZ5JLZRSU&lc=CH&item_name=rac&item_number=SpeakerProximity&currency_code=CHF&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted";
	// private final String marketDonateUrl = "market://search?q=pub:rac";
	private final String	marketDonateUrl	= "market://search?q=net.androcom.dev.speakerproximity";
	private final String	androcomUrl		= "http://www.androcom.net";

	/** Called when the activity is first created. */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/**
		 * use this XML layout as the main layout for the preference application
		 **/
		setContentView(R.layout.main);

		/** find the androcom logo view **/
		final ImageView androcom = (ImageView) findViewById(R.id.androcom);
		/** find the paypal donate view **/
		final Button paypal = (Button) findViewById(R.id.donatePaypal);
		/** find the market donate view **/
		final Button market = (Button) findViewById(R.id.donateMarket);

		/** get the layout object **/
		final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainlayout);

		/** create an intent that can be fired if the androcom logo gets pressed **/
		final Intent androcomIntent = new Intent("android.intent.action.VIEW",
				Uri.parse(androcomUrl));
		/**
		 * create an intent that can be fired if the paypal donate button gets
		 * pressed
		 **/
		final Intent paypalIntent = new Intent("android.intent.action.VIEW",
				Uri.parse(paypalDonateUrl));
		/**
		 * create an intent that can be fired if the market donate button gets
		 * pressed
		 **/
		final Intent marketIntent = new Intent("android.intent.action.VIEW",
				Uri.parse(marketDonateUrl));

		/**
		 * create an onClick listener for the logo and buttons that fires the
		 * intent
		 **/
		androcom.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startActivity(androcomIntent);
			}
		});
		paypal.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startActivity(paypalIntent);
			}
		});
		market.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startActivity(marketIntent);
			}
		});

		/** get the sensor service reference from the system **/
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		/** check if there is a proximitysensor available on this hardware **/
		if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null) {
			/** This device has no proximity sensor hardware **/

			/** used for preference editing **/
			final SharedPreferences.Editor prefsEditor = PreferenceManager
					.getDefaultSharedPreferences(this).edit();
			prefsEditor.putBoolean("active", false);
			prefsEditor.commit();
			mainLayout.addView(getLayoutInflater().inflate(R.layout.error,
					null, false));

		} else {
			/** Proximity check OK, show sensor name in title **/
			setTitle(getTitle()
					+ " - "
					+ sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
							.getName());
			mainLayout.addView(getViewFromIntent("preferences", new Intent(
					this, PreferenceScreen.class)));
		}

	}

	public View getViewFromIntent(String tag, Intent intent) {

		/** start an activity inside an ActivityGroup and get the window handler **/
		final Window w = getLocalActivityManager().startActivity(tag, intent);

		/** extract the view out of the window handler **/
		final View wd = w != null ? w.getDecorView() : null;

		return wd;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/**
		 * Inflate the menu from XML resource.
		 **/
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/** detect which menu item has been clicked **/
		switch (item.getItemId()) {
			case R.id.menu_send_logcat:
				startActivity(new Intent(this, SendLogActivity.class));
				return true;
			case R.id.menu_about:
				showDialog(R.id.dialog_about);
				return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case R.id.dialog_about:
				return buildAboutDialog();
			default:
				return null;
		}
	}

	/**
	 * Build about dialog.
	 */
	private Dialog buildAboutDialog() {

		String versionName = null;
		try {
			final PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			versionName = pi.versionName;
		} catch (final PackageManager.NameNotFoundException e) {
		}

		final View view = getLayoutInflater().inflate(R.layout.about, null,
				false);

		final TextView version = (TextView) view.findViewById(R.id.version);
		version.setText(getString(R.string.about_version, versionName));

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setView(view);
		builder.setPositiveButton(getString(android.R.string.ok), null);
		builder.setCancelable(true);

		return builder.create();
	}
}