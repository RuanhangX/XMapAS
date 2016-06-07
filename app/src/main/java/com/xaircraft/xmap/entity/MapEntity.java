package com.xaircraft.xmap.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.xaircraft.xmap.helper.FileLayout;


import EMap.IO_Base.IEStruct.Spatial_Ref;
import EMap.IO_GisDB.IEFeatureClass;
import EMap.IO_GisDB.IEFeatureClassRasterEdt;
import EMap.IO_GisDB.IEFeatureClassVectorEd2;
import EMap.IO_GisDB.IEFieldSet;
import EMap.IO_GisDB.IEGisDB_Base.E_GEO_TYPE;
import EMap.IO_Gps.IEPJTranslator;
import EMap.IO_Map.IELayer;
import EMap.IO_Map.IELayerRasterEdt;
import EMap.IO_Map.IELayerVector;
import EMap.IO_Map.IELayerVectorEd2;
import EMap.IO_Map.IEMap;
import EMap.IO_Map.IEMap_Base.FILE_PARAM;

/**
 * 地图实体类，包含地图文档和与其配合的坐标转换对象， 并实现了地图文档本身具有的图层管理功能
 * 
 */
public class MapEntity {
	private IEMap m_map;
	private IEPJTranslator m_translator;
	private static final MapEntity mMapEntity=new MapEntity();

	private MapEntity() {
		
	}
	
	public static MapEntity getInstance(){
		
		return mMapEntity;
		
	}
	/**
	 * 获取地图文档
	 * 
	 * @return
	 */
	public IEMap getMap() {
		return m_map;
	}

	/**
	 * 设置地图文档
	 * 
	 * @param map
	 */
	public void setMap(IEMap map) {
		this.m_map = map;
	}
	
	public boolean isOpen(){
		if(m_map == null){
			return false;
		}
		return m_map.IsOpend();
	}

	/**
	 * 获取坐标转换对象
	 * 
	 * @return
	 */
	public IEPJTranslator getTranslator() {
		return m_translator;
	}

	/**
	 * 设置坐标转换对象
	 * 
	 * @param translator
	 */
	public void setTranslator(IEPJTranslator translator) {
		this.m_translator = translator;
	}

	/**
	 * 创建地图
	 * 
	 * @param path
	 *            地图创建的路径
	 * @param mapName
	 *            地图名称
	 * @param translator
	 *            转换参数
	 * @return 返回值：
	 *         <p>
	 *         -2表示传入参数中路径或者地图名称为null
	 *         </p>
	 *         <p>
	 *         -1表示已经存在与地图名称同名的文件夹
	 *         </p>
	 *         <p>
	 *         0表示创建地图文件失败
	 *         </p>
	 *         <p>
	 *         1表示坐标转换参数为null或者坐标转换参数写入文件失败
	 *         </p>
	 *         <p>
	 *         2表示创建地图完全成功
	 *         </p>
	 */
	public int createMap(String path, String mapName, IEPJTranslator translator) {
		if (m_map != null && m_map.IsOpend()) {
			m_map.CloseMap();
			m_map = null;
		}
		if (path == null || path.length() == 0 || mapName == null
				|| mapName.length() == 0) {
			return -2;
		}
		// 创建地图目录
		File MapDir = new File(String.format("%s/%s", path, mapName));
		if (MapDir.exists()) {
			return -1;
		}
		MapDir.mkdirs();
		String mapPath = String.format("%s/%s.map", MapDir.getAbsolutePath(), mapName);
		// 创建地图文件
		m_map = new IEMap();
		int ret = m_map.CreateMap(mapPath, FILE_PARAM.E_CREATE);
		if (ret == 0) {
			return 0;
		}
		// 创建坐标系统文件
		if (translator == null) {
			return 1;
		}
		String refPath = String.format("%s/%s.ref", MapDir.getAbsolutePath(), mapName);
		boolean result = writeRefFile(translator, refPath);
		if (!result) {
			return 1;
		}
		// 将坐标系统中目标坐标系统设置到地图。一般情况下，是将其他坐标转换为地图坐标。
		Spatial_Ref ref = new Spatial_Ref();
		translator.GetDstSpatialRef(ref);
		m_map.SetSpatialRef(ref);
		m_map.Save();// 保存地图文件
		return 2;
	}

