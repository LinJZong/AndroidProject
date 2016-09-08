package com.linj.camera.view;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.linj.FileOperateUtil;
import com.linj.camera.view.CameraContainer.TakePictureListener;


/** 
 * @ClassName: CameraView 
 * @Description: 和相机绑定的SurfaceView 封装了拍照方法
 * @author LinJ
 * @date 2014-12-31 上午9:44:56 
 *  
 */
public class CameraView extends SurfaceView implements CameraOperation{

	public final static String TAG="CameraView";
	/** 和该View绑定的Camera对象 */
	private Camera mCamera;

	/** 当前闪光灯类型，默认为关闭 */ 
	private FlashMode mFlashMode=FlashMode.AUTO;

	/** 当前缩放级别  默认为0*/ 
	private int mZoom=0;

	/** 当前屏幕旋转角度*/ 
	private int mOrientation=0;
	/** 是否打开前置相机,true为前置,false为后置  */ 
	private boolean mIsFrontCamera;
	/**  录像类 */ 
	private MediaRecorder mMediaRecorder;
	/**  相机配置，在录像前记录，用以录像结束后恢复原配置 */ 
	private Camera.Parameters mParameters;
	/**  录像存放路径 ，用以生成缩略图*/ 
	private String mRecordPath=null;
	/** 设置默认自动对焦 */
	private AutoFocusCallback autoFocus;
	/** 录视频保存路径 */
	private String videoSavePath;
	/** 录视频时的尺寸 */
	private int videoMode = CamcorderProfile.QUALITY_1080P;
	public CameraView(Context context){
		super(context);
		//初始化容器
		getHolder().addCallback(callback);
		openCamera();
		mIsFrontCamera=false;
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//初始化容器
		getHolder().addCallback(callback);
		openCamera();
		mIsFrontCamera=false;
	}

