package org.example.mirai.plugin

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.isOperator
import java.io.ByteArrayInputStream

val YosPermission by lazy {
    PermissionService.INSTANCE.register(
        YosPluginMain.permissionId("yos-permission"), "早报功能管理权限（群管理员默认已有）",
        YosPluginMain.parentPermission
    )
}

object YosCommand : SimpleCommand(
    YosPluginMain, "早报",
    description = "早报功能",
    parentPermission = YosPluginMain.parentPermission
) {
    @ExperimentalCommandDescriptors
    @ConsoleExperimentalApi
    override val prefixOptional: Boolean = true

    @Handler
    suspend fun MemberCommandSenderOnMessage.handle(operation: String, extraContent: String? = null) {
        if (!hasPermission()) {
            return
        }
        when (operation) {
            "开启" -> {
                if (!checkAPI()) {
                    sendMessage("未配置 API，请先发送 /早报 配置 [API链接] 进行配置")
                    return
                }
                val groupId = this.group.id
                YosPluginMain.PluginConfig.groups.add(groupId)
                sendMessage("每日早报已开启")
            }

            "关闭" -> {
                val groupId = this.group.id
                YosPluginMain.PluginConfig.groups.remove(groupId)
                sendMessage("每日早报已关闭")
            }

            "刷新" -> {
                if (!checkAPI()) {
                    sendMessage("未配置 API，请先发送 /早报 配置 [API链接] 进行配置")
                    return
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
                if (operation == "配置") {
                    if (extraContent == null) {
                        sendMessage("空参，请检查格式")
                    } else {
                        YosPluginMain.PluginConfig.api = extraContent
                        sendMessage("当前 API 配置为：${YosPluginMain.PluginConfig.api}")
                    }
                }
            }
        }
    }

    private fun checkAPI(): Boolean {
        return YosPluginMain.PluginConfig.api.isNotBlank()
    }

    private fun MemberCommandSenderOnMessage.hasPermission(): Boolean {
        val perm = AbstractPermitteeId.ExactMember(this.group.id, this.user.id).hasPermission(YosPermission)
        val operator = this.user.isOperator()
        return perm || operator
    }
}