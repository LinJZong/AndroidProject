package com.linj.camera.view;



import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.linj.cameralibrary.R;

/** 
 * @ClassName: FocusImageView 
 * @Description:聚焦时显示的ImagView  
 * @author LinJ
 * @date 2015-1-4 下午2:55:34 
 *  
 */
public class FocusImageView extends ImageView {
	public final static String TAG="FocusImageView";
	private static final int NO_ID=-1;
	private int mFocusImg=NO_ID;
	private int mFocusSucceedImg=NO_ID;
	private int mFocusFailedImg=NO_ID;
	private Animation mAnimation;
	private Handler mHandler;
	public FocusImageView(Context context) {
		super(context);
		mAnimation=AnimationUtils.loadAnimation(getContext(), R.anim.focusview_show);
		setVisibility(View.GONE);
		mHandler=new Handler();
	}

	public FocusImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAnimation=AnimationUtils.loadAnimation(getContext(), R.anim.focusview_show);
		mHandler=new Handler();

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FocusImageView);
		mFocusImg = a.getResourceId(R.styleable.FocusImageView_focus_focusing_id, NO_ID);
		mFocusSucceedImg=a.getResourceId(R.styleable.FocusImageView_focus_success_id, NO_ID);
		mFocusFailedImg=a.getResourceId(R.styleable.FocusImageView_focus_fail_id, NO_ID);
		a.recycle();

		//聚焦图片不能为空
		if (mFocusImg==NO_ID||mFocusSucceedImg==NO_ID||mFocusFailedImg==NO_ID) 
			throw new RuntimeException("Animation is null");
	}

	/**  
	 *  显示聚焦图案
	 *  @param x 触屏的x坐标
	 *  @param y 触屏的y坐标
	 */
	public void startFocus(Point point){
		if (mFocusImg==NO_ID||mFocusSucceedImg==NO_ID||mFocusFailedImg==NO_ID) 
			throw new RuntimeException("focus image is null");
		//根据触摸的坐标设置聚焦图案的位置
		RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) getLayoutParams();
		params.topMargin= point.y-getHeight()/2;
		params.leftMargin=point.x-getWidth()/2;
		setLayoutParams(params);	
		//设置控件可见，并开始动画
		setVisibility(View.VISIBLE);
		setImageResource(mFocusImg);
		startAnimation(mAnimation);	
		//3秒后隐藏View。在此处设置是由于可能聚焦事件可能不触发。
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				setVisibility(View.GONE);
			}
		},3500);
	}
	
	/**  
	*   聚焦成功回调
	*/
	public void onFocusSuccess(){
		RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) getLayoutParams();
		if(params.topMargin==0&&params.leftMargin==0){
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			params.topMargin= metrics.heightPixels/2-getHeight()/2;
			params.leftMargin=metrics.widthPixels/2-getWidth()/2;
			setLayoutParams(params);	
		}
		setImageResource(mFocusSucceedImg);
		//移除在startFocus中设置的callback，1秒后隐藏该控件
		mHandler.removeCallbacks(null, null);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				setVisibility(View.GONE);
			}
		},1000);
		
	}
	
	/**  
	*   聚焦失败回调
	*/
	public void onFocusFailed(){
		RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) getLayoutParams();
		if(params.topMargin==0&&params.leftMargin==0){
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			params.topMargin= metrics.heightPixels/2-getHeight()/2;
			params.leftMargin=metrics.widthPixels/2-getWidth()/2;
			setLayoutParams(params);	
		}
		setImageResource(mFocusFailedImg);
		//移除在startFocus中设置的callback，1秒后隐藏该控件
		mHandler.removeCallbacks(null, null);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				setVisibility(View.GONE);
			}
		},1000);
	}

	/**  
	 * 设置开始聚焦时的图片
	 *  @param focus   
	 */
	public void setFocusImg(int focus) {
		this.mFocusImg = focus;
	}

	/**  
	 *  设置聚焦成功显示的图片
	 *  @param focusSucceed   
	 */
	public void setFocusSucceedImg(int focusSucceed) {
		this.mFocusSucceedImg = focusSucceed;
	}
}
