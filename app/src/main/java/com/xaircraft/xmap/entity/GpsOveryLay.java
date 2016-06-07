package com.xaircraft.xmap.entity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.aircraft.model.ScreenPoint;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import EMap.IO_Base.IEBase.lPoint_t;
import EMap.IO_Base.IEStruct.f643Point;
import EMap.IO_Gps.IEPJTranslator;
import EMap.IO_MapView.IEMapView_Base.EMAP_ACTION;
import EMap.OV_MapView.OV_MapView;
import EMap.OV_MapView.OV_Overlay;

public class GpsOveryLay extends OV_Overlay {

	public static final String KEY_GPS_OVERYLAY = "key_gps_overylay";
	private static final int DRAW_SCALE = 2;
	private static final String TAG = "GpsActivity";

	private Paint paint;
	private double azi;
	private ScreenPoint tempLoc;
	private ArrayBlockingQueue<ScreenPoint> gpsQueue = new ArrayBlockingQueue<ScreenPoint>(
			20);
	private ScreenPoint currentLocation;
	int width = -1;
	int height = -1;
	private OV_MapView mapView;
	private LocationManager mLocationManager;
	private IEPJTranslator mTranslator;

	public GpsOveryLay(OV_MapView mapView, IEPJTranslator translator) {
		super(Overlay_Config.Overlay_Config_Position_Float
				.and(Overlay_Config.Overlay_Config_Size_Float));
		paint = new Paint();
		this.mapView = mapView;
		this.mTranslator = translator;
		initLocationListener();
	}

