package com.linj.album.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;

/** 
 * @ClassName: MatrixImageView 
 * @Description:  带放大、缩小、移动效果的ImageView
 * @author LinJ
 * @date 2015-1-7 上午11:15:07 
 *  
 */
public class MatrixImageView extends ImageView{
	private final static String TAG="MatrixImageView";
	private GestureDetector mGestureDetector;
	public MatrixImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector=new GestureDetector(getContext(), new GestureListener());
		setScaleType(ScaleType.CENTER_CROP);
	}
    float scaleCount=0;
	private static final int MODE_INIT = 0;
	private static final int MODE_DRAG = 1;
	/** 放大缩小照片模式 */
	private static final int MODE_ZOOM = 2;
	/** 记录是拖拉照片模式还是放大缩小照片模式 */
	private int mode = MODE_INIT;// 初始状态 
	/** 拖拉照片模式 */
	private float startDis;
	private Matrix matrix = new Matrix();
	/** 用于记录图片要进行拖拉时候的坐标位置 */
	private Matrix currentMatrix = new Matrix();
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "ACTION_DOWN");
			mode=MODE_INIT;
			currentMatrix.set(getImageMatrix());
			setScaleType(ScaleType.MATRIX);
			break;
		case MotionEvent.ACTION_UP:
			setScaleType(ScaleType.FIT_XY);
			Log.i(TAG, "ACTION_UP");
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.i(TAG, "ACTION_CANCEL");
			break;
		case MotionEvent.ACTION_MOVE:

			if (mode == MODE_ZOOM) {
				//只有同时触屏两个点的时候才执行
				if(event.getPointerCount()<2) return true;
				float endDis = distance(event);// 结束距离
				if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
					float scale = endDis / startDis;// 得到缩放倍数
					if(scaleCount+scale>1){
						matrix.set(currentMatrix);
						Log.i(TAG, getWidth()+" "+getHeight());
						matrix.postScale(scale, scale,getWidth()/2,getHeight()/2);
						setImageMatrix(matrix);
						scaleCount+=scale-1;
					}
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mode=MODE_ZOOM;
			/** 计算两个手指间的距离 */
			startDis = distance(event);
			if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
				currentMatrix.set(getImageMatrix());
			}
			Log.i(TAG, "ACTION_POINTER_DOWN");
			break;
		case MotionEvent.ACTION_POINTER_UP:
			Log.i(TAG, "ACTION_POINTER_UP");
			break;
		default:
			break;
		}
		
		return true;

		//		return mGestureDetector.onTouchEvent(event);
	}
	/** 计算两个手指间的距离 */
	private float distance(MotionEvent event) {
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		/** 使用勾股定理返回两点之间的距离 */
		return (float) Math.sqrt(dx * dx + dy * dy);
	}
	/** 计算两个手指间的中间点 */
	private PointF mid(MotionEvent event) {
		float midX = (event.getX(1) + event.getX(0)) / 2;
		float midY = (event.getY(1) + event.getY(0)) / 2;
		return new PointF(midX, midY);
	}
	private class  GestureListener extends SimpleOnGestureListener{

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onSingleTapUp(e);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			super.onLongPress(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			super.onShowPress(e);
		}

		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onDown(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onDoubleTapEvent(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onSingleTapConfirmed(e);
		}

	}


}
