package com.hideorhunt.ftop;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {
    public static void log(String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', "&3[&d" + Main.getPlugin(Main.class).getName() + "&3]&r " + msg);
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public static void debug(String msg) {
        log("&7[&eDEBUG&7]&r " + msg);
    }

    public static void warning(String msg) {
        log("&7[&cWARNING&7]&r " + msg);
    } 

    public static void info(String msg) {
        log("&7[&aINFO&7]&r " + msg);
    }    
    
	public void log(Level lvl, String msg) {
		if (lvl == Level.WARNING) {
			warning(msg);
		}
		
		if (lvl == Level.INFO) {
			debug(msg);
		}
	}
}