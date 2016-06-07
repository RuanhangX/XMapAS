package com.xaircraft.xmap.component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import EMap.IO_GisDB.IEField;
import EMap.IO_GisDB.IERow;
import EMap.IO_GisDB.IETable;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 表单构建类
 * 
 */
public class FormCreater {

	private Context m_ctx;
	private IETable m_table;
	private HashMap<Integer, EditText> m_forms;
	public static final int ET_MEASURE_AREA=0x7f050001;

	/**
	 * 构建表单构建器对象
	 * 
	 * @param ctx
	 */
	public FormCreater(Context ctx, IETable table) {
		m_ctx = ctx;
		m_forms = new HashMap<Integer, EditText>();
		m_table = table;
	}

	/**
	 * 获取表单
	 * 
	 * @param table
	 *            表格
	 * @return
	 */
	public ScrollView getForm() {
		ScrollView scroller=new ScrollView(m_ctx);
		int num = m_table.GetFieldNum();
		IEField field;
		LinearLayout layout = createLayout(m_ctx);
		LinearLayout child = null;
		for (int i = 1; i < num; i++) {
			field = m_table.GetField(i);
			switch (field.GetFieldType()) {
			case ATT_FIELD_TYPE_DOUBLE:
				child = createChild(m_ctx, field.GetFieldName(),
						InputType.TYPE_NUMBER_FLAG_DECIMAL, i);
				break;
			case ATT_FIELD_TYPE_LONG:
				child = createChild(m_ctx, field.GetFieldName(),
						InputType.TYPE_NUMBER_FLAG_SIGNED, i);
				break;
			case ATT_FIELD_TYPE_STRING:
				child = createChild(m_ctx, field.GetFieldName(),
						InputType.TYPE_TEXT_VARIATION_NORMAL, i);
				break;
			case ATT_FIELD_TYPE_DATETIME:
				child = createChild(m_ctx, field.GetFieldName(),
						InputType.TYPE_DATETIME_VARIATION_NORMAL, i);
				break;
			default:
				child = createChild(m_ctx, field.GetFieldName(),
						InputType.TYPE_TEXT_VARIATION_NORMAL, i);
				break;
			}
			if (child != null) {
				layout.addView(child);
			}
		}
		scroller.addView(layout);
		return scroller;
	}

	/**
	 * 创建背景布局
	 * 
	 * @param ctx
	 *            上下文
	 * @return
	 */
	private LinearLayout createLayout(Context ctx) {
		LinearLayout layout = new LinearLayout(ctx);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		layout.setBackgroundColor(Color.WHITE);
		layout.setLayoutParams(params);
		layout.setOrientation(LinearLayout.VERTICAL);
		return layout;
	}

	/**
	 * 创建表单元素
	 * 
	 * @param ctx
	 *            上下文
	 * @param name
	 *            字段名称
	 * @param type
	 *            文本输入类型
	 * @return
	 */
	private LinearLayout createChild(Context ctx, String name, int type,
			int index) {
		LinearLayout child = new LinearLayout(ctx);
		LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 10, 10, 10);
		child.setLayoutParams(params);
		child.setOrientation(LinearLayout.HORIZONTAL);
		TextView tv = createTextView(ctx);
		tv.setText(String.format("%s:", name));
		child.addView(tv);

		EditText et = new EditText(ctx);
		params = new LayoutParams(0,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.weight = 2;
		params.gravity = Gravity.CENTER_VERTICAL;
		params.setMargins(5, 0, 0, 0);
		et.setLayoutParams(params);
		et.setTextSize(18);
		et.setTextColor(Color.GRAY);
		et.setPadding(3, 0, 0, 0);
		et.setSingleLine();
		et.setInputType(type);
		if(name.equals("实测面积"))
			et.setId(ET_MEASURE_AREA);
		child.addView(et);
		m_forms.put(index, et);
		return child;
	}

	/**
	 * 创建表单元素视图的名称部分
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	private TextView createTextView(Context context) {
		LayoutParams params = new LayoutParams(0,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		TextView tv = new TextView(context);
		params.weight = 1;
		params.gravity = Gravity.CENTER_VERTICAL;
		tv.setLayoutParams(params);
		tv.setGravity(Gravity.RIGHT | Gravity.CLIP_VERTICAL);
		tv.setTextColor(Color.BLACK);
		tv.setTextSize(20);
		return tv;
	}

	/**
	 * 获取填充好了的属性
	 * 
	 * @return
	 */
	public IERow getRow() {
		IERow row = m_table.CreateRow();
		int num = m_table.GetFieldNum();
		IEField field;
		EditText et;
		for (int i = 1; i < num; i++) {
			field = m_table.GetField(i);
			switch (field.GetFieldType()) {
			case ATT_FIELD_TYPE_DOUBLE:
				et = m_forms.get(i);
				row.SetDoubleValue(i, toDouble(et.toString()));
				break;
			case ATT_FIELD_TYPE_LONG:
				et = m_forms.get(i);
				row.SetLongValue(i, toInt(et.toString()));
				break;
			case ATT_FIELD_TYPE_STRING:
				et = m_forms.get(i);
				row.SetStringValue(i, et.toString());
				break;
			case ATT_FIELD_TYPE_DATETIME:
				et = m_forms.get(i);
				row.SetDateTimeValue(i, toCalendar(et.toString()));
				break;
			default:
				break;
			}
		}
		return row;
	}

	/**
	 * 字符串转换为Int
	 * 
	 * @param str
	 * @return
	 */
	public int toInt(String str) {
		if (str == null || str.length() == 0) {
			return 0;
		}
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}

	}

	/**
	 * 字符串转换为double
	 * 
	 * @param str
	 * @return
	 */
	public double toDouble(String str) {
		if (str == null || str.length() == 0) {
			return 0;
		}
		try {
			return Double.parseDouble(str);
		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}

	}

	public Calendar toCalendar(String str) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2015, 4, 1, 18, 0);
		if (str == null || str.length() == 0) {
			return calendar;
		}
		try {
			calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(str));
			return calendar;
		} catch (Exception e) {
			// TODO: handle exception
			return Calendar.getInstance();
		}
	}
}
