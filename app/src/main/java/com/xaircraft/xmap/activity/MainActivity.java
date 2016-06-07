package com.xaircraft.xmap.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xaircraft.xmap.R;
import com.xaircraft.xmap.adapter.FeatureParameter;
import com.xaircraft.xmap.component.FileDialog;
import com.xaircraft.xmap.component.FormCreater;
import com.xaircraft.xmap.component.MainImageViewControl;
import com.xaircraft.xmap.component.OpenFileDialog;
import com.xaircraft.xmap.entity.GpsOveryLay;
import com.xaircraft.xmap.entity.LayerAttrEntity;
import com.xaircraft.xmap.entity.MapEntity;
import com.xaircraft.xmap.helper.APPHelper;
import com.xaircraft.xmap.helper.CallbackBundle;
import com.xaircraft.xmap.helper.CommonHelper;
import com.xaircraft.xmap.helper.FileLayout;
import com.xaircraft.xmap.helper.Test;
import com.xaircraft.xmap.parsers.LayerAttrParser;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import EMap.IO_Base.IEStruct.QueryRect_Layer_FeatureSet;
import EMap.IO_Base.IEStruct.Spatial_Ref;
import EMap.IO_Base.IEStruct.f643Point;
import EMap.IO_Base.IEStruct.f64Rect;
import EMap.IO_GisDB.IEFeatureClassVectorEd2;
import EMap.IO_GisDB.IEFeatureSet;
import EMap.IO_GisDB.IEField;
import EMap.IO_GisDB.IEFieldSet;
import EMap.IO_GisDB.IEGisDB_Base.E_COOR_SYSTEM_TYPE;
import EMap.IO_GisDB.IEGisDB_Base.E_FILE_TYPE;
import EMap.IO_GisDB.IEGisDB_Base.E_GEO_TYPE;
import EMap.IO_GisDB.IERow;
import EMap.IO_GisDB.IETable;
import EMap.IO_Map.IELayer;
import EMap.IO_Map.IELayerVectorEd2;
import EMap.IO_Map.IEMap;
import EMap.IO_MapView.IEGisTool;
import EMap.IO_MapView.IEMapView_Base.EMAP_ACTION;
import EMap.IO_MapView.IEMapView_Base.EMAP_TOOL;
import EMap.OV_MapView.OV_GisTool;
import EMap.OV_MapView.OV_GisTool.ToolNotify;
import EMap.OV_MapView.OV_MapView;
import EMap.OV_MapView.OV_Widget;
import EMap.OV_MapView.OV_Widget.Widget;

public class MainActivity extends Activity {

	private OV_MapView mMapView;
	private MapEntity mMapEntity;
	private Button mBtnMapInfo;
	private Button mBtnNewMap;

	private Button mBtnAdd;
	private Button mBtnMinus;
	private Button mBtnLocation;

	private Button mBtnQueryCurLayer;
	private Button mBtnQueryAll;
	private Button mBtnManualCollect;
	private Button mBtnGPSCollect;
	private Button mBtnNodeCollect;

	private LinearLayout mLLDataCollect;
	private LinearLayout mLLMapQuery;

	private MainImageViewControl mMIVCOpenMap;
	private MainImageViewControl mMIVCLayerManage;
	private MainImageViewControl mMIVCDataCollect;
	private MainImageViewControl mMIVCMapQuare;

	private MainImageViewControl mMIVCNewMap;

	private MainImageViewControl mMIVCNewLayer;
	private MainImageViewControl mMIVCAddLayer;
	private MainImageViewControl mMIVCRemoveLayer;

	private static final int TAG_SELECT_ATTR = 1001;// 单图层查询标签
	private static final int TAG_SELECT_EX_ATTR = 1002;//跨图层查询标签
	private static final int TAG_SELECT_RECT = 1003;//包围盒查询标签

	private static final int FILEDIALOG_ADD_LAYER = 1;

	private TextView mTvCurLayer;
	private Button mBtnSelectLayer;

	private GpsOveryLay mGpsOveryLay;
	private Context mContext = this;
	private APPHelper application;

	private OV_GisTool mGisTool;//查询工具
	private Toast mToast;// 显示提示信息

	private LocationManager m_locationManager;
	private Location m_Location;

