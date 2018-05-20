package me.n0pe.mert.iotmapper

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.location.Location
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.math.absoluteValue

interface Consumer<T> {
	fun accept(arg: T?)
}

object Utils {

	open class RefBase(
		var mDbRef: DatabaseReference? = null,
		var mStorRef: StorageReference? = null
	) {
		init {
			Log.d(javaClass.simpleName, "init")
		}
		operator fun invoke(a: Any, b: Any) {
			return this(a, b)
		}
	}

	object RDevice: RefBase()
	object RUDEV: RefBase()
	object RUser: RefBase()

	fun getLocation(ctx: Context, callback: (location: Location?) -> Unit) {
		var client = LocationServices.getFusedLocationProviderClient(ctx)
		if (false
			|| Build.VERSION.SDK_INT < Build.VERSION_CODES.M
			|| ctx.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
		) {
			client.lastLocation.addOnCompleteListener {
				App.lastLoc = it.result
				callback(it.result)
			}
			return
		}
		return callback(null)
	}

	fun startBleScan(ctx: Context, deviceFoundCb: (result: ScanResult?) -> Unit) {

		var service = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

		service.adapter?.bluetoothLeScanner?.startScan(object: ScanCallback() {

			var TAG = javaClass.simpleName

			override fun onScanFailed(errorCode: Int) {
				super.onScanFailed(errorCode)
				Log.d(TAG, "onScanFailed: ${errorCode}")
				return
			}

			override fun onScanResult(callbackType: Int, result: ScanResult?) {
				super.onScanResult(callbackType, result)
				return deviceFoundCb(result)
			}

			override fun onBatchScanResults(results: MutableList<ScanResult>?) {
				super.onBatchScanResults(results)
				results?.forEach {
					onScanResult(-1, it)
				}
				return
			}
		})
	}

	fun getDeviceIdent(ctx: Context): String {
		return Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID).toUpperCase()
	}

	object Make {

		fun marker(
			position: LatLng,
			title: String = "<no title>",
			snippet: String? = "<no data>",
			alpha: Float = 1F,
			zIndex: Float = 1F,
			rotation: Float = 0F,
			draggable: Boolean = true,
			visible: Boolean = true,
			flat: Boolean = false,
			color: Float = (Math.random() * 360).toFloat(),
			other: Any? = null
		): MarkerOptions {
			return MarkerOptions()
				.draggable(draggable)
				.position(position)
				.rotation(rotation)
				.visible(visible)
				.snippet(snippet)
				.zIndex(zIndex)
				.alpha(alpha)
				.title(title)
				.flat(flat)
				.icon(BitmapDescriptorFactory.defaultMarker(color))
		}

		fun marker(device: Device): MarkerOptions {
			return marker(
				position = if (device.location != null) device.location!!.toLatLng() else LatLng(0.0, 0.0),
				title = if (device.name != null) device.name!! else "",
				snippet = device.details?.rssi.toString(),
				alpha = if (device.id == null) 0.5F else 1.0F,
				zIndex = try {
					device.details?.rssi?.toFloat()!!.absoluteValue
				} catch (e: Exception) {
					1.0F
				}
			)
		}

		fun overlay(
			position: LatLng
		): GroundOverlayOptions {
			return GroundOverlayOptions()
//				.transparency(transparancy)
//				.clickable(clickable)
//				.position(position)
//				.visible(visible)
//				.bearing(bearing)
//				.height(height)
//				.zIndex(zIndex)
//				.image(image)
		}
	}

	var callbackHandlerLast: Long = 0
	fun callbackHandler(ctx: Context?, map: GoogleMap?, sensor: Sensor?, data: FloatArray?, timeout: Int = 100) {
		// sensor == null || data == null || mMapView == null || mGMap == null
		if (Date().time - callbackHandlerLast < timeout || null in arrayOf(sensor, data, map)) {
			return
		}
		callbackHandlerLast = Date().time
		var data = data!!.map { x -> x * 90F }.toFloatArray()
		var tilt = 90F - (data[3] - data[0]).absoluteValue.coerceIn(0F, 90F)
		var bear = (data[1] + data[2]) * -2F
		//println(Arrays.toString(data))
		if(ctx != null) {
			Utils.getLocation(ctx, {
				if (it == null) {
					return@getLocation
				}
				return@getLocation
			})
		}
		if(map == null) {
			return
		}
		var pos = CameraPosition.Builder().bearing(bear).tilt(tilt)
			.target(LatLng(
				map.cameraPosition?.target!!.latitude,
				map.cameraPosition?.target!!.longitude
			))
			.zoom(map.cameraPosition!!.zoom.coerceIn(14F, 21F))
			.build()
		var upd = CameraUpdateFactory.newCameraPosition(pos)
		map.animateCamera(upd, timeout, null)
		return
	}

	fun focusLocation(ctx: Context, map: GoogleMap?, before: Runnable? = null, after: Runnable? = null) {
		Utils.getLocation(ctx, {
			if (it == null) {
				return@getLocation
			}
			try {
				var pos = LatLng(it.latitude, it.longitude)
				if(before != null)
					before.run() //Map.service?.unregisterListener(Map.mSensorHandler)
				map?.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 19F),
					Position(map?.cameraPosition?.target!!).distanceTo(Position(pos)).absoluteValue.div(10).toInt(),
					object: GoogleMap.CancelableCallback {
						override fun onFinish() {
							if(after != null)
								after.run()
							return
						}

						override fun onCancel() {
							return
						}
					})
			} catch (e: Exception) {
				Log.e("focusLocation", e.message)
			}
		})
		return
	}
}

