package mc.mec.commandtitle;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandTitle extends JavaPlugin {

    private String netMinecraftserver = "net.minecraft.server.";

    private Object enumTitle, enumSubtitle;
    private Constructor<?> constructorTitle, constructorTime;
    private Method methodChatSerializer, methodHandle, methodSendpacket;
    private Field fieldConnection;

    @Override
    public void onEnable() {
        // Plugin startup logic
        TitleSender();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    String prefix = "§e§l[CommandTitle]§f§l";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("ctitle")){
            if(args.length == 0){
                sender.sendMessage("§e==========" + prefix + "§e==========");
                sender.sendMessage("");
                sender.sendMessage("§d/ctitle <main> | <sub> | <time>");
                sender.sendMessage("");
                sender.sendMessage("§e===================================");
                return false;
            }
            if(!sender.hasPermission("ctitle.use")){
                sender.sendMessage(prefix + "§c§lYou don't have permission.");
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
                setTime(player, 0, time, 0);
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL,1,1);
                sendTitle(player, main, sub);
            }
        }
        return false;
    }

    public void TitleSender() {
        try {
            String[] tmpPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
            String tmpVersion = tmpPackage[tmpPackage.length - 1] + ".";

            netMinecraftserver += tmpVersion;

            Class<?> tmpPacketPlayout = getNMSClass("PacketPlayOutTitle"),
                    tmpIchatBase = getNMSClass("IChatBaseComponent"),
                    tmpEnumTitleAction = getNMSClass("PacketPlayOutTitle$EnumTitleAction");

            enumTitle = tmpEnumTitleAction.getDeclaredField("TITLE").get(null);
            enumSubtitle = tmpEnumTitleAction.getDeclaredField("SUBTITLE").get(null);

            constructorTitle = tmpPacketPlayout.getConstructor(tmpEnumTitleAction, tmpIchatBase);
            constructorTime = tmpPacketPlayout.getConstructor(int.class, int.class, int.class);

            methodChatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer").getMethod("a", String.class);
            methodSendpacket = getNMSClass("PlayerConnection").getMethod("sendPacket", getNMSClass("Packet"));

            try {
                methodHandle = Class.forName("org.bukkit.craftbukkit." + tmpVersion + "entity.CraftPlayer")
                        .getMethod("getHandle");
            } catch (Exception e) {
                e.printStackTrace();
            }

            fieldConnection = getNMSClass("EntityPlayer").getField("playerConnection");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetTitle(Player player) {
        sendTitle(player, "", "");
    }

    public void sendTitle(Player player, String title, String subtitle) {
        try {
            if (title != null) {
                sendPacket(player, constructorTitle.newInstance(
                        enumTitle,
                        methodChatSerializer.invoke(null, "{\"text\":\"" + title + "\"}")));
            }

            if (subtitle != null) {
                sendPacket(player, constructorTitle.newInstance(
                        enumSubtitle,
                        methodChatSerializer.invoke(null, "{\"text\":\"" + subtitle + "\"}")));
            }
        } catch (IllegalAccessException
                | InstantiationException
                | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setTime(Player player, int feedIn, int titleShow, int feedOut) {
        try {
            sendPacket(player, constructorTime.newInstance(feedIn, titleShow, feedOut));
        } catch (IllegalAccessException
                | InstantiationException
                | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(Player player, Object packet) {
        try {
            methodSendpacket.invoke(fieldConnection.get(methodHandle.invoke(player)), packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Class<?> getNMSClass(String name) {
        try {
            return Class.forName(netMinecraftserver + name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
