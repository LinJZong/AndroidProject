package com.example.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linj.album.view.AlbumViewPager;
import com.linj.album.view.AlbumViewPager.MyPageChangeListener;
import com.linj.imageloader.DisplayImageOptions;
import com.linj.imageloader.ImageLoader;
import com.linj.imageloader.displayer.MatrixBitmapDisplayer;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.FloatMath;
import android.util.Log;


import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 图片详情 
 * @author Administrator
 *
 */
public class AlbumDetailAty extends Activity {
	private String mSaveRoot;
	private AlbumViewPager pagerPhoto;//显示大图
	private TextView txtTitle;
	private int oldPosition=0;//当前选择的文件序号
	private boolean isWeb;//是否网络图片
	List<String> urls=new ArrayList<String>();//存放viewpager所有图片链接的数组 
	Bitmap lastEditBitmap;//表示最后一张编辑的图片 在viewpager切换时置为null
	Button save;//保存按钮
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.bigphoto);
		pagerPhoto=(AlbumViewPager)findViewById(R.id.pagerPhoto);
		mSaveRoot="test";
//		pagerPhoto.setOnPageChangeListener2(new PhotoPageChangeListener());//view改变事件

		//设置网络图片加载参数
		pagerPhoto.loadAlbum(mSaveRoot);

	}

	/**
	 * 设置adapter数组
	 * @param urls
	 * @param ROOTFOLDER
	 * @param CHILDFOLDER
	 */
	protected void loadAlbum() {
		//图片文件目录
		final String imageFolder=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_IMAGE, mSaveRoot);
		File folder=new File(imageFolder);
		List<File> files=FileOperateUtil.listFiles(folder, ".jpg");
		
		txtTitle.setText((oldPosition+1)+"/"+urls.size());
		pagerPhoto.setCurrentItem(oldPosition);


	}


