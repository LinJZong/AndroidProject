package com.linj.camera.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.camera.R;
import com.linj.camera.view.CameraContainer.TakePictureListener;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;


/** 
 * @ClassName: CameraView 
 * @Description: 和相机绑定的SurfaceView 封装了拍照方法
 * @author LinJ
 * @date 2014-12-31 上午9:44:56 
 *  
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback
,AutoFocusCallback{

	private final static String TAG="CameraView";

	/** 和该View绑定的Camera对象 */
	private Camera mCamera;

	/** 当前闪光灯类型，默认为关闭 */ 
	private FlashMode mFlashMode=FlashMode.OFF;

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


	/**  
	 *  获取当前闪光灯类型
	 *  @return   
	 */
	public FlashMode getFlashMode() {
		return mFlashMode;
	}

	/**  
	 *  设置闪光灯类型
	 *  @param flashMode   
	 */
	public void setFlashMode(FlashMode flashMode) {
		if(flashMode==mFlashMode) return;
		mFlashMode = flashMode;
		Camera.Parameters parameters=mCamera.getParameters();
		switch (flashMode) {
		case ON:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			break;
		case AUTO:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			break;
		case TORCH:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			break;
		default:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			break;
		}
		mCamera.setParameters(parameters);
	}

	public void takePicture(PictureCallback callback,TakePictureListener listener){
		mCamera.takePicture(null, null, callback);
	}

	/**  
	 * 手动聚焦 
	 *  @param x 触屏的x坐标
	 *  @param y   触屏的y坐标
	 */
	public void onFocus(int x,int y){
		Camera.Parameters parameters=mCamera.getParameters();
		//不支持设置自定义聚焦，则使用自动聚焦，返回
		if (parameters.getMaxNumFocusAreas()<=0) {
			mCamera.autoFocus(this);
			return;
		}
		List<Area> areas=new ArrayList<Camera.Area>();
		int left=x-300;
		int top=y-300;
		int right=x+300;
		int bottom=y+300;
		areas.add(new Area(new Rect(left,top,right,bottom), 100));
		parameters.setFocusAreas(areas);
		try {
			//本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
			//目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			// TODO: handle exception
		}
		mCamera.autoFocus(this);
	}



	/**
	 * 设置照相机参数
	 */
	private void setCameraParameters(){
		Camera.Parameters parameters = mCamera.getParameters();
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
//		if(parameters.isZoomSupported())
//           parameters.setZoom(parameters.getMaxZoom());
//		parameters.
		mCamera.setParameters(parameters);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera = Camera.open();
			setCameraParameters();
			mCamera.setPreviewDisplay(getHolder());
		} catch (IOException e) {
			Toast.makeText(getContext(), "打开相机失败", Toast.LENGTH_SHORT).show();
			Log.e(TAG,e.getMessage());
		}
		mCamera.startPreview();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		Camera.Parameters parameters = mCamera.getParameters();
		Log.i(TAG, getResources().getConfiguration().orientation+"");
		//判断屏幕朝向
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mCamera.setDisplayOrientation(90);//预览转90°
			parameters.set("rotation", 90);//生成的图片转90°
			
		}
		else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			parameters.set("orientation", "landscape");
			parameters.set("rotation", 0);
		}
		mCamera.setParameters(parameters);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		if (mCamera != null) {
			mCamera.stopPreview();
		}
		mCamera.release();
		mCamera = null;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {

	}

	/** 
	 * @Description: 闪光灯类型枚举 默认为关闭
	 */
	public enum FlashMode{
		/** ON:拍照时打开闪光灯   */ 
		ON,
		/** OFF：不打开闪光灯  */ 
		OFF,
		/** AUTO：系统决定是否打开闪光灯  */ 
		AUTO,
		/** TORCH：一直打开闪光灯  */ 
		TORCH
	}
}