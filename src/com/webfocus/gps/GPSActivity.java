package com.webfocus.gps;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class GPSActivity extends MapActivity {

	private MapController mapController;
	private GeoPoint geoPoint;
	private String msg;
	private Button reloadBtn;
	private MapView mapView;
	protected static final int MENU_REFRESH = Menu.FIRST;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gps);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		reloadBtn = (Button) findViewById(R.id.reloadGPS);
		
		// menu.add(0, MENU_REFRESH, 0, "刷新");
		 
		initMap();
		reloadBtn.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(View v) {
				loadMap();
			}

		});

	}

	private void initMap() {
		loadMap();
	}

	public void loadMap() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		// 取得經緯度
		String provider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(provider);
		Double latitude = location.getLatitude() * 1E6;
		Double longitude = location.getLongitude() * 1E6;

		// 定位
		geoPoint = new GeoPoint(latitude.intValue(), longitude.intValue());
		mapController = mapView.getController();
		mapController.setZoom(18);
		mapController.animateTo(geoPoint);

		// 加入地圖標籤
		MapOverlay myOverlay = new MapOverlay();
		mapView.getOverlays().add(myOverlay);
		TextView textView = (TextView) findViewById(R.id.textview);

		try {

			msg = "經度：" + location.getLongitude() + "\n";
			msg += "緯度：" + location.getLatitude() + "\n";

			Geocoder gc = new Geocoder(this);
			List<Address> addresses = gc.getFromLocation(
					location.getLatitude(), location.getLongitude(), 1);
			if (addresses.size() > 0) {
				msg += "AddressLine：" + addresses.get(0).getAddressLine(0)
						+ "\n";
				msg += "城市名稱：" + addresses.get(0).getCountryName() + "\n";
				msg += "地點：" + addresses.get(0).getLocality() + "\n";
				msg += "FeatureName：" + addresses.get(0).getFeatureName();

			}
			textView.setText(msg);
		} catch (Exception e) {
			setTitle(e.getMessage());
		}
	}

	class MapOverlay extends Overlay {

		private int count = 0;

		@Override
		public boolean onTouchEvent(MotionEvent e, MapView mapView) {

			Point screenPoint = new Point();
			mapView.getProjection().toPixels(geoPoint, screenPoint);

			int currentX = (int) e.getX();
			int currentY = (int) e.getY();

			if ((currentX - screenPoint.x) >= 0
					&& (currentX - screenPoint.x) < 50
					&& (currentY - screenPoint.y) >= 0
					&& (currentY - screenPoint.y) < 30) {

				if (count == 0) {
					new AlertDialog.Builder(GPSActivity.this).setMessage(msg)
							.setPositiveButton("確定", new OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									count = 0;
								}
							}).show();
				}
				count++;

			}
			return super.onTouchEvent(e, mapView);
		}

		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			Paint paint = new Paint();
			paint.setColor(Color.BLUE);
			Point screenPoint = new Point();
			mapView.getProjection().toPixels(geoPoint, screenPoint);
			mapView.getController().setZoom(18);
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.gps);
			canvas.drawBitmap(bmp, screenPoint.x, screenPoint.y, paint);
			canvas.drawText("車牌：EZ-1234", screenPoint.x, screenPoint.y, paint);

			return super.draw(canvas, mapView, shadow, when);
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