//	protected void setBigImages(File picDir, final List<String> urls) {
//		//大图以viewpager形式显示
//		final File[] files=picDir.listFiles(new FileFilter() {
//
//			@Override
//			public boolean accept(File pathname) {
//				// TODO Auto-generated method stub
//				String name=pathname.getName().toLowerCase();
//				//过滤掉其他文件和缩略图
//				if (name.indexOf(".jpg")>0) {
//					urls.add(pathname.getPath());
//					return true;
//				}
//				else {
//					return false;
//				}
//			}
//		});
//		if (files!=null) {
//			pagerPhoto.setAdapter(new ViewPagerAdapter(urls,BigPhotoAty.this));// 设置填充ViewPager页面的适配器
//			Intent intent=getIntent();
//			Bundle bundle=intent.getExtras();
//			if(bundle!=null){
//				String tag=bundle.getString("path");
//				for (int i=0;i<files.length;i++) {
//					if (files[i].getPath().equals(tag)) {
//						oldPosition=i;
//						break;
//					}
//				}
//			}
//			txtTitle.setText((oldPosition+1)+"/"+files.length);
//			pagerPhoto.setCurrentItem(oldPosition);
//		}
//		else {
//			Toast.makeText(getApplicationContext(), "没有可识别的图片", Toast.LENGTH_SHORT).show();
//			finish();
//		}
//	}

	/**
	 * 当ViewPager中页面的状态发生改变时调用
	 * 
	 * @author Administrator
	 * 
	 */
	private class PhotoPageChangeListener implements MyPageChangeListener {
		/**
		 * This method will be invoked when a new page becomes selected.
		 * position: Position index of the new selected page.
		 */


		public void onPageSelected(int position) {
			oldPosition = position;
			txtTitle.setText((oldPosition+1)+"/"+urls.size());
			//将最后一次编辑图片置为空
			lastEditBitmap=null;
			//隐藏保存按钮
			save.setVisibility(View.INVISIBLE);
		}

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageChanged(int oldPosition, int newPosition) {
			//非网络图片 执行图片还原操作
//			if (!isWeb) {
//				View oldView= ((ViewPagerAdapter) pagerPhoto.getAdapter()).getPrimaryItem();;
//				// TODO Auto-generated method stub
//				if (oldView!=null&&oldPosition>=0&&oldPosition<=urls.size()-1) {
//					ImageView img=(ImageView)oldView.findViewById(R.id.image);  
//					Bitmap bitmap= BitmapFactory.decodeFile(urls.get(oldPosition));
//					img.setImageBitmap(bitmap);//图片还原
//				}
//			}
		}
	}



	
	

	private final class TouchListener implements OnTouchListener {
		ImageView imageView;
		public TouchListener(){

		}
		public TouchListener( ImageView imageView){
			this.imageView=imageView; 
		}
		/** 记录是拖拉照片模式还是放大缩小照片模式 */
		private int mode = 0;// 初始状态 
		/** 拖拉照片模式 */
		private static final int MODE_DRAG = 1;
		/** 放大缩小照片模式 */
		private static final int MODE_ZOOM = 2;

		/** 用于记录开始时候的坐标位置 */
		private PointF startPoint = new PointF();
		/** 用于记录拖拉图片移动的坐标位置 */
		private Matrix matrix = new Matrix();
		/** 用于记录图片要进行拖拉时候的坐标位置 */
		private Matrix currentMatrix = new Matrix();

		/** 两个手指的开始距离 */
		private float startDis;
		/** 两个手指的中间点 */
		private PointF midPoint;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			/** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// 手指压下屏幕
			case MotionEvent.ACTION_DOWN:
				mode = MODE_DRAG;
				// 记录ImageView当前的移动位置
				currentMatrix.set(imageView.getImageMatrix());
				startPoint.set(event.getX(), event.getY());
				break;
				// 手指在屏幕上移动，改事件会被不断触发
			case MotionEvent.ACTION_MOVE:
				// 拖拉图片
				if (mode == MODE_DRAG) {
					float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
					float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
					// 在没有移动之前的位置上进行移动
					matrix.set(currentMatrix);
					matrix.postTranslate(dx, dy);
				}
				// 放大缩小图片
				else if (mode == MODE_ZOOM) {
					float endDis = distance(event);// 结束距离
					if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
						float scale = endDis / startDis;// 得到缩放倍数
						matrix.set(currentMatrix);
						matrix.postScale(scale, scale,midPoint.x,midPoint.y);
					}
				}
				break;
				// 手指离开屏幕
			case MotionEvent.ACTION_UP:
				// 当触点离开屏幕，但是屏幕上还有触点(手指)
			case MotionEvent.ACTION_POINTER_UP:
				mode = 0;
				break;
				// 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
			case MotionEvent.ACTION_POINTER_DOWN:
				mode = MODE_ZOOM;
				/** 计算两个手指间的距离 */
				startDis = distance(event);
				/** 计算两个手指间的中间点 */
				if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
					midPoint = mid(event);
					//记录当前ImageView的缩放倍数
					currentMatrix.set(imageView.getImageMatrix());
				}
				break;
			}
			imageView.setImageMatrix(matrix);
			return true;
		}

		/** 计算两个手指间的距离 */
		private float distance(MotionEvent event) {
			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);
			/** 使用勾股定理返回两点之间的距离 */
			return FloatMath.sqrt(dx * dx + dy * dy);
		}

		/** 计算两个手指间的中间点 */
		private PointF mid(MotionEvent event) {
			float midX = (event.getX(1) + event.getX(0)) / 2;
			float midY = (event.getY(1) + event.getY(0)) / 2;
			return new PointF(midX, midY);
		}

	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
	}

	public void btn_click(View view) {
		switch (view.getId()) {
//		case R.id.imgBtnback:
//			finish();
//			break;
//		case R.id.btnTitleRight:
//			if (isWeb) {
//				finish();
//			}
//			else {
//				if (lastEditBitmap!=null) {
//					//保存新图片
//					saveNewPhoto(lastEditBitmap,urls.get(pagerPhoto.getCurrentItem()));
//				}
//			}
//			break;
//		case R.id.btnRotateRight:
//			rotateBitmap(view.getId());
//			break;
//		case R.id.btnRotateLeft:
//			rotateBitmap(view.getId());
//			break;
//		case R.id.btnRotateLeftRight:
//			rotateBitmap(view.getId());
//			break;
//		case R.id.btnRotateUpDown:
//			rotateBitmap(view.getId());
//			break;
		default:
			break;
		}
	}
