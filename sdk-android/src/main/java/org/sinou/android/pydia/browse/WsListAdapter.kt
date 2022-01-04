package org.sinou.android.pydia.browse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.api.ui.WorkspaceNode
import org.sinou.android.pydia.R

class WsListAdapter(
    private val onItemClicked: (slug: String, action: String) -> Unit
) : RecyclerView.Adapter<WsListAdapter.ViewHolder>() {

    var data = listOf<WorkspaceNode>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: WsListAdapter.ViewHolder, position: Int) {
        val item = data[position]
        holder.slug = item.id
        holder.titleView.text = item.label
        holder.descView.text = item.description
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
//        private val TAG = "ViewHolder<Workspace>"

        var slug: String? = ""
        lateinit var titleView: TextView
        lateinit var descView: TextView

        constructor(v: View, onItemClicked: (slug: String, action: String) -> Unit) : this(v) {
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(View.OnClickListener { v ->
                onItemClicked(
                    slug!!,
                    "navigate"
                )
            })
            titleView = v.findViewById<View>(R.id.workspace_title) as TextView
            descView = v.findViewById<View>(R.id.workspace_desc) as TextView
        }
    }

}