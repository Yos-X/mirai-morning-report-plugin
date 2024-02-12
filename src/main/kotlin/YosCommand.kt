package org.example.mirai.plugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import java.io.ByteArrayInputStream


val YosPermission by lazy {
    PermissionService.INSTANCE.register(
        YosPluginMain.permissionId("yos-permission"), "早报功能管理权限（群主 / 管理员默认已有）",
        YosPluginMain.parentPermission
    )
}

val commandName = "早报"

@OptIn(ExperimentalCommandDescriptors::class)
fun registerYosCommandResolver(coroutineScope: CoroutineScope) {
    val eventChannel = GlobalEventChannel.parentScope(coroutineScope)
    eventChannel.subscribeAlways<GroupMessageEvent> {
        val command = message.contentToString().trim()
        if (command.startsWith(commandName) || command.startsWith("/$commandName")) {
            if (!hasPermission(group.id, user = sender)) {
                YosPluginMain.logger.warning("${sender.id} 没有权限，请检查是否为群主或管理员。亦可使用 /perm add u${sender.id} yos.code.morning-report:yos-permission 为其授予权限")
                return@subscribeAlways
            }
            val commandList = command.replace(Regex("\\s+"), " ").split(" ")
            when (commandList.size) {
                1 -> {
                    group.sendMessage(
                        "指令不完整，是否想输入：\n" +
                            "- (/)早报 开启\n" +
                            "- (/)早报 关闭\n" +
                            "- (/)早报 刷新\n" +
                            "- (/)早报 配置 [API链接]\n" +
                            "每日早报 1.0.0 By Yos-X"
                    )
                }

                2 -> {
                    val operation = commandList[1]
                    when (operation) {
                        "开启" -> {
                            if (!checkAPI()) {
                                group.sendMessage("未配置 API，请先发送 /早报 配置 [API链接] 进行配置")
                                return@subscribeAlways
                            }
                            val groupId = this.group.id
                            YosPluginMain.PluginConfig.groups.add(groupId)
                            group.sendMessage("每日早报已开启")
                        }

                        "关闭" -> {
                            val groupId = this.group.id
                            YosPluginMain.PluginConfig.groups.remove(groupId)
                            group.sendMessage("每日早报已关闭")
                        }

                        "刷新" -> {
                            if (!checkAPI()) {
                                group.sendMessage("未配置 API，请先发送 /早报 配置 [API链接] 进行配置")
                                return@subscribeAlways
                            }
                            val group = this.group
                            runBlocking {
                                val image = fetchImage()
                                if (image == null) {
                                    group.sendMessage("获取早报图片出错，请检查 API 配置")
                                } else {
                                    group.sendImage(ByteArrayInputStream(image))
                                }
                            }
                        }

                        else -> {
                            group.sendMessage("未知命令")
                        }
                    }
                }

                3 -> {
                    val operation = commandList[1]
                    val extraContent = commandList[2]
                    if (operation == "配置") {
                        YosPluginMain.PluginConfig.api = extraContent
                        group.sendMessage("当前 API 配置为：${YosPluginMain.PluginConfig.api}")
                    }
                }

                else -> {
                    group.sendMessage("入参项过多，请检查格式")
                }
            }
        }
    }
}


private fun checkAPI(): Boolean {
    return YosPluginMain.PluginConfig.api.isNotBlank()
}

private fun hasPermission(groupId: Long, user: Member): Boolean {
    val perm = AbstractPermitteeId.ExactMember(groupId, user.id).hasPermission(YosPermission)
    val operator = user.isOperator()
    return perm || operator
}