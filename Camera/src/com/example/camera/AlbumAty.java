package com.example.camera;

import java.util.Set;

import com.linj.album.view.AlbumGridView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/** 
* @ClassName: AlbumAty 
* @Description: 相册Activity
* @author LinJ
* @date 2015-1-6 下午5:03:48 
*  
*/
public class AlbumAty extends Activity implements View.OnClickListener,AlbumGridView.OnCheckedChangeListener{
	public final static String TAG="AlbumAty";
	/**
	 * 显示相册的View
	 */
	private AlbumGridView mAlbumView;

	private String mSaveRoot;

	private TextView mEnterView;
	private TextView mLeaveView;
	private TextView mSelectedCounterView;
	private TextView mSelectAllView;
	private Button mDeleteButton;
	private ImageView mBackView;
	private Button mCutButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.album);

		mAlbumView=(AlbumGridView)findViewById(R.id.albumview);
		mEnterView=(TextView)findViewById(R.id.header_bar_enter_selection);
		mLeaveView=(TextView)findViewById(R.id.header_bar_leave_selection);
		mSelectAllView=(TextView)findViewById(R.id.select_all);
		mSelectedCounterView=(TextView)findViewById(R.id.header_bar_select_counter);
		mDeleteButton=(Button)findViewById(R.id.delete);
		mCutButton=(Button)findViewById(R.id.move);
		mBackView=(ImageView)findViewById(R.id.header_bar_back);
		
		mSelectedCounterView.setText("0");

		mEnterView.setOnClickListener(this);
		mLeaveView.setOnClickListener(this);
		mSelectAllView.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        mCutButton.setOnClickListener(this);
        mBackView.setOnClickListener(this);
        
		mAlbumView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (mAlbumView.getEditable()) return; 
				Intent intent=new Intent(AlbumAty.this,AlbumItemAty.class);
				intent.putExtra("path", arg1.getTag().toString());
				startActivity(intent);
			}
		});
		mAlbumView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(mAlbumView.getEditable()) return true;
				enterEdit();
				return true;
			}
		});
		mSaveRoot="test";
		

	}

   

	@Override
	protected void onResume() {
		mAlbumView.loadAlbum(mSaveRoot);
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.header_bar_enter_selection:
			enterEdit();
			break;
		case R.id.header_bar_leave_selection:
			leaveEdit();
			break;
		case R.id.select_all:
			selectAllClick();
			break;
		case R.id.delete:
			showDeleteDialog();
			break;
		case R.id.header_bar_back:
			Intent intent=new Intent(this,CameraAty.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	private void enterEdit() {
		mAlbumView.setEditable(true,this);
		mSelectAllView.setText(getResources().getString(R.string.album_phoot_select_all));
		mDeleteButton.setEnabled(false);
		mCutButton.setEnabled(false);
		findViewById(R.id.header_bar_navi).setVisibility(View.GONE);
		findViewById(R.id.header_bar_select).setVisibility(View.VISIBLE);
		findViewById(R.id.album_bottom_bar).setVisibility(View.VISIBLE);
	}

	private void leaveEdit() {
		mAlbumView.setEditable(false);
		mCutButton.setEnabled(false);
		findViewById(R.id.header_bar_navi).setVisibility(View.VISIBLE);
		findViewById(R.id.header_bar_select).setVisibility(View.GONE);
		findViewById(R.id.album_bottom_bar).setVisibility(View.GONE);
	}
	
	private void selectAllClick() {
		if(mSelectAllView.getText().equals(getResources().getString(R.string.album_phoot_select_all))){
			mAlbumView.selectAll(this);
			mSelectAllView.setText(getResources().getString(R.string.album_phoot_unselect_all));
		}else{
			mAlbumView.unSelectAll(this);
			mSelectAllView.setText(getResources().getString(R.string.album_phoot_select_all));
		}
			
	}
	
	private void showDeleteDialog() {
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setMessage("确定要要删除?")
		.setPositiveButton("确认", new OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Set<String> items=mAlbumView.getSelectedItems();
				for (String path : items) {
					boolean flag=FileOperateUtil.deleteThumbFile(path,AlbumAty.this);
					if(!flag) Log.i(TAG, path);
				}
				mAlbumView.loadAlbum(mSaveRoot);
				leaveEdit();
			}
		})
		.setNegativeButton("取消", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.create().show();
	}


	
	


	


	@Override
	public void onCheckedChanged(Set<String> set) {
		// TODO Auto-generated method stub
		mSelectedCounterView.setText(String.valueOf(set.size()));
		if(set.size()>0){
			mDeleteButton.setEnabled(true);
			mCutButton.setEnabled(true);
		}else {
			mDeleteButton.setEnabled(false);
			mCutButton.setEnabled(false);
		}
	}
	@Override
	public void onBackPressed() {
		if(mAlbumView.getEditable()){
			leaveEdit();
			return;
		}
		super.onBackPressed();
	}
}
