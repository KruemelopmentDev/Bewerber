package de.kruemelopment.org.bewerber

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InfosListAdapter(private val rowItems: Array<String>) :
    RecyclerView.Adapter<InfosListAdapter.MyViewHolder>() {
    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var titleTextView: TextView

        init {
            titleTextView = v.findViewById(R.id.textView009)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.liste, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = rowItems[position]
        holder.titleTextView.text = item
    }

    override fun getItemId(position: Int): Long {
        return rowItems[position].hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return rowItems.size
    }
}