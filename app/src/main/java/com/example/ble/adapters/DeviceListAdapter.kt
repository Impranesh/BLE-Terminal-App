package com.example.ble.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ble.databinding.ItemDeviceBinding

class DeviceListAdapter(private val onItemClick: (BluetoothDevice) -> Unit) : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {

    private val deviceList = mutableListOf<BluetoothDevice>()


    fun addDevice(device: BluetoothDevice) {
        if (!deviceList.contains(device)) {
            deviceList.add(device)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceList[position]
        holder.bind(device)
    }

    override fun getItemCount(): Int = deviceList.size

    inner class ViewHolder(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: BluetoothDevice) {
            binding.deviceNameTextView.text = device.name ?: "Unknown device"
            binding.root.setOnClickListener {
                onItemClick(device)
            }
        }
    }
}