	private SurfaceHolder.Callback callback=new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if(mCamera==null){
					openCamera();
				}
				setCameraParameters();
				mCamera.setPreviewDisplay(getHolder());
			} catch (Exception e) {
				Toast.makeText(getContext(), "打开相机失败", Toast.LENGTH_SHORT).show();
				Log.e(TAG,e.getMessage());
			}
			mCamera.startPreview();
			mCamera.cancelAutoFocus();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			mCamera.autoFocus(autoFocus);
			setCameraParameters();
			updateCameraOrientation();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			//停止录像
			stopRecord();
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}

		}
	};

	protected boolean isRecording(){
		return mMediaRecorder!=null;
	}
	/**
	 * 
	 * @param quality </br>{@link CamcorderProfile#QUALITY_480P}
	 * </br>{@link CamcorderProfile#QUALITY_720P }
	 * </br>{@link CamcorderProfile#QUALITY_1080P}
	 * </br>{@link CamcorderProfile#QUALITY_QVGA}
	 * </br>{@link CamcorderProfile#QUALITY_TIME_LAPSE_QCIF}
	 * </br>{@link CamcorderProfile#QUALITY_TIME_LAPSE_CIF}
	 * </br>{@link CamcorderProfile#QUALITY_TIME_LAPSE_480P}
	 * </br>{@link CamcorderProfile#QUALITY_TIME_LAPSE_720P}
	 * </br>{@link CamcorderProfile#QUALITY_TIME_LAPSE_1080P}
	 * </br>{@link CamcorderProfile#QUALITY_TIME_LAPSE_QVGA}
	 */
	public void setProfile(int quality){
		this.videoMode = quality;
	}
 
	/**  
	 *  开始录像
	 *  @return 开始录像是否成功   
	 */
	@Override
	public boolean startRecord(){
		if(mCamera==null)
			openCamera();
		if (mCamera==null) {
			return false;
		}
		if(mMediaRecorder==null)
			mMediaRecorder = new MediaRecorder();
		else
			mMediaRecorder.reset();
		mParameters=mCamera.getParameters();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		mMediaRecorder
		.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder
		.setAudioSource(MediaRecorder.AudioSource.MIC);
		//设置录像参数，由于应用需要此处取一个较小格式的视频
		mMediaRecorder.setProfile(CamcorderProfile.get(videoMode));
		//设置输出视频朝向，便于播放器识别。由于是竖屏录制，需要正转90°
		mMediaRecorder.setOrientationHint(90);
		String path=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_VIDEO, videoSavePath);
		File directory=new File(path);
		if(!directory.exists())
			directory.mkdirs();
		try {
			String name="video"+FileOperateUtil.createFileNmae(".3gp");
			mRecordPath=path+File.separator+name;
			File mRecAudioFile = new File(mRecordPath);
			mMediaRecorder.setOutputFile(mRecAudioFile
					.getAbsolutePath());
			mMediaRecorder.prepare();
			mMediaRecorder.start();
			Log.w(TAG, "video path"+mRecordPath);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public Bitmap stopRecord(){
		Log.w(TAG, "video stopRecord"+mRecordPath);
		Bitmap bitmap=null;
		try {
			if(mMediaRecorder!=null){
				mMediaRecorder.stop();
				mMediaRecorder.reset();
				mMediaRecorder.release();
				mMediaRecorder=null;
				//保存视频的缩略图
				bitmap=saveThumbnail();
			}
			if(mParameters!=null&&mCamera!=null){
				//重新连接相机
				mCamera.reconnect();
				//停止预览，注意这里必须先调用停止预览再设置参数才有效
				mCamera.stopPreview();
				//设置参数为录像前的参数，不然如果录像是低配，结束录制后预览效果还是低配画面
				mCamera.setParameters(mParameters);
				//重新打开
				mCamera.startPreview();
				mParameters=null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bitmap;
	}

	private Bitmap saveThumbnail() throws FileNotFoundException, IOException {
		if(mRecordPath!=null){
			//创建缩略图,该方法只能获取384X512的缩略图，舍弃，使用源码中的获取缩略图方法
			//			Bitmap bitmap=ThumbnailUtils.createVideoThumbnail(mRecordPath, Thumbnails.MINI_KIND);
			Bitmap bitmap=getVideoThumbnail(mRecordPath);

			if(bitmap!=null){
				String mThumbnailFolder=FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, videoSavePath);
				File folder=new File(mThumbnailFolder);
				if(!folder.exists()){
					folder.mkdirs();
				}
				File file=new File(mRecordPath);
				file=new File(folder+File.separator+file.getName().replace("3gp", "jpg"));
				//存图片小图
				BufferedOutputStream bufferos=new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(Bitmap.CompressFormat.JPEG,100, bufferos);
				bufferos.flush();
				bufferos.close();
				return bitmap;
			}
			mRecordPath=null;
		}
		return null;
	}

	/**  
	 *  获取帧缩略图，根据容器的高宽进行缩放
	 *  @param filePath
	 *  @return   
	 */
	public Bitmap getVideoThumbnail(String filePath) {  
		Bitmap bitmap = null;  
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();  
		try {  
			retriever.setDataSource(filePath);  
			bitmap = retriever.getFrameAtTime(-1);  
		}   
		catch(IllegalArgumentException e) {  
			e.printStackTrace();  
		}   
		catch (RuntimeException e) {  
			e.printStackTrace();  
		}   
		finally {  
			try {  
				retriever.release();  
			}   
			catch (RuntimeException e) {  
				e.printStackTrace();  
			}  
		} 
		if(bitmap==null)
			return null;
		// Scale down the bitmap if it's too large.
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Log.i(TAG, "bitmap:"+width+" "+height);
		int pWidth=getWidth();// 容器宽度
		int pHeight=getHeight();//容器高度
		Log.i(TAG, "parent:"+pWidth+" "+pHeight);
		//获取宽高跟容器宽高相比较小的倍数，以此为标准进行缩放
		float scale = Math.min((float)width/pWidth, (float)height/pHeight);
		Log.i(TAG, scale+"");
		int w = Math.round(scale * pWidth);
		int h = Math.round(scale * pHeight);
		Log.i(TAG, "parent:"+w+" "+h);
		bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
		return bitmap;  
	}  

	/**  
	 *   转换前置和后置照相机
	 */
	@Override
	public void switchCamera(){
		mIsFrontCamera=!mIsFrontCamera;
		openCamera();
		if(mCamera!=null){
			setCameraParameters();
			updateCameraOrientation();
			try {
				mCamera.setPreviewDisplay(getHolder());
				mCamera.startPreview();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**  
	 *   根据当前照相机状态(前置或后置)，打开对应相机
	 */
	private boolean openCamera()  {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		if(mIsFrontCamera){
			Camera.CameraInfo cameraInfo=new CameraInfo();
			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if(cameraInfo.facing==Camera.CameraInfo.CAMERA_FACING_FRONT){
					try {
						mCamera=Camera.open(i);
					} catch (Exception e) {
						mCamera =null;
						return false;
					}

				}
			}
		}else {
			try {
				mCamera=Camera.open();
			} catch (Exception e) {
				mCamera =null;
				return false;
			}

		}
		return true;
	}

	/**  
	 *  获取当前闪光灯类型
	 *  @return   
	 */
	@Override
	public FlashMode getFlashMode() {
		return mFlashMode;
	}

	/**  
	 *  设置闪光灯类型
	 *  @param flashMode   
	 */
	@Override
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
	@Override
	public void takePicture(PictureCallback callback,TakePictureListener listener){
		mCamera.takePicture(null, null, callback);
	}

	/**  
	 * 手动聚焦 
	 *  @param point 触屏坐标
	 */
	protected void onFocus(Point point,AutoFocusCallback callback){
		this.autoFocus = callback;
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
		left=left<-1000?-1000:left;
		top=top<-1000?-1000:top;
		right=right>1000?1000:right;
		bottom=bottom>1000?1000:bottom;
		areas.add(new Area(new Rect(left,top,right,bottom), 100));
		parameters.setFocusAreas(areas);
		try {
			//本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
			//目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		mCamera.autoFocus(callback);
	}

	/**  
	 *  获取最大缩放级别，最大为40
	 *  @return   
	 */
	@Override
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
	@Override
	public void setZoom(int zoom){
		if(mCamera==null) return;
		Camera.Parameters parameters;
		//注意此处为录像模式下的setZoom方式。在Camera.unlock之后，调用getParameters方法会引起android框架底层的异常
		//stackoverflow上看到的解释是由于多线程同时访问Camera导致的冲突，所以在此使用录像前保存的mParameters。
		if(mParameters!=null)
			parameters=mParameters;
		else {
			parameters=mCamera.getParameters();
		}

		if(!parameters.isZoomSupported()) return;
		parameters.setZoom(zoom);
		mCamera.setParameters(parameters);
		mZoom=zoom;
	}
	@Override
	public int getZoom(){
		return mZoom;
	}

	/**
	 * 设置照相机参数
	 */
	private void setCameraParameters(){
		Camera.Parameters parameters = mCamera.getParameters();
		// 选择合适的预览尺寸   
//		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
//		if (sizeList.size()>0) {
//			Size cameraSize=sizeList.get(0);
//			//预览图片大小
//			parameters.setPreviewSize(cameraSize.width, cameraSize.height);
//		}
//
//		//设置生成的图片大小
//		sizeList = parameters.getSupportedPictureSizes();
//		if (sizeList.size()>0) {
//			Size cameraSize=sizeList.get(0);
//			for (Size size : sizeList) {
//				//小于100W像素
//				if (size.width*size.height<100*10000) {
//					cameraSize=size;
//					break;
//				}
//			}
//			parameters.setPictureSize(cameraSize.width, cameraSize.height);
//		}
//		//设置图片格式
//		parameters.setPictureFormat(ImageFormat.JPEG);       
//		parameters.setJpegQuality(100);
//		parameters.setJpegThumbnailQuality(100);
//		//自动聚焦模式
		if(mIsFrontCamera){
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		}else{
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//前摄像头会崩溃
		}
		mCamera.setParameters(parameters);
		//设置闪光灯模式。此处主要是用于在相机摧毁后又重建，保持之前的状态
//		setFlashMode(mFlashMode);
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

				if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)){
					rotation=0;
				}else if ((rotation > 45) && (rotation <= 135))  {
					rotation=90;
				}
				else if ((rotation > 135) && (rotation <= 225)) {
					rotation=180;
				} 
				else if((rotation > 225) && (rotation <= 315)) { 
					rotation=270;
				}else {
					rotation=0;
				}
				if(rotation==mOrientation)
					return;
				mOrientation=rotation;
				updateCameraOrientation();
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
			//rotation参数为 0、90、180、270。水平方向为0。
			int rotation=90+mOrientation==360?0:90+mOrientation;
			//前置摄像头需要对垂直方向做变换，否则照片是颠倒的
			if(mIsFrontCamera){
				if(rotation==90) rotation=270;
				else if (rotation==270) rotation=90;
			}
			parameters.setRotation(rotation);//生成的图片转90°
			//预览图片旋转90°
			mCamera.setDisplayOrientation(90);//预览转90°
			mCamera.setParameters(parameters);
		}
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

	public void setAutoFocus(AutoFocusCallback autoFocusCallback) {
		this.autoFocus = autoFocusCallback;
		
	}
	/**
	 * 设置视频保存路径
	 * @param videoSavePath
	 */
	public void setSaveVideoPath(String videoSavePath) {
		this.videoSavePath = videoSavePath;
	}
	
	
}