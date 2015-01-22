package com.linj.video.view;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

/** 
 * @ClassName: VideoSurfaceView 
 * @Description:  和MediaPlayer绑定的SurfaceView，用以播放视频
 * @author LinJ
 * @date 2015-1-21 下午2:38:53 
 *  
 */
public class VideoView extends SurfaceView implements OnCompletionListener{
	private final static String TAG="VideoSurfaceView";
	private MediaPlayer mMediaPlayer;
	public VideoView(Context context){
		super(context);
		init();
	}
	public VideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();

	}
	private void init() {
		mMediaPlayer=new MediaPlayer();
		mMediaPlayer.setOnCompletionListener(this);
		//初始化容器
		getHolder().addCallback(callback);
	}

	private SurfaceHolder.Callback callback=new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mMediaPlayer.setDisplay(getHolder());       	
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if(mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			mMediaPlayer.reset();
		}
	};

	public void play(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		((View) getParent()).setVisibility(View.VISIBLE);
		if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
			mMediaPlayer.stop();
			mMediaPlayer.reset();
		}
		mMediaPlayer.setDataSource(path);
		mMediaPlayer.prepare();
		mMediaPlayer.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		((View) getParent()).setVisibility(View.GONE);
	}

}
