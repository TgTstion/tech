package com.example.tech.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tech.data.Formality

/** Formality / slang level. Colour AND text label, so meaning never relies on colour alone. */
@Composable
fun FormalityChip(formality: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (label, container) = when (formality) {
        Formality.FORMAL -> "Formal" to scheme.secondaryContainer
        Formality.INFORMAL -> "Informal" to scheme.tertiaryContainer
        Formality.SLANG -> "Slang" to scheme.primaryContainer
        else -> "Neutral" to scheme.surfaceVariant
    }
    Surface(color = container, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** The "context" half of a card: two examples plus the anti-example. */
@Composable
fun ContextBlock(
    example1: String,
    example2: String,
    antiExample: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Examples", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Text("1. $example1", style = MaterialTheme.typography.bodyLarge)
        Text("2. $example2", style = MaterialTheme.typography.bodyLarge)
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    "Don't say",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    antiExample,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}
