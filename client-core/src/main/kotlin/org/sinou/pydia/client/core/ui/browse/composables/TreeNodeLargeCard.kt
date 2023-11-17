package org.sinou.pydia.client.core.ui.browse.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.sinou.pydia.client.core.ui.core.composables.getNodeDesc
import org.sinou.pydia.client.core.ui.core.composables.getNodeTitle
import org.sinou.pydia.client.core.ui.core.composables.lists.LargeCard
import org.sinou.pydia.client.core.ui.core.composables.lists.LargeCardGenericIconThumb
import org.sinou.pydia.client.core.ui.core.composables.lists.LargeCardImageThumb
import org.sinou.pydia.client.core.ui.models.TreeNodeItem

@Composable
fun TreeNodeLargeCard(
    nodeItem: TreeNodeItem,
    more: (() -> Unit)?,
//    isSelectionMode: Boolean,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    LargeCard(
        isSelected = isSelected,
        title = getNodeTitle(name = nodeItem.name, mime = nodeItem.mime),
        desc = getNodeDesc(
            nodeItem.remoteModTs,
            nodeItem.size,
            nodeItem.localModStatus
        ),
        modifier = modifier
    ) {
        if (nodeItem.hasThumb) {
            LargeCardImageThumb(
                stateID = nodeItem.defaultStateID(),
                eTag = nodeItem.eTag,
                metaHash = nodeItem.metaHash,
                title = getNodeTitle(name = nodeItem.name, mime = nodeItem.mime),
                openMoreMenu = more
            )
        } else {
            LargeCardGenericIconThumb(
                title = getNodeTitle(name = nodeItem.name, mime = nodeItem.mime),
                mime = nodeItem.mime,
                sortName = nodeItem.sortName,
                more = more
            )
        }
    }
}
