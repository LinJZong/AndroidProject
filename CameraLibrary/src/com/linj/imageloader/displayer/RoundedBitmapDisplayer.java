package com.linj.imageloader.displayer;


import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/** 
* @ClassName: RoundedBitmapDisplayer 
* @Description:  圆形图片
* @author LinJ
* @date 2015-1-8 上午9:47:31 
*  
*/
public class RoundedBitmapDisplayer implements BitmapDisplayer {

	protected final int cornerRadius;
	protected final int margin;

	public RoundedBitmapDisplayer(int cornerRadiusPixels) {
		this(cornerRadiusPixels, 0);
	}

	public RoundedBitmapDisplayer(int cornerRadiusPixels, int marginPixels) {
		this.cornerRadius = cornerRadiusPixels;
		this.margin = marginPixels;
	}

	@Override
	public void display(Bitmap bitmap, ImageView imageView) {
		imageView.setImageDrawable(new RoundedDrawable(bitmap, cornerRadius, margin));
	}
	@Override
	public void display(int resouceID, ImageView imageView) {
		imageView.setImageResource(resouceID);
	}

	public static class RoundedDrawable extends Drawable{

		protected final float cornerRadius;
		protected final int margin;

		protected final RectF mRect = new RectF(),
				mBitmapRect;
		protected final BitmapShader bitmapShader;
		protected final Paint paint;

		public RoundedDrawable(Bitmap bitmap, int cornerRadius, int margin) {
			this.cornerRadius = cornerRadius;
			this.margin = margin;

			bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			mBitmapRect = new RectF (margin, margin, bitmap.getWidth() - margin, bitmap.getHeight() - margin);

			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setShader(bitmapShader);
		}

		@Override
		protected void onBoundsChange(Rect bounds) {
			super.onBoundsChange(bounds);
			mRect.set(margin, margin, bounds.width() - margin, bounds.height() - margin);

			// Resize the original bitmap to fit the new bound
			Matrix shaderMatrix = new Matrix();
			shaderMatrix.setRectToRect(mBitmapRect, mRect, Matrix.ScaleToFit.FILL);
			bitmapShader.setLocalMatrix(shaderMatrix);	
		}
		@Override
		public void draw(Canvas canvas) {
			canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, paint);

		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}
		@Override
		public void setFilterBitmap(boolean filter) {
			paint.setFilterBitmap(filter);
			invalidateSelf();
		}

		public void setAntiAlias(boolean aa) {
			paint.setAntiAlias(aa);
			invalidateSelf();
		}
		 @Override
		    public void setDither(boolean dither) {
		        paint.setDither(dither);
		        invalidateSelf();
		    }
		@Override
		public void setAlpha(int alpha) {
			int oldAlpha = paint.getAlpha();
			if (alpha != oldAlpha) {
				paint.setAlpha(alpha);
				invalidateSelf();
			}
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			paint.setColorFilter(cf);
			invalidateSelf();
		}
		@Override
		public void setColorFilter(int color, Mode mode) {
			// TODO Auto-generated method stub
			paint.setColorFilter(new PorterDuffColorFilter(color, mode));
			invalidateSelf();
		}
	}


	
}