//	/**
//	 * 保存图片
//	 * @param lastEditBitmap2  修改后的图片
//	 * @param string 路径
//	 */
//	private void saveNewPhoto(Bitmap lastEditBitmap2, String path) {
//		// TODO Auto-generated method stub
//		File myCaptureFile=new File(path);
//		//生成缩略图
//		Bitmap thumbnail=ThumbnailUtils.extractThumbnail(lastEditBitmap2, 213, 213);
//		//获取缩略图路径
//		String tPath=myCaptureFile.getParentFile().getPath()+"/"+getString(R.string.ImageThumbnail)+myCaptureFile.getName();//小图为大图目录加缩略图文件夹
//		File thumFile=new File(tPath);
//		try{
//			//存图片大图
//			BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(myCaptureFile));
//			lastEditBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//			bos.flush();
//			bos.close();
//			//存图片小图
//			bos=new BufferedOutputStream(new FileOutputStream(thumFile));
//			thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//			bos.flush();
//			bos.close();
//			Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
//		}catch(Exception e){
//			Log.i("pic", e.toString());
//		}
//	}

//	class onDoubleClick implements View.OnTouchListener{ 
//		ImageView imageView;
//		public onDoubleClick(ImageView imageView) {
//			this.imageView=imageView;
//		}
//		int count = 0;   
//		int firClick = 0;   
//		int secClick = 0;   
//		@Override    
//		public boolean onTouch(View v, MotionEvent event) {     
//			if(MotionEvent.ACTION_DOWN == event.getAction()){  
//				Log.i("", count+"");
//				count++;     
//				if(count == 1){     
//					firClick = (int) System.currentTimeMillis();           
//				} else if (count == 2){     
//					secClick = (int) System.currentTimeMillis();     
//					if(secClick - firClick < 1000){     
//						Toast.makeText(getApplicationContext(), "双击", 1).show();
//					}     
//					count = 0;     
//					firClick = 0;     
//					secClick = 0;  
//					imageView.setOnTouchListener(new TouchListener(imageView));
//					imageView.setScaleType(ScaleType.MATRIX);
//					Matrix matrix=new Matrix();
//
//					matrix.postScale(2, 2);
//					imageView.setImageMatrix(matrix);
//					pagerPhoto.setLock(true);
//				} 
//
//			}     
//			return false;
//		}     
//	}   
//
//
//	private void rotateBitmap(int id) {
//
//		View imageLayout= ((ViewPagerAdapter) pagerPhoto.getAdapter()).getPrimaryItem();;
//		ImageView img=(ImageView)imageLayout.findViewById(R.id.image);  
//		//当前编辑图片为空时 加载图片 否则继续编辑当前图片
//		if (lastEditBitmap==null) {
//			lastEditBitmap= BitmapFactory.decodeFile(urls.get(pagerPhoto.getCurrentItem()));
//		}
//		//		 Getting width & height of the given image.  
//		int w = lastEditBitmap.getWidth();  
//		int h = lastEditBitmap.getHeight();  
//		// Setting post rotate to 90  
//		Matrix mtx = new Matrix();  
//		switch (id) {
//		case R.id.btnRotateRight:
//			mtx.postRotate(90);  
//			break;
//		case R.id.btnRotateLeft:
//			mtx.postRotate(-90);  
//			break;
//		case R.id.btnRotateLeftRight:
//			mtx.postScale(-1,1);  
//			break;
//		case R.id.btnRotateUpDown:
//			mtx.postScale(1, -1);//上下翻转
//			break;
//		}
//		// Rotating Bitmap  
//		Bitmap rotatedBMP = Bitmap.createBitmap(lastEditBitmap, 0, 0, w, h, mtx, true);  
//		//将编辑后的图片设置为当前编辑图片
//		lastEditBitmap=rotatedBMP;
//		//保存当前view
//
//		//显示保存按钮
//		save.setVisibility(View.VISIBLE);
//		img.setImageBitmap(rotatedBMP);   
//	}

}
