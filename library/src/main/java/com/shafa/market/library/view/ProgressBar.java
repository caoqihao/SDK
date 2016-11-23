package com.shafa.market.library.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.shafa.market.library.utils.Strings;
import com.shafa.market.library.utils.ShafaLayout;

public class ProgressBar extends View {
	
	public ProgressBar(Context context) {
		super(context);
		initView();
	}
	
    public ProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public ProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private static final int MAX_LEVEL = 10000;
	
	private int mProgress;
	private int mMax = 100;
	
	private int mWidth;
	private int mHeight;
	
	private int mDrawableWidth;
	private int mDrawableHeight;
	
	private Rect mRect;
	private Drawable mPgDownloading;
	private Drawable mBg;

	private LayerDrawable mDrawables; //0:背景  1:下载中进度   2:下载完成

	private String alertText = Strings.SHAFA_DOWNLOADING_ALERT;
	private void initView(){
		mRect = new Rect();
	}

	public void setProgress(int progress) {
		if(progress == 100){
			if(null != mDrawables){
				mPgDownloading = mDrawables.getDrawable(2);
			}
			alertText = Strings.SHAFA_DOWNLOADED_ALERT;
		}

		if(progress == 0){
			alertText = Strings.SHAFA_DOWNLOADING_ALERT;
			if(null != mDrawables){
				mPgDownloading = mDrawables.getDrawable(1);
			}
		}
		mProgress = progress;
		postInvalidate();
	}

	public void setProgressDrawable(Drawable d) {
		if (d instanceof LayerDrawable) {
			mDrawables = (LayerDrawable)d;
			mDrawableWidth = d.getMinimumWidth();
			mDrawableHeight = d.getMinimumHeight();

			mBg = ((LayerDrawable)d).getDrawable(0);
			mPgDownloading = ((LayerDrawable)d).getDrawable(1);
		}
		
		postInvalidate();
	}

	private TextPaint mTextPaint = new TextPaint();

	@Override
	protected void onDraw(Canvas canvas) {

		Log.i("AAAAA",">>>>>>>>>>>>>>>>>>>>>>onDraw----------");

		mTextPaint.setTextSize(ShafaLayout.L1080P.getFontSize(42));
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

		canvas.translate(getPaddingLeft(), getPaddingTop());
		if (mWidth <= 0) {
			mWidth = getWidth();
		}

		mHeight = getHeight();
		try {
			if (mWidth > 0) {
				mRect.set(0, 0, mWidth, mHeight);
				mBg.setBounds(0, 0, mWidth, mHeight);
				mBg.draw(canvas);

				if (mPgDownloading instanceof ClipDrawable) {
					mPgDownloading.setBounds(0, 0, mWidth, mHeight);
					int level = 0;
					if (mMax > 0) {
						level = MAX_LEVEL * mProgress / mMax;
						if (level < 0) {
							level = 0;
						} else if (level > MAX_LEVEL) {
							level = MAX_LEVEL;
						}
					}
				} else {
					int w = mWidth * mProgress / mMax;
					if (w < 0) {
						w = 0;
					} else if (w > mWidth) {
						w = mWidth;
					}
					mPgDownloading.setBounds(0, 0, w, mHeight);
				}

				mPgDownloading.draw(canvas);
			}
		} catch (Exception e) {
		}

		// 计算Baseline绘制的起点X轴坐标 ，计算方式：画布宽度的一半 - 文字宽度的一半
		int baseX = (int) (canvas.getWidth() / 2 - mTextPaint.measureText(alertText) / 2);

		// 计算Baseline绘制的Y坐标 ，计算方式：画布高度的一半 - 文字总高度的一半
		int baseY = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));

		// 居中画一个文字
		canvas.drawText(alertText, baseX, baseY, mTextPaint);
	}

//	private Drawable tileify(Drawable drawable, boolean clip) {
//		if (drawable instanceof LayerDrawable) {
//            LayerDrawable background = (LayerDrawable) drawable;
//            final int N = background.getNumberOfLayers();
//            Drawable[] outDrawables = new Drawable[N];
//
//            for (int i = 0; i < N; i++) {
//                int id = background.getId(i);
//                outDrawables[i] = tileify(background.getDrawable(i),
//                        (id == android.R.id.progress || id == android.R.id.secondaryProgress));
//            }
//
//            LayerDrawable newBg = new LayerDrawable(outDrawables);
//
//            for (int i = 0; i < N; i++) {
//                newBg.setId(i, background.getId(i));
//            }
//
//            return newBg;
//
//        } else if (drawable instanceof BitmapDrawable) {
//            final Bitmap tileBitmap = ((BitmapDrawable) drawable).getBitmap();
//
//            final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
//    		Shape shape = new RoundRectShape(roundedCorners, null, null);
//            final ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
//
//            final BitmapShader bitmapShader = new BitmapShader(tileBitmap,
//                    Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
//            shapeDrawable.getPaint().setShader(bitmapShader);
//
//            return (clip) ? new ClipDrawable(shapeDrawable, Gravity.LEFT,
//                    ClipDrawable.HORIZONTAL) : shapeDrawable;
//        }
//
//		return drawable;
//	}
}
