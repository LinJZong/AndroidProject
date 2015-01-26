package com.linj.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.camera.view.CameraView.FlashMode;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


/** 
 * @ClassName: CameraContainer 
 * @Description:  相机界面的容器 包含相机绑定的surfaceview、拍照后的临时图片View和聚焦View 
 * @author LinJ
 * @date 2014-12-31 上午9:38:52 
 *  
 */
public class CameraContainer extends RelativeLayout implements CameraOperation{

	public final static String TAG="CameraContainer";

	/** 相机绑定的SurfaceView  */ 
	private CameraView mCameraView;

	/** 拍照生成的图片，产生一个下移到左下角的动画效果后隐藏 */ 
	private TempImageView mTempImageView;

	/** 触摸屏幕时显示的聚焦图案  */ 
	private FocusImageView mFocusImageView;

	/** 显示录像用时的TextView  */ 
	private TextView mRecordingInfoTextView;

	/** 显示水印图案  */ 
	private ImageView mWaterMarkImageView; 

	/** 存放照片的根目录 */ 
	private String mSavePath;

	/** 照片字节流处理类  */ 
	private DataHandler mDataHandler;

	/** 拍照监听接口，用以在拍照开始和结束后执行相应操作  */ 
	private TakePictureListener mListener;

	/** 缩放级别拖动条 */ 
	private SeekBar mZoomSeekBar;

