package mc.mec.commandtitle

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Constructor


class CommandTitle : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    val prefix = "§b[CommandTitle]"

    var skip = false
    var time = 50
    var main = ""
    var mode = "main"
    var sub = ""

    override fun onCommand(sender: CommandSender, command: Command, label: String?, args: Array<String>): Boolean {
        if (command.name == "ctitle") {
            if (args.isEmpty()) {
                sender.sendMessage("§e==========$prefix§e==========")
                sender.sendMessage("")
                sender.sendMessage("§d/ctitle <main> | <sub> | <time>")
                sender.sendMessage("")
                sender.sendMessage("§e===================================")
                return false
            }
            if (!sender.hasPermission("ctitle.use")) {
                sender.sendMessage(prefix + "あなたには権限がありません")
                return false
            }
            for (i in args.indices) {
                if (mode == "main") {
                    if (args[i] == "|" && !skip) {
                        mode = "sub"
                        skip = true
                    }
                    main = main + " " + args[i].replace("&".toRegex(), "§").replace("|", "")
                }
                if (mode == "sub") {
                    if (args[i] == "|" && !skip) {
                        mode = "time"
                        skip = true
                    }
                    skip = false
                    sub = sub + " " + args[i].replace("&".toRegex(), "§").replace("|", "")
                }
                if (mode == "time") {
                    if (!skip) {
                        try {
                            time = args[i].toInt() * 20
                        } catch (e: NumberFormatException) {
                        }
                    }
                    skip = false
                }
            }
            for (player in Bukkit.getOnlinePlayers()) {
                sendTitle(player, main, sub, 10, time, 10)
            }
        }
        return false
    }

    fun sendTitle(player: Player?, main: String, sub: String, fadeInTime: Int, showTime: Int, fadeOutTime: Int) {
        try {
            val chatTitle = getNMSClass("IChatBaseComponent")!!.declaredClasses[0].getMethod("a", String::class.java)
                    .invoke(null, "{\"text\": \"$main\"}")
            val titleConstructor: Constructor<*> = getNMSClass("PacketPlayOutTitle")!!.getConstructor(
                    getNMSClass("PacketPlayOutTitle")!!.declaredClasses[0], getNMSClass("IChatBaseComponent"),
                    Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            val packet: Any = titleConstructor.newInstance(
                    getNMSClass("PacketPlayOutTitle")!!.declaredClasses[0].getField("TITLE")[null], chatTitle,
                    fadeInTime, showTime, fadeOutTime)
            val chatsTitle = getNMSClass("IChatBaseComponent")!!.declaredClasses[0].getMethod("a", String::class.java)
                    .invoke(null, "{\"text\": \"$sub\"}")
            val timingTitleConstructor: Constructor<*> = getNMSClass("PacketPlayOutTitle")!!.getConstructor(
                    getNMSClass("PacketPlayOutTitle")!!.declaredClasses[0], getNMSClass("IChatBaseComponent"),
                    Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            val timingPacket: Any = timingTitleConstructor.newInstance(
                    getNMSClass("PacketPlayOutTitle")!!.declaredClasses[0].getField("SUBTITLE")[null], chatsTitle,
                    fadeInTime, showTime, fadeOutTime)
            sendPacket(player!!, packet)
            sendPacket(player, timingPacket)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun sendPacket(player: Player, packet: Any?) {
        try {
            val handle = player.javaClass.getMethod("getHandle").invoke(player)
            val playerConnection = handle.javaClass.getField("playerConnection")[handle]
            playerConnection.javaClass.getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getNMSClass(name: String): Class<*>? {
        try {
            return Class.forName("net.minecraft.server."
                    + Bukkit.getServer().javaClass.getPackage().name.split("\\.".toRegex()).toTypedArray()[3] + "." + name)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        return null
    }
}