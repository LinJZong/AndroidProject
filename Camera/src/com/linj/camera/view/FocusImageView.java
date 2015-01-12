package com.linj.camera.view;

import com.example.camera.R;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;

/** 
 * @ClassName: FocusImageView 
 * @Description:聚焦时显示的ImagView  
 * @author LinJ
 * @date 2015-1-4 下午2:55:34 
 *  
 */
public class FocusImageView extends ImageView implements AnimationListener{
	public final static String TAG="FocusImageView";
	private static final int NO_ID=-1;
	private int mFocusImg=NO_ID;
	private int mFocusSucceedImg=NO_ID;
	private Animation mAnimation;
	public FocusImageView(Context context) {
		super(context);
		mAnimation=AnimationUtils.loadAnimation(getContext(), R.anim.focusview_show);
		mAnimation.setAnimationListener(this);
		setVisibility(View.GONE);
	}


	/**  
	 *  显示聚焦图案
	 *  @param x 触屏的x坐标
	 *  @param y 触屏的y坐标
	 */
	public void show(Point point){
		if (mFocusImg==NO_ID||mFocusSucceedImg==NO_ID) 
			throw new RuntimeException("Animation is null");
		//根据触摸的坐标设置聚焦图案的位置
		RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) getLayoutParams();
		params.topMargin= point.y-getHeight()/2;
		params.leftMargin=point.x-getWidth()/2;
		setLayoutParams(params);	
		//设置控件可见，并开始动画
		setVisibility(View.VISIBLE);
		startAnimation(mAnimation);	
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

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		setImageResource(mFocusImg);
	}

	@Override
	public void onAnimationEnd(Animation animation) {

		//1秒后隐藏View 设置token为mFocusImageView防止被误删除
		new Handler().postAtTime(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				setVisibility(View.GONE);
			}
		},SystemClock.uptimeMillis()+3000);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}


}