	/** 用以执行定时任务的Handler对象*/
	private Handler mHandler;
	private long mRecordStartTime;
	private SimpleDateFormat mTimeFormat;
	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		mHandler=new Handler();
		mTimeFormat=new SimpleDateFormat("mm:ss",Locale.getDefault());
		setOnTouchListener(new TouchListener());
	}

	/**  
	 *  初始化子控件
	 *  @param context   
	 */
	private void initView(Context context) {
		inflate(context, R.layout.cameracontainer, this);
		mCameraView=(CameraView) findViewById(R.id.cameraView);

		mTempImageView=(TempImageView) findViewById(R.id.tempImageView);

		mFocusImageView=(FocusImageView) findViewById(R.id.focusImageView);

		mRecordingInfoTextView=(TextView) findViewById(R.id.recordInfo);

		mWaterMarkImageView=(ImageView) findViewById(R.id.waterMark);

		mZoomSeekBar=(SeekBar) findViewById(R.id.zoomSeekBar);
		//获取当前照相机支持的最大缩放级别，值小于0表示不支持缩放。当支持缩放时，加入拖动条。
		int maxZoom=mCameraView.getMaxZoom();
		if(maxZoom>0){
			mZoomSeekBar.setMax(maxZoom);
			mZoomSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		}
	}


	@Override
	public boolean startRecord(){
		mRecordStartTime=SystemClock.uptimeMillis();
		mRecordingInfoTextView.setVisibility(View.VISIBLE);
		mRecordingInfoTextView.setText("00:00");
		if(mCameraView.startRecord()){
			mHandler.postAtTime(recordRunnable, mRecordingInfoTextView, SystemClock.uptimeMillis()+1000);
			return true;
		}else {
			return false;
		}
	}

	Runnable recordRunnable=new Runnable() {	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mCameraView.isRecording()){
				long recordTime=SystemClock.uptimeMillis()-mRecordStartTime;
				mRecordingInfoTextView.setText(mTimeFormat.format(new Date(recordTime)));
				mHandler.postAtTime(this,mRecordingInfoTextView, SystemClock.uptimeMillis()+500);
			}else {
				mRecordingInfoTextView.setVisibility(View.GONE);
			}
		}
	};

	@Override
	public Bitmap stopRecord(){
		mRecordingInfoTextView.setVisibility(View.GONE);
		return mCameraView.stopRecord();
	}
	
	/**  
	 *  改变相机模式 在拍照模式和录像模式间切换 两个模式的初始缩放级别不同
	 *  @param zoom   缩放级别
	 */
	public void switchMode(int zoom){
		mZoomSeekBar.setProgress(zoom);
		mCameraView.setZoom(zoom);
		//自动对焦
		mCameraView.onFocus(new Point(getWidth()/2, getHeight()/2), autoFocusCallback);   
		//隐藏水印
		mWaterMarkImageView.setVisibility(View.GONE);
	}



	public void setWaterMark(){
		if (mWaterMarkImageView.getVisibility()==View.VISIBLE) {
			mWaterMarkImageView.setVisibility(View.GONE);
		}else {
			mWaterMarkImageView.setVisibility(View.VISIBLE);
		}
	}

	/**  
	 *   前置、后置摄像头转换
	 */
	@Override
	public void switchCamera(){
		mCameraView.switchCamera();
	}
	/**  
	 *  获取当前闪光灯类型
	 *  @return   
	 */
	@Override
	public FlashMode getFlashMode() {
		return mCameraView.getFlashMode();
	}

	/**  
	 *  设置闪光灯类型
	 *  @param flashMode   
	 */
	@Override
	public void setFlashMode(FlashMode flashMode) {
		mCameraView.setFlashMode(flashMode);
	}

	/**
	 * 设置文件保存路径
	 * @param rootPath
	 */
	public void setRootPath(String rootPath){
		this.mSavePath=rootPath;

	}



	/**
	 * 拍照方法
	 * @param callback
	 */
	public void takePicture(){
		takePicture(pictureCallback,mListener);
	}

	/**  
	 * @Description: 拍照方法
	 * @param @param listener 拍照监听接口
	 * @return void    
	 * @throws 
	 */
	public void takePicture(TakePictureListener listener){
		this.mListener=listener;
		takePicture(pictureCallback, mListener);
	}


	@Override
	public void takePicture(PictureCallback callback,
			TakePictureListener listener) {
		mCameraView.takePicture(callback,listener);
	}

	@Override
	public int getMaxZoom() {
		// TODO Auto-generated method stub
		return mCameraView.getMaxZoom();
	}

	@Override
	public void setZoom(int zoom) {
		// TODO Auto-generated method stub
		mCameraView.setZoom(zoom);
	}

	@Override
	public int getZoom() {
		// TODO Auto-generated method stub
		return mCameraView.getZoom();
	} 

	private final OnSeekBarChangeListener onSeekBarChangeListener=new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			mCameraView.setZoom(progress);
			mHandler.removeCallbacksAndMessages(mZoomSeekBar);
			//ZOOM模式下 在结束两秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务
			mHandler.postAtTime(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mZoomSeekBar.setVisibility(View.GONE);
				}
			}, mZoomSeekBar,SystemClock.uptimeMillis()+2000);
		}



		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}



		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}
	};

	private final AutoFocusCallback autoFocusCallback=new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			//聚焦之后根据结果修改图片
			if (success) {
				mFocusImageView.onFocusSuccess();
			}else {
				//聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
				mFocusImageView.onFocusFailed();

			}
		}
	};

	private final PictureCallback pictureCallback=new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if(mSavePath==null) throw new RuntimeException("mSavePath is null");
			if(mDataHandler==null) mDataHandler=new DataHandler();	
			mDataHandler.setMaxSize(200);
			Bitmap bm=mDataHandler.save(data);

			//重新打开预览图，进行下一次的拍照准备
			mTempImageView.setListener(mListener);
			mTempImageView.setImageBitmap(bm);
			mTempImageView.startAnimation(R.anim.tempview_show);
			camera.startPreview();
			if(mListener!=null) mListener.onTakePictureEnd(bm);
		}
	};

	private final class TouchListener implements OnTouchListener {

		/** 记录是拖拉照片模式还是放大缩小照片模式 */

		private static final int MODE_INIT = 0;
		/** 放大缩小照片模式 */
		private static final int MODE_ZOOM = 1;
		private int mode = MODE_INIT;// 初始状态 

		/** 用于记录拖拉图片移动的坐标位置 */

		private float startDis;


		@Override
		public boolean onTouch(View v, MotionEvent event) {
			/** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// 手指压下屏幕
			case MotionEvent.ACTION_DOWN:
				mode = MODE_INIT;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				//如果mZoomSeekBar为null 表示该设备不支持缩放 直接跳过设置mode Move指令也无法执行
				if(mZoomSeekBar==null) return true;
				//移除token对象为mZoomSeekBar的延时任务
				mHandler.removeCallbacksAndMessages(mZoomSeekBar);
				mZoomSeekBar.setVisibility(View.VISIBLE);

				mode = MODE_ZOOM;
				/** 计算两个手指间的距离 */
				startDis = distance(event);
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == MODE_ZOOM) {
					//只有同时触屏两个点的时候才执行
					if(event.getPointerCount()<2) return true;
					float endDis = distance(event);// 结束距离
					//每变化10f zoom变1
					int scale=(int) ((endDis-startDis)/10f);
					if(scale>=1||scale<=-1){
						int zoom=mCameraView.getZoom()+scale;
						//zoom不能超出范围
						if(zoom>mCameraView.getMaxZoom()) zoom=mCameraView.getMaxZoom();
						if(zoom<0) zoom=0;
						mCameraView.setZoom(zoom);
						mZoomSeekBar.setProgress(zoom);
						//将最后一次的距离设为当前距离
						startDis=endDis;
					}
				}
				break;
				// 手指离开屏幕
			case MotionEvent.ACTION_UP:
				if(mode!=MODE_ZOOM){
					//设置聚焦
					Point point=new Point((int)event.getX(), (int)event.getY());
					mCameraView.onFocus(point,autoFocusCallback);
					mFocusImageView.startFocus(point);
				}else {
					//ZOOM模式下 在结束两秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务
					mHandler.postAtTime(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mZoomSeekBar.setVisibility(View.GONE);
						}
					}, mZoomSeekBar,SystemClock.uptimeMillis()+2000);
				}
				break;
			}
			return true;
		}
		/** 计算两个手指间的距离 */
		private float distance(MotionEvent event) {
			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);
			/** 使用勾股定理返回两点之间的距离 */
			return (float) Math.sqrt(dx * dx + dy * dy);
		}

	}

	/**
	 * 拍照返回的byte数据处理类
	 * @author linj
	 *
	 */
	private final class DataHandler{
		/** 大图存放路径  */
		private String mThumbnailFolder;
		/** 小图存放路径 */
		private String mImageFolder;
		/** 压缩后的图片最大值 单位KB*/
		private int maxSize=200;

		public DataHandler(){
			mImageFolder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, mSavePath);
			mThumbnailFolder=FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, mSavePath);
			File folder=new File(mImageFolder);
			if(!folder.exists()){
				folder.mkdirs();
			}
			folder=new File(mThumbnailFolder);
			if(!folder.exists()){
				folder.mkdirs();
			}
		}

		/**
		 * 保存图片
		 * @param 相机返回的文件流
		 * @return 解析流生成的缩略图
		 */
		public Bitmap save(byte[] data){
			if(data!=null){
				//解析生成相机返回的图片
				Bitmap bm=BitmapFactory.decodeByteArray(data, 0, data.length);
				//获取加水印的图片
				bm=getBitmapWithWaterMark(bm);
				//生成缩略图
				Bitmap thumbnail=ThumbnailUtils.extractThumbnail(bm, 213, 213);
				//产生新的文件名
				String imgName=FileOperateUtil.createFileNmae(".jpg");
				String imagePath=mImageFolder+File.separator+imgName;
				String thumbPath=mThumbnailFolder+File.separator+imgName;

				File file=new File(imagePath);  
				File thumFile=new File(thumbPath);
				try{
					//存图片大图
					FileOutputStream fos=new FileOutputStream(file);
					ByteArrayOutputStream bos=compress(bm);
					fos.write(bos.toByteArray());
					fos.flush();
					fos.close();
					//存图片小图
					BufferedOutputStream bufferos=new BufferedOutputStream(new FileOutputStream(thumFile));
					thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bufferos);
					bufferos.flush();
					bufferos.close();
					return bm; 
				}catch(Exception e){
					Log.e(TAG, e.toString());
					Toast.makeText(getContext(), "解析相机返回流失败", Toast.LENGTH_SHORT).show();

				}
			}else{
				Toast.makeText(getContext(), "拍照失败，请重试", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		private Bitmap getBitmapWithWaterMark(Bitmap bm) {
			// TODO Auto-generated method stub
			if(!(mWaterMarkImageView.getVisibility()==View.VISIBLE)){
				return bm;
			}
			Drawable mark=mWaterMarkImageView.getDrawable();
			Bitmap wBitmap=drawableToBitmap(mark);
			int w = bm.getWidth();

			int h = bm.getHeight();

			int ww = wBitmap.getWidth();

			int wh = wBitmap.getHeight();
			Bitmap newb = Bitmap.createBitmap( w, h, Config.ARGB_8888 );
			Canvas canvas=new Canvas(newb);
			//draw src into

			canvas.drawBitmap( bm, 0, 0, null );//在 0，0坐标开始画入src
			canvas.drawBitmap( wBitmap, w - ww + 5, h - wh + 5, null );//在src的右下角画入水印
			//save all clip

			canvas.save( Canvas.ALL_SAVE_FLAG );//保存

			//store

			canvas.restore();//存储
			bm.recycle();
			bm=null;
			wBitmap.recycle();
			wBitmap=null;
			return newb;

		}
		public  Bitmap drawableToBitmap(Drawable drawable) {       
			Bitmap bitmap = Bitmap.createBitmap(
					drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight(),
					drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
							: Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			return bitmap;
		}
		/**
		 * 图片压缩方法
		 * @param bitmap 图片文件
		 * @param max 文件大小最大值
		 * @return 压缩后的字节流
		 * @throws Exception
		 */
		public ByteArrayOutputStream compress(Bitmap bitmap){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
			int options = 99;
			while ( baos.toByteArray().length / 1024 > maxSize) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
				options -= 3;// 每次都减少10
				//压缩比小于0，不再压缩
				if (options<0) {
					break;
				}
				Log.i(TAG,baos.toByteArray().length / 1024+"");
				baos.reset();// 重置baos即清空baos
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			}
			return baos;
		}

		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}
	}

	/** 
	 * @ClassName: TakePictureListener 
	 * @Description:  拍照监听接口，用以在拍照开始和结束后执行相应操作
	 * @author LinJ
	 * @date 2014-12-31 上午9:50:33 
	 *  
	 */
	public static interface TakePictureListener{		
		/**  
		 *拍照结束执行的动作，该方法会在onPictureTaken函数执行后触发
		 *  @param bm 拍照生成的图片 
		 */
		public void onTakePictureEnd(Bitmap bm);

		/**  临时图片动画结束后触发
		 * @param bm 拍照生成的图片 
		 * */
		public void onAnimtionEnd(Bitmap bm);
	}


	/**  
	 * dip转px
	 *  @param dipValue
	 *  @return   
	 */
	private  int dip2px(float dipValue){ 
		final float scale = getResources().getDisplayMetrics().density; 
		return (int)(dipValue * scale + 0.5f); 
	}
}