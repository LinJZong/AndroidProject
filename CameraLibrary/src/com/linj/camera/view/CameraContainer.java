package com.linj.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.linj.FileOperateUtil;
import com.linj.camera.view.CameraView.FlashMode;
import com.linj.cameralibrary.R;


/** 
 * @ClassName: CameraContainer 
 * @Description:  相机界面的容器 包含相机绑定的surfaceview、拍照后的临时图片View和聚焦View 
 * @author LinJ
 * @date 2014-12-31 上午9:38:52 
 *  
 */
public class CameraContainer extends RelativeLayout implements CameraOperation,View.OnDragListener{

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
	/** 设置保存图片的旋转角度 */
	private int bitmepRotateAngle;

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
		setOnDragListener(this);
		displayMetrics = context.getResources().getDisplayMetrics();
	}
	
	@Override
    public boolean onDrag(View v, DragEvent event) {
        int action = event.getAction();
//        Log.e("jingo", "x =" + event.getX() +" y = "+event.getY() + "event" + event.getAction());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mWaterMarkImageView.getLayoutParams();
        switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			Log.d(TAG, "Action is DragEvent.ACTION_DRAG_STARTED");
			// Do nothing
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			Log.d(TAG, "Action is DragEvent.ACTION_DRAG_ENTERED");
			int x_cord = (int) event.getX();
			int y_cord = (int) event.getY();
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			Log.d(TAG, "Action is DragEvent.ACTION_DRAG_EXITED");
			break;
		case DragEvent.ACTION_DRAG_LOCATION:

			Log.d(TAG, "Action is DragEvent.ACTION_DRAG_LOCATION");
			x_cord = (int) event.getX();
			y_cord = (int) event.getY();
			break;
		case DragEvent.ACTION_DRAG_ENDED:// 结束时
			int x_cord2 = (int) event.getX();
			int y_cord2 = (int) event.getY();
			Log.d(TAG, "Action is DragEvent.ACTION_DRAG_ENDED");
			// Do nothing
			break;
		case DragEvent.ACTION_DROP:
			Log.d(TAG, "ACTION_DROP event");
			int height = mWaterMarkImageView.getHeight();
			int width = mWaterMarkImageView.getWidth();
			float x2 = mWaterMarkImageView.getX();
			System.out.println("x2-----"+x2);
			
			x_cord = (int) event.getX()-width/2;
			y_cord = (int) event.getY()-height/2;
			layoutParams.leftMargin = x_cord;
			layoutParams.topMargin = y_cord;
			mWaterMarkImageView.setLayoutParams(layoutParams);
			x2 = mWaterMarkImageView.getX();
			System.out.println("x2-----"+x2);
			// v.setLayoutParams(layoutParams);
			break;
		default:
			break;
		}
		return true;
    }

	/**  
	 *  初始化子控件
	 *  @param context   
	 */
	private void initView(Context context) {
		inflate(context, R.layout.cameracontainer, this);
		mImageFolder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, mSavePath);
		mThumbnailFolder=FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, mSavePath);
		imgName = FileOperateUtil.createFileNmae(".jpg");
		imagePath = mImageFolder+File.separator+imgName;
		thumbPath = mThumbnailFolder+File.separator+imgName;
		
		mCameraView=(CameraView) findViewById(R.id.cameraView);
		
		mCameraView.setAutoFocus(autoFocusCallback);

		mTempImageView=(TempImageView) findViewById(R.id.tempImageView);

		mFocusImageView=(FocusImageView) findViewById(R.id.focusImageView);//对焦位置显示的图片

		mRecordingInfoTextView=(TextView) findViewById(R.id.recordInfo);

		mWaterMarkImageView=(ImageView) findViewById(R.id.waterMark);

		mZoomSeekBar=(SeekBar) findViewById(R.id.zoomSeekBar);
		//获取当前照相机支持的最大缩放级别，值小于0表示不支持缩放。当支持缩放时，加入拖动条。
		int maxZoom=mCameraView.getMaxZoom();
		if(maxZoom>0){
			mZoomSeekBar.setMax(maxZoom);
			mZoomSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		}
		mWaterMarkImageView.setTag("IMAGE_TAG");
		mWaterMarkImageView.setOnLongClickListener(new View.OnLongClickListener() {
	         @Override
	         public boolean onLongClick(View v) {
	            ClipData.Item item = new ClipData.Item((CharSequence)v.getTag());

	            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
//	            ClipData d = new ClipData(description, new Item(""));
	            ClipData dragData = new ClipData(v.getTag().toString(), 
	            mimeTypes, item);

	            // Instantiates the drag shadow builder.
	            View.DragShadowBuilder myShadow = new DragShadowBuilder(mWaterMarkImageView);

	            // Starts the drag
	            v.startDrag(dragData,  // the data to be dragged
	            myShadow,  // the drag shadow builder
	            null,      // no need to use local data
	            0          // flags (not currently used, set to 0)
	            );
	            return true;
	         }
	      });
	}
	
	/**
	 * 设置录制视频的尺寸
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
		mCameraView.setProfile(quality);
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

	public Bitmap stopRecord(TakePictureListener listener){
		mListener=listener;
		return stopRecord();
	}
	
	@Override
	public Bitmap stopRecord(){
		mRecordingInfoTextView.setVisibility(View.GONE);
		Bitmap thumbnailBitmap=mCameraView.stopRecord();
		if(thumbnailBitmap!=null){
			mTempImageView.setListener(mListener);
			mTempImageView.isVideo(true);
			mTempImageView.setImageBitmap(thumbnailBitmap);
			mTempImageView.startAnimation(R.anim.tempview_show);
		}
		return thumbnailBitmap;
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
	 * 设置图片保存位置
	 * @param imagePath 图位置
	 * @param thumbPath 缩略图位置
	 */
	public void setSavePath(String imagePath,String thumbPath){
		imgName = FileOperateUtil.createFileNmae(".jpg");
		if(!TextUtils.isEmpty(imagePath)){
			mImageFolder = FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, thumbPath);
			this.imagePath = mImageFolder+File.separator + imgName;
		}
		if(!TextUtils.isEmpty(thumbPath)){
			mThumbnailFolder = FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, thumbPath);
			this.thumbPath = mThumbnailFolder + File.separator + imgName;
		}
	}
	/**
	 * 视频保存路径
	 */
	public void setSaveVideoPath(String path){
		mCameraView.setSaveVideoPath(path);
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
	/**
	 * 设置相机的自动对焦，监听
	 */
	private final AutoFocusCallback autoFocusCallback=new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			//聚焦之后根据结果修改图片
			if (success) {
				mFocusImageView.onFocusSuccess();
				camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
//				camera.startPreview();
			}else {
				//聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
				mFocusImageView.onFocusFailed();

			}
		}
	};
	/**
	 * 压缩后的图片最大值 单位KB
	 * @param maxSize
	 */
	public void setMaxSize(int maxSize){
		this.maxSize = maxSize;
	}
	/**
	 * 调用保存时的监听
	 */
	private final PictureCallback pictureCallback=new PictureCallback() {

		@Override
		public void onPictureTaken(final byte[] data, Camera camera) {
			if(mSavePath==null) throw new RuntimeException("mSavePath is null");
			if(mDataHandler==null) mDataHandler=new DataHandler();
			new AsyncTask<Bitmap, Void, Bitmap>() {
				@Override
				protected void onPostExecute(Bitmap bm) {
					mTempImageView.setListener(mListener);
					mTempImageView.isVideo(false);
					mTempImageView.setImageBitmap(bm);
					mTempImageView.startAnimation(R.anim.tempview_show);
					//重新打开预览图，进行下一次的拍照准备
					
					if(mListener!=null) mListener.onTakePictureEnd(bm);
					super.onPostExecute(bm);
				}
				@Override
				protected Bitmap doInBackground(Bitmap... params) {
					Bitmap bm=mDataHandler.handerSave(data);//添加水印/并保存
					return bm;
				}
			}.execute();
//			at.execute(Void);
			camera.startPreview();
//			mTempImageView.setListener(mListener);
//			mTempImageView.isVideo(false);
//			mTempImageView.setImageBitmap(bm);
//			mTempImageView.startAnimation(R.anim.tempview_show);
//			//重新打开预览图，进行下一次的拍照准备
//			camera.startPreview();
//			if(mListener!=null) mListener.onTakePictureEnd(bm);
		}
	};

	private DisplayMetrics displayMetrics;

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
	/** 压缩后的图片最大值 单位KB*/
	private int maxSize=200;
	/**
	 * 拍照返回的byte数据处理类
	 * @author linj
	 *
	 */
	/** 大图存放路径  */
	private String mThumbnailFolder;
	/** 小图存放路径 */
	private String mImageFolder;

	private String imgName;

	private String imagePath;

	private String thumbPath;
	private final class DataHandler{
		
		public DataHandler(){
			
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
		public Bitmap handerSave(byte[] data){
			if(data!=null){
				//解析生成相机返回的图片
				Bitmap bm=BitmapFactory.decodeByteArray(data, 0, data.length);
//				Matrix m = new Matrix();
//				m.setRotate(bitmepRotateAngle, bm.getWidth(), bm.getHeight());
//				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
				//获取加水印的图片
				bm=getBitmapWithWaterMark(bm);
				//生成缩略图
				Bitmap thumbnail=ThumbnailUtils.extractThumbnail(bm, 213, 213);
				//产生新的文件名
				

				File file=new File(imagePath);  
				File thumFile=new File(thumbPath);
				try{
					//存图片大图
					FileOutputStream fos=new FileOutputStream(file);
					ByteArrayOutputStream bos=compress(bm);//压缩要保存的图片
					fos.write(bos.toByteArray());
					fos.flush();
					fos.close();
					Log.w(TAG, "bitmap--> path:"+imagePath+" fileSize: "+maxSize);
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
		/**
		 * 设置上水印
		 * @param bm
		 * @return
		 */
		private Bitmap getBitmapWithWaterMark(Bitmap bm) {
			// TODO Auto-generated method stub
			if(!(mWaterMarkImageView.getVisibility()==View.VISIBLE)){
				return bm;
			}
			//计算图片和屏幕的比例
			int zoom = displayMetrics.widthPixels/bm.getWidth();
			
			
			Drawable mark=mWaterMarkImageView.getDrawable();
			Bitmap wBitmap=drawableToBitmap(mark);
			int w = bm.getWidth();

			int h = bm.getHeight();

			int ww = wBitmap.getWidth();

			int wh = wBitmap.getHeight();
			Bitmap newb = Bitmap.createBitmap( w, h, Config.ARGB_8888 );
			Matrix matrix = new Matrix();
			
			Bitmap b = Bitmap.createBitmap( w/zoom, h/zoom, Config.ARGB_8888 );
			wBitmap = Bitmap.createBitmap(wBitmap, 0, 0, mWaterMarkImageView.getWidth(), mWaterMarkImageView.getHeight());
			Canvas canvas=new Canvas(newb);
			//draw src into

			canvas.drawBitmap( bm, 0, 0, null );//在 0，0坐标开始画入src
			wBitmap = ThumbnailUtils.extractThumbnail(wBitmap, w/zoom, h/zoom,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
			
//			canvas.drawBitmap( wBitmap, w - ww + 5, h - wh + 5, null );//在src的右下角画入水印
			float x2 = mWaterMarkImageView.getX();
			float y2 = mWaterMarkImageView.getY();
			System.out.println(x2);
			System.out.println(y2);
			System.out.println(b.getWidth());
			canvas.drawBitmap( wBitmap, 0, 0, null );//在src的右下角画入水印
//			canvas.drawBitmap( wBitmap, 0, 0, null );//在src的右下角画入水印
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
		 * @param isVideo true：当前为录像缩略图 false:为拍照缩略图
		 * */
		public void onAnimtionEnd(Bitmap bm,boolean isVideo);
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