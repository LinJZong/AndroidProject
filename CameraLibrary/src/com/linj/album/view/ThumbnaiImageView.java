package com.linj.album.view;

import com.linj.cameralibrary.R;
import com.linj.imageloader.DisplayImageOptions;
import com.linj.imageloader.ImageLoader;
import com.linj.imageloader.displayer.RoundedBitmapDisplayer;

import android.R.bool;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

/** 
 * @ClassName: AlbumItemView 
 * @Description:  相册Item项 提取出来主要是为了实现点击ImageView变暗效果
 * @author LinJ
 * @date 2015-1-5 下午5:39:35 
 *  
 */
public class ThumbnaiImageView extends FrameLayout  {
	public static  final String TAG="AlbumItemView";
	private final ViewHolder mViewHolder;
	private final ImageLoader mImageLoader;
	private final DisplayImageOptions mOptions;
	private String mPath;
	private int mPosition;

	public ThumbnaiImageView(Context context,ImageLoader imageLoader,DisplayImageOptions options) {
		super(context);
		inflate(context, R.layout.item_album_grid, this);
		FilterImageView imageView=(FilterImageView) findViewById(R.id.imgThumbnail);
		CheckBox checkBox=(CheckBox) findViewById(R.id.checkbox);
		ImageView icon=(ImageView)findViewById(R.id.videoicon);
		mViewHolder=new ViewHolder(imageView,checkBox,icon);
		this.mImageLoader=imageLoader;
		this.mOptions=options;
	}

	/**  
	 *  设置标签
	 *  @param path 设置item指向的文件路径 会同时把checkbox的标签设置为该值
	 *  @param editable 是否可编辑状态
	 *  @param checked  checkbox是否选中
	 */
	public void setTags(String path,int position,boolean editable,boolean checked){
		//可编辑状态，显示checkbox
		if (editable) {
			mViewHolder.checkBox.setVisibility(View.VISIBLE);
			mViewHolder.checkBox.setChecked(checked);
		}else {
			mViewHolder.checkBox.setVisibility(View.GONE);
		}
		//原路径和当前路径不同，更新图片
		if (mPath==null||!mPath.equals(path)) {
			mImageLoader.loadImage(path, mViewHolder.imageView, mOptions);
			mPath=path;
			//给checkbox设置tag,用以记录当前选中项
			mViewHolder.checkBox.setTag(path);
			setTag(path);
			if(mPath.contains("video")){
				mViewHolder.videoIconView.setVisibility(View.VISIBLE);
			}else {
				mViewHolder.videoIconView.setVisibility(View.GONE);
			}
			mPosition=position;
		}
	}

	public int getPosition(){
		return mPosition;
	}
	/**  
	 * 设置checkbox的状态改变事件
	 *  @param listener   
	 */
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener){
		mViewHolder.checkBox.setOnCheckedChangeListener(listener);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		//重写click事件，将该View的click转到imageview触发
		mViewHolder.imageView.setOnClickListener(l);
	}

	public class ViewHolder {
		public ViewHolder(ImageView imageView,CheckBox checkBox,ImageView icon){
			this.imageView=imageView;
			this.checkBox=checkBox;
			this.videoIconView=icon;
		}
		ImageView imageView;//缩略图
		ImageView videoIconView;//播放视频图标
		CheckBox checkBox;//勾选框

	}
}
