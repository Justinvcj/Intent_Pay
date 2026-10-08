package com.intentpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.intentpay.ui.theme.MeshAccent1
import com.intentpay.ui.theme.MeshAccent2
import com.intentpay.ui.theme.MeshDarkBg

@Composable
fun MeshBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Create a subtle radial/linear mesh gradient
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            MeshAccent1,
            MeshDarkBg,
            MeshAccent2,
            MeshDarkBg
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        content()
    }
}
