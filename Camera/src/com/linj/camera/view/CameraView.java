package com.linj.camera.view;

import java.io.IOException;
import java.util.List;

import com.example.camera.R;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;


public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
	
	private String TAG="CameraView";
	
	/**
	 * 和该View绑定的Camera对象
	 */
	private Camera camera;

	public CameraView(Context context){
		super(context);
		//初始化容器
		getHolder().addCallback(this);
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//初始化容器
		getHolder().addCallback(this);
	}

	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera = Camera.open();
			setCameraParameters();
			camera.setPreviewDisplay(getHolder());
		} catch (IOException e) {
			Toast.makeText(getContext(), "打开相机失败", Toast.LENGTH_SHORT).show();
			Log.e(TAG,e.getMessage());
		}
		camera.startPreview();
	}

	
	/**
	 * 设置照相机参数
	 */
	private void setCameraParameters(){
		Camera.Parameters parameters = camera.getParameters();
		// 选择合适的预览尺寸   
		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
		if (sizeList.size()>0) {
			Size cameraSize=sizeList.get(0);
			//预览图片大小
			parameters.setPreviewSize(cameraSize.width, cameraSize.height);
		}

		//设置生成的图片大小
		sizeList = parameters.getSupportedPictureSizes();
		if (sizeList.size()>0) {
			Size cameraSize=sizeList.get(0);
			for (Size size : sizeList) {
				//小于500W像素
				if (size.width*size.height<100*10000) {
					cameraSize=size;
					break;
				}
			}
			parameters.setPictureSize(cameraSize.width, cameraSize.height);
		}
		//设置图片格式
		parameters.setPictureFormat(ImageFormat.JPEG);       
		parameters.setJpegQuality(100);
		parameters.setJpegThumbnailQuality(100);
		//自动校准
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		camera.setParameters(parameters);
	}

	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		Camera.Parameters parameters = camera.getParameters();
		//判断屏幕朝向
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			camera.setDisplayOrientation(90);//预览转90°
			parameters.set("rotation", 90);//生成的图片转90°
		}
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			parameters.set("orientation", "landscape");
			parameters.set("rotation", 0);
		}
		camera.setParameters(parameters);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	
		if (camera != null) {
			camera.stopPreview();
		}
		camera.release();
		camera = null;
	}

	public void takePicture(PictureCallback callback){
		camera.takePicture(null, null, callback);
	}
}