	private static int OpenMapId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		application = (APPHelper) APPHelper.getInstance();
		application.addActivity(this);
		initView();
		initMapView();
		FileLayout.initFileLayout();
		initData();
		initGps();
	}


	/**
	 * 初始化界面
	 */
	private void initView() {
		mBtnNewMap = (Button) findViewById(R.id.btnNewMap);
		mBtnMapInfo = (Button) findViewById(R.id.btnMapInfo);
		mBtnAdd = (Button) findViewById(R.id.btnAdd);
		mBtnMinus = (Button) findViewById(R.id.btnMinus);
		mBtnLocation = (Button) findViewById(R.id.btnLocation);
		mTvCurLayer = (TextView) findViewById(R.id.tvCurLayer);
		mLLDataCollect = (LinearLayout) findViewById(R.id.llDataCollect);
		mLLMapQuery = (LinearLayout) findViewById(R.id.llMapQuery);

		mBtnSelectLayer = (Button) findViewById(R.id.btnSelectLayer);
		mBtnQueryCurLayer = (Button) findViewById(R.id.btnQueryCurLayer);
		mBtnQueryAll = (Button) findViewById(R.id.btnQueryAll);
		mBtnManualCollect = (Button) findViewById(R.id.btnManualCollect);
		mBtnGPSCollect = (Button) findViewById(R.id.btnGPSCollect);
		mBtnNodeCollect = (Button) findViewById(R.id.btnNodeCollect);

		mMIVCOpenMap = (MainImageViewControl) findViewById(R.id.mivcOpenMap);
		mMIVCOpenMap.setImageResource(R.drawable.selector_map);
		mMIVCOpenMap.setText(getResources()
				.getString(R.string.act_main_openMap));

		mMIVCLayerManage = (MainImageViewControl) findViewById(R.id.mivcLayerManage);
		mMIVCLayerManage.setImageResource(R.drawable.selector_layers);
		mMIVCLayerManage.setText(getResources().getString(
				R.string.act_main_layerManage));

		mMIVCDataCollect = (MainImageViewControl) findViewById(R.id.mivcDataCollect);
		mMIVCDataCollect.setImageResource(R.drawable.selector_database);
		mMIVCDataCollect.setText(getResources().getString(
				R.string.act_main_dataCollect));

		mMIVCMapQuare = (MainImageViewControl) findViewById(R.id.mivcMapQuery);
		mMIVCMapQuare.setImageResource(R.drawable.selector_search);
		mMIVCMapQuare.setText(getResources().getString(
				R.string.act_main_mapQuery));

		mMIVCNewMap = (MainImageViewControl) findViewById(R.id.mivcNewMap);
		mMIVCNewMap.setImageResource(R.drawable.selector_map);
		mMIVCNewMap.setText(getResources().getString(R.string.act_main_newMap));

		mMIVCNewLayer = (MainImageViewControl) findViewById(R.id.MainImageViewControl03);
		mMIVCNewLayer.setImageResource(R.drawable.selector_layers);
		mMIVCNewLayer.setText(getResources().getString(
				R.string.act_layer_newLayer));

		mMIVCAddLayer = (MainImageViewControl) findViewById(R.id.MainImageViewControl02);
		mMIVCAddLayer.setImageResource(R.drawable.selector_add_layer);
		mMIVCAddLayer.setText(getResources().getString(
				R.string.act_layer_addLayer));

		mMIVCRemoveLayer = (MainImageViewControl) findViewById(R.id.MainImageViewControl01);
		mMIVCRemoveLayer.setImageResource(R.drawable.selector_layers_clear);
		mMIVCRemoveLayer.setText(getResources().getString(
				R.string.act_layer_delLayer));

		mBtnNewMap.setOnClickListener(mClickListener);
		mBtnAdd.setOnClickListener(mClickListener);
		mBtnMinus.setOnClickListener(mClickListener);
		mBtnLocation.setOnClickListener(mClickListener);
		mBtnMapInfo.setOnClickListener(mClickListener);
		mBtnSelectLayer.setOnClickListener(mClickListener);

		mBtnQueryCurLayer.setOnClickListener(mClickListener);
		mBtnQueryAll.setOnClickListener(mClickListener);
		mBtnManualCollect.setOnClickListener(mClickListener);
		mBtnGPSCollect.setOnClickListener(mClickListener);
		mBtnNodeCollect.setOnClickListener(mClickListener);

		mMIVCOpenMap.setOnClickListener(mClickListener);
		mMIVCLayerManage.setOnClickListener(mClickListener);
		mMIVCDataCollect.setOnClickListener(mClickListener);
		mMIVCMapQuare.setOnClickListener(mClickListener);
		mMIVCNewMap.setOnClickListener(mClickListener);
		mMIVCNewLayer.setOnClickListener(mClickListener);
		mMIVCRemoveLayer.setOnClickListener(mClickListener);
		mMIVCAddLayer.setOnClickListener(mClickListener);

		hideTool();
	}

	/**
	 * 初始化地图界面
	 */
	private void initMapView() {
		mMapView = (OV_MapView) findViewById(R.id.mapview);
		OV_Widget compass = mMapView.addWidget(Widget.Compass, Widget.POS_RIGHT
				| Widget.POS_TOP);
		compass.setZoomScale(2);
		int sreenWidth = getWindowManager().getDefaultDisplay().getWidth();
		compass.setPosition(sreenWidth - 88, 192);
		mMapView.addWidget(Widget.Scale, Widget.POS_LEFT | Widget.POS_CENTER_H);
		mMapView.getGisToolManager().setNotify(mMapToolNotify);
	}

	private void initData() {

		String path = FileLayout.getMapPath();

		mMapEntity = MapEntity.getInstance();
		int ret = mMapEntity.openMap(path);
		if (ret < 1) {
			Toast.makeText(this, R.string.act_main_mapFault, Toast.LENGTH_SHORT)
					.show();
		} else {
			mMapView.setMap(mMapEntity.getMap());

			File f = new File(FileLayout.getAppMain() + "/HiMap Symbols.ttf");
			if (!f.exists())
				System.out.println("?????ttf");
			mMapView.setFontFile(FileLayout.getAppMain(), "HiMap Symbols.ttf");

			if (mMapEntity.getTranslator() != null) {
				mGpsOveryLay = new GpsOveryLay(mMapView,
						mMapEntity.getTranslator());
				mMapView.addOverlay(mGpsOveryLay);
			}
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		FileLayout.saveFileLayout();
		int ret = mMapEntity.openMap(FileLayout.getMapPath());
		if (ret < 1) {
			Toast.makeText(this, R.string.act_main_mapFault, Toast.LENGTH_SHORT)
					.show();
		} else {
			mMapView.setMap(mMapEntity.getMap());
		}

		mMapView.setFontFile(FileLayout.getAppMain(), "HiMap Symbols.ttf");
	}

	/**
	 * 界面按钮事件
	 */
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
				case R.id.btnSelectLayer:

					selectLayer();
					break;

				case R.id.btnMapInfo:

					showMapInfoDialog();
					break;
				case R.id.mivcNewMap:

					createMap();
					hideTool();
					break;

				case R.id.mivcOpenMap:
					openMap();
					hideTool();
					break;

				case R.id.mivcDataCollect:

					hideTool();
					mLLDataCollect.setVisibility(View.VISIBLE);
					break;

				case R.id.mivcMapQuery:

					hideTool();
					mLLMapQuery.setVisibility(View.VISIBLE);
					break;

				case R.id.btnAdd:

					zoomIn();
					break;

				case R.id.btnMinus:

					zoomOut();
					break;

				case R.id.btnLocation:

					location();
					break;

				case R.id.MainImageViewControl03:

					showNewLayerDialog();
					hideTool();
					break;

				case R.id.MainImageViewControl01:

					removeLayer();
					hideTool();
					break;

				case R.id.MainImageViewControl02:

					addLayer();
					hideTool();
					break;

				case R.id.btnQueryCurLayer:
					queryCurrentLayer();
					break;

				case R.id.btnQueryAll:
					queryAll();
					break;

				case R.id.btnManualCollect:
					collectByHand();
					break;

				case R.id.btnGPSCollect:
					collectByGps();
					break;

				case R.id.btnNodeCollect:
					collectByNode();
					break;

				default:
					break;
			}
		}
	};

	protected void addLayer() {

		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return;
		}
		Intent intent = new Intent(getBaseContext(), FileDialog.class);
		intent.putExtra(FileDialog.START_PATH, FileLayout.getAppMain());
		intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "ed2", "edt",
				"eds" });
		startActivityForResult(intent, FILEDIALOG_ADD_LAYER);

	}

	@Override
	public synchronized void onActivityResult(final int requestCode,
											  int resultCode, final Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		if (requestCode == FILEDIALOG_ADD_LAYER) {
			String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
			int ret = mMapEntity.addLayer(filePath);
			if (ret > 0) {
				int pox = filePath.lastIndexOf("/");
				String fileName = filePath.substring(pox + 1);
				IELayer l = mMapEntity.getMap().GetLayerByName(fileName, false);// 取出刚才添加的图层
				if (l == null) {
					return;
				}
				f64Rect rect = new f64Rect();
				l.GetLayerRange(rect);// 获取图层的范围
				mMapView.getMapView().SetViewRange(rect);// 将地图范围设置为图层范围,方便图层显示
				mMapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_REDRAW, true);// 刷新地图
			} else {
				Toast.makeText(this, R.string.act_main_addLayerError,
						Toast.LENGTH_SHORT).show();
			}
		}
	}


	/**
	 * 移除图层
	 */
	private void removeLayer() {
		// TODO Auto-generated method stub
		final IEMap map = mMapView.getMap();
		if (map == null || !map.IsOpend()) {
			return;
		}
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
						// TODO Auto-generated method stub
						String name = layerNames[which];
						mMapEntity.removeLayer(name);
						mMapView.SetAction(
								EMAP_ACTION.EMAP_ACT_UPDATE_NO_REDRAW, true);
						dialog.dismiss();
					}
				});

		builder.setNegativeButton(R.string.cmm_dlg_cancel, null);
		builder.show();
	}

	protected void createMap() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.cmm_dlg_name);
		final EditText et = new EditText(this);
		builder.setView(et);
		builder.setPositiveButton(R.string.cmm_dlg_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String name = et.getText().toString();
						if (name == null || name.length() == 0) {
							return;
						}
						IEMap map = newMap(name);
						if (map == null) {
							return;
						}
						mMapView.setMap(map);
						mMapView.setFontFile(FileLayout.getAppMain(),
								"HiMap Symbols.ttf");
					}
				});
		builder.show();

	}

	/**
	 * 新建地图
	 *
	 * @param mapName
	 *            地图名称
	 * @return
	 */
	private IEMap newMap(String mapName) {
		IEMap map = mMapView.getMap();
		if (map.IsOpend()) {
			mMapView.CloseMap();
		}

		String path = FileLayout.getAppMain();
		int ret = mMapEntity.createMap(path, mapName,
				CommonHelper.getPJTranslator());
		if (ret > 0) {
			return mMapEntity.getMap();
		}
		return null;
	}

	/**
	 * 打开地图
	 */
	@SuppressWarnings("deprecation")
	private void openMap() {
//		showDialog(OpenMapId);
		new Thread(networkTask).start();

	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("value");
			Log.i("mylog", "请求结果为-->" + val);
			// TODO
			// UI界面的更新等相关操作
		}
	};


	/**
	 * 网络操作相关的子线程
	 */
	Runnable networkTask = new Runnable() {

		@Override
		public void run() {
			// TODO
			// 在这里进行 http request.网络请求相关操作
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("value", "请求结果");
			Map<String, String> parameters = new HashMap<String, String>();
			String clientId = "3";
			String timestamp = String
					.valueOf(System.currentTimeMillis() / 1000);
			String noise = "100001";
			String signature = Test.encryptSHA1(String.valueOf(System
					.currentTimeMillis() / 1000)
					+ "100001"
					+ "64c918dbc29643ed8c5829a11c6c24cd");
			parameters.put("clientId", "3");
			parameters.put("timestamp", timestamp);
			parameters.put("noise", noise);
			parameters.put("signature", signature);

//			parameters.put("formId",
//					"1");
			String params = "";
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			for (int i = 0; i < 100000; i++) {
				sb.append("{\"id\":\"19\",\"the_geom\":\"POINT(86.6429901123047 42.1901065018226)\"},");

			}
			params = sb.substring(0, sb.length() - 1);
			params += ("]");
			System.out.println(sb.toString());
			String result = Test
					.sendGet(
							"http://192.168.12.96:8080/front/dynamicform.shtml?act=dispatch&apiName=attr_table:selectformid",
							parameters);
//			String result = Test
//					.sendPost(
//							String.format(
//									"http://192.168.12.96:8080/front/dynamicform.shtml?act=dispatch&apiName=test_form2:batchInsert:post&clientId=%s&timestamp=%s&noise=%s&signature=%s",
//									clientId, timestamp, noise, signature),
//							params);
			System.out.println(result);

			msg.setData(data);
			handler.sendMessage(msg);
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == OpenMapId) {
			Map<String, Integer> images = new HashMap<String, Integer>();
			images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
			images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
			images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
			images.put("img", R.drawable.filedialog_file);
			images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
			Dialog dialog = OpenFileDialog.createDialog(id, this, "??",
					new CallbackBundle() {
						@Override
						public void callback(Bundle bundle) {
							String filepath = bundle.getString("path");
							Log.i("Update", filepath);

							IEMap map = mMapView.getMap();
							if (map.IsOpend()) {
								mMapView.CloseMap();
							}

							if (filepath == null) {
								return;
							}

							int ret = mMapEntity.openMap(filepath);
							if (ret > 0) {
								mMapView.setMap(mMapEntity.getMap());
								mMapView.setFontFile(FileLayout.getAppMain(),
										"HiMap Symbols.ttf");
								FileLayout.setMAP_PATH(filepath);
							}
						}
					}, ".map;", images, FileLayout.getMapPath());
			return dialog;
		}
		return null;
	}

	private void zoomIn() {
		// TODO Auto-generated method stub
		mMapView.SetAction(EMAP_ACTION.EMAP_ACT_ZOOMIN, true);
	}

	private void zoomOut() {
		// TODO Auto-generated method stub
		mMapView.SetAction(EMAP_ACTION.EMAP_ACT_ZOOMOUT, true);
	}

	private void location() {
		// TODO Auto-generated method stub
		if (mGpsOveryLay != null) {
			mGpsOveryLay.location();
		}
	}

	/**
	 * 显示缩放到图层的对话框
	 */
	private void showToLayerDialog() {
		// TODO Auto-generated method stub
		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return;
		}
		final IEMap map = mMapEntity.getMap();
		final String[] layerNames = new String[map.GetLayerNum()];
		for (int i = 0; i < layerNames.length; i++) {
			layerNames[i] = map.GetLayerByNo(i).GetLayerName();
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.act_main_selectLayer);
		builder.setSingleChoiceItems(layerNames, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						String name = layerNames[which];
						IELayer layer = map.GetLayerByName(name, false);
						if (layer == null) {
							return;
						}

						if (mTvCurLayer != null)
							mTvCurLayer.setText(name.split("\\.")[0]);
						f64Rect rect = FileLayout.resetLayer(layer);
						if (rect != null) {
							mMapView.getMapView().SetViewRange(rect);// 地图显示范围设置为图层的包围盒
							mMapView.SetAction(
									EMAP_ACTION.EMAP_ACT_UPDATE_REDRAW, true);
						} else
							Toast.makeText(mContext, "图层选择错误！", Toast.LENGTH_LONG);
					}

				});

		builder.setNegativeButton(android.R.string.cancel, null);
		builder.show();
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
							mMapView.getMapView().SetViewRange(rect);// ????????��??????????��??
							mMapView.SetAction(
									EMAP_ACTION.EMAP_ACT_UPDATE_REDRAW, true);
						} else
							Toast.makeText(mContext,  "图层选择错误！",  Toast.LENGTH_LONG);

						dialog.dismiss();
					}
				});
		builder.setNegativeButton(R.string.cmm_dlg_cancel, null);
		builder.show();
	}

	/**
	 * 显示地图信息
	 */
	private void showMapInfoDialog() {
		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return;
		}
		ScrollView scroll = new ScrollView(this);
		TextView tv = new TextView(this);
		tv.setBackgroundColor(Color.WHITE);
		tv.setTextColor(Color.BLACK);
		tv.setTextSize(18);
		tv.setText(getMapInfo());
		scroll.addView(tv);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.act_main_mapInfo);
		builder.setView(scroll);
		builder.setNegativeButton(R.string.cmm_dlg_ok, null);
		builder.show();
	}

	/**
	 * 获取地图信息
	 *
	 * @return
	 */
	private String getMapInfo() {
		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return null;
		}
		IEMap map = mMapEntity.getMap();
		String temp = String.format("地图名称：%s\n", map.GetMapName());
		StringBuilder sb = new StringBuilder();
		sb.append(temp);
		Spatial_Ref map_ref = new Spatial_Ref();
		map.GetSpatialRef(map_ref);
		sb.append("----------------------------------\n");
		sb.append("空间参考\n");
		temp = getResources().getStringArray(R.array.act_main_earth_type)[map_ref
				.earthType()];
		temp = String.format("椭球类型：%s\n", temp);
		sb.append(temp);
		if (map_ref.coorSystem() == E_COOR_SYSTEM_TYPE.E_COOR_SYSTEM_TYPE_GEO
				.toInt()) {
			temp = "坐标系统：大地坐标系\n";
		} else {
			temp = "坐标系统：平面坐标系\n";
		}
		sb.append(temp);
		sb.append(String.format("中央子午线：%1.0f\n",
				CommonHelper.toAngle(map_ref.Lo())));
		sb.append("----------------------------------\n");
		sb.append("图层信息\n");
		IELayer layer;
		IELayerVectorEd2 vectorEd2;
		IEFeatureClassVectorEd2 featureClass;
		IETable table;
		IEField field;
		for (int i = 0, size = map.GetLayerNum(); i < size; i++) {
			layer = map.GetLayerByNo(i);
			sb.append(String.format("图层名：%s", layer.GetLayerName()) + "\n");
			if (layer.GetLayerType() == E_FILE_TYPE.E_FILE_TYPE_RASTER) {
				sb.append("图层类型：栅格\n");
				continue;
			} else if (layer.GetLayerType() == E_FILE_TYPE.E_FILE_TYPE_VECTOR_EDS) {
				sb.append("图层类型：不可编辑矢量\n");
				continue;
			}
			sb.append("图层类型：可编辑矢量\n");
			vectorEd2 = layer.As(IELayerVectorEd2.class);
			featureClass = vectorEd2.GetFeatureClassEd2();
			table = featureClass.GetTable();
			int num = table.GetFieldNum();
			sb.append("图层字段：");
			for (int j = 0; j < num; j++) {
				field = table.GetField(j);
				sb.append(field.GetFieldName() + ",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\n\n");
		}
		return sb.toString();
	}

	/**
	 * 新建图层
	 */
	private void showNewLayerDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.cmm_new_layer);
		View view = getLayoutInflater().inflate(R.layout.dlg_main_new_layer,
				null);
		final Spinner sp_type = (Spinner) view.findViewById(R.id.sp_main_type);
		final EditText et_name = (EditText) view
				.findViewById(R.id.et_main_name);
		builder.setView(view);
		builder.setPositiveButton(R.string.cmm_dlg_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String name = et_name.getText().toString();
						if (name == null || name.length() == 0) {
							return;
						}
						int position = sp_type.getSelectedItemPosition();
						switch (position) {
							case 0:
								newLayer(name, E_GEO_TYPE.E_GEO_TYPE_PNT);
								break;
							case 1:
								newLayer(name, E_GEO_TYPE.E_GEO_TYPE_LIN);
								break;
							case 2:
								newLayer(name, E_GEO_TYPE.E_GEO_TYPE_REG);
								break;
							default:
								break;
						}
					}
				});
		builder.setNegativeButton(R.string.cmm_dlg_cancel, null);
		builder.show();
	}

	/**
	 * 新建图层
	 */
	private void newLayer(String name, E_GEO_TYPE geoType) {
		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return;
		}

		IEFieldSet fieldSet = new IEFieldSet();
		try {
			InputStream is = getAssets().open("LayerAttrTemplate.xml");
			LayerAttrParser parser = new LayerAttrParser(); // 创建SaxBookParser实例
			List<LayerAttrEntity> layerAttrs = parser.parse(is);  // 解析输入流
			for (LayerAttrEntity layerAttr : layerAttrs) {
				IEField field = new IEField();
				field.SetFieldName(layerAttr.getName());
				field.SetFieldType(layerAttr.getType());
				field.SetFieldDefaultValue(layerAttr.getDefaultValue());
				fieldSet.AddField(field);
			}
		} catch (Exception e) {
			Log.e("Xmap", e.getMessage());
		}
		mMapEntity.createLayer(name, geoType, fieldSet);
	}

	/**
	 * 保存地图
	 */
	private void saveMap() {
		// TODO Auto-generated method stub
		IEMap map = mMapView.getMap();
		if (map.IsOpend()) {
			map.Save();
		}
	}

	/**
	 * 查询当前图层
	 */
	private void queryCurrentLayer() {
		if (mMapView.GetCurEditLayer() == null) {// ????????????
			showToast(R.string.act_toast_select_layer);
			return;
		}
		;

		mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_HIT_SELECT);// EMAP_TOOL_HIT_SELECT?????//EMAP_TOOL_RECT_SELECT???
	}

	/**
	 * 跨图层查询
	 */
	private void queryAll() {
		mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_RECT_SELECT_EX);// ??????????

	}

	/**
	 * 跳转属性查询界面
	 */
	private void startAttributeActivity() {
		Intent intent = new Intent(mContext, AttributeActivity.class);
		mContext.startActivity(intent);
	}

	/****************************** 数据采集部分 *****************************************/

	/**
	 * 初始化GPS
	 */
	private void initGps() {
		m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 0, m_locationListener);
	}

	private LocationListener m_locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			m_Location = location;
		}
	};

	@Override
	protected void onDestroy() {

		if (m_locationManager != null) {
			m_locationManager.removeUpdates(m_locationListener);
			m_locationManager = null;
		}
		if (mMapView != null) {
			mMapView.CloseMap();
			mMapView = null;
		}
		super.onDestroy();
	}

	/**
	 * 手绘采集
	 */
	private void collectByHand() {
		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return;
		}
		IELayerVectorEd2 editLayer = mMapView.GetCurEditLayer();// 获取当前图层
		if (editLayer == null) {
			showToast(R.string.act_toast_select_layer);
			return;
		}
		switch (editLayer.GetLayerGeoType()) {// 根据图层类型设置采集工具
			case E_GEO_TYPE_LIN:
				this.mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_MAKE_LIN);// 手绘采集线地物
				break;
			case E_GEO_TYPE_PNT:
				this.mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_MAKE_PNT);// 手绘采集点地物
				break;
			case E_GEO_TYPE_REG:
				this.mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_MAKE_REG);// 手绘采集面地物
				break;
			default:
				break;
		}
	}

	/**
	 * Gps采集
	 */
	private void collectByGps() {
		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return;
		}
		if (m_Location == null) {// 判断当前GPS是否存在
			return;
		}
		IELayerVectorEd2 editLayer = mMapView.GetCurEditLayer();// 获取编辑图层
		if (editLayer == null) {
			showToast(R.string.act_toast_select_layer);
			return;
		}
		switch (editLayer.GetLayerGeoType()) {
			case E_GEO_TYPE_LIN:
				mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_GPS_MAKE_LIN);// 设置GPS采集线
				break;
			case E_GEO_TYPE_PNT:
				this.mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_GPS_MAKE_PNT);// 设置GPS采集点
				break;
			case E_GEO_TYPE_REG:
				this.mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_GPS_MAKE_REG);// 设置GPS采集面
			default:
				break;
		}
		f643Point[] pts = new f643Point[1];
		pts[0] = new f643Point();
		boolean ret = CommonHelper.latlonaltToMap(m_Location.getLatitude(),
				m_Location.getLongitude(), m_Location.getAltitude(), pts[0],
				mMapEntity);// 将经纬度坐标转换地图坐标
		if (!ret) {
			Toast.makeText(this, R.string.act_collect_translator_fault,
					Toast.LENGTH_SHORT).show();
			return;
		}
		this.mMapView.getMapView().SetCenterD(pts[0].x(), pts[0].y());// GPS 居中
		this.mMapView.getGisTool().AddPoints(pts);// 将转换后的坐标填充到工具中，用于显示
		mMapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_REDRAW, true);// 刷新地图显示
	}

	/**
	 * 节点捕获采集
	 */
	private void collectByNode() {
		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return;
		}
		IELayerVectorEd2 editLayer = mMapView.GetCurEditLayer();// 获取编辑图层
		if (editLayer == null) {
			showToast(R.string.act_toast_select_layer);
			return;
		}
		switch (editLayer.GetLayerGeoType()) {
			case E_GEO_TYPE_LIN:
				mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_GPS_MAKE_LIN);//设置外部添加方式采集线
				break;
			case E_GEO_TYPE_PNT:
				mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_GPS_MAKE_PNT);// 设置外部添加方式采集点
				break;
			case E_GEO_TYPE_REG:
				mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_GPS_MAKE_REG);// 设置外部添加方式采集面
			default:
				break;
		}
		mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_SNAP_NODES_RECT);// 设置节点捕获工具，捕获工具不与采集工具冲突
		mMapView.getGisToolManager().setNotify(mMapToolNotify);// 设置监听捕获的监听器
	}

	/**
	 * 采集确认
	 */
	private void collectConfirm() {
		if (!isAcceptConfirm()) {

			return;
		}
		IELayerVectorEd2 editLayer = mMapView.GetCurEditLayer();
		final IEFeatureClassVectorEd2 featureClass = editLayer
				.GetFeatureClassEd2();
		IETable table = featureClass.GetTable();

		final FormCreater creater = new FormCreater(getApplicationContext(),
				table);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.cmm_dlg_attr);
		builder.setView(creater.getForm());
		builder.setNegativeButton(R.string.cmm_dlg_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						IERow row = creater.getRow();
						mMapView.getGisTool().SetRow(row);
						int areaID = mMapView.ConfirmTool();
						mMapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_REDRAW,
								true);// 刷新地图显示
						double area;
						area = FileLayout.acculateArea(featureClass, areaID);
						initMivcBackground();
					}
				});
		builder.show();
	}


	/**
	 * 是否允许提交数据
	 */
	private boolean isAcceptConfirm() {
		OV_GisTool gisTool = this.mMapView.getGisTool();
		if (gisTool == null) {// 是否设置过采集工具
			return false;
		}
		IELayerVectorEd2 editLayer = mMapView.GetCurEditLayer();// 获取编辑图层
		if (editLayer == null) {
			return false;
		}
		switch (editLayer.GetLayerGeoType()) {
			case E_GEO_TYPE_PNT:
				return gisTool.GetPointsNum() > 0;// 提交点，至少存在一个点
			case E_GEO_TYPE_LIN:
				return gisTool.GetPointsNum() > 1;// 提交线，至少存在两个点
			case E_GEO_TYPE_REG:
				return gisTool.GetPointsNum() > 2;// 提交面，至少存在三个点
			default:
				break;
		}
		return false;
	}

	/**
	 * 取消采集
	 */
	private void cancel() {
		OV_GisTool ovGistool = mMapView.getGisTool();
		if (ovGistool == null)
			finish();
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.cmm_notice);
			builder.setTitle(R.string.cmm_cancel_collect);
			builder.setPositiveButton(R.string.cmm_dlg_ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							initMivcBackground();
							collectCancel();
						}
					});
			builder.setNegativeButton(R.string.cmm_dlg_cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			builder.show();
		}
	}

	/**
	 * 取消采集
	 */
	private void collectCancel() {
		mMapView.CancelTool();// ????
		mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_NULL);// ???��????????null
	}

	/**
	 * 撤销
	 */
	private void collectUndo() {
		OV_GisTool gisTool = mMapView.getGisTool();
		if (gisTool == null) {
			return;
		}
		int nodesNum = mMapView.getGisToolManager().GetSnapNodesCount();
		int num = gisTool.GetPointsNum();// ????�ʦ�????????????
		if (num < 1 && nodesNum < 1) {
			return;
		}
		mMapView.getGisToolManager().Undo();// ???????
		mMapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_NO_REDRAW, true);// ????????
	}

	/**
	 * 重做
	 */
	private void collectRedo() {
		OV_GisTool gisTool = mMapView.getGisTool();
		if (gisTool == null) {
			return;
		}
		mMapView.getGisToolManager().Redo();// ???????
		mMapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_NO_REDRAW, true);// ????????
	}

	private void initMivcBackground() {
		try {
			mBtnGPSCollect.setBackgroundColor(Color.parseColor("#00FFFFFF"));
			mBtnManualCollect.setBackgroundColor(Color.parseColor("#00FFFFFF"));
			mBtnNodeCollect.setBackgroundColor(Color.parseColor("#00FFFFFF"));
		} catch (Exception e) {

		}
	}

	/***************************** ToolNotify ***************************************/
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
				case EditStatus_NodeSelect:// 判断是否是捕获工具
					f643Point[] points = mMapView.getGisToolManager()
							.GetSnapNodes();// 获取捕获点
					if (points == null) {
						return;
					}
					mMapView.getGisToolManager().getCurGisTool().AddPoints(points);// 将捕获点设置到采集工具中
					mMapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_NO_REDRAW, true);// 刷新显示
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

			}
		}
	};

	/**
	 * 显示相应的提示
	 *
	 * @param resId
	 */
	private void showToast(int resId) {
		if (mToast == null) {
			mToast = Toast.makeText(mContext, resId, Toast.LENGTH_SHORT);
		}
		mToast.setText(resId);
		mToast.show();
	}

	private void hideTool() {
		mLLDataCollect.setVisibility(View.INVISIBLE);
		mLLMapQuery.setVisibility(View.INVISIBLE);
	}
}
