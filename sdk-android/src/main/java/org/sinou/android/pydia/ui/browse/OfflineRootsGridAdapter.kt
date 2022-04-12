package org.sinou.android.pydia.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.databinding.GridItemOfflineRootBinding
import org.sinou.android.pydia.db.nodes.RLiveOfflineRoot
import org.sinou.android.pydia.services.FileService

class OfflineRootsGridAdapter(
    private val onItemClicked: (node: RLiveOfflineRoot, command: String) -> Unit
) : ListAdapter<RLiveOfflineRoot, OfflineRootsGridAdapter.ViewHolder>(OfflineDiffCallback()),
    KoinComponent {

    private val fileService  by inject<FileService>()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val thumbDirPath =
            fileService.dataParentPath(item.getStateID().accountId, AppNames.LOCAL_FILE_TYPE_THUMB)
        holder.bind(item, thumbDirPath)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).with(onItemClicked)
    }

    class ViewHolder private constructor(val binding: GridItemOfflineRootBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RLiveOfflineRoot, thumbDirPath: String?) {
            binding.offlineRoot = item
            binding.thumbDirPath = thumbDirPath
            binding.executePendingBindings()
        }

        fun with(
            onItemClicked: (node: RLiveOfflineRoot, command: String) -> Unit
        ): ViewHolder {

            binding.root.setOnClickListener {
                binding.offlineRoot?.let { onItemClicked(it, AppNames.ACTION_OPEN) }
            }

            binding.gridItemMoreButton.setOnClickListener {
                binding.offlineRoot?.let { onItemClicked(it, AppNames.ACTION_MORE) }
            }
            return this
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GridItemOfflineRootBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}
