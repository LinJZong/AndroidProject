package com.linj.video.view;

import java.io.IOException;

import com.example.camera.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

/** 
 * @ClassName: VideoContainer 
 * @Description:  视频播放器容器，包含了视频操作的一些控件和视频播放SurfaceView
 * @author LinJ
 * @date 2015-1-21 下午4:49:31 
 *  
 */
public class VideoContainer extends LinearLayout {
	private VideoView mVideoView;
	private LinearLayout mBottomBar;
	public VideoContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	private void initView(Context context){
		setOrientation(VERTICAL);
		mVideoView=new VideoView(context);
		LinearLayout.LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.weight=1;
		mVideoView.setLayoutParams(layoutParams);
		addView(mVideoView);
		mBottomBar=(LinearLayout) inflate(context, R.layout.video_bottom_bar, null);
		layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		layoutParams.gravity=Gravity.BOTTOM;
		mBottomBar.setLayoutParams(layoutParams);
		addView(mBottomBar);
	}
	public void playVideo(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
		// TODO Auto-generated method stub
		mVideoView.play(path);
	}

}
