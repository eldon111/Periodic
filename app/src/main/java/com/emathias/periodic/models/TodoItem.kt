package com.emathias.periodic.models

import androidx.versionedparcelable.VersionedParcelize
import java.util.UUID

@VersionedParcelize
data class TodoItem(
    val text: String,
    val id: UUID = UUID.randomUUID(),
    val checked: Boolean = false
)
