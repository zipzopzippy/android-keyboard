/*
 * Personal build: a "Try typing here" field pinned to the bottom of the settings screens.
 */

package org.futo.inputmethod.latin.uix

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.futo.inputmethod.latin.R

/**
 * A text field for trying the keyboard without leaving settings. It is a regular Android text
 * field, so whatever keyboard is active types straight into it, and it doesn't take focus on its
 * own, so the keyboard only opens when you tap it.
 */
@Composable
fun TryTypingBar(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    val editText = remember {
        EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
            imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN
            maxLines = 4
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setHint(R.string.settings_try_typing_here)
        }
    }

    LaunchedEffect(containerColor, textColor, primaryColor) {
        val radius = with(density) { 24.dp.toPx() }
        val horizontal = with(density) { 20.dp.roundToPx() }
        val vertical = with(density) { 12.dp.roundToPx() }
        editText.background = GradientDrawable().apply {
            cornerRadius = radius
            setColor(containerColor.toArgb())
        }
        editText.setPadding(horizontal, vertical, horizontal, vertical)
        editText.minHeight = with(density) { 48.dp.roundToPx() }
        editText.setTextColor(textColor.toArgb())
        editText.setHintTextColor(textColor.copy(alpha = 0.6f).toArgb())
        editText.highlightColor = primaryColor.copy(alpha = 0.4f).toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            editText.textCursorDrawable?.setTint(primaryColor.toArgb())
            editText.textSelectHandle?.setTint(primaryColor.toArgb())
            editText.textSelectHandleLeft?.setTint(primaryColor.toArgb())
            editText.textSelectHandleRight?.setTint(primaryColor.toArgb())
        }
    }

    AndroidView(
        factory = { editText },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
