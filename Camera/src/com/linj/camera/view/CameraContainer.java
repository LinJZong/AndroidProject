package com.linj.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.camera.view.CameraView.FlashMode;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;


/** 
 * @ClassName: CameraContainer 
 * @Description:  相机界面的容器 包含相机绑定的surfaceview、拍照后的临时图片View和聚焦View 
 * @author LinJ
 * @date 2014-12-31 上午9:38:52 
 *  
 */
public class CameraContainer extends FrameLayout implements PictureCallback{

	private final static String TAG="CameraContainer";

	/** 相机绑定的SurfaceView  */ 
	private CameraView mCameraView;
	
	/** 拍照生成的图片，产生一个下移到左下角的动画效果后隐藏 */ 
	private TempImageView mTempImageView;
	
	/** 触摸屏幕时显示的聚焦图案  */ 
	private FocusImageView mFocusImageView;
	
	/** 存放照片的根目录 */ 
	private String mSavePath;
	
	/** 照片字节流处理类  */ 
	private DataHandler mhandler;
	
	/** 拍照监听接口，用以在拍照开始和结束后执行相应操作  */ 
	private TakePictureListener mListener;

	private SeekBar mSeekBar;
	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);

		mCameraView=new CameraView(context);
		FrameLayout.LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mCameraView.setLayoutParams(layoutParams);
		addView(mCameraView);

		mTempImageView=new TempImageView(context);
		layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mTempImageView.setLayoutParams(layoutParams);
		addView(mTempImageView);

		mFocusImageView=new FocusImageView(context);
		layoutParams=new LayoutParams(150,150);
		mFocusImageView.setLayoutParams(layoutParams);
		mFocusImageView.setFocusImg(R.drawable.focus);
		mFocusImageView.setFocusSucceedImg(R.drawable.focus_succeed);
		addView(mFocusImageView);
		
		mSeekBar=new SeekBar(context);
		mSeekBar.setMax(10);
	}



	/**  
	 *  获取当前闪光灯类型
	 *  @return   
	 */
	public FlashMode getFlashMode() {
		return mCameraView.getFlashMode();
	}

	/**  
	 *  设置闪光灯类型
	 *  @param flashMode   
	 */
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
		mCameraView.takePicture(this,mListener);
	}

	/**  
	 * @Description: 拍照方法
	 * @param @param listener 拍照监听接口
	 * @return void    
	 * @throws 
	 */
	public void takePicture(TakePictureListener listener){
		this.mListener=listener;
		mCameraView.takePicture(this,mListener);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		if(mSavePath==null) throw new RuntimeException("mSavePath is null");
		if(mhandler==null) mhandler=new DataHandler();	
		mhandler.setMaxSize(200);
		Bitmap img=mhandler.save(data);

		//重新打开预览图，进行下一次的拍照准备
		mTempImageView.setListener(mListener);
		mTempImageView.setImageBitmap(img);
		mTempImageView.startAnimation(R.anim.tempview_show);
		camera.startPreview();
		if(mListener!=null) mListener.onTakePictureEnd();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//监听UP事件，在此显示聚焦图片
		if (event.getAction()==KeyEvent.ACTION_UP) {
			//设置聚焦
			mCameraView.onFocus((int)event.getX(), (int)event.getY());

			mFocusImageView.show(event.getX(),event.getY());
		}
		return true;
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
		 * @return 解析流生成的大图
		 */
		public Bitmap save(byte[] data){
			if(data!=null){
				//解析生成相机返回的图片
				Bitmap bm=BitmapFactory.decodeByteArray(data, 0, data.length);
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

		/**  拍照结束执行的动作，该方法会在onPictureTaken函数执行后触发 */
		public void onTakePictureEnd();

		/**  临时图片动画结束后触发*/
		public void onAnimtionEnd();
	}



}