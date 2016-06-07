package com.xaircraft.xmap.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * 地物OID显示，属性查询索引列表
 * @author 中海达测绘仪器有限公司
 *
 */
public class FeatureIdAdapter extends BaseExpandableListAdapter {

	private Context mContext;
	private String[] mLayerNames = null;
	private int[][] mIds = null;
	
	/**
	 * 地物OID显示适配器，属性查询索引列表
	 * @param context
	 * @param layerNames 所有图层名
	 * @param ids 所有oid
	 */
	public FeatureIdAdapter(Context context, String[] layerNames, int[][] ids){
		mContext = context;
		mLayerNames = layerNames;
		mIds = ids;
	}
	
	@Override
	public int getGroupCount() {
		if (mLayerNames == null) {
			return 0;
		}
		return mLayerNames.length;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (mIds == null) {
			return 0;
		}
		return mIds[groupPosition].length;
	}

	@Override
	public String getGroup(int groupPosition) {
		if (mLayerNames == null) {
			return "";
		}
		return mLayerNames[groupPosition];
	}

	@Override
	public Integer getChild(int groupPosition, int childPosition) {
		if (mIds == null) {
			return 0;
		}
		return mIds[groupPosition][childPosition];
	}

	@Override
	public long getGroupId(int groupPosition) {
		if (mLayerNames == null) {
			return 0;
		}
		return mLayerNames[groupPosition].hashCode();
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		if (mIds == null) {
			 return 0;
		}
		return mIds[groupPosition][childPosition];
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView tv;
		if (convertView == null) {
			tv = getTextView();
			tv.setGravity(Gravity.RIGHT);
			convertView = tv;
		} else {
			tv = (TextView) convertView;
		}
		if (mLayerNames != null) {
			tv.setText(mLayerNames[groupPosition]);
		}
		
		return tv;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		TextView tv;
		if (convertView == null) {
			tv = getTextView();
			tv.setGravity(Gravity.CENTER);
			convertView = tv;
		} else {
			tv = (TextView) convertView;
		}
		
		if (mIds != null) {
			tv.setText(String.valueOf(mIds[groupPosition][childPosition]));
		}
		
		return tv;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	/**
	 * 获取列表单元视图
	 * @return
	 */
	private TextView getTextView(){
		TextView tv = new TextView(mContext);
		tv.setBackgroundColor(Color.TRANSPARENT);
		tv.setTextSize(20f);
		tv.setPadding(0, 7, 5, 7);
		return tv;
	}

}
