package org.sinou.pydia.client.core.ui.core.composables.menus

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.ui.core.composables.lists.WithListTheme
import org.sinou.pydia.client.core.ui.core.composables.modal.ModalBottomSheetLayout
import org.sinou.pydia.client.core.ui.core.composables.modal.ModalBottomSheetState
import org.sinou.pydia.client.core.ui.models.TreeNodeItem
import org.sinou.pydia.client.core.ui.theme.CellsIcons

// private const val logTag = "BottomSheet"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellsModalBottomSheetLayout(
    isExpandedScreen: Boolean,
    sheetContent: @Composable ColumnScope.() -> Unit,
    sheetState: ModalBottomSheetState,
    content: @Composable () -> Unit
) {
    ModalBottomSheetLayout(
        isExpandedScreen = isExpandedScreen,
        sheetContent = sheetContent,
        sheetState = sheetState,
        sheetElevation = 3.dp,
        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
        content = content,
    )
}

@Composable
fun BottomSheetContent(
    header: @Composable () -> Unit,
    simpleMenuItems: List<SimpleMenuItem>,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = dimensionResource(R.dimen.bottom_sheet_v_spacing),
                bottom = dimensionResource(R.dimen.bottom_sheet_v_spacing).times(2),
            )
            .verticalScroll(scrollState)

    ) {
        header()
        for (item in simpleMenuItems) {
            BottomSheetListItem(
                icon = item.icon,
                title = item.title,
                onItemClick = item.onClick,
                selected = item.selected,
            )
        }
    }
}

@Composable
fun BottomSheetHeader(
    icon: ImageVector,
    title: String,
    desc: String,
) {
    BottomSheetHeader(
        thumb = { Icon(imageVector = icon, contentDescription = title) },
        title = title,
        desc = desc,
    )
}

@Composable
fun GenericBottomSheetHeader(
    icon: ImageVector,
    title: String,
) {
    Row(
        modifier = Modifier
            .padding(
                horizontal = dimensionResource(R.dimen.bottom_sheet_start_padding),
                vertical = dimensionResource(R.dimen.bottom_sheet_header_v_padding),
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(imageVector = icon, contentDescription = title)

        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.item_spacer_width)))

        Column(
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.Start)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun BottomSheetHeader(
    thumb: @Composable () -> Unit,
    title: String,
    desc: String? = null,
) {
    WithListTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(R.dimen.bottom_sheet_start_padding),
                        end = dimensionResource(R.dimen.bottom_sheet_start_padding),
                        top = dimensionResource(R.dimen.bottom_sheet_header_v_padding),
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                thumb()

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.item_spacer_width)))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.Start)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    desc?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            BottomSheetDivider()
        }
    }
}

@Composable
fun BottomSheetNoAction() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.bottom_sheet_start_padding),
                vertical = dimensionResource(R.dimen.bottom_sheet_v_padding),
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.more_menu_no_action))
    }
}

@Composable
fun BottomSheetListItem(
    title: String,
    onItemClick: () -> Unit,
    selected: Boolean = false,
    icon: ImageVector? = null,
    @DrawableRes iconId: Int? = null,
) {

    val (mTint, mBg) = if (selected) {
        MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface to Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .background(color = mBg)
            .padding(
                horizontal = dimensionResource(R.dimen.bottom_sheet_start_padding),
                // Warning: this is only the "inner" padding of the menu item.
                // See also parent's vertical spacing
                vertical = dimensionResource(R.dimen.bottom_sheet_v_padding),
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        icon?.let {
            // Make the list items aligned with the header. TODO rather use base line
            val pad = dimensionResource(R.dimen.list_thumb_bg_size)
                .minus(dimensionResource(R.dimen.list_icon_size)).div(2)
            val pad2 = pad.plus(dimensionResource(R.dimen.item_spacer_width))
            Spacer(modifier = Modifier.width(pad))
            Icon(imageVector = it, contentDescription = title, tint = mTint)
            Spacer(modifier = Modifier.width(pad2))
        } ?: run {
            iconId?.let {
                val pad = dimensionResource(R.dimen.list_thumb_bg_size)
                    .minus(dimensionResource(R.dimen.list_icon_size)).div(2)
                val pad2 = pad.plus(dimensionResource(R.dimen.item_spacer_width))
                Spacer(modifier = Modifier.width(pad))
                Icon(painter = painterResource(it), contentDescription = title, tint = mTint)
                Spacer(modifier = Modifier.width(pad2))
            }
        }
        Text(text = title, color = mTint)
    }
}

@Composable
fun BottomSheetFlagItem(
    nodeItem: TreeNodeItem?,
    icon: ImageVector? = null,
    @DrawableRes iconId: Int? = null,
    title: String,
    flagType: Int,
    onItemClick: (Boolean) -> Unit
) {
    var localSelected by remember(key1 = nodeItem, key2 = flagType) {
        val selected = nodeItem?.isFlag(flagType) ?: false
        mutableStateOf(selected)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onItemClick(!localSelected) })
            .padding(
                horizontal = dimensionResource(R.dimen.bottom_sheet_start_padding),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // Make the list items aligned with the header. TODO rather use base line
        val pad = dimensionResource(R.dimen.list_thumb_bg_size)
            .minus(dimensionResource(R.dimen.list_icon_size)).div(2)
        val pad2 = pad.plus(dimensionResource(R.dimen.item_spacer_width))
        Spacer(modifier = Modifier.width(pad))

        icon?.let {
            Icon(imageVector = it, contentDescription = title)
        } ?: run {
            iconId?.let {
                Icon(painterResource(id = it), contentDescription = title, Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.width(pad2))

        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )
        Switch(
            modifier = Modifier.semantics { contentDescription = title },
            checked = localSelected,
            onCheckedChange = { localSelected = it; onItemClick(it) }
        )
    }
}

@Composable
fun BottomSheetDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant //.copy(alpha = .6f)
) {
    Divider(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = dimensionResource(R.dimen.bottom_sheet_v_padding),
            ),
        color = color,
        thickness = 1.dp,
    )
}

@Preview(showBackground = true)
@Composable
fun BottomSheetContentPreview() {
    val context = LocalContext.current
    val onClick: (String) -> Unit = { title ->
        Toast.makeText(
            context, title, Toast.LENGTH_SHORT
        ).show()
    }
    val simpleMenuItems: List<SimpleMenuItem> = listOf(
        SimpleMenuItem(CellsIcons.Share, "Share", { onClick("Share") }, false),
        SimpleMenuItem(CellsIcons.Link, "Get Link", { onClick("Get Link") }, false),
        SimpleMenuItem(CellsIcons.Edit, "Edit", { onClick("Edit") }, false),
        SimpleMenuItem(CellsIcons.Delete, "Delete", { onClick("Delete") }, false),
    )

    BottomSheetContent(
        {
            BottomSheetHeader(
                icon = CellsIcons.Processing,
                title = "My Transfer of jpg.pdf",
                desc = "45MB, started at 5.54 AM, 46% done",
            )
        },
        simpleMenuItems,
    )
}

@Preview(showBackground = true)
@Composable
fun BottomSheetListItemPreview() {
    BottomSheetListItem(
        icon = CellsIcons.Processing,
        title = "Share",
        onItemClick = { },
    )
}

@Preview(showBackground = true)
@Composable
fun BottomSheetNoActionPreview() {
    BottomSheetNoAction()
}
