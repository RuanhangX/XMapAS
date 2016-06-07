package com.xaircraft.xmap.activity;


import com.xaircraft.xmap.R;
import com.xaircraft.xmap.adapter.FeatureContentAdapter;
import com.xaircraft.xmap.adapter.FeatureIdAdapter;
import com.xaircraft.xmap.adapter.FeatureParameter;

import EMap.IO_GisDB.IEFeature;
import EMap.IO_GisDB.IEFeatureSet;
import EMap.IO_Map.IELayerVector;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;

public class AttributeActivity extends Activity {

	private ExpandableListView mElv_mapQuery_showId;
	private ListView mLv_mapQuery_showAttr;
	private FeatureIdAdapter mFeatureIdAdapter;
	private FeatureContentAdapter mFeatureContentAdapter;
	
	private IEFeatureSet mFeatureSet = null;
	private IELayerVector[] mLayers = null;
	private int[][] mOids = null;
	private String[] mLayerNames = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attribute_showattr);
		
		mFeatureSet = FeatureParameter.getParameter().getFeatureSet();
		mLayers = FeatureParameter.getParameter().getLayers();
		mOids = FeatureParameter.getParameter().getOids();
		mLayerNames = FeatureParameter.getParameter().getLayerNames();
		
		initView();
	}
	
	private void initView(){
		mElv_mapQuery_showId = (ExpandableListView) findViewById(R.id.elv_mapQuery_showId);
		mFeatureIdAdapter = new FeatureIdAdapter(AttributeActivity.this, mLayerNames, mOids);
		mElv_mapQuery_showId.setAdapter(mFeatureIdAdapter);
		mElv_mapQuery_showId.setOnChildClickListener(mOnChildClickListener);
		
		mLv_mapQuery_showAttr = (ListView) findViewById(R.id.lv_mapQuery_showAttr);
		mFeatureContentAdapter = new FeatureContentAdapter(AttributeActivity.this.getLayoutInflater(), null);
		mLv_mapQuery_showAttr.setAdapter(mFeatureContentAdapter);
	}
	
	private OnChildClickListener mOnChildClickListener = new OnChildClickListener(){

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			if (mFeatureContentAdapter != null) {
				IEFeature feature = null;
				if (mFeatureSet != null) {
					feature = mFeatureSet.GetFeature(childPosition);
				} else if (mLayers != null && mLayers.length > 0) {
					int oid = mFeatureIdAdapter.getChild(groupPosition, childPosition);
					feature = mLayers[groupPosition].GetFeatureClass().GetFeature(oid);
				}
				mFeatureContentAdapter.setFeature(feature);
				mFeatureContentAdapter.notifyDataSetChanged();
			}
			return true;
		}
		
	};

	@Override
	protected void onDestroy() {
		FeatureParameter.getParameter().reset();
		super.onDestroy();
	}
}