	/**
	 * 定位
	 */
	public void location() {
		if (currentLocation == null) {
			return;
		}
		mapView.getMapView().SetCenterD(currentLocation.getMapX(),
				currentLocation.getMapY());
		mapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_REDRAW, true);
		
	}

	private void initLocationListener() {
		mLocationManager = (LocationManager) mapView.getContext()
				.getSystemService(Context.LOCATION_SERVICE);
		// 为获取地理位置信息时设置查询条件
		String bestProvider = mLocationManager.getBestProvider(getCriteria(), true);
		Location location =mLocationManager.getLastKnownLocation(bestProvider);
		updateCurrLocation(location);
		// 监听状态
		mLocationManager.addGpsStatusListener(listener);
		
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 0, mLocationListener);
		
		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			Toast.makeText(mapView.getContext(), "请开启GPS导航...", Toast.LENGTH_SHORT).show();  
            return;  
		}
	}

	private LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			Location location = mLocationManager.getLastKnownLocation(provider);

			if (location == null) {
				return;
			}
			ScreenPoint point = new ScreenPoint();
			f643Point retPt = new f643Point();
			mTranslator.Translator3d(location.getLongitude() * Math.PI / 180,
					location.getLatitude() * Math.PI / 180,
					location.getAltitude(), retPt);
			point.setMapX(retPt.x());
			point.setMapY(retPt.y());
			point.setMapZ(retPt.z());
			
			currentLocation = point;
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			updateCurrLocation(location);
		}
	};


	private boolean isfill;
	private int[] last_sreenXY;
	private void updateCurrLocation(Location location) {
		if (location == null) {
			return;
		}
		ScreenPoint point = new ScreenPoint();
		f643Point retPt = new f643Point();
		mTranslator.Translator3d(location.getLongitude() * Math.PI / 180,
				location.getLatitude() * Math.PI / 180,
				location.getAltitude(), retPt);
		point.setMapX(retPt.x());
		point.setMapY(retPt.y());
		point.setMapZ(retPt.z());

		isfill = gpsQueue.offer(point);
		if (!isfill) {
			gpsQueue.remove();
			gpsQueue.offer(point);
		}
		currentLocation = point;
		mapView.SetAction(EMAP_ACTION.EMAP_ACT_UPDATE_NO_REDRAW, true);
	}
	@Override
	public void onDestroy() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(mLocationListener);
			mLocationManager = null;
		}
		mapView = null;
	}

	@Override
	public boolean Draw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (gpsQueue.isEmpty()) {
			return false;
		}
		lPoint_t wpt = null;
		for (ScreenPoint loc : gpsQueue) {

			if (loc == null) {
				return false;
			}
			wpt = ComputeSreen(loc);
			// canvas.drawCircle(wpt.x, wpt.y, 3, paint);
			drawCross(canvas, wpt.x, wpt.y);
			loc.setScreenX(wpt.x);
			loc.setScreenY(wpt.y);
		}
		if (tempLoc == null) {
			azi = 0;
		} else {
			azi = getAzimuth(tempLoc.getMapY(), tempLoc.getMapX(),
					currentLocation.getMapY(), currentLocation.getMapX());
		}

		tempLoc = currentLocation;
		// 绘制船型
		drawShipForm(canvas, currentLocation.getScreenX(),
				currentLocation.getScreenY(), azi);
		return true;
	}

	private void drawCross(Canvas canvas, float x, float y) {
		paint.setColor(Color.GREEN);
		canvas.drawLine(x - 5, y, x + 5, y, paint);
		canvas.drawLine(x, y - 5, x, y + 5, paint);
	}

	// 状态监听
	GpsStatus.Listener listener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			switch (event) {
			// 第一次定位
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.i(TAG, "第一次定位");
				break;
			// 卫星状态改变
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Log.i(TAG, "卫星状态改变");
				// 获取当前状态
				GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
				// 获取卫星颗数的默认最大值
				int maxSatellites = gpsStatus.getMaxSatellites();
				// 创建一个迭代器保存所有卫星
				Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
						.iterator();
				int count = 0;
				while (iters.hasNext() && count <= maxSatellites) {
					GpsSatellite s = iters.next();
					count++;
				}
				System.out.println("搜索到：" + count + "颗卫星");
				break;
			// 定位启动
			case GpsStatus.GPS_EVENT_STARTED:
				Log.i(TAG, "定位启动");
				break;
			// 定位结束
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.i(TAG, "定位结束");
				break;
			}
		};
	};
	

	/**
	 * 返回查询条件
	 * 
	 * @return
	 */
	private Criteria getCriteria() {
		Criteria criteria = new Criteria();
		// 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// 设置是否要求速度
		criteria.setSpeedRequired(false);
		// 设置是否允许运营商收费
		criteria.setCostAllowed(false);
		// 设置是否需要方位信息
		criteria.setBearingRequired(false);
		// 设置是否需要海拔信息
		criteria.setAltitudeRequired(false);
		// 设置对电源的需求
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}
	
	/**
	 * 方位角计算
	 * 
	 * @param X1
	 *            前一点测量坐标系x
	 * @param Y1
	 *            前一点测量坐标系y
	 * @param X2
	 *            后一点测量坐标系x
	 * @param Y2
	 *            后一点测量坐标系y
	 * @return
	 */
	private double getAzimuth(double X1, double Y1, double X2, double Y2) {
		double dx = X2 - X1;
		double dy = Y2 - Y1;
		double a = 1;
		if (dx != 0 || dy != 0) {
			double s = Math.pow((dx * dx + dy * dy), 0.5);
			if (dx > 0 && dy > 0) {
				a = Math.asin(dy / s);
			}// 第一象限
			if (dx <= 0 && dy >= 0) {
				a = Math.PI / 2 + Math.acos(dy / s);
			}// 第二象限
			if (dx <= 0 && dy <= 0) {
				a = 3 * Math.PI / 2 - Math.acos(Math.abs(dy) / s);
			}// 第三象限
			if (dx > 0 && dy < 0) {
				a = 3 * Math.PI / 2 + Math.asin(dx / s);
			}// 第三象限
		}
		return a;
	}

	private void drawShipForm(Canvas canvas, float x, float y, double azi) {
		paint.setColor(Color.RED);
		canvas.drawLine(x - 2 * DRAW_SCALE, y, x + DRAW_SCALE, y, paint);
		canvas.drawLine(x, y - 2 * DRAW_SCALE, x, y + 2 * DRAW_SCALE, paint);

		// //3.1.计算方位角

		// ------------------
		// x1
		// x2| x3
		// x
		// 1.局部坐标系坐标
		float x1 = 20 * DRAW_SCALE, y1 = 0;// 第二个点屏幕坐标[顶点]
		float x2 = -7 * DRAW_SCALE, y2 = -7 * DRAW_SCALE;// 第三个点屏幕坐标[左边点]
		float x3 = -7 * DRAW_SCALE, y3 = 7 * DRAW_SCALE;// 第四个点屏幕坐标[右边点]
		float x4 = -5 * DRAW_SCALE, y4 = 0;// 第五个点屏幕坐标[右边点]
		// 2.坐标转换
		// 2.1旋转
		float deltaZ = 0;
		deltaZ = (float) (azi - Math.PI / 2);

		float xt;
		float cosdeltaz = (float) Math.cos(deltaZ);// 局部优化
		float sindeltaz = (float) Math.sin(deltaZ);
		xt = x1;
		x1 = xt * cosdeltaz - y1 * sindeltaz;
		y1 = xt * sindeltaz + y1 * cosdeltaz;
		xt = x2;
		x2 = xt * cosdeltaz - y2 * sindeltaz;
		y2 = xt * sindeltaz + y2 * cosdeltaz;
		xt = x3;
		x3 = xt * cosdeltaz - y3 * sindeltaz;
		y3 = xt * sindeltaz + y3 * cosdeltaz;
		xt = x4;
		x4 = xt * cosdeltaz - y4 * sindeltaz;
		y4 = xt * sindeltaz + y4 * cosdeltaz;

		// 2.2平移
		x1 += x;
		y1 += y;
		x2 += x;
		y2 += y;
		x3 += x;
		y3 += y;
		x4 += x;
		y4 += y;

		// 3.绘制
		canvas.drawLine(x - 2 * DRAW_SCALE, y, x + DRAW_SCALE, y, paint);
		canvas.drawLine(x2, y2, x1, y1, paint);
		canvas.drawLine(x3, y3, x1, y1, paint);
		canvas.drawLine(x2, y2, x4, y4, paint);
		canvas.drawLine(x3, y3, x4, y4, paint);
	}

	private lPoint_t ComputeSreen(ScreenPoint location) {
		// 坐标转换
		lPoint_t wpt = null;
		wpt = mapView.getMapView()
				.Lp2Wp(location.getMapX(), location.getMapY());
		return wpt;
	}

}
