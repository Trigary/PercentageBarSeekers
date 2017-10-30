package hu.trigary.percentagebarseekers;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		List<Pair<SeekBar, PercentageBar.Category>> categories = new ArrayList<>();
		categories.add(new Pair<>((SeekBar)findViewById(R.id.redSeekBar), new PercentageBar.Category(Color.RED, 0)));
		categories.add(new Pair<>((SeekBar)findViewById(R.id.greenSeekBar), new PercentageBar.Category(Color.GREEN, 0)));
		categories.add(new Pair<>((SeekBar)findViewById(R.id.blueSeekBar), new PercentageBar.Category(Color.BLUE, 0)));
		configureGroups((PercentageBar)findViewById(R.id.colorPercentageBar), null, categories);
		
		categories.clear();
		SeekBar graySeekBar = (SeekBar)findViewById(R.id.graySeekBar);
		categories.add(new Pair<>((SeekBar)findViewById(R.id.blackSeekBar), new PercentageBar.Category(Color.BLACK, 0)));
		categories.add(new Pair<>(graySeekBar, new PercentageBar.Category(Color.GRAY, 0)));
		categories.add(new Pair<>((SeekBar)findViewById(R.id.whiteSeekBar), new PercentageBar.Category(Color.WHITE, 0)));
		configureGroups((PercentageBar)findViewById(R.id.grayPercentageBar), graySeekBar, categories);
	}
	
	
	
	private void configureGroups(final PercentageBar percentageBar, @Nullable final SeekBar defaultSeekBar, List<Pair<SeekBar, PercentageBar.Category>> seekBarIdCategoryPairs) {
		final Map<SeekBar, PercentageBar.Category> categories = new HashMap<>();
		
		SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				PercentageBar.Category category = categories.get(seekBar);
				int oldValue = category.getValue();
				category.setValue(progress);
				if (fromUser) {
					changeValue(seekBar, oldValue, progress);
					percentageBar.invalidate();
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			
			}
			
			private void changeValue(SeekBar seekBar, int fromValue, int toValue) {
				int deltaValue = fromValue - toValue;
				if (defaultSeekBar != null && seekBar != defaultSeekBar) { //Logic for 'default' seek bar
					int defaultProgress = defaultSeekBar.getProgress();
					if (defaultProgress > 0) {
						if (fromValue > toValue) {
							defaultSeekBar.setProgress(defaultProgress + deltaValue);
							return;
						} else {
							int newEmptyProgress = defaultProgress + deltaValue;
							if (newEmptyProgress < 0) {
								defaultSeekBar.setProgress(0);
								deltaValue += defaultProgress;
							} else {
								defaultSeekBar.setProgress(newEmptyProgress);
								return;
							}
						}
					}
				}
				
				int sumExcludingCurrent = 0;
				for (SeekBar bar : categories.keySet()) {
					if (bar != seekBar) {
						sumExcludingCurrent += bar.getProgress();
					}
				}
				
				int progressSum = 0;
				if (sumExcludingCurrent == 0 && (defaultSeekBar == null || defaultSeekBar == seekBar)) { //Increase all by the same amount
					int deltaProportion = deltaValue / (categories.size() - 1);
					for (SeekBar bar : categories.keySet()) {
						if (bar != seekBar) {
							bar.setProgress(normalizeProgress(bar.getProgress() + deltaProportion));
							progressSum += bar.getProgress();
						}
					}
				} else {
					float deltaProportion = (float)deltaValue / sumExcludingCurrent; //Increase all relative to their values
					for (SeekBar bar : categories.keySet()) {
						if (bar != seekBar) {
							bar.setProgress(normalizeProgress(bar.getProgress() + Math.round(bar.getProgress() * deltaProportion)));
							progressSum += bar.getProgress();
						}
					}
				}
				
				int newProgress = percentageBar.getMaxValue() - progressSum;
				if (newProgress < 0) { //security precaution (in case rounding made something go very wrong)
					seekBar.setProgress(0);
					
					newProgress *= -1;
					for (SeekBar bar : categories.keySet()) {
						if (bar != seekBar) {
							if (bar.getProgress() < newProgress) {
								newProgress -= bar.getProgress();
								bar.setProgress(0);
							} else {
								bar.setProgress(bar.getProgress() - newProgress);
								break;
							}
						}
					}
				} else {
					seekBar.setProgress(newProgress);
				}
			}
			
			private int normalizeProgress(int unsafeValue) {
				if (unsafeValue < 0) {
					return 0;
				} else if (unsafeValue > percentageBar.getMaxValue()) {
					return percentageBar.getMaxValue();
				} else {
					return unsafeValue;
				}
			}
		};
		
		PercentageBar.Category[] categoryArray = new PercentageBar.Category[seekBarIdCategoryPairs.size()];
		
		for (int i = 0; i < seekBarIdCategoryPairs.size(); i++) {
			Pair<SeekBar, PercentageBar.Category> pair = seekBarIdCategoryPairs.get(i);
			categoryArray[i] = pair.second;
			categories.put(pair.first, pair.second);
			pair.second.setValue(pair.first.getProgress());
			pair.first.setOnSeekBarChangeListener(listener);
		}
		
		percentageBar.setCategories(categoryArray, true);
		percentageBar.invalidate();
	}
}
