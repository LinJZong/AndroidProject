package com.linj.imageloader;

import com.linj.imageloader.displayer.BitmapDisplayer;


/** 
* @ClassName: DisplayImageOptions 
* @Description:  图片加载参数
* @author LinJ
* @date 2015-1-8 上午9:47:04 
*  
*/
public class DisplayImageOptions {
	
	public int imageResOnLoading;
	public int imageResOnFail;
	public boolean cacheInMemory;
	public boolean cacheOnDisk;
	public BitmapDisplayer displayer;
	public boolean fromNet;
	private DisplayImageOptions(Builder builder) {
		this.imageResOnLoading=builder.imageResOnLoading;
		this.imageResOnFail=builder.imageResOnFail;
		this.cacheInMemory=builder.cacheInMemory;
		this.cacheOnDisk=builder.cacheOnDisk;
		this.displayer=builder.displayer;
		this.fromNet=builder.fromNet;
	}

	public static class Builder{
		
		private int imageResOnLoading;
		private int imageResOnFail;
		private boolean cacheInMemory;
		private boolean cacheOnDisk;
		private BitmapDisplayer displayer;
		private boolean fromNet;
		/**
		 * 设置 加载图片中显示的图片
		 * @param imageRes 图片ID
		 * @return
		 */
		public Builder showImageOnLoading(int imageRes) {
			this.imageResOnLoading = imageRes;
			return this;
		}
		/**
		 * 设置加载图片失败显示的图片
		 * @param imageRes 图片ID
		 * @return
		 */
		public Builder showImageOnFail(int imageRes) {
			this.imageResOnFail = imageRes;
			return this;
		}
		/**
		 * 设置是否在内存中缓存
		 * @param cacheInMemory 
		 * @return
		 */
		public Builder cacheInMemory(boolean cacheInMemory) {
			this.cacheInMemory = cacheInMemory;
			return this;
		}
		/**
		 * 设置是否在sd卡上缓存
		 * @param cacheOnDisk
		 * @return
		 */
		public Builder cacheOnDisk(boolean cacheOnDisk) {
			this.cacheOnDisk = cacheOnDisk;
			return this;
		}
		/**
		 * 图片显示器
		 * @param displayer
		 * @return
		 */
		public Builder displayer(BitmapDisplayer displayer) {
			if (displayer == null) throw new IllegalArgumentException("displayer can't be null");
			this.displayer = displayer;
			return this;
		}
		/**  
		*  设置是否加载网络图片
		*  @param fromNet
		*  @return   
		*/
		public Builder setFromNet(boolean fromNet) {
			this.fromNet = fromNet;
			return this;
		}
		/**
		 * 创建DisplayImageOptions对象
		 * @return
		 */
		public DisplayImageOptions build() {
			//在这里做builder所有字段的非空判断
			return new DisplayImageOptions(this);
		}

		
	}
}
