package com.xaircraft.xmap.helper;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import EMap.IO_Base.IEStruct.Spatial_Ref;
import EMap.IO_Base.IEStruct.f643Point;
import EMap.IO_Base.IEStruct.f64Rect;
import EMap.IO_GisDB.IEFeatureClassRasterEdt;
import EMap.IO_GisDB.IEFeatureClassVector;
import EMap.IO_GisDB.IEFeatureClassVectorEd2;
import EMap.IO_GisDB.IEGeoLine;
import EMap.IO_GisDB.IEGeoPoint;
import EMap.IO_GisDB.IEGeoPolygon;
import EMap.IO_GisDB.IEGeometry;
import EMap.IO_GisDB.IEGisDB_Base.E_COOR_SYSTEM_TYPE;
import EMap.IO_GisDB.IEGisDB_Base.E_COOR_UNIT_TYPE;
import EMap.IO_GisDB.IEGisDB_Base.E_EARTH_TYPE;
import EMap.IO_GisDB.IEGisDB_Base.E_GEO_TYPE;
import EMap.IO_GisDB.IEGisDB_Base.E_PROJECT_TYPE;
import EMap.IO_Gps.IEPJTranslator;
import EMap.IO_Map.IELayer;
import EMap.IO_Map.IELayerRasterEdt;
import EMap.IO_Map.IELayerVector;
import EMap.IO_MapView.IEMapView_Base.EMAP_ACTION;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.hardware.GeomagneticField;
import android.os.Environment;

public class FileLayout {

	private static String MAIN_PATH;
	private static String MAP_PATH;
	private static Context CONTEXT = ContextUtil.getInstance();

	/**
	 * 初始化配置信息
	 * 
	 * @param context
	 */
	public static void initFileLayout() {

		MAP_PATH = getSharePreferences("xMap", "MapPath", CONTEXT);

	}

	public static void saveFileLayout() {
		if (MAP_PATH == null || MAP_PATH.equals(""))
			return;
		Editor ed = CONTEXT.getSharedPreferences("xMap", Context.MODE_PRIVATE)
				.edit();
		ed.putString("MapPath", MAP_PATH);
		ed.commit();

	}

	/**
	 * 获取软件路径
	 * 
	 * @return
	 */
	public static String getAppMain() {
		if (MAIN_PATH != null) {
			return MAIN_PATH;
		}
		if (Environment.getExternalStorageState().equals("mounted")) {
			File sdcard = Environment.getExternalStorageDirectory();
			File app = new File(String.format("%s/HiMapDemo",
					sdcard.getAbsolutePath()));
			if (!app.exists()) {
				app.mkdirs();
			}
			MAIN_PATH = app.getAbsolutePath();
		} else {
			File inner = Environment.getDataDirectory();
			File app = new File(String.format("%s/HiMapDemo",
					inner.getAbsolutePath()));
			if (!app.exists()) {
				app.mkdirs();
			}
			MAIN_PATH = app.getAbsolutePath();
		}
		return MAIN_PATH;
	}

	/**
	 * 获取椭球文件路径
	 * 
	 * @return
	 */
	public static String getEllipse() {
		String main = getAppMain();
		return String.format("%s/Ellipse.csv", main);
	}

	/**
	 * 获取系统符号库
	 * 
	 * @return
	 */
	public static String getSystemSymbol() {
		String main = getAppMain();
		return String.format("%s/HiMap.msf", main);
	}

	/**
	 * 获取符号绘制字体
	 * 
	 * @return
	 */
	public static String getSymbolTTF() {
		String main = getAppMain();
		return String.format("%s/HiMap Symbols.ttf", main);
	}

	public static String getWebCache() {
		String main = getAppMain();
		return String.format("%s/Himap.cache", main);
	}

