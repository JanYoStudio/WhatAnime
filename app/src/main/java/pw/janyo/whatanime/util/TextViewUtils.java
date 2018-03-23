/*
 *    Copyright (C) 2017.  Aesean
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package pw.janyo.whatanime.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.util.Log;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * TextViewUtils.
 * {@link android.support.v7.widget.AppCompatTextViewAutoSizeHelper}
 *
 * @author danny
 * @version 1.0
 * @since 1/17/18
 */
@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TextViewUtils {
	private static final String TAG = "TextViewUtils";
	private static Hashtable<String, Method> sTextViewMethodByNameCache = new Hashtable<>();

	private static final long DURATION = 600;       // ms
	private static final long MIN_DURATION = 100;   // ms
	private static final long MAX_DURATION = 1000;  // ms
	private static final long BASE_HEIGHT = 1000;   // px

	// horizontal scrolling is activated.
	private static final int VERY_WIDE = 1024 * 1024;
	private static RectF TEMP_RECT_F = new RectF();
	private static TextPaint sTempTextPaint;

	@Nullable
	public static ValueAnimator setMaxLinesWithAnimation(@NotNull final TextView textView, final int maxLine) {
		measureTextHeight(textView, textView.getText().toString());

		final int textHeight = measureTextHeight(textView, textView.getText(), maxLine);
		if (textHeight < 0) {
			// measure failed. setMaxLines directly.
			textView.setMaxLines(maxLine);
			return null;
		}
		final int minLines = textView.getMinLines();
		final int targetHeight = textHeight + textView.getCompoundPaddingBottom() + textView.getCompoundPaddingTop();
		return animatorToHeight(textView, targetHeight, new AnimatorListenerAdapter() {
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
			}

			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				textView.setMinLines(minLines);
				textView.setMaxLines(maxLine);
			}
		});
	}

	@Nullable
	public static ValueAnimator animatorToHeight(@NotNull final TextView textView, int h, @Nullable Animator.AnimatorListener listener) {
		final int height = textView.getHeight();
		if (height == h) {
			return null;
		}
		ValueAnimator valueAnimator = new ValueAnimator();
		valueAnimator.setIntValues(height, h);
		long duration = makeDuration(h, height);
		valueAnimator.setDuration(duration);
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = (int) animation.getAnimatedValue();
				textView.setHeight(value);
			}
		});
		if (listener != null) {
			valueAnimator.addListener(listener);
		}
		valueAnimator.start();
		return valueAnimator;
	}

	private static long makeDuration(int h, int height) {
		long d = (long) (DURATION * (Math.abs(h - height) * 1f / BASE_HEIGHT));
		return Math.max(MIN_DURATION, Math.min(d, MAX_DURATION));
	}

	public static int measureTextHeight(@NotNull TextView textView, @NotNull CharSequence text) {
		return measureTextHeight(textView, text, Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? textView.getMaxLines() : Integer.MAX_VALUE);
	}

	public static int measureTextHeight(@NotNull TextView textView, @NotNull CharSequence text, int maxLines) {
		if (sTempTextPaint == null) {
			sTempTextPaint = new TextPaint();
		}
		sTempTextPaint.set(textView.getPaint());
		sTempTextPaint.setTextSize(textView.getTextSize());

		final int targetHeight = measureTextHeight(textView, text, sTempTextPaint, maxLines);
		sTempTextPaint.reset();
		return targetHeight;
	}

	public static int measureTextHeight(@NotNull TextView textView, @NotNull CharSequence text, @NotNull TextPaint textPaint, int maxLines) {
		final boolean horizontallyScrolling = invokeAndReturnWithDefault(textView, "getHorizontallyScrolling", false);
		final int availableWidth = horizontallyScrolling
				? VERY_WIDE
				: textView.getMeasuredWidth() - textView.getTotalPaddingLeft()
				- textView.getTotalPaddingRight();
		final int availableHeight = textView.getHeight() - textView.getCompoundPaddingBottom()
				- textView.getCompoundPaddingTop();

		if (availableWidth <= 0 || availableHeight <= 0) {
			return -1;
		}
		TEMP_RECT_F.setEmpty();
		TEMP_RECT_F.right = availableWidth;
		// TEMP_RECT_F.bottom = availableHeight;
		TEMP_RECT_F.bottom = VERY_WIDE;

		// Needs reflection call due to being private.
		Layout.Alignment alignment = invokeAndReturnWithDefault(
				textView, "getLayoutAlignment", Layout.Alignment.ALIGN_NORMAL);
		final StaticLayout layout = createStaticLayoutForMeasuringCompat(text, alignment, Math.round(TEMP_RECT_F.right), Integer.MAX_VALUE, textPaint, textView);
		return layout.getLineTop(Math.min(layout.getLineCount(), maxLines));
	}

	private static StaticLayout createStaticLayoutForMeasuringCompat(CharSequence text, Layout.Alignment alignment, int availableWidth, int maxLines, TextPaint textPaint, TextView textView) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return createStaticLayoutForMeasuring(text, alignment, availableWidth, maxLines, textPaint, textView);
		} else {
			return createStaticLayoutForMeasuringPre23(text, alignment, availableWidth, textPaint, textView);
		}
	}

	@RequiresApi(Build.VERSION_CODES.M)
	private static StaticLayout createStaticLayoutForMeasuring(CharSequence text, Layout.Alignment alignment, int availableWidth, int maxLines, TextPaint textPaint, TextView textView) {
		// Can use the StaticLayout.Builder (along with TextView params added in or after
		// API 23) to construct the layout.
		final TextDirectionHeuristic textDirectionHeuristic = invokeAndReturnWithDefault(textView, "getTextDirectionHeuristic", TextDirectionHeuristics.FIRSTSTRONG_LTR);

		final StaticLayout.Builder layoutBuilder = StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, availableWidth);

		return layoutBuilder.setAlignment(alignment)
				.setLineSpacing(textView.getLineSpacingExtra(), textView.getLineSpacingMultiplier())
				.setIncludePad(textView.getIncludeFontPadding())
				.setBreakStrategy(textView.getBreakStrategy())
				.setHyphenationFrequency(textView.getHyphenationFrequency())
				.setMaxLines(maxLines == -1 ? Integer.MAX_VALUE : maxLines)
				.setTextDirection(textDirectionHeuristic)
				.build();
	}

	@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static StaticLayout createStaticLayoutForMeasuringPre23(CharSequence text, Layout.Alignment alignment, int availableWidth, TextPaint textPaint, TextView textView) {
		// Setup defaults.
		float lineSpacingMultiplier = 1.0f;
		float lineSpacingAdd = 0.0f;
		boolean includePad = true;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			// Call public methods.
			lineSpacingMultiplier = textView.getLineSpacingMultiplier();
			lineSpacingAdd = textView.getLineSpacingExtra();
			includePad = textView.getIncludeFontPadding();
		} else {
			// Call private methods and make sure to provide fallback defaults in case something
			// goes wrong. The default values have been inlined with the StaticLayout defaults.
			lineSpacingMultiplier = invokeAndReturnWithDefault(textView,
					"getLineSpacingMultiplier", lineSpacingMultiplier);
			lineSpacingAdd = invokeAndReturnWithDefault(textView,
					"getLineSpacingExtra", lineSpacingAdd);
			//noinspection ConstantConditions
			includePad = invokeAndReturnWithDefault(textView,
					"getIncludeFontPadding", includePad);
		}

		// The layout could not be constructed using the builder so fall back to the
		// most broad constructor.
		return new StaticLayout(text, textPaint, availableWidth,
				alignment,
				lineSpacingMultiplier,
				lineSpacingAdd,
				includePad);
	}


	private static <T> T invokeAndReturnWithDefault(@NonNull Object object, @NonNull final String methodName, @NonNull final T defaultValue) {
		T result = null;
		boolean exceptionThrown = false;

		try {
			// Cache lookup.
			Method method = getTextViewMethod(methodName);
			if (method != null) {
				//noinspection unchecked
				result = (T) method.invoke(object);
			}
		} catch (Exception ex) {
			exceptionThrown = true;
			Log.w(TAG, "Failed to invoke TextView#" + methodName + "() method", ex);
		} finally {
			if (result == null && exceptionThrown) {
				result = defaultValue;
			}
		}

		return result;
	}

	@SuppressLint("PrivateApi")
	@Nullable
	private static Method getTextViewMethod(@NonNull final String methodName) {
		try {
			Method method = sTextViewMethodByNameCache.get(methodName);
			if (method == null) {
				method = TextView.class.getDeclaredMethod(methodName);
				if (method != null) {
					method.setAccessible(true);
					// Cache update.
					sTextViewMethodByNameCache.put(methodName, method);
				}
			}
			return method;
		} catch (Exception ex) {
			Log.w(TAG, "Failed to retrieve TextView#" + methodName + "() method", ex);
			return null;
		}
	}
}