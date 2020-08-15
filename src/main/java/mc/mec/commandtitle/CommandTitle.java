package mc.mec.commandtitle;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;

public class CommandTitle extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    String prefix = "§e§l[§d§lMan10TitleBar§e§l]§f§l";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("ctitle")){
            if(args.length == 0){
                sender.sendMessage("§e==========" + prefix + "§e==========");
                sender.sendMessage("");
                sender.sendMessage("§d/mtitle <main> | <sub> | <time>");
                sender.sendMessage("");
                sender.sendMessage("§e===================================");
                sender.sendMessage("§d§lCreated By Sho0");
                return false;
            }
            if(!sender.hasPermission("man10.title")){
                sender.sendMessage(prefix + "あなたには権限がありません");
                return false;
            }
            boolean skip = false;
            int time = 50;
            String main = "";
            String mode = "main";
            String sub = "";
            for(int i = 0;i < args.length;i++){
                if(mode.equals("main")) {
                    if(args[i].equals("|") && !skip){
                        mode = "sub";
                        skip = true;
                    }
                    main = main + " " + args[i].replaceAll("&","§").replace("|","");
                }
                if(mode.equals("sub")){
                    if(args[i].equals("|") && !skip){
                        mode = "time";
                        skip = true;
                    }
                    skip = false;
                    sub = sub + " " + args[i].replaceAll("&","§").replace("|","");
                }
                if(mode.equals("time")){
                    if(!skip) {
                        try {
                            time = Integer.parseInt(args[i]) * 20;
                        } catch (NumberFormatException e) {
                        }
                    }
                    skip = false;
                }
            }
            for(Player player : Bukkit.getOnlinePlayers()){
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL,1,1);
                sendTitle(player, main, sub, 0, time, 0);
            }
        }
        return false;
    }

    public void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server."
                    + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendTitle(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
        try {
            Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + title + "\"}");
            Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
                    int.class, int.class, int.class);
            Object packet = titleConstructor.newInstance(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle,
                    fadeInTime, showTime, fadeOutTime);

            Object chatsTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + subtitle + "\"}");
            Constructor<?> timingTitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
                    int.class, int.class, int.class);
            Object timingPacket = timingTitleConstructor.newInstance(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null), chatsTitle,
                    fadeInTime, showTime, fadeOutTime);

            sendPacket(player, packet);
            sendPacket(player, timingPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
