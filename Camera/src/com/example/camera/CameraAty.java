package com.example.camera;

import java.io.File;

import com.linj.camera.view.CameraContainer;
import com.linj.camera.view.CameraContainer.TakePictureListener;
import com.linj.camera.view.CameraView.FlashMode;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

/** 
 * @ClassName: CameraAty 
 * @Description:  自定义照相机类
 * @author LinJ
 * @date 2014-12-31 上午9:44:25 
 *  
 */
public class CameraAty extends Activity implements View.OnClickListener,TakePictureListener{
	private final static String TAG="CameraAty";
	private String mSaveRoot;
	private CameraContainer mContainer;
	private ImageButton mThumbButton;
	private ImageButton mShutterButton;
	private ImageView mFlashView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera);

		mContainer=(CameraContainer)findViewById(R.id.container);
		mThumbButton=(ImageButton)findViewById(R.id.btn_thumbnail);
		mShutterButton=(ImageButton)findViewById(R.id.btn_shutter);
		mFlashView=(ImageView)findViewById(R.id.btn_flash_mode);

		mThumbButton.setOnClickListener(this);
		mShutterButton.setOnClickListener(this);
		mFlashView.setOnClickListener(this);

		mSaveRoot="test";
		mContainer.setRootPath(mSaveRoot);
		initThumbnail();
	}

	/**
	 * 加载缩略图
	 */
	private void initThumbnail() {
		String thumbFolder=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_THUMBNAIL, mSaveRoot);
		File[] files=FileOperateUtil.listFiles(thumbFolder, ".jpg");
		if(files!=null&&files.length>0){
			Bitmap thumbBitmap=BitmapFactory.decodeFile(files[files.length-1].getAbsolutePath());
			if(thumbBitmap!=null)
				mThumbButton.setImageBitmap(thumbBitmap);
		}

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_shutter:
			mShutterButton.setClickable(false);
			mContainer.takePicture(this);
			break;
		case R.id.btn_thumbnail:

			break;
		case R.id.btn_flash_mode:
			if(mContainer.getFlashMode()==FlashMode.OFF){
				mContainer.setFlashMode(FlashMode.ON);
				mFlashView.setImageResource(R.drawable.btn_flash_on);
			}else if (mContainer.getFlashMode()==FlashMode.ON) {
				mContainer.setFlashMode(FlashMode.AUTO);
				mFlashView.setImageResource(R.drawable.btn_flash_auto);
			}
			else if (mContainer.getFlashMode()==FlashMode.AUTO) {
				mContainer.setFlashMode(FlashMode.TORCH);
				mFlashView.setImageResource(R.drawable.btn_flash_torch);
			}
			else if (mContainer.getFlashMode()==FlashMode.TORCH) {
				mContainer.setFlashMode(FlashMode.OFF);
				mFlashView.setImageResource(R.drawable.btn_flash_off);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onTakePictureEnd() {
		mShutterButton.setClickable(true);	
	}

	@Override
	public void onAnimtionEnd() {
		//重置缩略图
		initThumbnail();
	}
}