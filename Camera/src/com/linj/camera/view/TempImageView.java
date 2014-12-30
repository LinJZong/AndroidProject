package com.linj.camera.view;

import com.example.camera.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

/**
 * 临时ImageView，在执行完一个动画后自动隐藏
 * @author linj
 *
 */
public class TempImageView extends ImageView implements AnimationListener{
	/**
	 * 不存在的动画ID
	 */
	public static final int NO_ID=-1;
	/**
	 * 设置的动画效果ID
	 */
	private int animatID=NO_ID;


	public TempImageView(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
	}

	public TempImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TempImageView);
		animatID = a.getResourceId(R.styleable.TempImageView_animat_id, NO_ID);
		a.recycle();
		setVisibility(View.GONE);
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		setVisibility(View.VISIBLE);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		setVisibility(View.GONE);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	/**
	 * 开始动画
	 */
	public void startAnimation(){
		startAnimation(null);
	}


	public void startAnimation(int resourceID){
		animatID=resourceID;
		startAnimation();
	}
	/**
	 * 开始动画
	 */
	public void startAnimation(Animation animation){
		if(animation!=null){
			animation.setAnimationListener(this);
			super.startAnimation(animation);
			return;
		}
		if(animatID!=NO_ID){
			animation=AnimationUtils.loadAnimation(getContext(), animatID);
			animation.setAnimationListener(this);
			super.startAnimation(animation);
		}
	}
}