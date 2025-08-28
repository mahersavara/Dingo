package io.sukhuat.dingo.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.ButtonCornerRadius
import io.sukhuat.dingo.common.theme.DustyRose
import io.sukhuat.dingo.common.theme.InputFieldHeight
import io.sukhuat.dingo.common.theme.MountainShadow
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.theme.SpaceSmall

/**
 * A reusable text field component with Mountain Sunrise design system
 * @param value Current text value
 * @param onValueChange Called when the text changes
 * @param modifier Modifier to be applied to the text field
 * @param label Optional label for the text field
 * @param placeholder Optional placeholder text
 * @param leadingIcon Optional icon to display at the start of the text field
 * @param trailingIcon Optional icon/action to display at the end of the text field
 * @param isError Whether the text field is in an error state
 * @param errorText Error message to display when isError is true
 * @param visualTransformation Transformation to apply to the input text (e.g., for password fields)
 * @param keyboardOptions Options controlling keyboard behavior
 * @param keyboardActions Actions to perform based on keyboard input
 * @param singleLine Whether the text field should be a single line
 * @param maxLines Maximum number of lines
 * @param enabled Whether the text field is enabled
 * @param readOnly Whether the text field is read-only
 */
@Composable
fun DingoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(InputFieldHeight),
            label = label?.let { { Text(text = it) } },
            placeholder = placeholder?.let { { Text(text = it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            readOnly = readOnly,
            shape = RoundedCornerShape(ButtonCornerRadius), // Using 0dp corner radius for crisp edges
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = RusticGold,
                unfocusedIndicatorColor = MountainShadow,
                errorIndicatorColor = DustyRose,
                cursorColor = RusticGold,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                focusedLabelColor = RusticGold,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                errorLabelColor = DustyRose,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )

        if (isError && !errorText.isNullOrEmpty()) {
            Text(
                text = errorText,
                color = DustyRose,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = SpaceSmall, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoTextFieldPreview() {
    MountainSunriseTheme {
        DingoTextField(
            value = "",
            onValueChange = {},
            label = "Email",
            placeholder = "Enter your email",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DingoTextFieldErrorPreview() {
    MountainSunriseTheme {
        DingoTextField(
            value = "invalid email",
            onValueChange = {},
            label = "Email",
            isError = true,
            errorText = "Please enter a valid email address",
            modifier = Modifier.padding(16.dp)
        )
    }
}
