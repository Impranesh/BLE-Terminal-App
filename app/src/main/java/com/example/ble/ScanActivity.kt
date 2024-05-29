package com.example.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ble.adapters.DeviceListAdapter
import com.example.ble.databinding.ActivityScanBinding

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var deviceListAdapter: DeviceListAdapter

    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())

    private val SCAN_PERIOD: Long = 10000

    private val bluetoothEnableRequestCode = 1001
    private val locationPermissionRequestCode = 1002
    private val nearbyDevicesPermissionRequestCode=1003
    private val locationEnableRequestCode=1004

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        // Initialize RecyclerView and adapter
        deviceListAdapter = DeviceListAdapter { device -> connectToDevice(device) }
        binding.recyclerView.adapter = deviceListAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Set click listeners for start and stop scan buttons
        binding.btnStartScan.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                if(hasLocationPermission()){
                    if(hasNearbyDevicesPermission()){
                        startScan()
                    }else{
                        requestNearbyDevicesPermission()
                    }
                }else{
                    requestLocationPermission()
                }
            }else {
                if (hasLocationPermission()) {
                    startScan()
                } else {
                    requestLocationPermission()
                }
            }
        }
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(hasNearbyDevicesPermission()) {
                    startScan()
                }else{
                    requestNearbyDevicesPermission()
                }
            } else {
                Toast.makeText(this, "Location permission is required to scan for Bluetooth devices", Toast.LENGTH_SHORT).show()
            }
        }else if(requestCode==nearbyDevicesPermissionRequestCode){
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScan()
                } else {
                    Toast.makeText(this, "Nearby Devices permission is required to scan for Bluetooth devices", Toast.LENGTH_SHORT).show()
                }
            }else{
                //
                startScan()
            }
        }
    }
    private fun hasNearbyDevicesPermission():Boolean{
        return if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED
        }else{
            true
        }
    }
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNearbyDevicesPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT),
            nearbyDevicesPermissionRequestCode
        )
    }
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            locationPermissionRequestCode
        )
    }
    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (!scanning) {
            if (!bluetoothAdapter.isEnabled) {
                // Bluetooth is not enabled, prompt the user to turn it on
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, bluetoothEnableRequestCode)
            } else if(!isLocationEnabled()){
                // Location is not enabled, prompt the user to turn it on
                val locationSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(locationSettingsIntent, locationEnableRequestCode)
            }
            else {
                // Bluetooth is enabled, start scanning
                handler.postDelayed({
                    scanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)
                    binding.btnStartScan.text = "Start Scan"
                    binding.btnStartScan.background = ContextCompat.getDrawable(this, R.drawable.start)
                }, SCAN_PERIOD)
                scanning = true
                bluetoothLeScanner.startScan(leScanCallback)
                binding.btnStartScan.text = "Stop Scan"
                binding.btnStartScan.background = ContextCompat.getDrawable(this, R.drawable.stop)
            }
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
            binding.btnStartScan.text = "Start Scan"
            binding.btnStartScan.background = ContextCompat.getDrawable(this, R.drawable.start)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == bluetoothEnableRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth was enabled by the user, start scanning
                startScan()
            } else {
                // Bluetooth was not enabled by the user
                Toast.makeText(this, "Bluetooth must be enabled to scan for devices", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val leScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { scanResult ->
                val device = scanResult.device
                if(device.name != null) {
                    deviceListAdapter.addDevice(device)
                    deviceListAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        if (scanning) {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
            binding.btnStartScan.text = "Start Scan"
            binding.btnStartScan.background = ContextCompat.getDrawable(this, R.drawable.start)
        }
        val intent = Intent(this, DeviceControlActivity::class.java)
        intent.putExtra("device_address", device.address)
        startActivity(intent)
    }

}
