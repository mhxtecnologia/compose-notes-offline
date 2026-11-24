package com.mhxtecnologia.notes.core.model

import java.time.Instant

/**
 * Nota como o resto do app a enxerga, sem saber como ela é guardada.
 * A entidade do Room vive no :core:database e é convertida para este tipo.
 */
data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
