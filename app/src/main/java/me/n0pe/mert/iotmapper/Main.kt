package me.n0pe.mert.iotmapper

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.ImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.net.URL
import kotlin.concurrent.thread

class Main: Activity() {

	var TAG = javaClass.simpleName
	var img = URL("https://src.n0pe.me/hax/icon.mert.n0pe.me.png")

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(initSplash(img))
		Handler().postDelayed({ request() }, 999)
		return
	}

	override fun onPause() {
		super.onPause()
		return
	}

	override fun onPostResume() {
		super.onPostResume()
		return
	}

	override fun onDestroy() {
		super.onDestroy()
		return
	}

	override fun onStart() {
		super.onStart()
		return
	}

	override fun onResume() {
		super.onResume()
		return
	}

	override fun onStop() {
		super.onStop()
		return
	}

	override fun onRestart() {
		super.onRestart()
		return
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		return super.onOptionsItemSelected(item)
	}

	override fun onBackPressed() {
		super.onBackPressed()
		return
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		var all = true
		for (result in grantResults!!) {
			if (result == PackageManager.PERMISSION_DENIED) {
				all = false
			}
			continue
		}
		if (all) {
			redirect()
		} else {
			request()
		}
		return
	}

	fun request() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(arrayOf(
				android.Manifest.permission.CAMERA,
//				android.Manifest.permission.INTERNET,
//				android.Manifest.permission.BLUETOOTH,
//				android.Manifest.permission.BLUETOOTH_ADMIN,
//				android.Manifest.permission.ACCESS_FINE_LOCATION,
//				android.Manifest.permission.ACCESS_COARSE_LOCATION,
//				android.Manifest.permission_group.LOCATION
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.ACCESS_COARSE_LOCATION
				), 1)
		} else {
			redirect()
		}
		return
	}

	fun initSplash(res: URL): ImageView {
		var view = ImageView(this)
		view.scaleType = ImageView.ScaleType.CENTER_INSIDE
		view.setBackgroundColor(Color.WHITE)
		thread {
			var pic = BitmapFactory.decodeStream(res.openStream())
			runOnUiThread {
				view.setImageBitmap(pic)
				return@runOnUiThread
			}
			return@thread
		}
		return view
	}

	fun redirect() {
		App.mFirebaseRdb.getReference("devices").addListenerForSingleValueEvent(object: ValueEventListener {
			override fun onCancelled(p0: DatabaseError?) {
				return
			}

			override fun onDataChange(p0: DataSnapshot?) {
				return startActivity(
					Intent(this@Main, Map::class.java)
						.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
				)
				return
			}
		})
		return
	}
}
