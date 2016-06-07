package com.xaircraft.xmap.activity;

import java.io.InputStream;
import java.util.List;

import com.xaircraft.xmap.R;
import com.xaircraft.xmap.component.FileDialog;
import com.xaircraft.xmap.component.MainImageViewControl;
import com.xaircraft.xmap.entity.GpsOveryLay;
import com.xaircraft.xmap.entity.LayerAttrEntity;
import com.xaircraft.xmap.entity.MapEntity;
import com.xaircraft.xmap.helper.FileLayout;
import com.xaircraft.xmap.parsers.LayerAttrParser;

import EMap.IO_Base.IEStruct.f64Rect;
import EMap.IO_GisDB.IEField;
import EMap.IO_GisDB.IEFieldSet;
import EMap.IO_GisDB.IEGisDB_Base.E_GEO_TYPE;
import EMap.IO_Map.IELayer;
import EMap.IO_Map.IEMap;
import EMap.IO_MapView.IEMapView_Base.EMAP_ACTION;
import EMap.OV_MapView.OV_MapView;
import EMap.OV_MapView.OV_Widget;
import EMap.OV_MapView.OV_Widget.Widget;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ActivityLayerManage extends Activity {

	private static final int FILEDIALOG_ADD_LAYER = 1;

	private OV_MapView mMapView;

	private Button mBtnSaveMap;
	private Button mBtnBack;

	private Button mBtnAdd;
	private Button mBtnMinus;
	private Button mBtnLocation;

	private MainImageViewControl mMIVCNewLayer;
	private MainImageViewControl mMIVCAddLayer;
	private MainImageViewControl mMIVCRemoveLayer;

	private MapEntity mMapEntity;
	private GpsOveryLay mGpsOveryLay;
	private Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_layer_manage);
		initView();
		initMapView();
		initData();
	}

	private void initView() {
		mBtnSaveMap = (Button) findViewById(R.id.btnSave);
		mBtnAdd = (Button) findViewById(R.id.btnAdd);
		mBtnMinus = (Button) findViewById(R.id.btnMinus);
		mBtnLocation = (Button) findViewById(R.id.btnLocation);
		mBtnBack = (Button) findViewById(R.id.btnBack);

		mMIVCNewLayer = (MainImageViewControl) findViewById(R.id.mivcNewLayer);
		mMIVCNewLayer.setImageResource(R.drawable.selector_layers);
		mMIVCNewLayer.setText(getResources().getString(
				R.string.act_layerManage_NewLayer));

		mMIVCAddLayer = (MainImageViewControl) findViewById(R.id.mivcAddLayer);
		mMIVCAddLayer.setImageResource(R.drawable.selector_add_layer);
		mMIVCAddLayer.setText(getResources().getString(
				R.string.act_layerManage_AddLayer));

		mMIVCRemoveLayer = (MainImageViewControl) findViewById(R.id.mivcRemoveLayer);
		mMIVCRemoveLayer.setImageResource(R.drawable.selector_layers_clear);
		mMIVCRemoveLayer.setText(getResources().getString(
				R.string.act_layerManage_RemoveLayer));

		mBtnSaveMap.setOnClickListener(mClickListener);
		mBtnBack.setOnClickListener(mClickListener);

		mMIVCNewLayer.setOnClickListener(mClickListener);
		mMIVCAddLayer.setOnClickListener(mClickListener);
		mMIVCRemoveLayer.setOnClickListener(mClickListener);

		mBtnAdd.setOnClickListener(mClickListener);
		mBtnMinus.setOnClickListener(mClickListener);
		mBtnLocation.setOnClickListener(mClickListener);
	}

	/**
	 * 初始化地图控件
	 */
	private void initMapView() {

		mMapView = (OV_MapView) findViewById(R.id.mapview);
		OV_Widget compass = mMapView.addWidget(Widget.Compass, Widget.POS_RIGHT
				| Widget.POS_TOP);
		compass.setZoomScale(2);
		int sreenWidth = getWindowManager().getDefaultDisplay().getWidth();
		compass.setPosition(sreenWidth - 88, 192);
		mMapView.addWidget(Widget.Scale, Widget.POS_LEFT | Widget.POS_CENTER_H);
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

		if (mMapEntity.getTranslator() != null) {
			mGpsOveryLay = new GpsOveryLay(mMapView, mMapEntity.getTranslator());
			mMapView.addOverlay(mGpsOveryLay);
		}

	}

	private View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {

			case R.id.mivcNewLayer:

				showNewLayerDialog();
				break;

			case R.id.btnSave:

				saveMap();
				break;

			case R.id.mivcAddLayer:

				addLayer();
				break;

			case R.id.mivcRemoveLayer:

				removeLayer();
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

			case R.id.btnBack:

				finish();
				break;

			default:
				break;
			}
		}
	};

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
	 * 添加图层
	 */
	private void addLayer() {
		// TODO Auto-generated method stub
		if (mMapEntity == null || !mMapEntity.isOpen()) {
			return;
		}
		Intent intent = new Intent(getBaseContext(), FileDialog.class);
		intent.putExtra(FileDialog.START_PATH, FileLayout.getAppMain());
		intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "ed2", "edt",
				"eds" });
		startActivityForResult(intent, FILEDIALOG_ADD_LAYER);
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
            LayerAttrParser parser = new LayerAttrParser();  //创建SaxBookParser实例  
            List<LayerAttrEntity> layerAttrs = parser.parse(is);  //解析输入流  
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
}
