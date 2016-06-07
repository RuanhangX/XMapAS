package com.xaircraft.xmap.helper;


import com.xaircraft.xmap.entity.MapEntity;

import EMap.IO_Base.IEStruct.E_PJTProjPars;
import EMap.IO_Base.IEStruct.Spatial_Ref;
import EMap.IO_Base.IEStruct.f643Point;
import EMap.IO_GisDB.IEGisDB_Base.E_COOR_SYSTEM_TYPE;
import EMap.IO_GisDB.IEGisDB_Base.E_COOR_UNIT_TYPE;
import EMap.IO_GisDB.IEGisDB_Base.E_EARTH_TYPE;
import EMap.IO_GisDB.IEGisDB_Base.E_PROJECT_TYPE;
import EMap.IO_Gps.IEPJTranslator;

public class CommonHelper {
	
	/**
	 * 将经纬度坐标转换为地图的坐标
	 * @param lat 纬度
	 * @param lon 经度
	 * @param alt 高程
	 * @param map_xyz 地图坐标
	 */
	public static  boolean latlonaltToMap(double lat,double lon,double alt,f643Point map_xyz,MapEntity mapEntity){
		if(mapEntity==null)
			return false;
		IEPJTranslator translator = mapEntity.getTranslator();
		if(translator == null){
			return false;
		}
		double in_x = lon*Math.PI/180;//度转换弧度
		double in_y = lat*Math.PI/180;//度转换弧度
		double in_z = alt;
		translator.Translator3d(in_x, in_y, in_z, map_xyz);
		return true;
	}
	
	/**
	 * 获取坐标转换对象
	 * @return
	 */
	public static IEPJTranslator getPJTranslator(){
		//投影参数
		E_PJTProjPars projPars = new E_PJTProjPars();
		projPars.W(3);//设置高斯三度带
		projPars.Lo(toRadian(114));//中央子午线  广州地区为114
		projPars.FN(0);//北向加常数
		projPars.FE(500000);//东向加常数
		projPars.Bc(0);//平均纬度
		projPars.Ko(1);//尺度缩放比
		projPars.North(1);//坐标轴x正向是北向
		projPars.East(1);//坐标轴Y正向是东向
		//源椭球
		Spatial_Ref src_ref = new Spatial_Ref();
		src_ref.earthType(E_EARTH_TYPE.E_EARTH_TYPE_WGS84.toInt());//椭球类型为WGS84
		src_ref.coorSystem(E_COOR_SYSTEM_TYPE.E_COOR_SYSTEM_TYPE_GEO.toInt());//大地坐标系统（经纬度坐标）
		src_ref.coorUnit(E_COOR_UNIT_TYPE.E_COOR_UNIT_TYPE_DEGREE.toInt());//单位度
		//目标椭球
		Spatial_Ref dst_ref = new Spatial_Ref();
		dst_ref.earthType(E_EARTH_TYPE.E_EARTH_TYPE_WGS84.toInt());//椭球类型为国家WGS84
		dst_ref.coorSystem(E_COOR_SYSTEM_TYPE.E_COOR_SYSTEM_TYPE_GEO.toInt());//平面坐标系统
		dst_ref.coorUnit(E_COOR_UNIT_TYPE.E_COOR_UNIT_TYPE_DEGREE.toInt());//单位度
		dst_ref.prjType(E_PROJECT_TYPE.E_PROJECT_TYPE_Gauss_Kruger.toInt());//设置投影类型为高斯克吕格
		//定义坐标转换
		IEPJTranslator translater = new IEPJTranslator();
		translater.SetSrcSpatialRef(src_ref);//源椭球信息
		translater.SetSrcPorjectParam(projPars);//设置源投影信息，一般源都是WGS84的经纬度坐标。这里设置投影信息没有实际意义
		translater.SetDstSpatialRef(dst_ref);//目标椭球信息
		translater.SetDstPorjectParam(projPars);//设置目标投影信息
		return translater;
	}
	/**
	 * 获取坐标转换对象
	 * @return
	 */
	public static IEPJTranslator getMeterPJTranslator(){
		//投影参数
		E_PJTProjPars projPars = new E_PJTProjPars();
		projPars.W(3);//设置高斯三度带
		projPars.Lo(toRadian(114));//中央子午线  广州地区为114
		projPars.FN(0);//北向加常数
		projPars.FE(500000);//东向加常数
		projPars.Bc(0);//平均纬度
		projPars.Ko(1);//尺度缩放比
		projPars.North(1);//坐标轴x正向是北向
		projPars.East(1);//坐标轴Y正向是东向
		//源椭球
		Spatial_Ref src_ref = new Spatial_Ref();
		src_ref.earthType(E_EARTH_TYPE.E_EARTH_TYPE_WGS84.toInt());//椭球类型为WGS84
		src_ref.coorSystem(E_COOR_SYSTEM_TYPE.E_COOR_SYSTEM_TYPE_GEO.toInt());//大地坐标系统（经纬度坐标）
		src_ref.coorUnit(E_COOR_UNIT_TYPE.E_COOR_UNIT_TYPE_DEGREE.toInt());//单位度
		//目标椭球
		Spatial_Ref dst_ref = new Spatial_Ref();
		dst_ref.earthType(E_EARTH_TYPE.E_EARTH_TYPE_WGS84.toInt());//椭球类型为国家WGS84
		dst_ref.coorSystem(E_COOR_SYSTEM_TYPE.E_COOR_SYSTEM_TYPE_GEO.toInt());//平面坐标系统
		dst_ref.coorUnit(E_COOR_UNIT_TYPE.E_COOR_UNIT_TYPE_METER.toInt());//单位度
		
		dst_ref.prjType(E_PROJECT_TYPE.E_PROJECT_TYPE_Gauss_Kruger.toInt());//设置投影类型为高斯克吕格
		//定义坐标转换
		IEPJTranslator translater = new IEPJTranslator();
		translater.SetSrcSpatialRef(src_ref);//源椭球信息
		translater.SetSrcPorjectParam(projPars);//设置源投影信息，一般源都是WGS84的经纬度坐标。这里设置投影信息没有实际意义
		translater.SetDstSpatialRef(dst_ref);//目标椭球信息
		translater.SetDstPorjectParam(projPars);//设置目标投影信息
		return translater;
	}
	/**
	 * 度转换为弧度
	 * @param angle 角度
	 * @return 弧度
	 */
	public static double toRadian(double angle){
		return angle*Math.PI/180;
	}
	
	/**
	 * 弧度转换为度
	 * @param radian 弧度
	 * @return 度
	 */
	public static double toAngle(double radian){
		return radian*180/Math.PI;
	}
	
}
