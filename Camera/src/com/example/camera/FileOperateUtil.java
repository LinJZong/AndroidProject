package com.example.camera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

/** 
* @ClassName: FileOperateUtil 
* @Description:  文件操作工具类
* @author LinJ
* @date 2014-12-31 上午9:44:38 
*  
*/
public class FileOperateUtil {
	private final static String TAG="FileOperateUtil";

	public final static int ROOT=0;//根目录
	public final static int TYPE_IMAGE=1;//图片
	public final static int TYPE_THUMBNAIL=2;//缩略图
	public final static int TYPE_VEDIO=3;//视频

	/**
	 *获取文件夹路径
	 * @param type 文件夹类别
	 * @param rootPath 根目录文件夹名字 为业务流水号
	 * @return
	 */
	public static String getFolderPath(Context context,int type,String rootPath) {
		//本业务文件主目录
		StringBuilder pathBuilder=new StringBuilder();
		//添加应用存储路径
		pathBuilder.append(context.getExternalFilesDir(null).getAbsolutePath());
		pathBuilder.append(File.separator);
		//添加文件总目录
		pathBuilder.append(context.getString(R.string.Files));
		pathBuilder.append(File.separator);
		//添加当然文件类别的路径
		pathBuilder.append(rootPath);
		pathBuilder.append(File.separator);
		switch (type) {
		case TYPE_IMAGE:
			pathBuilder.append(context.getString(R.string.Image));
			break;
		case TYPE_VEDIO:
			pathBuilder.append(context.getString(R.string.Vedio));
			break;
		case TYPE_THUMBNAIL:
			pathBuilder.append(context.getString(R.string.Thumbnail));
			break;
		default:
			break;
		}
		return pathBuilder.toString();
	}

	/**
	 * 获取目标文件夹内指定后缀名的文件数组,按照修改日期排序
	 * @param file 目标文件夹路径
	 * @param format 指定后缀名
	 * @return
	 */
	public static List<File> listFiles(String file,final String format){
		return listFiles(new File(file), format);
	}
	/**
	 * 获取目标文件夹内指定后缀名的文件数组,按照修改日期排序
	 * @param file 目标文件夹
	 * @param format 指定后缀名
	 * @return
	 */
	public static List<File> listFiles(File file,final String extension){
		File[] files=null;
		if(file==null||!file.exists()||!file.isDirectory())
			return null;
		files=file.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				return arg1.endsWith(extension);
			}
		});
		if(files!=null){
			List<File> list=Arrays.asList(files);
			//按修改日期排序
			Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    if (file.lastModified() > newFile.lastModified()) {
                        return 1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return -1;
                    }
 
                }
            });
           return list;
		}
		return null;
	}

	/**
	 * 
	 * @param extension 后缀名 如".jpg"
	 * @return
	 */
	public static String createFileNmae(String extension){
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss",Locale.getDefault());
		// 转换为字符串
		String formatDate = format.format(new Date());
		//查看是否带"."
		if(!extension.startsWith("."))
			extension="."+extension;
		return formatDate+extension;
	}
}