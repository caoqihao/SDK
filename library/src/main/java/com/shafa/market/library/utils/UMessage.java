package com.shafa.market.library.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;


public final class UMessage {
	
	//持有toast 引用 可以让 toast显示 的信息更及时
	private static Toast mToast;
	
	private UMessage(){
	}

	/**
	 * 显示消息
	 * 一般用于在非主线程中调用 
	 * @see #show(Context context, String content)
	 * @see #show(Context context, int resId)
	 * @see #showLong(Context context, String content)
	 */
	public static void showTask(final Context context, final String content) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				showToast(context, content, Toast.LENGTH_SHORT);
			}
		});
	}
	
	public static void showTask(final Context context, int textId) {
		showTask(context, context.getString(textId));
	}
	/**
	 * 较短时间的显示消息 主线程中调用
	 * @see #showTask(Context context, String content)
	 * @see #show(Context context, int resId)
	 * @see #showLong(Context context, String content)
	 */
	public static void show(Context context, String content) {
		showToast(context, content, Toast.LENGTH_SHORT);
	}

	/**
	 * 较短时间的显示消息 主线程中调用
	 * @see #showTask(Context context, String content)
	 * @see #show(Context context, String content)
	 * @see #showLong(Context context, String content)
	 */
	public static void show(Context context, int resId) {
		final String content =context.getString(resId);
		showToast(context, content, Toast.LENGTH_SHORT);
	}

	/**
	 * 较长时间的显示消息 主线程中调用
	 * @see #showTask(Context context, String content)
	 * @see #show(Context context, int resId)
	 * @see #show(Context context, String content)
	 */
	public static void showLong(Context context, String content) {
		showToast(context, content, Toast.LENGTH_LONG);
	}
	
	private static void showToast(Context context, String content, int duration){
		if(mToast == null){
			mToast = Toast.makeText(context, null, duration);
			mToast.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.BOTTOM, 0, ShafaLayout.L1080P.h(92));
			View view = mToast.getView();
			if(view != null && view instanceof LinearLayout){
				LinearLayout layout = (LinearLayout) mToast.getView();
				layout.setBackgroundColor(0x99000000);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					layout.setBackground(getBackground());
				}else{
					layout.setBackgroundDrawable(getBackground());
				}
				layout.setOrientation(LinearLayout.HORIZONTAL);
				layout.setPadding(10 ,5 , 10 , 5);
				layout.setGravity(Gravity.CENTER);
				layout.removeAllViews();
				TextView tv = new TextView(context);
				tv.setLayoutParams(new LayoutParams(-1, -2));
				tv.setGravity(Gravity.CENTER_VERTICAL);
				tv.setTextColor(0x99ffffff);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ShafaLayout.L1080P.getFontSize(36));
				tv.setPadding(0, 0, 0, 0); 
				tv.setText(content);  
				layout.addView(tv);
				layout.setTag(tv);
			}else if(view != null && view instanceof TextView){
				TextView tv = (TextView) view;
				tv.setBackgroundColor(0x99000000);
				tv.setTextColor(0x99ffffff);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ShafaLayout.L1080P.getFontSize(36));
				tv.setPadding(0, 0, 0, 0); 
				tv.setText(content);
			}
		}else{
			mToast.setDuration(duration);
			View view = mToast.getView();
			if(view != null && view instanceof LinearLayout){
				LinearLayout layout = (LinearLayout) mToast.getView();
				TextView tv = (TextView) layout.getTag();
				tv.setText(content);
			}else if(view != null && view instanceof TextView){
				TextView tv = (TextView) view;
				tv.setText(content);
			}
		}
		
		mToast.show();
	}

	private static Drawable getBackground(){
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(0x99000000);//内部填充颜色
		gd.setCornerRadius(5);
		return gd;
	}
}
