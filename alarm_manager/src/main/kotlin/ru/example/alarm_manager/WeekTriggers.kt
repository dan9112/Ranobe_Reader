package ru.example.alarm_manager

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeekTriggers(
    val monday: Boolean = false,
    val tuesday: Boolean = false,
    val wednesday: Boolean = false,
    val thursday: Boolean = false,
    val friday: Boolean = false,
    val saturday: Boolean = false,
    val sunday: Boolean = false
) : Parcelable