	/**
	 * 将坐标转换数据写为文件
	 * 
	 * @param translator
	 *            坐标转换对象
	 * @param path
	 *            文件路径
	 * @return
	 */
	private boolean writeRefFile(IEPJTranslator translator, String path) {
		try {
			File refFile = new File(path);
			if (refFile.exists()) {
				refFile.delete();
			}
			refFile.createNewFile();
			FileOutputStream os = new FileOutputStream(refFile);
			os.write(translator.GetData());
			os.flush();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 从Ref文件中读取坐标转换参数
	 * 
	 * @param path
	 *            ref路径
	 * @return
	 */
	private IEPJTranslator readRefFile(String path) {
		try {
			File refFile = new File(path);
			FileInputStream is = new FileInputStream(refFile);
			byte[] Tempdata = new byte[(int) refFile.length()];
			is.read(Tempdata);
			is.close();
			IEPJTranslator translator = new IEPJTranslator();
			translator.SetData(Tempdata);
			return translator;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 打开地图
	 * 
	 * @param path
	 *            地图路径
	 * @return 返回值：
	 * 			<p>
	 *         -1 表示传入路径为null
	 *         </p>
	 *         <p>
	 *         0 表示打开地图文档失败
	 *         </p>
	 *         <p>
	 *         1表示打开地图成功，但打开坐标转换文件失败
	 *         </p>
	 *         <p>
	 *         2表示打开地图成功，打开坐标转换文件成功
	 *         </p>
	 */
	public int openMap(String path) {
		if (m_map != null && m_map.IsOpend()) {
			m_map.CloseMap();
			m_map = null;
		}
		if(path == null){
			return -1;
		}
		m_map = new IEMap();
		int ret = m_map.OpenMap(path, FILE_PARAM.E_EXIST);//返回值：1表示打开成功，0表示打开失败，-10表示注册权限正确
		// 打开地图，如果仅仅显示地图就可以了。
		// 当需要进行数据采集，编辑时，常常需要将其他的坐标系统的数据转换地图数据，并保存下来。我们需要坐标转换参数
		if (ret > 0) {
			int pox = path.lastIndexOf("/");
			path = path.substring(0, pox);
			String[] refPaths = FileLayout.getSonFilePaths(path, "ref");
			if (refPaths == null || refPaths.length == 0) {
				return 1;
			}
			m_translator = readRefFile(refPaths[0]);
			if (m_translator != null) {
				return 2;
			}
		}
		return 0;
	}

	/**
	 * 关闭地图
	 */
	public void closeMap() {
		if (m_map != null && m_map.IsOpend()) {
			m_map.CloseMap();
			m_map = null;
		}
		m_translator = null;
	}

	/**
	 * 创建图层
	 * 
	 * @param layerName
	 *            图层名称
	 * @param geoType
	 *            图层类型
	 * @param fieldSet
	 *            属性字段信息
	 * @return 返回值:
	 *         <p>
	 *         -2表示地图没有打开
	 *         </p>
	 *         <p>
	 *         -1表示地图同级目录下存在同名的图层文件
	 *         </p>
	 *         <p>
	 *         0表示生成的图层添加地图的过程失败
	 *         </p>
	 *         <p>
	 *         1表示创建地图的过程中，投影参数发生异常
	 *         </p>
	 *         <p>
	 *         2表示图层创建成功，并成功添加到地图中
	 *         </p>
	 */
	public int createLayer(String layerName, E_GEO_TYPE geoType,
			IEFieldSet fieldSet) {
		if (m_map == null || !m_map.IsOpend()) {
			return -2;
		}
		String path = m_map.GetMapPath();
		int pox = path.lastIndexOf("/");
		if (layerName.endsWith(".ed2")) {
			path = String.format("%s/%s", path.substring(0, pox), layerName);
		} else {
			path = String
					.format("%s/%s.ed2", path.substring(0, pox), layerName);
		}
		// 创建物理图层
		File ed2File = new File(path);
		if (ed2File.exists()) {
			return -1;
		}
		IEFeatureClassVectorEd2 layer_ed2 = new IEFeatureClassVectorEd2();
		Spatial_Ref ref = new Spatial_Ref();
		m_map.GetSpatialRef(ref);
		
		layer_ed2.Create(path, geoType.toInt(), ref, fieldSet);
		layer_ed2.Close();
		// 添加物理图层到当前地图中管理
		int[] flag = new int[1];
		IELayer l = m_map.AddLayer(path, flag);
		if (l == null) {
			return 0;
		}
		return flag[0];

	}

	public void removeLayer(String layerName) {
		if (m_map == null || !m_map.IsOpend()) {
			return;
		}
		m_map.RemoveLayer(layerName);
	}

	// ////////////////////////////////////////////////
	// /////////////////添加图层功能
	// /////////////////////////////////////////////////
	/**ed2和eds不存在对应edb文件*/
	public static final int ERROR_NO_EDB = -1;
	/**地图中已经存在同名的图层*/
	public static final int ERROR_EXISTS_NAME = -2;
	/**图层坐标信息和地图坐标信息不一致*/
	public static final int ERROR_REF = -3;
	/**HiMap底层出现异常*/
	public static final int ERROR_HIMAP = -4;
	/**复制文件，读写文件过程中，出现IO异常*/
	public static final int ERROR_IO = -5;
	/**地图文件夹下存在同名图层*/
	public static final int ERROR_EXISTS_LAYER = -6;
	/**图层添加成功*/
	public static final int ERROR_NONE = 1;

	/**
	 * 添加图层
	 * <p>图层分为矢量图层和栅格图层，通常矢量图层比较小，
	 * 通过拷贝文件的方式将其添加到地图文档所在文件夹下，并添加到地图文档中；
	 * 栅格图层通过比较大，普遍超过100M，使用过程中通常多个地图同时使用同一份栅格数据，所以采用直接引用方式</p>
	 * @param geoPath 图层路径
	 * @return
	 */
	public int addLayer(String geoPath) {
		File srcGeoFile = new File(geoPath);
		if (!srcGeoFile.exists()) {
			return ERROR_IO;
		}
		return addLayer(srcGeoFile);
	}

	/**
	 * 添加图层
	 * <p>图层分为矢量图层和栅格图层，通常矢量图层比较小，
	 * 通过拷贝文件的方式将其添加到地图文档所在文件夹下，并添加到地图文档中；
	 * 栅格图层通过比较大，普遍超过100M，使用过程中通常多个地图同时使用同一份栅格数据，所以采用直接引用方式</p>
	 * @param srcGeoFile
	 * @return
	 */
	public int addLayer(File srcGeoFile) {
		String geoPath;
		String edbPath;
		String mapPath = m_map.GetMapPath();
		File srcAttrFile;
		File dstFile;
		String layerName;
		int xpos;

		geoPath = srcGeoFile.getAbsolutePath();
		xpos = geoPath.lastIndexOf(".");

		edbPath = String.format("%s.edb", geoPath.substring(0, xpos));
		srcAttrFile = new File(edbPath);
		if (geoPath.toLowerCase().endsWith(".edt")||geoPath.toLowerCase().endsWith(".cache")) {// edt不复制，直接添加
			return addLayerToMap(geoPath);
		}
		if (!srcAttrFile.exists()) {// ed2和eds必须存在edb
			return ERROR_NO_EDB;
		}
		layerName = srcGeoFile.getName();
		geoPath = String.format("%s/%s", mapPath, layerName);
		dstFile = new File(geoPath);
		if (dstFile.exists() && dstFile.equals(srcGeoFile)) {// 图层已经在该地图目录下
			return addInnerLayer(srcGeoFile);
		} else {// 从其他地方拷贝图层
			return addExternalLayer(srcGeoFile, srcAttrFile, mapPath);
		}
	}
	
	/**
	 * 检测是否存在同名图层
	 * 
	 * @return
	 */
	public boolean checkLayerName(String layerName) {
		int len = this.getMap().GetLayerNum();
		for (int i = len - 1; i >= 0; i--) {
			IELayer layer = this.getMap().GetLayerByNo(i);
			if (layer.GetLayerName().equalsIgnoreCase(layerName))
				return false;
		}
		return true;
	}

	/**
	 * 添加地图文件夹外部ed2，eds图层
	 * 
	 * @param srcGeoFile
	 *            外部ed2,eds文件
	 * @param srcAttrFile
	 *            相应的edb文件
	 * @param mapPath
	 *            地图的目录
	 */
	private int addExternalLayer(File srcGeoFile, File srcAttrFile,
			String mapPath) {
		if (!checkLayerName(srcGeoFile.getName())) {
			return ERROR_EXISTS_NAME;
		}
		String geoPath = String.format("%s/%s", mapPath, srcGeoFile.getName());
		File dstFile = new File(geoPath);
		if (dstFile.exists()) {			
			backupOldLayer(dstFile);// 备份旧文件
		}
		String edbPath = String.format("%s/%s", mapPath, srcAttrFile.getName());
		File dstAttrFile = new File(edbPath);
		if (dstAttrFile.exists()) {			
			backupOldLayer(dstAttrFile);// 备份旧文件
		}
		try {
			int ret = 0;
			dstFile.createNewFile();

			RandomAccessFile geoInput = new RandomAccessFile(srcGeoFile, "r");
			FileOutputStream geoOutput = new FileOutputStream(dstFile, true);
			ret = breakRead(geoInput, geoOutput, (int) srcGeoFile.length());
			geoInput.close();
			geoOutput.close();
			if (ret != ERROR_NONE) {// 拷贝失败 ，删除拷贝过来的数据
				if (dstFile.exists()) {
					dstFile.delete();
				}
				return ret;
			}
			RandomAccessFile attrInput = new RandomAccessFile(srcAttrFile, "r");
			FileOutputStream attrOutput = new FileOutputStream(dstAttrFile,
					true);
			breakRead(attrInput, attrOutput, (int) srcAttrFile.length());
			attrInput.close();
			attrOutput.close();
			if (ret != ERROR_NONE) {// 拷贝失败 ，删除拷贝过来的数据
				if (dstFile.exists()) {
					dstFile.delete();
				}
				if (dstAttrFile != null && dstAttrFile.exists()) {
					dstAttrFile.delete();
				}
				return ret;
			}
			// 添加图层到地图文件中
			ret = addLayerToMap(dstFile.getAbsolutePath());
			if (ret != ERROR_NONE) {// 添加失败，删除拷贝过来的数据
				if (dstFile.exists()) {
					dstFile.delete();
				}
				if (dstAttrFile != null && dstAttrFile.exists()) {
					dstAttrFile.delete();
				}
			}
			return ret;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ERROR_IO;
		}
	}

	/**
	 * 备份旧的图层文件
	 * 
	 * @param file
	 */
	private void backupOldLayer(File file) {
		File newFile = FileLayout.getUniqueName(file);// 更改名字
		file.renameTo(newFile);
	}

	/**
	 * 分段读取，避免内存溢出
	 */
	private int breakRead(RandomAccessFile input, FileOutputStream output,
			int length) {
		try {
			int oneTrillion = 1048576;// 每次最大读取1M的数据
			int trillionCount = length / oneTrillion;// 计算共有多少M数据
			int remainder = length % oneTrillion;// 取完以M为单位的数据后的余数
			byte[] data;
			for (int i = 0; i < trillionCount; i++) {
				data = new byte[oneTrillion];
				input.seek(oneTrillion * i);// 定位到正确的位置
				input.readFully(data);
				output.write(data);
				output.flush();
			}
			if (remainder != 0) {
				data = new byte[remainder];
				input.seek(oneTrillion * trillionCount);// 定位到正确的位置
				input.readFully(data);
				output.write(data);
				output.flush();
			}
			return ERROR_NONE;
		} catch (IOException e) {
			return ERROR_IO;
		}
	}

	/**
	 * 添加地图文件夹内部ed2,eds图层
	 * 
	 * @param srcGeoFile
	 *            内部ed2文件
	 */
	private int addInnerLayer(final File srcGeoFile) {
		if(!checkLayerName(srcGeoFile.getName())){// 地图文件已经包含图层
			return ERROR_EXISTS_LAYER;
		}
		addLayerToMap(srcGeoFile.getAbsolutePath());
		return ERROR_NONE;
	}

	/**
	 * 将图层添加到地图文档的管理中
	 * @param layerPath
	 * @return
	 */
	private int addLayerToMap(String layerPath) {
		IELayer layer;
		IEFeatureClass featureCls;
		//取出添加的图层的坐标信息
		Spatial_Ref layerRef = new Spatial_Ref();
		if (layerPath.toLowerCase().endsWith("ed2")) {
			layer = new IELayerVectorEd2();
			layer.Open(layerPath);
			featureCls = layer.As(IELayerVector.class).GetFeatureClass();
			featureCls.GetSpatialRef(layerRef);
			layer.Close();
		} else if (layerPath.toLowerCase().endsWith("edt")) {
			IELayerRasterEdt layerEdt = new IELayerRasterEdt();
			layerEdt.Open(layerPath);
			IEFeatureClassRasterEdt featureClsEdt = layerEdt
					.GetFeatureClassEdt();
			int ret = featureClsEdt.GetSpatialRef(layerRef);
			layerEdt.Close();
		}
		//取出地图的坐标信息
		Spatial_Ref mapRef = new Spatial_Ref();
		m_map.GetSpatialRef(mapRef);
		//对比图层单位和地图单位是否一致
		if(mapRef.coorUnit()!=layerRef.coorUnit()){
			return ERROR_REF;
		}
		//添加到地图文档的管理中
		int[] flag = new int[1];
		this.m_map.AddLayer(layerPath, flag);
		if (flag[0] == 1) {
			return ERROR_REF;
		} else if (flag[0] == 0) {
			return ERROR_HIMAP;
		}
		m_map.Save();
		return ERROR_NONE;
	}
}
