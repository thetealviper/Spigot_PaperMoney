package me.TheTealViper.papermoney;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class holographShit {
	public static main plugin = null;
	public static Map<UUID, Hologram> holographDatabase = new HashMap<UUID, Hologram>();
	
	public static void handle(UUID entityUuid){
		if(!plugin.getConfig().getBoolean("Use_Holograms_If_Possible"))
			return;
		if(!holographDatabase.containsKey(entityUuid)){
			if(plugin.getServer().getEntity(entityUuid) == null) {
				return;
			}
			Hologram holo = HologramsAPI.createHologram(plugin, plugin.getServer().getEntity(entityUuid).getLocation().clone().add(0, 1, 0));
			holo.appendTextLine(ChatColor.GREEN + "$" + ChatColor.GOLD + plugin.amountMap.get(entityUuid));
			holographDatabase.put(entityUuid, holo);
		}else{
			if(plugin.getServer().getEntity(entityUuid) == null) {
				holographDatabase.get(entityUuid).delete();
				holographDatabase.remove(entityUuid);
			}
			else {
				Hologram holo = holographDatabase.get(entityUuid);
				holo.teleport(plugin.getServer().getEntity(entityUuid).getLocation().clone().add(0, 1, 0));
			}
		}
	}
}
