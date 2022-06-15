package me.TheTealViper.papermoney.implementations;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderAPIImplementation {
	public static String insertPlaceholders(Player p, String s) {
		return PlaceholderAPI.setPlaceholders(p, s);
	}
	public static String insertPlaceholders(OfflinePlayer p, String s) {
		return PlaceholderAPI.setPlaceholders(p, s);
	}
}
