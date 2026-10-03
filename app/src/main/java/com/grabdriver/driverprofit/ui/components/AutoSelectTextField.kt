package com.grabdriver.driverprofit.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * OutlinedTextField ที่ไฮไลต์เลือกข้อความทั้งหมด (Select-All) อัตโนมัติเมื่อแตะหรือโฟกัส
 * ช่วยให้คนขับสามารถพิมพ์ตัวเลขใหม่ทับของเดิมได้ทันทีโดยไม่ต้องกด Backspace ลบทีละตัว
 */
@Composable
fun AutoSelectTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    isError: Boolean = false,
    enabled: Boolean = true,
    onBlur: (() -> Unit)? = null
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    // ซิงค์ข้อความภายในเมื่อค่าภายนอกเปลี่ยน (เช่น ค่าที่สืบทอดมาจากรอบก่อน หรือกดชิปปรับค่า)
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    var wasFocused by remember { mutableStateOf(false) }
    var justGainedFocus by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newVal ->
            if (justGainedFocus && newVal.text == textFieldValue.text) {
                // จังหวะแตะครั้งแรกหลังจากได้โฟกัส Compose มักจะพยายามย้ายเคอร์เซอร์ไปตรงจุดที่แตะ
                // เราคงสถานะ Select All ไว้เพื่อให้ตัวเลขทั้งหมดถูกไฮไลต์
                justGainedFocus = false
                textFieldValue = newVal.copy(
                    selection = TextRange(0, newVal.text.length)
                )
            } else {
                justGainedFocus = false
                textFieldValue = newVal
                if (newVal.text != value) {
                    onValueChange(newVal.text)
                }
            }
        },
        modifier = modifier.onFocusChanged { focusState ->
            if (focusState.isFocused) {
                if (!wasFocused) {
                    wasFocused = true
                    justGainedFocus = true
                    if (textFieldValue.text.isNotEmpty()) {
                        textFieldValue = textFieldValue.copy(
                            selection = TextRange(0, textFieldValue.text.length)
                        )
                    }
                }
            } else {
                if (wasFocused) {
                    onBlur?.invoke()
                }
                wasFocused = false
                justGainedFocus = false
            }
        },
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = isError,
        enabled = enabled
    )
}
