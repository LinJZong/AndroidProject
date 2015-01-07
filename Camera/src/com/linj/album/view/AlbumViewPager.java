package com.linj.album.view;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.imageloader.DisplayImageOptions;
import com.linj.imageloader.ImageLoader;
import com.linj.imageloader.displayer.FadeInBitmapDisplayer;
import com.linj.imageloader.displayer.RoundedBitmapDisplayer;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/***
 * 自定义viewpager  在page改变时传递旧的position和新的position
 * @author Administrator
 *
 */
public class AlbumViewPager extends ViewPager  {
	int cPosition=0;//表示当前的position
	boolean lock=false;//是否锁定viewpager 当lock=true时 无法滑动viewpager

	/**  图片加载器 优化了了缓存  */ 
	private ImageLoader mImageLoader;
	/**  加载图片配置参数 */ 
	private DisplayImageOptions mOptions;	


	public AlbumViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImageLoader= ImageLoader.getInstance(context);
		//设置网络图片加载参数
		DisplayImageOptions.Builder builder= new DisplayImageOptions.Builder();
		builder =builder
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageOnFail(R.drawable.ic_error)
				.cacheInMemory(true)
				.cacheOnDisk(false)
				.displayer(new FadeInBitmapDisplayer());
		mOptions=builder.build();
	}




	/**  
	 *  加载图片
	 *  @param rootPath   图片根路径
	 */
	public void loadAlbum(String rootPath){
		//获取根目录下缩略图文件夹
		String folder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, rootPath);
		List<File> files=FileOperateUtil.listFiles(folder, ".jpg");
		if(files!=null&&files.size()>0){
			List<String> paths=new ArrayList<String>();
			for (File file : files) {
				paths.add(file.getAbsolutePath());
			}
			setAdapter(new ViewPagerAdapter(paths));
		}
	}

	public class ViewPagerAdapter extends PagerAdapter {
		private List<String> paths;//大图地址 如果为网络图片 则为大图url
		private View mCurrentView;
		public ViewPagerAdapter(List<String> paths){
			this.paths=paths;
		}

		@Override
		public int getCount() {
			return paths.size();
		}

		@Override
		public Object instantiateItem(ViewGroup viewGroup, int position) {
			//注意，这里不可以加inflate的时候直接添加到viewGroup下，而需要用addView重新添加
			View imageLayout = inflate(getContext(),R.layout.item_album_pager, null);
			viewGroup.addView(imageLayout);
			assert imageLayout != null;
			ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
			String path=paths.get(position);
			//			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			mImageLoader.loadImage(path, imageView, mOptions, false, getContext());
//			imageView.setImageBitmap(BitmapFactory.decodeFile(path));
			return imageLayout;
		}




		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;			
		}



		//设置当前的view
		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			mCurrentView = (View)object;
		}
		//自定义获取当前view方法                              
		public View getPrimaryItem() {
			return mCurrentView;
		}
	}

	/**
	 * 自定义page改变时的监听事件
	 * @param myPageChangeListener
	 */
	public void setOnPageChangeListener(final MyPageChangeListener myPageChangeListener){

		//实现原有的OnPageChangeListener
		setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// 实现原有的onPageSelected事件
				myPageChangeListener.onPageSelected(position);
				//实现自定义的onPageChanged事件
				myPageChangeListener.onPageChanged(cPosition,position);
				//更新当前position为oldPosition
				cPosition=position;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				myPageChangeListener.onPageScrolled(arg0,arg1,arg2);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				myPageChangeListener.onPageScrollStateChanged(arg0);
			}
		});
	}

	public void setLock(boolean islock) {
		this.lock=islock;
	}

	@Override  
	public boolean onTouchEvent(MotionEvent arg0) {  
		// 锁定时 范围false 不捕捉touch事件 使viewpager无法滑动
		return super.onTouchEvent(arg0);
	}  

	public interface MyPageChangeListener extends OnPageChangeListener{
		/**
		 * 
		 * @param oldPosition 移动前的position 当该值为-1时 表示第一次载入 还未移动过
		 * @param newPosition 移动后的position
		 */
		public void onPageChanged(int oldPosition,int newPosition);   
	}

}
