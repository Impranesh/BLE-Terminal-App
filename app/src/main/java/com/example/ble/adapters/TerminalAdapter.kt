package com.example.ble.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ble.R
import com.example.ble.models.TerminalData

class TerminalAdapter(private val dataList: MutableList<TerminalData>) :
    RecyclerView.Adapter<TerminalAdapter.TerminalViewHolder>() {

    class TerminalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TerminalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data, parent, false)
        return TerminalViewHolder(view)
    }

    override fun onBindViewHolder(holder: TerminalViewHolder, position: Int) {
        holder.messageTextView.text = dataList[position].message
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun addMessage(message: String) {
        dataList.add(TerminalData(message))
        notifyItemInserted(dataList.size - 1)
    }
}
