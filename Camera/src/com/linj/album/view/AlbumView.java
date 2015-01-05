package com.linj.album.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.imageloader.DisplayImageOptions;
import com.linj.imageloader.ImageLoader;
import com.linj.imageloader.displayer.RoundedBitmapDisplayer;


import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

/** 
 * @ClassName: AlbumView 
 * @Description:  相册View，继承于GridView，封装了Adapter和图片加载方法
 * @author LinJ
 * @date 2015-1-5 下午5:09:08 
 *  
 */
public class AlbumView extends GridView{
	/**  图片加载器 优化了了缓存  */ 
	private ImageLoader imageLoader;
	/**  加载图片配置参数 */ 
	private DisplayImageOptions options;	
	/**  当前是否处于编辑状态 true为编辑 */ 
	private boolean mEditable;
	public AlbumView(Context context, AttributeSet attrs) {
		super(context, attrs);
		imageLoader= ImageLoader.getInstance(context);
		//设置网络图片加载参数
		DisplayImageOptions.Builder builder= new DisplayImageOptions.Builder();
		builder =builder
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageOnFail(R.drawable.ic_error)
				.cacheInMemory(true)//为了在本地修改图片后及时更新小图标 不在内存中缓存
				.cacheOnDisk(false)
				.displayer(new RoundedBitmapDisplayer(20));
		options=builder.build();
	}


	/**  
	 *  加载图片
	 *  @param rootPath 根目录文件夹名 
	 */
	public void loadAlbum(String rootPath){
		//获取根目录下缩略图文件夹
		String thumbFolder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_THUMBNAIL, rootPath);
		List<File> files=FileOperateUtil.listFiles(thumbFolder, ".jpg");
		if(files!=null&&files.size()>0){
			List<String> paths=new ArrayList<String>();
			for (File file : files) {
				paths.add(file.getAbsolutePath());
			}
			setAdapter(new AlbumViewAdapter(paths));
		}
	}

	/**  
	 *  全选图片
	 *  @param listener 选择图片后执行的回调函数   
	 */
	public void selectAll(AlbumView.OnCheckedChangeListener listener){
		((AlbumViewAdapter)getAdapter()).selectAll(listener);
	}
	/**  
	 * 取消全选图片
	 *  @param listener   选择图片后执行的回调函数  
	 */
	public void unSelectAll(AlbumView.OnCheckedChangeListener listener){
		((AlbumViewAdapter)getAdapter()).unSelectAll(listener);
	}

	/**  
	 * 设置可编辑状态
	 *  @param editable 是否可编辑   
	 */
	public void setEditable(boolean editable){
		mEditable=editable;
		((AlbumViewAdapter)getAdapter()).notifyDataSetChanged(null);
	}
	/**  
	 * 设置可编辑状态
	 *  @param editable 是否可编辑   
	 *  @param listener 选择图片后执行的回调函数  
	 */
	public void setEditable(boolean editable,AlbumView.OnCheckedChangeListener listener){
		mEditable=editable;
		((AlbumViewAdapter)getAdapter()).notifyDataSetChanged(listener);
	}

	/**  
	 *  获取可编辑状态
	 *  @return   
	 */
	public boolean getEditable(){
		return mEditable;
	}

	/**  
	 *  获取当前选择的图片路径集合
	 *  @return   
	 */
	public Set<String> getSelectedItems(){
		return ((AlbumViewAdapter)getAdapter()).getSelectedItems();
	}

	/** 
	 * @ClassName: OnCheckedChangeListener 
	 * @Description:  图片选中后的监听接口，用以在activity内做回调处理
	 * @author LinJ
	 * @date 2015-1-5 下午5:13:43 
	 *  
	 */
	public interface OnCheckedChangeListener{
		public void onCheckedChanged(Set<String> set);
	}
	/** 
	 * @ClassName: AlbumViewAdapter 
	 * @Description:  相册GridView适配器
	 * @author LinJ
	 * @date 2015-1-5 下午5:14:14 
	 *  
	 */
	public class AlbumViewAdapter extends BaseAdapter
	{

		/** 加载的文件路径集合 */ 
		List<String> mPaths;

		/**  当前选中的文件的集合 */ 
		Set<String> itemSelectedSet=new HashSet<String>();

		/**  选中图片后执行的回调函数 */ 
		AlbumView.OnCheckedChangeListener listener=null;


		public AlbumViewAdapter(List<String> paths) {
			super();
			this.mPaths = paths;
		}

		private class ViewHolder {
			ImageView imgThumbnail;//缩略图
			CheckBox checkBox;//勾选框
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mPaths.size();
		}


		@Override
		public String getItem(int position) {
			// TODO Auto-generated method stub
			return mPaths.get(position);
		}


		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			AlbumItemView view = (AlbumItemView)convertView;
			if (view == null) {
				view = new AlbumItemView(getContext());
				holder = new ViewHolder();
				holder.imgThumbnail = (ImageView) view.findViewById(R.id.imgThumbnail);
				holder.checkBox=(CheckBox)view.findViewById(R.id.checkbox);
				holder.checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
				view.setTag(holder);
				view.setOnTouchListener(onTouchListener);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			Object tag=holder.imgThumbnail.getTag();
			String path=getItem(position);
			//文件相同时，不替换
			if(tag==null||!tag.equals(path)){
				imageLoader.loadImage(path, holder.imgThumbnail, options, false,getContext());
				holder.imgThumbnail.setTag(path);
				holder.checkBox.setTag(path);
			}
			if (mEditable){ 
				holder.checkBox.setVisibility(View.VISIBLE);
				//设置Checkbox选中状态
				holder.checkBox.setChecked(itemSelectedSet.contains(path));
			}
			else 
				holder.checkBox.setVisibility(View.GONE);
			return view;
		}




		/**  
		 * 适配器内容改变时，重新绘制
		 *  @param listener   
		 */
		public void notifyDataSetChanged(AlbumView.OnCheckedChangeListener listener) {
			//重置map
			itemSelectedSet=new HashSet<String>();
			this.listener=listener;
			super.notifyDataSetChanged();
		}
		/**  
		 * 选中所有文件
		 *  @param listener   
		 */
		public void selectAll(AlbumView.OnCheckedChangeListener listener){
			for (String path : mPaths) {
				itemSelectedSet.add(path);
			}
			this.listener=listener;
			super.notifyDataSetChanged();
			if(listener!=null) listener.onCheckedChanged(itemSelectedSet);
		}

		/**  
		 *  取消选中所有文件
		 *  @param listener   
		 */
		public void unSelectAll(AlbumView.OnCheckedChangeListener listener){
			notifyDataSetChanged(listener);
			if(listener!=null) listener.onCheckedChanged(itemSelectedSet);
		}
		/**  
		 * 获取当前选中文件的集合
		 *  @return   
		 */
		public Set<String> getSelectedItems(){
			return itemSelectedSet;
		}

		//Checkbox状态改变监听函数
		CompoundButton.OnCheckedChangeListener onCheckedChangeListener=new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(buttonView.getTag()==null) return;
				if (isChecked) itemSelectedSet.add(buttonView.getTag().toString());
				else itemSelectedSet.remove(buttonView.getTag().toString());
				if(listener!=null) listener.onCheckedChanged(itemSelectedSet);
			}
		};

		View.OnTouchListener onTouchListener=new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		};
		
		View.OnClickListener onClickListener=new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		};
	}
}
