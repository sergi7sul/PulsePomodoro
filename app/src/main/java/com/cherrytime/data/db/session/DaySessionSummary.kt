package com.cherrytime.data.db.session

import androidx.room.ColumnInfo

data class DaySessionSummary(
    @ColumnInfo(name = "epochDay") val epochDay: Long,
    @ColumnInfo(name = "focusMinutes") val focusMinutes: Long,
    @ColumnInfo(name = "sessionCount") val sessionCount: Int,
)
