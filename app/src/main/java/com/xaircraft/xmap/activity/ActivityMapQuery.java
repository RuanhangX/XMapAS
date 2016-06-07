package com.xaircraft.xmap.activity;

import com.xaircraft.xmap.R;
import com.xaircraft.xmap.adapter.FeatureParameter;
import com.xaircraft.xmap.component.MainImageViewControl;
import com.xaircraft.xmap.entity.MapEntity;
import com.xaircraft.xmap.helper.FileLayout;

import EMap.IO_Base.IEStruct.QueryRect_Layer_FeatureSet;
import EMap.IO_Base.IEStruct.f64Rect;
import EMap.IO_GisDB.IEFeatureClassVectorEd2;
import EMap.IO_GisDB.IEFeatureSet;
import EMap.IO_GisDB.IEGisDB_Base.E_FILE_TYPE;
import EMap.IO_Map.IELayer;
import EMap.IO_Map.IELayerVectorEd2;
import EMap.IO_Map.IEMap;
import EMap.IO_MapView.IEGisTool;
import EMap.IO_MapView.IEMapView_Base.EMAP_ACTION;
import EMap.IO_MapView.IEMapView_Base.EMAP_TOOL;
import EMap.OV_MapView.OV_GisTool;
import EMap.OV_MapView.OV_MapView;
import EMap.OV_MapView.OV_Widget;
import EMap.OV_MapView.OV_GisTool.ToolNotify;
import EMap.OV_MapView.OV_Widget.Widget;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class ActivityMapQuery extends Activity {
	private static final int TAG_SELECT_ATTR = 1001;// 单图层查询标签
	private static final int TAG_SELECT_EX_ATTR = 1002;// 跨图层查询标签
	private static final int TAG_SELECT_RECT = 1003;// 包围盒查询标签

	private OV_MapView mMapView;
	private Button mBtnSelectLayer;

	private MainImageViewControl mMIVCQueryCurLayer;
	private MainImageViewControl mMIVCQueryAllLayer;
	private MainImageViewControl mMivcQueryByRect;
	private EditText mEtMainRectQuery_xmin;// 包围盒最小x
	private EditText mEtMainRectQuery_xmax;// 包围盒最大x
	private EditText mEtMainRectQuery_ymin;// 包围盒最小y
	private EditText mEtMainRectQuery_ymax;// 包围盒最大y

	private MapEntity mMapEntity;
	private OV_GisTool mGisTool;// 查询工具
	private Toast mToast;// 显示提示信息

	private TextView mTvCurLayer;
	private Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_query);

		initView();// 初始化界面
		initMapView();// 初始化地图控件
		initData();// 初始化地图数据
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		mBtnSelectLayer = (Button) findViewById(R.id.btnSelectLayer);
		mTvCurLayer = (TextView) findViewById(R.id.tvCurLayer);

		mMIVCQueryCurLayer = (MainImageViewControl) findViewById(R.id.mivcQueryCurLayer);
		mMIVCQueryCurLayer.setText(getResources().getString(
				R.string.act_mapQuery_query));
		mMIVCQueryCurLayer.setImageResource(R.drawable.selector_fold_image);

		mMIVCQueryAllLayer = (MainImageViewControl) findViewById(R.id.mivcQueryAllLayer);
		mMIVCQueryAllLayer.setText(getResources().getString(
				R.string.act_mapQuery_queryAll));
		mMIVCQueryAllLayer.setImageResource(R.drawable.selector_folders_image);

		mMivcQueryByRect = (MainImageViewControl) findViewById(R.id.mivcQueryByRect);
		mMivcQueryByRect.setText(getResources().getString(
				R.string.act_mapQuery_queryByRet));
		mMivcQueryByRect.setImageResource(R.drawable.selector_checkbox);

		mBtnSelectLayer.setOnClickListener(mClickListener);
		mMIVCQueryCurLayer.setOnClickListener(mClickListener);
		mMIVCQueryAllLayer.setOnClickListener(mClickListener);
		mMivcQueryByRect.setOnClickListener(mClickListener);
	}

	/**
	 * 界面按钮事件
	 */
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.btnSelectLayer) {
				selectLayer();// 选择图层
			} else if (id == R.id.mivcQueryCurLayer) {
				queryCurrentLayer();// 单图层查询
			} else if (id == R.id.mivcQueryAllLayer) {
				queryAll();// 跨图层查询
			} else if (id == R.id.mivcQueryByRect) {
				queryRect();// 包围盒查询
			}
		}
	};

	/**
	 * 初始化地图控件
	 */
	private void initMapView() {
		mMapView = (OV_MapView) findViewById(R.id.mapview);
		OV_Widget compass = mMapView.addWidget(Widget.Compass, Widget.POS_RIGHT
				| Widget.POS_TOP);
		compass.setZoomScale(2);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int sreenWidth = dm.widthPixels;
		compass.setPosition(sreenWidth - 88, 192);
		mMapView.addWidget(Widget.Scale, Widget.POS_LEFT | Widget.POS_CENTER_H);
		mMapView.getGisToolManager().setNotify(mMapToolNotify);// 设置地图框选查询返回事件
	}

	/**
	 * 初始化地图数据
	 */
	private void initData() {

		mMapEntity = MapEntity.getInstance();
		
		int ret = mMapEntity.openMap(FileLayout.getMapPath());
		if (ret < 1) {
			Toast.makeText(this, R.string.act_main_mapFault, Toast.LENGTH_SHORT)
					.show();
		} else {
			
			mMapView.setMap(mMapEntity.getMap());
			
			mMapView.setFontFile(FileLayout.getAppMain(), "HiMap Symbols.ttf");
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 显示相应的提示
	 * 
	 * @param resId
	 */
	private void showToast(int resId) {
		if (mToast == null) {
			mToast = Toast.makeText(ActivityMapQuery.this, resId,
					Toast.LENGTH_SHORT);
		}
		mToast.setText(resId);
		mToast.show();
	}

	/**
	 * 图层设置
	 */
	private void selectLayer() {
		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return;
		}
		final IEMap map = mMapEntity.getMap();
		final String[] layerNames = new String[map.GetLayerNum()];
		for (int i = 0; i < layerNames.length; i++) {
			layerNames[i] = map.GetLayerByNo(i).GetLayerName();
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.cmm_dlg_selectLayer);
		builder.setSingleChoiceItems(layerNames, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String name = layerNames[which];
						IELayer layer = map.GetLayerByName(name, false);
						mMapView.SetCurEditLayer(layer);

						if (mTvCurLayer != null)
							mTvCurLayer.setText(name.split("\\.")[0]);
						f64Rect rect = FileLayout.resetLayer(layer);
						if (rect != null) {
							mMapView.getMapView().SetViewRange(rect);// 地图显示范围设置为图层的包围盒
							mMapView.SetAction(
									EMAP_ACTION.EMAP_ACT_UPDATE_REDRAW, true);
						} else
							Toast.makeText(mContext, "图层选择错误！", 2000);

						dialog.dismiss();
					}
				});
		builder.setNegativeButton(R.string.cmm_dlg_cancel, null);
		builder.show();
	}

	/**
	 * 查询当前图层
	 */
	private void queryCurrentLayer() {
		if (mMapView.GetCurEditLayer() == null) {// 必须先选择图层
			showToast(R.string.act_toast_select_layer);
			return;
		}
		;

		mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_HIT_SELECT);// EMAP_TOOL_HIT_SELECT点选，//EMAP_TOOL_RECT_SELECT框选
	}

	private void query() {
		ToolNotify toolNotify = new ToolNotify() {// 定义监听器

			@Override
			public void onEditStatusChange(OV_GisTool m_gisTool,
					EditStatus status, ExtraData changedData) {
				// TODO Auto-generated method stub
				QueryRect_Layer_FeatureSet queryFeatureSet = mGisTool
						.asIEGisTool().GetRectSelExFeatureSet();// 所有选中的地物的集合
				if (queryFeatureSet.getLayerNum() > 0) {
					FeatureParameter.getParameter()
							.setQueryRect_Layer_FeatureSet(queryFeatureSet);// 设置参数，供属性查询使用
					startAttributeActivity();
				}
			}
		};
		mMapView.getGisToolManager().setNotify(toolNotify);
		IELayer layer = mMapView.getMap().GetFirstLayer();
		if (layer == null
				|| layer.GetLayerType() != E_FILE_TYPE.E_FILE_TYPE_VECTOR_ED2) {
			return;
		}
		mMapView.SetCurEditLayer(layer);// 设置查询图层
		mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_RECT_SELECT_EX);// EMAP_TOOL_HIT_SELECT_EX跨图层点选，EMAP_TOOL_RECT_SELECT_EX跨图层框选

	}

	private IEFeatureSet sqlQueryCurrentLayer() {
		IELayerVectorEd2 layer_ed2 = mMapView.GetCurEditLayer();
		if (layer_ed2 == null) {
			return null;
		}
		IEFeatureClassVectorEd2 featureClass = layer_ed2.GetFeatureClassEd2();// 获取被查询要素类
		String subSql = "E_OID = 4";// SQL子句
		IEFeatureSet set = featureClass.QuerySetBySql(subSql);// 查询
		if (set.GetFeatureCount() == 0) {
			return null;
		}
		return set;
	}

	/**
	 * 跨图层查询
	 */
	private void queryAll() {
		mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_RECT_SELECT_EX);// 设置跨图层查询

	}

	/**
	 * 输入包围盒查询
	 */
	private void queryRect() {
		if (mMapView.GetCurEditLayer() == null) {// 必须先选择图层
			showToast(R.string.act_toast_select_layer);
			return;
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(
				ActivityMapQuery.this);
		dialog.setTitle(R.string.act_main_queryByRet);
		dialog.setView(getRectView());
		dialog.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Message msg = Message.obtain();
						msg.what = TAG_SELECT_RECT;
						mHandler.removeMessages(TAG_SELECT_RECT);
						mHandler.sendMessage(msg);
					}
				});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.create().show();
	}

	/**
	 * 获取包围盒输入框的视图
	 * 
	 * @return
	 */
	private View getRectView() {
		View view = LayoutInflater.from(ActivityMapQuery.this).inflate(
				R.layout.activity_main_dlg_rectquery, null);
		mEtMainRectQuery_xmin = (EditText) view
				.findViewById(R.id.et_main_rectquery_xmin);
		mEtMainRectQuery_xmax = (EditText) view
				.findViewById(R.id.et_main_rectquery_xmax);
		mEtMainRectQuery_ymin = (EditText) view
				.findViewById(R.id.et_main_rectquery_ymin);
		mEtMainRectQuery_ymax = (EditText) view
				.findViewById(R.id.et_main_rectquery_ymax);

		// 默认设置范围为该图层第一个地物的包围盒
		IELayerVectorEd2 layerEd2 = mMapView.GetCurEditLayer();
		f64Rect rect = layerEd2.GetFeatureClass().GetFeature(1).GetRange();
		mEtMainRectQuery_xmin.setText(Double.toString(rect.ymin()));
		mEtMainRectQuery_xmax.setText(Double.toString(rect.ymax()));
		mEtMainRectQuery_ymin.setText(Double.toString(rect.xmin()));
		mEtMainRectQuery_ymax.setText(Double.toString(rect.xmax()));
		return view;
	}

	/**
	 * 地图框选查询返回事件
	 */
	private ToolNotify mMapToolNotify = new ToolNotify() {

		@Override
		public void onEditStatusChange(OV_GisTool arg0, EditStatus arg1,
				ExtraData arg2) {
			switch (arg1) {
			case EditStatus_Select:
				mGisTool = arg0;
				Message msg = Message.obtain();
				msg.what = TAG_SELECT_ATTR;
				mHandler.removeMessages(TAG_SELECT_ATTR);
				mHandler.sendMessage(msg);
				break;
			case EditStatus_Select_Ex:
				mGisTool = arg0;
				Message msgEx = Message.obtain();
				msgEx.what = TAG_SELECT_EX_ATTR;
				mHandler.removeMessages(TAG_SELECT_EX_ATTR);
				mHandler.sendMessage(msgEx);
				break;
			default:
				break;
			}
		}

	};

	/**
	 * 处理UI相关事件
	 */
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == TAG_SELECT_ATTR) {
				IEGisTool tool = mGisTool.asIEGisTool();
				IELayerVectorEd2 layer = tool.GetLayer();// 当前选择图层
				IEFeatureSet featureSet = mGisTool.asIEGisTool()
						.GetFeatureSet();// 选中的地物集
				if (featureSet.GetFeatureCount() > 0) {
					FeatureParameter.getParameter().setFeatureSet(
							layer.GetLayerName(), featureSet);// 设置参数，供属性查询使用
					startAttributeActivity();
				}
			} else if (msg.what == TAG_SELECT_EX_ATTR) {
				QueryRect_Layer_FeatureSet queryFeatureSet = mGisTool
						.asIEGisTool().GetRectSelExFeatureSet();// 所有选中的地物的集合
				if (queryFeatureSet.getLayerNum() > 0) {
					FeatureParameter.getParameter()
							.setQueryRect_Layer_FeatureSet(queryFeatureSet);// 设置参数，供属性查询使用
					startAttributeActivity();
				}
			} else if (msg.what == TAG_SELECT_RECT) {
				String xmin_str = mEtMainRectQuery_ymin.getText().toString();
				String xmax_str = mEtMainRectQuery_ymax.getText().toString();
				String ymin_str = mEtMainRectQuery_xmin.getText().toString();
				String ymax_str = mEtMainRectQuery_xmax.getText().toString();
				try {
					double xmin = Double.parseDouble(xmin_str);
					double xmax = Double.parseDouble(xmax_str);
					double ymin = Double.parseDouble(ymin_str);
					double ymax = Double.parseDouble(ymax_str);

					f64Rect rect = new f64Rect();
					rect.xmin(xmin);
					rect.xmax(xmax);
					rect.ymin(ymin);
					rect.ymax(ymax);

					IELayerVectorEd2 layerEd2 = mMapView.GetCurEditLayer();
					IEFeatureSet featureSet = layerEd2.Query(rect);// 包围盒查询
					if (featureSet.GetFeatureCount() > 0) {
						FeatureParameter.getParameter().setFeatureSet(
								layerEd2.GetLayerName(), featureSet);// 设置参数，供属性查询使用
						startAttributeActivity();
					}
				} catch (NumberFormatException e) {
					showToast(R.string.act_toast_enter_number);
				}

			}
		}

	};

	/**
	 * 跳转属性查询界面
	 */
	private void startAttributeActivity() {
		Intent intent = new Intent(ActivityMapQuery.this,
				AttributeActivity.class);
		ActivityMapQuery.this.startActivity(intent);
	}

}
