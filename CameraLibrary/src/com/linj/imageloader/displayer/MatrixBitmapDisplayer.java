/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich, Daniel Martí
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.linj.imageloader.displayer;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;


/** 
* @ClassName: MatrixBitmapDisplayer 
* @Description:  Matrix效果的BitmapDisplayer
* @author LinJ
* @date 2015-1-8 上午9:54:22 
*  
*/
public class MatrixBitmapDisplayer implements BitmapDisplayer {
	
	public MatrixBitmapDisplayer() {
		
	}

	@Override
	public void display(Bitmap bitmap, ImageView imageView) {
		//正常的图片设置为FIT_CENTER
		imageView.setScaleType(ScaleType.FIT_CENTER);
		imageView.setImageBitmap(bitmap);
	}

	@Override
	public void display(int resouceID, ImageView imageView) {
		// 加载前和出错的的图片不自动拉伸
		imageView.setScaleType(ScaleType.CENTER);
		imageView.setImageResource(resouceID);
	}
}
