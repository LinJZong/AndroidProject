package com.linj.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;


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
import android.widget.FrameLayout;
import android.widget.Toast;


public class CameraContainer extends FrameLayout implements PictureCallback{
	private final static String TAG="CameraContainer";
	private CameraView cameraView;
	private TempImageView tempImageView;
	private String rootPath;
	private String THUMBNAIL_FOLDER;
	private String IMAGE_FOLDER;
	private DataHandler handler;
	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		handler=new DataHandler();
		cameraView=new CameraView(context);
		FrameLayout.LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		cameraView.setLayoutParams(layoutParams);
		addView(cameraView);

		tempImageView=new TempImageView(context);
		layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		tempImageView.setLayoutParams(layoutParams);

		addView(tempImageView);
	}


	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		if(rootPath==null)
			throw new RuntimeException("rootPath未设置");
		Bitmap img=handler.save(data);

		//重新打开预览图，进行下一次的拍照准备
		tempImageView.setImageBitmap(img);
		tempImageView.startAnimation(R.anim.tempview_show);
		camera.startPreview();
	}

	/**
	 * 设置文件保存路径
	 * @param rootPath
	 */
	public void setRootPath(String rootPath){
		this.rootPath=rootPath;
		IMAGE_FOLDER=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, rootPath);
		THUMBNAIL_FOLDER=FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, rootPath);
		File folder=new File(IMAGE_FOLDER);
		if(!folder.exists()){
			folder.mkdirs();
		}
		folder=new File(THUMBNAIL_FOLDER);
		if(!folder.exists()){
			folder.mkdirs();
		}
	}

	/**
	 * 拍照函数
	 * @param callback
	 */
	public void takePicture(){
		cameraView.takePicture(this);
	}

	/**
	 * 拍照返回的byte数据处理类
	 * @author linj
	 *
	 */
	private final class DataHandler{
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
				String imagePath=THUMBNAIL_FOLDER+File.separator+imgName;
				String thumbPath=IMAGE_FOLDER+File.separator+imgName;

				File file=new File(imagePath);  
				File thumFile=new File(thumbPath);
				try{
					//存图片大图
					FileOutputStream fos=new FileOutputStream(file);
					ByteArrayOutputStream bos=compress(bm, 200);
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
		public ByteArrayOutputStream compress(Bitmap bitmap,int max){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
			int options = 100;
			while ( baos.toByteArray().length / 1024 > max) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
				options -= 10;// 每次都减少10
				//压缩比小于0，不再压缩
				if (options<0) {
					break;
				}
				baos.reset();// 重置baos即清空baos
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			}
			return baos;
		}
	}

}