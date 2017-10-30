package hu.trigary.percentagebarseekers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class PercentageBar extends View {
	public PercentageBar(Context context) {
		this(context, null);
	}
	
	public PercentageBar(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PercentageBar, 0, 0);
		try {
			drawOutline = array.getBoolean(R.styleable.PercentageBar_drawOutline, false);
			outlineColor = array.getColor(R.styleable.PercentageBar_outlineColor, Color.BLACK);
			paint.setStrokeWidth(array.getDimension(R.styleable.PercentageBar_outlineWidth, 6));
			maxValue = array.getInt(R.styleable.PercentageBar_maxValue, 100);
		} finally {
			array.recycle();
		}
		
		categories = new Category[] {new Category(Color.BLACK, maxValue)};
	}
	
	private final Paint paint = new Paint();
	private volatile boolean drawOutline;
	private volatile int outlineColor;
	private int maxValue;
	private Category[] categories;
	
	
	
	public boolean isDrawOutline() {
		return drawOutline;
	}
	
	public int getOutlineColor() {
		return outlineColor;
	}
	
	public float getOutlineWidth() {
		return paint.getStrokeWidth();
	}
	
	public void setDrawOutline(boolean drawOutline) {
		this.drawOutline = drawOutline;
	}
	
	public void setOutlineColor(int outlineColor) {
		this.outlineColor = outlineColor;
	}
	
	public void setOutlineWidth(float outlineWidth) {
		paint.setStrokeWidth(outlineWidth);
	}
	
	
	
	public synchronized int getMaxValue() {
		return maxValue;
	}
	
	public synchronized Category[] getCategories() {
		return categories;
	}
	
	public synchronized void setMaxValue(int maxValue) {
		if (maxValue <= 0) {
			throw new IllegalArgumentException("The max value must be a positive number.");
		}
		
		this.maxValue = maxValue;
	}
	
	public synchronized void setCategories(Category[] categories, boolean updateMaxValue) {
		if (categories == null || categories.length == 0) {
			throw new IllegalArgumentException("The categories array mustn't be null or empty.");
		}
		
		this.categories = categories;
		
		if (updateMaxValue) {
			int counter = 0;
			for (Category category : categories) {
				counter += category.value;
			}
			setMaxValue(counter);
		}
	}
	
	
	
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		int contentRight = getWidth() - getPaddingRight();
		int contentBottom = getHeight() - getPaddingBottom();
		
		int widthCounter = paddingLeft;
		int contentWidth = contentRight - paddingLeft;
		Category lastNonZeroCategory = categories[0];
		
		for (Category category : categories) {
			paint.setColor(category.color);
			int width = (int)((float)category.value * contentWidth / maxValue);
			canvas.drawRect(widthCounter, paddingTop, width + widthCounter, contentBottom, paint);
			widthCounter += width;
			
			if (width != 0) {
				lastNonZeroCategory = category;
			}
		}
		
		paint.setColor(lastNonZeroCategory.color);
		canvas.drawRect(widthCounter, paddingTop, contentRight, contentBottom, paint);
		
		if (drawOutline) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(outlineColor);
			canvas.drawRect(paddingLeft, paddingTop, contentRight, contentBottom, paint);
			paint.setStyle(Paint.Style.FILL);
		}
	}
	
	
	
	public static class Category {
		public Category(int color, int value) {
			this.color = color;
			this.value = value;
		}
		
		private volatile int color;
		private volatile int value;
		
		
		
		public int getColor() {
			return color;
		}
		
		public int getValue() {
			return value;
		}
		
		public void setColor(int color) {
			this.color = color;
		}
		
		public void setValue(int value) {
			if (value < 0) {
				throw new IllegalArgumentException("The value must be a non-negative number.");
			} else {
				this.value = value;
			}
		}
	}
}
