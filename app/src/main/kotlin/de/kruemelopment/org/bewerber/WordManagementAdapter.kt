package de.kruemelopment.org.bewerber

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class WordManagementAdapter(
    private val rowItems: MutableList<String>,
    private val myDB: DataBaseHelper,
    private val type: String?,
    private val adapterChanges: AdapterChanges
) : RecyclerView.Adapter<WordManagementAdapter.MyViewHolder>() {
    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var titleTextView: TextView
        var constraintLayout: ConstraintLayout

        init {
            titleTextView = v.findViewById(R.id.textView009)
            constraintLayout = v.findViewById(R.id.relativ)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.liste, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = rowItems[position]
        holder.titleTextView.text = item
        holder.constraintLayout.setOnLongClickListener {
            myDB.deleteData(item, type)
            rowItems.removeAt(position)
            notifyItemRemoved(position)
            if (rowItems.isNotEmpty()) notifyItemRangeChanged(position, rowItems.size)
            if (rowItems.isEmpty()) {
                adapterChanges.adapterIsEmpty()
            }
            false
        }
    }

    override fun getItemId(position: Int): Long {
        return rowItems[position].hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return rowItems.size
    }
}