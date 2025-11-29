package neko.neko.bedwarsscoreboardaddon.utils;



import org.bukkit.Bukkit;

import org.bukkit.ChatColor;

import org.bukkit.entity.Player;



import me.clip.placeholderapi.PlaceholderAPI;



public class PlaceholderAPIUtil {



	public static String setPlaceholders(Player player, String text) {

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {

			String result = PlaceholderAPI.setPlaceholders(player, text);

			// 处理PlaceholderAPI返回的文本中的颜色代码

			return ChatColor.translateAlternateColorCodes('&', result);

		}

		return ChatColor.translateAlternateColorCodes('&', text);

	}

}
