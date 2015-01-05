package com.example.camera;

import java.io.File;
import java.util.List;

import com.linj.camera.view.CameraContainer;
import com.linj.camera.view.CameraContainer.TakePictureListener;
import com.linj.camera.view.CameraView.FlashMode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
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
		List<File> files=FileOperateUtil.listFiles(thumbFolder, ".jpg");
		if(files!=null&&files.size()>0){
			Bitmap thumbBitmap=BitmapFactory.decodeFile(files.get(0).getAbsolutePath());
			if(thumbBitmap!=null)
				mThumbButton.setImageBitmap(thumbBitmap);
		}else {
			mThumbButton.setImageBitmap(null);
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
			
            startActivity(new Intent(this,AlbumAty.class));
			break;
		case R.id.btn_flash_mode:
			if(mContainer.getFlashMode()==FlashMode.ON){
				mContainer.setFlashMode(FlashMode.OFF);
				mFlashView.setImageResource(R.drawable.btn_flash_off);
			}else if (mContainer.getFlashMode()==FlashMode.OFF) {
				mContainer.setFlashMode(FlashMode.AUTO);
				mFlashView.setImageResource(R.drawable.btn_flash_auto);
			}
			else if (mContainer.getFlashMode()==FlashMode.AUTO) {
				mContainer.setFlashMode(FlashMode.TORCH);
				mFlashView.setImageResource(R.drawable.btn_flash_torch);
			}
			else if (mContainer.getFlashMode()==FlashMode.TORCH) {
				mContainer.setFlashMode(FlashMode.ON);
				mFlashView.setImageResource(R.drawable.btn_flash_on);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onTakePictureEnd(Bitmap thumBitmap) {
		mShutterButton.setClickable(true);	
	}

	@Override
	public void onAnimtionEnd(Bitmap bm) {
		if(bm!=null){
			//生成缩略图
			Bitmap thumbnail=ThumbnailUtils.extractThumbnail(bm, 213, 213);
			mThumbButton.setImageBitmap(thumbnail);
		}
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
	}
}