package com.linj.album.view;

import com.example.camera.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/** 
* @ClassName: AlbumItemView 
* @Description:  相册Item项 提取出来主要是为了实现点击ImageView变暗效果
* @author LinJ
* @date 2015-1-5 下午5:39:35 
*  
*/
public class AlbumItemView extends FrameLayout{
	public AlbumItemView(Context context) {
		super(context);
		inflate(context, R.layout.item_album_image, this);
	}
	public AlbumItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			break;
		case MotionEvent.ACTION_UP:

			break;
		default:
			break;
		}
		return true;
	}
}