	/**
	 * 获取地图路径
	 * <p>
	 * 1.应用程序目录下，包含map文件夹，则在里面寻找.map文件，如果存在，则认为是地图文件
	 * </p>
	 * <p>
	 * 2.应用程序目录下,不存在map文件夹，则在其下的所有文件夹中，寻找存在.map文件的作为地图文件
	 * </p>
	 * 
	 * @return
	 */
	public static String getMapPath() {
		if (!(MAP_PATH == null || MAP_PATH.equals("")))
			return MAP_PATH;
		String main = getAppMain();
		File mapDir = new File(String.format("%s/map", main));
		if (mapDir.exists()) {
			String[] mapPaths = getSonFilePaths(mapDir.getAbsolutePath(), "map");
			if (mapPaths != null && mapPaths.length > 0) {
				return mapPaths[0];
			}
		}
		File appFile = new File(main);
		File[] mapFiles = appFile.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.isFile()) {
					return false;
				}
				File[] children = pathname.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						// TODO Auto-generated method stub
						if (filename.endsWith(".map")) {
							return true;
						}
						return false;
					}
				});

				if (children == null || children.length == 0) {
					return false;
				}
				return true;
			}
		});
		if (mapFiles == null || mapFiles.length == 0) {
			return null;
		}
		File mapFile = mapFiles[0];
		String[] mapPaths = getSonFilePaths(mapFile.getAbsolutePath(), "map");
		if (mapPaths == null || mapPaths.length == 0) {
			return null;
		}
		MAP_PATH = mapPaths[0];
		return MAP_PATH;
	}

	public static void setMAP_PATH(String path) {
		MAP_PATH = path;
	}

	/**
	 * 安装默认文件
	 */
	public static void installDefaultFile(Context ctx) {
		createAssetsFile(ctx, getEllipse(), "Ellipse.csv");
		createAssetsFile(ctx, getSystemSymbol(), "HiMap.msf");
		createAssetsFile(ctx, getSymbolTTF(), "HiMap Symbols.ttf");
	}

	/**
	 * 复制Assets文件夹下的文件到其他目录中
	 * 
	 * @param ctx
	 *            android上下文
	 * @param newPath
	 *            部署路径
	 * @param assetsPath
	 *            assets路径
	 * @return
	 */
	private static String createAssetsFile(Context ctx, String newPath,
			String assetsPath) {

		AssetManager am = ctx.getAssets();
		File file = new File(newPath);
		if (file.exists()) {
			return file.getAbsolutePath();
		}
		try {
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file, true);
			InputStream is = am.open(assetsPath);
			byte[] buff = new byte[is.available()];
			while (is.read(buff) != -1) {
				os.write(buff);
			}
			is.close();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}

	/**
	 * 获取路径下 扩展名为Extension的子文件路径数组
	 * 
	 * @param path
	 *            查找路径
	 * @param Extension
	 *            扩展名
	 * @return
	 */
	public static String[] getSonFilePaths(String path, final String Extension) {
		File parent = new File(path);
		if (!parent.exists() || parent.isFile()) {
			return null;
		}
		File[] children = parent.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				if (Extension == null || Extension.length() == 0) {
					return true;
				}
				return filename.toLowerCase().endsWith(Extension);
			}
		});
		String[] paths = new String[children.length];
		for (int i = 0; i < paths.length; i++) {
			paths[0] = children[i].getAbsolutePath();
		}
		return paths;
	}

	/**
	 * 在指定路径下，创建文件，或者文件子目录
	 * 
	 * @param filePath
	 *            名指定路径
	 * @param fileName
	 *            文件名，或者子目录
	 * @return 文件对象
	 */
	public static File constructFilePaths(String filePath, String fileName) {
		File newFile;
		if (filePath == null || filePath.length() == 0) {
			return null;
		}
		if (filePath.endsWith(fileName)) {
			newFile = new File(filePath);
		} else {
			newFile = new File(filePath + "/" + fileName);
		}

		return newFile;
	}

	/**
	 * 获取当前目录下唯一名字
	 * 
	 * @param filePath
	 * @param fileName
	 * @return
	 */
	public static File getDifferentName(String filePath, String fileName) {
		File newFile = constructFilePaths(filePath, fileName);
		if (newFile != null) {
			newFile = getUniqueName(newFile);
		}
		return newFile;
	}

	/**
	 * 获取当前目录下的唯一名称
	 * 
	 * @param file
	 * @return
	 */
	public static File getUniqueName(File file) {
		if (file == null) {
			return null;
		}
		if (!file.exists()) {
			return file;
		}

		Pattern pat_num = Pattern.compile("\\(\\d\\)(?=\\.\\w+$)");// (?<=\\()\\d(?=\\)\\.\\w+$)
																	// 只取数字
		File parentFile = file.getParentFile();
		String[] children = parentFile.list();
		String childName;
		Matcher mat;
		int max_num = 0;
		for (int i = 0; i < children.length; i++) {
			childName = children[i];
			mat = pat_num.matcher(childName);
			if (mat.find()) {
				childName = mat.replaceFirst("");
				if (!childName.equals(file.getName())) {
					continue;
				}
				int num = Integer.parseInt(mat.group().substring(1, 2));// (1)
																		// 取数字
				if (num >= max_num) {
					max_num = num + 1;
				}
			} else {
				if (!childName.equals(file.getName())) {
					continue;
				}
				if (max_num == 0) {
					max_num = 1;
				}
			}
		}
		if (max_num == 0) {// 这个名称可用
			return file;
		}
		StringBuilder name = new StringBuilder(parentFile.getAbsolutePath());
		name.append("/");
		name.append(file.getName());
		int pox = name.lastIndexOf(".");
		name.insert(pox, String.format("(%d)", max_num));

		return new File(name.toString());
	}

	public static String getSharePreferences(String preferencesName,
			String filter, Context contexwrapter) {
		final SharedPreferences sharedPreferences = contexwrapter
				.getSharedPreferences(preferencesName, Activity.MODE_PRIVATE);

		String value = sharedPreferences.getString(filter, "");

		return value;
	}

	/**
	 * 缩放到图层
	 * 
	 * @param layer
	 */
	public static f64Rect resetLayer(IELayer layer) {
		Spatial_Ref pSpatialRef = new Spatial_Ref();
		switch (layer.GetLayerType()) {
		case E_FILE_TYPE_VECTOR_ED2:
		case E_FILE_TYPE_VECTOR_EDS:
			IEFeatureClassVector featurecls = layer.As(IELayerVector.class)
					.GetFeatureClass();
			featurecls.GetSpatialRef(pSpatialRef);
			break;
		case E_FILE_TYPE_RASTER:
			IEFeatureClassRasterEdt rasterCls = layer
					.As(IELayerRasterEdt.class).GetFeatureClassEdt();
			rasterCls.GetSpatialRef(pSpatialRef);
			break;
		default:
			break;
		}

		f64Rect rect = new f64Rect();
		layer.GetLayerRange(rect); // 获取图层的包围盒
		if (rect.xmin() == rect.xmax()) {
			return null;
		}
		if (pSpatialRef.coorUnit() == E_COOR_UNIT_TYPE.E_COOR_UNIT_TYPE_DEGREE
				.toInt()) {// 默认大地坐标使用弧度计算
			rect.xmin(rect.xmin() * Math.PI / 180);// 度转弧度
			rect.ymin(rect.ymin() * Math.PI / 180);
			rect.xmax(rect.xmax() * Math.PI / 180);
			rect.ymax(rect.ymax() * Math.PI / 180);
		}
		return rect;
	}

	/**
	 * 获取图层的面积
	 * 
	 * @param featureClass
	 * @return
	 */
	public static double acculateArea(IEFeatureClassVectorEd2 featureClass,int id) {

		IEFeatureClassVectorEd2 layer_ed2 = featureClass;
		Spatial_Ref ref = new Spatial_Ref();
		ref.earthType(E_EARTH_TYPE.E_EARTH_TYPE_WGS84.toInt());// 椭球类型为国家WGS84
		ref.coorSystem(E_COOR_SYSTEM_TYPE.E_COOR_SYSTEM_TYPE_GEO.toInt());// 平面坐标系统
		ref.coorUnit(E_COOR_UNIT_TYPE.E_COOR_UNIT_TYPE_METER.toInt());// 单位米
		ref.prjType(E_PROJECT_TYPE.E_PROJECT_TYPE_Gauss_Kruger.toInt());// 设置投影类型为高斯克吕格

		double area = 0;
		IEGeoPolygon geo=(IEGeoPolygon)layer_ed2.GetFeature(id).GetGeometry();
		for(int i=0;i<geo.GetCircleCount();i++)
		{
			IEGeoLine line= geo.GetCircleData(i);
			for(int j=0;j<line.GetPointCount();j++){
				IEGeoPoint point=line.GetGeoPoint(j);
				IEPJTranslator translator = CommonHelper.getMeterPJTranslator();
				
					double in_x = point.GetX()*Math.PI/180;//度转换弧度 
					double in_y = point.GetY()*Math.PI/180;//度转换弧度
					double in_z = point.GetZ();
					f643Point map_xyz=new f643Point();
					translator.Translator3d(in_x, in_y, in_z, map_xyz);
					
					
			}
		}
		
		
		
		
		ref.coorUnit(E_COOR_UNIT_TYPE.E_COOR_UNIT_TYPE_DEGREE.toInt());// 单位度
		featureClass.SetSpatialRef(ref);
		return area;
	}
}
