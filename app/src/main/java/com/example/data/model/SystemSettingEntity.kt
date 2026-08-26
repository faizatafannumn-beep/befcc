package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_settings")
data class SystemSettingEntity(
    @PrimaryKey val key: String,
    val value: String,
    val description: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val KEY_OFFICIAL_CONTACT_NUMBER = "official_contact_number"
        const val KEY_ORGANIZATION_NAME = "organization_name"
    }
}
