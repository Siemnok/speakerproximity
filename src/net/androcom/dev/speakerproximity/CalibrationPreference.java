package net.androcom.dev.speakerproximity;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CalibrationPreference extends Preference {

	private TextView	view;

	public CalibrationPreference(Context context) {
		super(context);
	}

	public CalibrationPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CalibrationPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		view = new TextView(getContext());
		final String[] svalues = getPersistedString(
				"not calibrated;not calibrated;not calibrated").split(";");
		view.setText(getContext().getString(R.string.calibration_init,
				svalues[0])
				+ "\n"
				+ getContext().getString(R.string.calibration_covered,
						svalues[1])
				+ "\n"
				+ getContext().getString(R.string.calibration_uncovered,
						svalues[2]) + "\n");
		view.setTextSize(18);
		view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		return view;
	}
}
