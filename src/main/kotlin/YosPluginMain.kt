package org.example.mirai.plugin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.utils.info
import java.io.ByteArrayInputStream
import java.util.TimerTask

object YosPluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "yos.code.morning-report",
        name = "Morning Report",
        version = "1.0.0"
    ) {
        author("Yos-X")
        info(
            "每天早上向已设置的群聊推送早报".trimIndent()
        )
    }
) {
    private var lastImageMd5: String? = null

    object PluginConfig : AutoSavePluginConfig("MorningReport") {
        var api by value("")
        val groups: MutableSet<Long> by value(mutableSetOf())
    }

    override fun onEnable() {
        logger.info { "早报插件 已载入" }
        YosPermission
        PluginConfig.reload()
        YosCommand.register()

        val startTask = object : TimerTask() {
            override fun run() = runBlocking {
                for (i in 0 until 10) {
                    //最多尝试十次
                    val image = fetchImage()
                    val md5 = if (image != null) md5(image) else lastImageMd5
                    if (md5 != lastImageMd5) {
                        lastImageMd5 = md5
                        if (image != null) {
                            Bot.instances.forEach {
                                launch {
                                    withContext(Dispatchers.IO) {
                                        for (groupId in PluginConfig.groups) {
                                            val group = it.getGroup(groupId)
                                            if (group != null) {
                                                group.sendImage(ByteArrayInputStream(image))
                                            }
                                            delay(100)
                                            //防风控
                                        }
                                    }
                                }
                            }
                            return@runBlocking
                        }
                    } else {
                        delay(60 * 10000)
                        //一分钟后重试
                    }
                }
                for (groupId in PluginConfig.groups) {
                    Bot.instances.forEach {
                        val group = it.getGroup(groupId)
                        if (group != null) {
                            group.sendMessage("获取早报图片出错，请检查 API 配置")
                        }
                    }
                }
            }
        }

        //即从零点到 7:30
        newDailyTask((7 * 60 * 60 * 1000) + (30 * 60 * 1000), startTask)
        //新建每日任务

    }

    override fun onDisable() {
        logger.info { "早报插件 已卸载" }
    }
}
