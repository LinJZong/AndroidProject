package com.linj.camera.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.camera.R;
import com.linj.camera.view.CameraContainer.TakePictureListener;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
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
import android.view.OrientationEventListener;
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
public class CameraView extends SurfaceView implements SurfaceHolder.Callback{

	private final static String TAG="CameraView";

	/** 和该View绑定的Camera对象 */
	private Camera mCamera;

	/** 当前闪光灯类型，默认为关闭 */ 
	private FlashMode mFlashMode=FlashMode.ON;

	/** 当前缩放级别  默认为0*/ 
	private int mZoom=0;

	/** 当前屏幕朝向  竖屏:true  横屏：false 此处的朝向不是Acitivty的朝向 而是重力感应获得的屏幕朝向*/ 
	private boolean mCurrentOrientation=true;

	public CameraView(Context context){
		super(context);
		//初始化容器
		getHolder().addCallback(this);
		mCamera = Camera.open();
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
		if(mCamera==null) return;
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
	 *  @param point 触屏坐标
	 */
	public void onFocus(Point point,AutoFocusCallback callback){
		Camera.Parameters parameters=mCamera.getParameters();
		//不支持设置自定义聚焦，则使用自动聚焦，返回
		if (parameters.getMaxNumFocusAreas()<=0) {
			mCamera.autoFocus(callback);
			return;
		}
		List<Area> areas=new ArrayList<Camera.Area>();
		int left=point.x-300;
		int top=point.y-300;
		int right=point.x+300;
		int bottom=point.y+300;
		areas.add(new Area(new Rect(left,top,right,bottom), 100));
		parameters.setFocusAreas(areas);
		try {
			//本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
			//目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			// TODO: handle exception
		}
		mCamera.autoFocus(callback);
	}

	/**  
	 *  获取最大缩放级别，最大为40
	 *  @return   
	 */
	public int getMaxZoom(){
		if(mCamera==null) return -1;		
		Camera.Parameters parameters=mCamera.getParameters();
		if(!parameters.isZoomSupported()) return -1;
		return parameters.getMaxZoom()>40?40:parameters.getMaxZoom();
	}
	/**  
	 *  设置相机缩放级别
	 *  @param zoom   
	 */
	public void setZoom(int zoom){
		if(mCamera==null) return;
		Camera.Parameters parameters=mCamera.getParameters();
		if(!parameters.isZoomSupported()) return;
		parameters.setZoom(zoom);
		mCamera.setParameters(parameters);
		mZoom=zoom;
	}
	public int getZoom(){
		return mZoom;
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			if(mCamera==null)
				mCamera=Camera.open();
			setCameraParameters();
			mCamera.setPreviewDisplay(getHolder());
		} catch (IOException e) {
			Toast.makeText(getContext(), "打开相机失败", Toast.LENGTH_SHORT).show();
			Log.e(TAG,e.getMessage());
		}
		mCamera.startPreview();
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
				//小于100W像素
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
		//自动聚焦模式
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(parameters);
		//设置闪光灯模式。此处主要是用于在相机摧毁后又重建，保持之前的状态
		setFlashMode(mFlashMode);
		//设置缩放级别
		setZoom(mZoom);
		//开启屏幕朝向监听
		startOrientationChangeListener();
	}

	/**  
	 *   启动屏幕朝向改变监听函数 用于在屏幕横竖屏切换时改变保存的图片的方向  
	 */
	private  void startOrientationChangeListener() {  
		OrientationEventListener mOrEventListener = new OrientationEventListener(getContext()) {  
			@Override  
			public void onOrientationChanged(int rotation) {  
				if (((rotation >= 0) && (rotation <= 45)) || (rotation >= 315)  
						|| ((rotation >= 135) && (rotation <= 225))) {// portrait  
					//朝向相同，不做处理
					if(mCurrentOrientation)
						return;
					mCurrentOrientation = true;
					updateCameraOrientation();
				} else if (((rotation > 45) && (rotation < 135))  
						|| ((rotation > 225) && (rotation < 315))) {// landscape  
					if(!mCurrentOrientation)
						return;
					mCurrentOrientation=false;
					updateCameraOrientation();
				}  
			}  
		};  
		mOrEventListener.enable();  
	}  

	/**  
	 *   根据当前朝向修改保存图片的旋转角度
	 */
	private void updateCameraOrientation(){

		if(mCamera!=null){
			Camera.Parameters parameters = mCamera.getParameters();
			//相机默认是横屏模式，当前为竖屏时，需要旋转90°
			if (mCurrentOrientation) {
				mCamera.setDisplayOrientation(90);//预览转90°
				parameters.set("rotation", 90);//生成的图片转90°
			}
			else  {
				parameters.set("orientation", "landscape");
				parameters.set("rotation", 0);
			}
			mCamera.setParameters(parameters);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		updateCameraOrientation();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		if (mCamera != null) {
			mCamera.stopPreview();
		}
		mCamera.release();
		mCamera = null;
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