package com.example.camera;

import java.io.File;

import com.linj.camera.view.CameraContainer;
import com.linj.camera.view.CameraView;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
/**
 * 自定义照相机类
 * @author Administrator
 *
 */
public class CameraAty extends Activity implements View.OnClickListener{

	private String saveRoot;
	private CameraContainer container;
	private ImageButton thumbButton;
	private ImageButton shutterButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera);

		container=(CameraContainer)findViewById(R.id.container);
		thumbButton=(ImageButton)findViewById(R.id.btn_thumbnail);
		shutterButton=(ImageButton)findViewById(R.id.btn_shutter);

		thumbButton.setOnClickListener(this);
		shutterButton.setOnClickListener(this);

		saveRoot="test";
		container.setRootPath(saveRoot);
		initThumbnail();
	}

	/**
	 * 加载缩略图
	 */
	private void initThumbnail() {
		String thumbFolder=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_THUMBNAIL, saveRoot);
		File[] files=FileOperateUtil.listFiles(thumbFolder, ".jpg");
		if(files!=null&&files.length>0){
			Bitmap thumbBitmap=BitmapFactory.decodeFile(files[files.length-1].getAbsolutePath());
			if(thumbBitmap!=null)
				thumbButton.setImageBitmap(thumbBitmap);
		}

	}


	private AutoFocusCallback takepicFocusCallback =new AutoFocusCallback() {

		public void onAutoFocus(boolean success, Camera camera) {
			// TODO Auto-generated method stub
			if(success)//success表示对焦成功
			{
				//在拍照前 打开闪光灯
				Camera.Parameters parameters=camera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
				camera.setParameters(parameters);
//				camera.takePicture(null, null, new CameraCallback(CameraAty.this, saveRoot));
				//拍完 关闭闪光灯
				parameters=camera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				camera.setParameters(parameters);
			}
			else
			{
				//未对焦成功
				Log.i("", "myAutoFocusCallback: 失败了...");
			} 
			findViewById(R.id.btn_thumbnail).setClickable(true);
		}
	};


	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_shutter:
			view.setClickable(false);
			container.takePicture();
			break;
		case R.id.btn_thumbnail:

			break;

		default:
			break;
		}
	}
}