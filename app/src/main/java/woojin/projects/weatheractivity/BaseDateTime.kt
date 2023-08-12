package woojin.projects.weatheractivity

import android.util.Log
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class BaseDateTime(
    val baseDate: String,
    val baseTime: String,
) {
    companion object {
        fun getBaseDateTime(): BaseDateTime {
            var dateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
            Log.d("maybe", dateTime.toLocalTime().toString())

            val baseTime = when (dateTime.toLocalTime()) {
                in LocalTime.of(0, 0)..LocalTime.of(2, 30) -> {
                    dateTime = dateTime.minusDays(1)
                    "2300"
                }
                in LocalTime.of(2, 30)..LocalTime.of(5, 30) -> "0200"
                in LocalTime.of(5, 30)..LocalTime.of(8, 30) -> "0500"
                in LocalTime.of(8, 30)..LocalTime.of(11, 30) -> "0800"
                in LocalTime.of(11, 30)..LocalTime.of(14, 30) -> "1100"
                in LocalTime.of(14, 30)..LocalTime.of(17, 30) -> "1400"
                in LocalTime.of(17, 30)..LocalTime.of(20, 30) -> "1700"
                in LocalTime.of(20, 30)..LocalTime.of(23, 30) -> "2000"
                else -> "2300"
            }
            //baseTime값 기준 1시간 주기로 예상일보
            val baseDate = String.format(
                "%04d%02d%02d",
                dateTime.year,
                dateTime.monthValue,
                dateTime.dayOfMonth
            )
            return BaseDateTime(baseDate, baseTime)
        }
    }
}