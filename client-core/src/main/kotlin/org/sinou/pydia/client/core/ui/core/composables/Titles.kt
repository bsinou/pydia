package org.sinou.pydia.client.core.ui.core.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.sinou.pydia.client.R

@Composable
fun DialogTitle(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_item_inner_padding)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {

        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier
                .weight(1f)
                .wrapContentWidth(Alignment.Start)
                .alpha(.8f)
        )
    }
}

@Composable
fun DefaultTitleText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier
            .wrapContentWidth(Alignment.Start)
            .alpha(.8f)
    )
}

@Composable
fun MainTitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text.uppercase(),
        color = color,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .paddingFromBaseline(top = 32.dp, bottom = 8.dp)
            .alpha(.9f)
    )
}

@Composable
fun MenuTitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text.uppercase(),
        color = color,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
//            .paddingFromBaseline(top = 36.dp, bottom = 6.dp)
//            .alpha(.9f)
    )
}


@Composable
fun TitleDescColumnBloc(
    title: String,
    desc: String?,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .alpha(.9f)
            .padding(top = dimensionResource(R.dimen.margin_header))
    )

    if (!desc.isNullOrEmpty()) {
        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .alpha(.9f)
                .padding(bottom = dimensionResource(R.dimen.margin_header))
        )
    }
}
