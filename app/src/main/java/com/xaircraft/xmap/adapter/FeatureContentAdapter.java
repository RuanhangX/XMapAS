package com.xaircraft.xmap.adapter;


import com.xaircraft.xmap.R;

import EMap.IO_GisDB.IEFeature;
import EMap.IO_GisDB.IEField;
import EMap.IO_GisDB.IERow;
import EMap.IO_GisDB.IETable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
/**
 * 地物属性显示适配器类
 * @author 中海达测绘仪器有限公司
 *
 */
public class FeatureContentAdapter extends BaseAdapter {

	private LayoutInflater mLayoutInflater = null;
	private IEFeature mFeature = null;
	private IETable mTable = null;
	private IERow mRow = null;

	/**
	 * 初始化属性显示界面
	 * @param flater
	 * @param feature 显示内容地物
	 */
	public FeatureContentAdapter(LayoutInflater flater, IEFeature feature) {
		mLayoutInflater = flater;
		setFeature(feature);
	}

	/**
	 * 设置当前地物
	 * @param feature
	 */
	public void setFeature(IEFeature feature) {
		if (feature == null) {
			return;
		}
		mFeature = feature;
		mTable = feature.GetTable();
		mRow = feature.GetRow();
	}

	@Override
	public int getCount() {
		if (mTable == null) {
			return 0;
		}
		return mTable.GetFieldNum();
	}

	@Override
	public String[] getItem(int position) {
		return getFieldInfo(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.activity_attribute_content, null);
			viewHolder = new ViewHolder();
			viewHolder.tv_fieldName = (TextView) convertView.findViewById(R.id.att_edit_fieldname);
			viewHolder.ed_fieldValue = (EditText) convertView.findViewById(R.id.att_edit_fieldvalue);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		String[] info = getFieldInfo(position);
		if (info == null || info.length != 2) {
			return convertView;
		}

		viewHolder.tv_fieldName.setText(info[0]);
		viewHolder.ed_fieldValue.setText(info[1]);

		return convertView;
	}

	/**
	 * 获取地物的所有字段信息
	 * @param index
	 * @return
	 */
	private String[] getFieldInfo(int index) {
		String[] info = null;
		try {
			if (mFeature == null || mTable == null || mRow == null) {
				return null;
			}

			IEField field = mTable.GetField(index);
			if (field == null) {
				return null;
			}
			String fieldName = field.GetFieldName();
			if (fieldName == null || fieldName.length() < 1) {
				return null;
			}
			String value = "";
			
			switch (field.GetFieldType()) {
			case ATT_FIELD_TYPE_DATETIME:
				value = mRow.GetStringValue(fieldName);
				break;
			case ATT_FIELD_TYPE_DOUBLE:
				value = String.valueOf(mRow.GetDoubleValue(fieldName));
				break;
			case ATT_FIELD_TYPE_LONG:
				value = String.valueOf(mRow.GetLongValue(fieldName));
				break;
			case ATT_FIELD_TYPE_STRING:
				value = mRow.GetStringValue(fieldName);
				break;
			default:
				break;
			}
			
			if (value == null) {
				value = "";
			}
			info = new String[2];
			info[0] = fieldName;
			info[1] = value;
		} catch (Exception e) {
		}
		return info;
	}

	/**
	 * Listview控件重用
	 *
	 */
	class ViewHolder {
		TextView tv_fieldName;
		EditText ed_fieldValue;
	}

}
