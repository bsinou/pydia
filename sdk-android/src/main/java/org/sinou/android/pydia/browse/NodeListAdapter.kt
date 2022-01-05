package org.sinou.android.pydia.browse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.BrowseActivity
import org.sinou.android.pydia.R
import org.sinou.android.pydia.room.browse.RTreeNode

class NodeListAdapter(
    private val parentStateID: StateID,
    private val onItemClicked: (stateID: StateID, command: String) -> Unit
) : RecyclerView.Adapter<NodeListAdapter.ViewHolder>() {

    var data = listOf<RTreeNode>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: NodeListAdapter.ViewHolder, position: Int) {
        val item = data[position]
        holder.node = item
        holder.labelView.text = item.name
        holder.descView.text = item.parentPath
    }

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_node, parent, false)
        return NodeListAdapter.ViewHolder(view, parentStateID, onItemClicked)
    }

    // We pass a click listener to each view holder via the constructor...
    class ViewHolder(
        itemView: View
    ) :
        RecyclerView.ViewHolder(itemView) {
//        private val TAG = "ViewHolder<Workspace>"

        var node: RTreeNode? = null
        lateinit var labelView: TextView
        lateinit var descView: TextView

        constructor(v: View, parentStateID: StateID, onItemClicked: (stateID: StateID, action: String) -> Unit) : this(v) {
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(View.OnClickListener { v ->
                node?.let {
                    val child = parentStateID.child(it.name)
                    onItemClicked(
                        child,
                        BrowseActivity.NAVIGATE
                    )
                }
            })
            labelView = v.findViewById<View>(R.id.node_label) as TextView
            descView = v.findViewById<View>(R.id.node_desc) as TextView
        }
    }

}