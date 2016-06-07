package com.xaircraft.xmap.activity;


import com.xaircraft.xmap.R;
import com.xaircraft.xmap.component.FormCreater;
import com.xaircraft.xmap.component.MainImageViewControl;
import com.xaircraft.xmap.entity.GpsOveryLay;
import com.xaircraft.xmap.entity.MapEntity;
import com.xaircraft.xmap.helper.CommonHelper;
import com.xaircraft.xmap.helper.FileLayout;

import EMap.IO_Base.IEStruct.f643Point;
import EMap.IO_Base.IEStruct.f64Rect;
import EMap.IO_GisDB.IEFeatureClassVectorEd2;
import EMap.IO_GisDB.IERow;
import EMap.IO_GisDB.IETable;
import EMap.IO_Map.IELayer;
import EMap.IO_Map.IELayerVectorEd2;
import EMap.IO_Map.IEMap;
import EMap.IO_MapView.IEMapView_Base.EMAP_ACTION;
import EMap.IO_MapView.IEMapView_Base.EMAP_TOOL;
import EMap.OV_MapView.OV_GisTool;
import EMap.OV_MapView.OV_MapView;
import EMap.OV_MapView.OV_Widget;
import EMap.OV_MapView.OV_GisTool.ToolNotify;
import EMap.OV_MapView.OV_Widget.Widget;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityDataCollect extends Activity {

	private MainImageViewControl mMivcManualCollect;
	private MainImageViewControl mMivcGPSCollect;
	private MainImageViewControl mMivcNodeCollect;

	private Button mBtnSelectLayer;

	private Button mBtnSave;
	private Button mBtnCancel;
	private Button mBtnUndo;
	private Button mBtnRedo;
	private Button mBtnAdd;
	private Button mBtnMinus;
	private Button mBtnLocation;

	private TextView mTvCurLayer;
	private OV_MapView mMapView;
	private MapEntity mMapEntity;

	private LocationManager m_locationManager;
	private Location m_Location;

	private GpsOveryLay mGpsOveryLay;
	protected Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acitivity_data_collect);
		initView();
		initMapView();
		initData();
		initGps();
	}

	private void initView() {
		// TODO Auto-generated method stub
		mMivcManualCollect = (MainImageViewControl) findViewById(R.id.mivcManualCollect);
		mMivcManualCollect.setImageResource(R.drawable.ic_person_black_48dp);
		mMivcManualCollect.setText("手动采集");

		mMivcGPSCollect = (MainImageViewControl) findViewById(R.id.mivcGPSCollect);
		mMivcGPSCollect
				.setImageResource(R.drawable.ic_location_searching_black_48dp);
		mMivcGPSCollect.setText("GPS采集");

		mMivcNodeCollect = (MainImageViewControl) findViewById(R.id.mivcNodeCollect);
		mMivcNodeCollect.setImageResource(R.drawable.ic_share_black_48dp);
		mMivcNodeCollect.setText("节点捕捉");

		mBtnSave = (Button) findViewById(R.id.btnSave);
		mBtnCancel = (Button) findViewById(R.id.btnChevronLeft);
		mBtnUndo = (Button) findViewById(R.id.btnUndo);
		mBtnRedo = (Button) findViewById(R.id.btnRedo);
		mBtnSelectLayer = (Button) findViewById(R.id.btnSelectLayer);
		mBtnAdd = (Button) findViewById(R.id.btnAdd);
		mBtnMinus = (Button) findViewById(R.id.btnMinus);
		mBtnLocation = (Button) findViewById(R.id.btnLocation);

		mTvCurLayer = (TextView) findViewById(R.id.tvCurLayer);

		mMivcManualCollect.setOnClickListener(mClickListener);
		mMivcNodeCollect.setOnClickListener(mClickListener);
		mMivcGPSCollect.setOnClickListener(mClickListener);
		mBtnSave.setOnClickListener(mClickListener);
		mBtnCancel.setOnClickListener(mClickListener);
		mBtnUndo.setOnClickListener(mClickListener);
		mBtnRedo.setOnClickListener(mClickListener);
		mBtnSelectLayer.setOnClickListener(mClickListener);
		mBtnAdd.setOnClickListener(mClickListener);
		mBtnMinus.setOnClickListener(mClickListener);
		mBtnLocation.setOnClickListener(mClickListener);

	}

	private void initMapView() {
		mMapView = (OV_MapView) findViewById(R.id.mapview);
		OV_Widget compass = mMapView.addWidget(Widget.Compass, Widget.POS_RIGHT
				| Widget.POS_TOP);
		compass.setZoomScale(2);
		int sreenWidth = getWindowManager().getDefaultDisplay().getWidth();
		compass.setPosition(sreenWidth - 88, 192);
		mMapView.addWidget(Widget.Scale, Widget.POS_LEFT | Widget.POS_CENTER_H);
	}

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
		if (mMapEntity.getTranslator() != null) {
			mGpsOveryLay = new GpsOveryLay(mMapView, mMapEntity.getTranslator());
			mMapView.addOverlay(mGpsOveryLay);
		}

	}

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

	private View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btnSelectLayer:
				selectLayer();
				break;
			case R.id.mivcManualCollect:

				collectByHand();
				initMivcBackground();
				mMivcManualCollect.setBackgroundColor(Color
						.parseColor("#444444"));
				break;

			case R.id.mivcGPSCollect:

				collectByGps();
				initMivcBackground();
				mMivcGPSCollect.setBackgroundColor(Color.parseColor("#444444"));

				break;
			case R.id.mivcNodeCollect:

				collectByNode();
				initMivcBackground();
				mMivcNodeCollect
						.setBackgroundColor(Color.parseColor("#444444"));

				break;
			case R.id.btnSave:
				collectConfirm();
				break;
			case R.id.btnChevronLeft:
				cancel();
				break;
			case R.id.btnUndo:
				collectUndo();
				break;
			case R.id.btnRedo:
				collectRedo();
				break;
			case R.id.btnAdd:
				zoomOut();
				break;
			case R.id.btnMinus:
				zoomIn();
				break;
			case R.id.btnLocation:
				location();
				break;

			default:
				break;
			}
		}
	};

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
						// TODO Auto-generated method stub
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
	 * 手绘采集
	 */
	private void collectByHand() {
		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return;
		}
		IELayerVectorEd2 editLayer = mMapView.GetCurEditLayer();// 获取当前图层
		if (editLayer == null) {
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
			return;
		}
		switch (editLayer.GetLayerGeoType()) {
		case E_GEO_TYPE_LIN:
			mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_GPS_MAKE_LIN);// 设置外部添加方式采集线
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
		mMapView.getGisToolManager().setNotify(notify);// 设置监听捕获的监听器
	}

	private ToolNotify notify = new ToolNotify() {

		@Override
		public void onEditStatusChange(OV_GisTool gisTool, EditStatus status,
				ExtraData dataChanged) {
			// TODO Auto-generated method stub
			if (status == EditStatus.EditStatus_NodeSelect) {// 判断是否是捕获工具
				f643Point[] points = mMapView.getGisToolManager()
						.GetSnapNodes();// 获取捕获点
				if (points == null) {
					return;
				}
				mMapView.getGisToolManager().getCurGisTool().AddPoints(points);// 将捕获点设置到采集工具中
				mMapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_NO_REDRAW, true);// 刷新显示
			}
		}
	};

	/**
	 * 采集确认
	 */
	private void collectConfirm() {
		if (!isAcceptConfirm()) {

			return;
		}
		IELayerVectorEd2 editLayer = mMapView.GetCurEditLayer();
		final IEFeatureClassVectorEd2 featureClass = editLayer.GetFeatureClassEd2();
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
						int areaID=mMapView.ConfirmTool();
						mMapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_REDRAW,
								true);// 刷新地图显示
						double area;
						area= FileLayout.acculateArea(featureClass,areaID);
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
		mMapView.CancelTool();// 取消工具
		mMapView.setGisTool(EMAP_TOOL.EMAP_TOOL_NULL);// 设置工具类型为null
	}

	/**
	 * 撤消
	 */
	private void collectUndo() {
		OV_GisTool gisTool = mMapView.getGisTool();
		if (gisTool == null) {
			return;
		}
		int nodesNum = mMapView.getGisToolManager().GetSnapNodesCount();
		int num = gisTool.GetPointsNum();// 没有任何采集，就不能撤消
		if (num < 1 && nodesNum < 1) {
			return;
		}
		mMapView.getGisToolManager().Undo();// 工具撤销
		mMapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_NO_REDRAW, true);// 刷新地图显示
	}

	/**
	 * 回退
	 */
	private void collectRedo() {
		OV_GisTool gisTool = mMapView.getGisTool();
		if (gisTool == null) {
			return;
		}
		mMapView.getGisToolManager().Redo();// 工具回退
		mMapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_NO_REDRAW, true);// 刷新地图显示
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

	private void initMivcBackground() {
		try {
			mMivcGPSCollect.setBackgroundColor(Color.parseColor("#00FFFFFF"));
			mMivcManualCollect
					.setBackgroundColor(Color.parseColor("#00FFFFFF"));
			mMivcNodeCollect.setBackgroundColor(Color.parseColor("#00FFFFFF"));
		} catch (Exception e) {

		}
	}
}
