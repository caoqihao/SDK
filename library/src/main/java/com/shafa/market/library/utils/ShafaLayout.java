package com.shafa.market.library.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ShafaLayout {

	private int mWidth;
	private int mHeight;

	private static Map<Point, ShafaLayout> sHolders = new HashMap<Point, ShafaLayout>();

	private static volatile Point screen;

	public static ShafaLayout L1080P = obtain(1920, 1080);

	public static void init(Context context) {
		Point size = new Point();
		try {
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Method method = Display.class.getMethod("getRealSize", Point.class);
			method.invoke(wm.getDefaultDisplay(), size);
		} catch (Exception e) {
			DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			size.x = metrics.widthPixels;
			size.y = metrics.heightPixels;
		}
		if (size.x < size.y) {
			int temp = size.x;
			size.x = size.y;
			size.y = temp;
		}
		ShafaLayout.screen = size;
	}

	public static ShafaLayout obtain(int w, int h) {
		Point key = new Point(w, h);
		if (!sHolders.containsKey(key)) {
			synchronized (ShafaLayout.class) {
				if (!sHolders.containsKey(key)) {
					sHolders.put(key, new ShafaLayout(w, h));
				}
			}
		}

		return sHolders.get(key);
	}

	private ShafaLayout(int w, int h) {
		mWidth = w;
		mHeight = h;
	}

	public float w(float px) {
		if (screen != null) {
			return px * wScale();
		}
		return px;
	}

	public float h(float px) {
		if (screen != null) {
			return px * hScale();
		}
		return px;
	}

	/**
	 * 
	 * @param px
	 * @param useReallyScale true 按真实比例适配 ，false 只按宽比例适配
	 * @return
	 */
	public float h(float px, boolean useRealyScale) {
		if (screen != null) {
			return px * hScale(useRealyScale);
		}
		return px;
	}

	public int w(int px) {
		if (screen != null) {
			return (int) (px * wScale());
		}
		return px;
	}

	public int h(int px) {
		if (screen != null) {
			return (int) (px * hScale());
		}
		return px;
	}

	/**
	 * 
	 * @param px
	 * @param useRealyScale true 按真实比例适配 ，false 只按宽比例适配
	 * @return
	 */
	public int h(int px, boolean useRealyScale) {
		if (screen != null) {
			return (int) (px * hScale(useRealyScale));
		}
		return px;
	}

	public float getFontSize(float size) {
		return w(size);
	}

	public float screenW() {
		return screen.x;
	}

	public float screenH() {
		return screen.y;
	}

	public float wScale() {
		return 1f * screen.x / mWidth;
	}

	public float hScale() {
		return hScale(false);
	}

	/**
	 * 
	 * @param useRealyScale true 按真实比例适配 ，false 只按宽比例适配
	 * @return 适配比例
	 */
	public float hScale(boolean useRealyScale) {
		return useRealyScale ? (1f * screen.y / mHeight) : wScale();
	}

	public void compact(View view) {
		compactView(view);
	}

	public void compatActivity(Activity activity) {
		compactView(activity.findViewById(android.R.id.content));
	}

	public void compatDialog(Dialog dialog) {
		compactView(dialog.findViewById(android.R.id.content));
	}

	public void compactView(View view) {
		compactViewImpl(view, false);
	}

	public void compactView(View view, boolean useReallyScale) {
		compactViewImpl(view, useReallyScale);
	}

	public void compactSingleView(View view, boolean useReallyScale){
		if (view == null) {
			return;
		}

		if (screen == null || screen.x == 0 || screen.y == 0) {
			init(view.getContext());
		}
		
		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (params != null) {
			if (params.width > 0) {
				params.width = w(params.width);
			}
			if (params.height > 0) {
				params.height = h(params.height, useReallyScale);
			}
			if (params instanceof MarginLayoutParams) {
				MarginLayoutParams mParams = (MarginLayoutParams) params;
				mParams.leftMargin = w(mParams.leftMargin);
				mParams.topMargin = h(mParams.topMargin, useReallyScale);
				mParams.rightMargin = w(mParams.rightMargin);
				mParams.bottomMargin = h(mParams.bottomMargin, useReallyScale);
			}
		}

		view.setPadding(w(view.getPaddingLeft()), h(view.getPaddingTop(), useReallyScale), w(view.getPaddingRight()), h(view.getPaddingBottom(), useReallyScale));

		if (view instanceof TextView) {
			TextView tv = (TextView) view;
			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, w((int) tv.getTextSize()));
			// if (android.os.Build.VERSION.SDK_INT >= 16) {
			// tv.setLineSpacing(h((int) (tv.getLineSpacingExtra())),
			// tv.getLineSpacingMultiplier());
			// }
			tv.setCompoundDrawablePadding(w(tv.getCompoundDrawablePadding()));
			Drawable[] cds = tv.getCompoundDrawables();
			for (Drawable d : cds) {
				Rect bounds = d == null ? null : d.getBounds();
				if (bounds != null && !bounds.isEmpty()) {
					bounds.set(w(bounds.left), h(bounds.top, useReallyScale), w(bounds.right), h(bounds.bottom, useReallyScale));
				}
			}
			tv.setCompoundDrawables(cds[0], cds[1], cds[2], cds[3]);
		}
	}
	
	
	/**
	 * @param view
	 * @param useReallyScale true 按真实比例适配 ，false 只按宽比例适配
	 */
	private void compactViewImpl(View view, boolean useReallyScale) {

		if (view == null) {
			return;
		}

		if (screen == null || screen.x == 0 || screen.y == 0) {
			init(view.getContext());
		}

		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (params != null) {
			if (params.width > 0) {
				params.width = w(params.width);
			}
			if (params.height > 0) {
				params.height = h(params.height, useReallyScale);
			}
			if (params instanceof MarginLayoutParams) {
				MarginLayoutParams mParams = (MarginLayoutParams) params;
				mParams.leftMargin = w(mParams.leftMargin);
				mParams.topMargin = h(mParams.topMargin, useReallyScale);
				mParams.rightMargin = w(mParams.rightMargin);
				mParams.bottomMargin = h(mParams.bottomMargin, useReallyScale);
			}
		}

		view.setPadding(w(view.getPaddingLeft()), h(view.getPaddingTop(), useReallyScale), w(view.getPaddingRight()), h(view.getPaddingBottom(), useReallyScale));

		if (view instanceof TextView) {
			TextView tv = (TextView) view;
			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, w((int) tv.getTextSize()));
			// if (android.os.Build.VERSION.SDK_INT >= 16) {
			// tv.setLineSpacing(h((int) (tv.getLineSpacingExtra())),
			// tv.getLineSpacingMultiplier());
			// }
			tv.setCompoundDrawablePadding(w(tv.getCompoundDrawablePadding()));
			Drawable[] cds = tv.getCompoundDrawables();
			for (Drawable d : cds) {
				Rect bounds = d == null ? null : d.getBounds();
				if (bounds != null && !bounds.isEmpty()) {
					bounds.set(w(bounds.left), h(bounds.top, useReallyScale), w(bounds.right), h(bounds.bottom, useReallyScale));
				}
			}
			tv.setCompoundDrawables(cds[0], cds[1], cds[2], cds[3]);
		}

		if (view instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) view;
			final int count = vg.getChildCount();
			for (int i = 0; i < count; i++) {
				compactViewImpl(vg.getChildAt(i), useReallyScale);
			}
		}

	}

}
