package org.sinou.android.pydia.browse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import org.sinou.android.pydia.R

class WsListAdapter(
    private val onItemClicked: (slug: String, action: String) -> Unit
) : RecyclerView.Adapter<WsListAdapter.ViewHolder>() {

    var data = listOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: WsListAdapter.ViewHolder, position: Int) {
        val item = data[position]
        holder.titleView.text = item
    }

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_workspace, parent, false)
        return WsListAdapter.ViewHolder(view, onItemClicked)
    }

    // We pass a click listener to each view holder via the constructor...
    class ViewHolder(
        itemView: View
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val TAG = "ViewHolder<Workspace>"

        lateinit var titleView: TextView

        constructor(v: View, onItemClicked: (slug: String, action: String) -> Unit) : this(v) {
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(View.OnClickListener { v ->
                onItemClicked(
                    titleView.text.toString(),
                    "navigate"
                )
            })
            titleView = v.findViewById<View>(R.id.workspace_title) as TextView
        }
    }

}