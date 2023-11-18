package org.sinou.pydia.client.ui.core.composables.lists

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.sinou.pydia.client.R
import org.sinou.pydia.client.ui.models.MultipleItem

@Composable
fun MultipleGridItem(
    item: MultipleItem,
    more: () -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    LargeCard(
        title = item.name,
        desc = "Re-implement me",
//        desc = getAppearsInDesc(item),
        isSelected = isSelected,
        modifier = modifier
    ) {
        if (item.hasThumb) {
            LargeCardImageThumb(
                stateID = item.defaultStateID(),
                eTag = item.eTag,
                metaHash = item.metaHash,
                title = item.name,
                openMoreMenu = if (!isSelectionMode) more else null
            )
        } else {
            LargeCardGenericIconThumb(
                title = item.name,
                mime = item.mime,
                sortName = item.sortName,
                more = if (!isSelectionMode) more else null
            )
        }
    }
}

@Composable
public fun getAppearsInDesc(item: MultipleItem): String {
    val suffix = item.appearsIn
        .joinToString(", ") { item.appearsInWorkspace[it.slug] ?: it.slug ?: "" }
    return stringResource(R.string.appears_in_prefix, suffix)
}