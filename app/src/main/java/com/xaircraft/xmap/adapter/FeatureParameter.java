package com.xaircraft.xmap.adapter;

import EMap.IO_Base.IEStruct.Layer_FeatureSet;
import EMap.IO_Base.IEStruct.QueryRect_Layer_FeatureSet;
import EMap.IO_GisDB.IEFeatureSet;
import EMap.IO_Map.IELayerVector;

/**
 * 参数辅助类，辅助Activity间参数的传递
 * @author 中海达测绘仪器有限公司
 *
 */
public class FeatureParameter {
	private static FeatureParameter mParameter = null;
	
	private IEFeatureSet mFeatureSet = null;//单图层查询需要显示的要素集
	private IELayerVector[] mLayers = null;//跨图层查询需要显示的要素集
	private int[][] mOids = null;//所有要素的oid
	private String[] mLayerNames = null;//所有要素所在的所有图层名
	
	public static FeatureParameter getParameter(){
		if (mParameter == null) {
			mParameter = new FeatureParameter();
		}
		return mParameter;
	}
	
	/**
	 * 获取所有oid
	 * @return
	 */
	public int[][] getOids(){
		return mOids;
	}
	
	/**
	 * 获取所有图层名
	 * @return
	 */
	public String[] getLayerNames(){
		return mLayerNames;
	}
	
	/**
	 * 设置单图层查询的数据
	 * @param layerName 图层名
	 * @param featureSet 选中的要素集
	 */
	public void setFeatureSet(String layerName, IEFeatureSet featureSet){
		if (layerName == null || featureSet == null) {
			mLayerNames = null;
			mFeatureSet = null;
			mOids = null;
			return;
		}
		setQueryRect_Layer_FeatureSet(null);
		mLayerNames = new String[1];
		mLayerNames[0] = layerName;
		
		mFeatureSet = featureSet;
		int featureCount = featureSet.GetFeatureCount();
		mOids = new int[1][featureCount];
		featureSet.GetFeatureIDList(mOids[0]);
	}
	
	/**
	 * 获取单图层查询到的要素集
	 * @return
	 */
	public IEFeatureSet getFeatureSet(){
		return mFeatureSet;
	}
	
	/**
	 * 设置跨图层查询到的所有数据
	 * @param featureSet
	 */
	public void setQueryRect_Layer_FeatureSet(QueryRect_Layer_FeatureSet featureSet){
		if (featureSet == null) {
			mOids = null;
			mLayerNames = null;
			mLayers = null;
			return;
		}
		setFeatureSet(null, null);
		Layer_FeatureSet[] layer_FeatureSets = null;
		try {
			layer_FeatureSets = featureSet.getLayerQuery();
		} catch (OutOfMemoryError e) {
			//数据过大会内存溢出
		}
		if (layer_FeatureSets != null && layer_FeatureSets.length > 0) {
			int layerCount = layer_FeatureSets.length;
			mOids = new int[layerCount][];
			mLayerNames = new String[layerCount];
			mLayers = new IELayerVector[layerCount];
			for (int i = 0; i < layerCount; i++) {
				mLayers[i] = layer_FeatureSets[i].getLayer();
				mLayerNames[i] = mLayers[i].GetLayerName();
				IEFeatureSet tmpFeatureSet = layer_FeatureSets[i].getFeatureSet();
				int fetureCount = tmpFeatureSet.GetFeatureCount();
				mOids[i] = new int[fetureCount];
				tmpFeatureSet.GetFeatureIDList(mOids[i]);
			}
		}
	}
	
	/**
	 * 获取跨图层查询到的所有图层
	 * @return
	 */
	public IELayerVector[] getLayers(){
		return mLayers;
	}
	
	/**
	 * 重置所有参数
	 */
	public void reset(){
		mFeatureSet = null;
		mOids = null;
		mLayerNames = null;
		mLayers = null;
	}
	
}
