package org.example.mirai.plugin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest
import java.time.ZoneId
import java.util.Date
import java.util.Timer
import java.util.TimerTask

private val startTimer = Timer()
private val oneDayMill = 24 * 60 * 60 * 1000L
private val zeroTime = Date.from(
    Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
)!!

fun newDailyTask(startTime: Long, startTask: TimerTask) {
    val dailyStartTime: Date = Date(
        zeroTime.time + startTime
    )
    if (dailyStartTime.before(Date())) {
        dailyStartTime.time += oneDayMill
        startTimer.schedule(startTask, dailyStartTime, oneDayMill)
    } else {
        startTimer.schedule(startTask, dailyStartTime, oneDayMill)
    }
}


suspend fun fetchImage(): ByteArray? = withContext(Dispatchers.IO) {
    runCatching {
        URL(YosPluginMain.PluginConfig.api).openStream().readAllBytes()
    }.getOrNull()
}

fun md5(input: ByteArray): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input)).toString(16).padStart(32, '0')
}