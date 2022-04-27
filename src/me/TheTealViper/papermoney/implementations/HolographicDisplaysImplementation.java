package me.TheTealViper.papermoney.implementations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.persistence.PersistentDataType;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import me.TheTealViper.papermoney.PaperMoney;

public class HolographicDisplaysImplementation {
	public static PaperMoney plugin = null;
	public static Map<UUID, Hologram> holographDatabase = new HashMap<UUID, Hologram>();
	
	public static void createHologram(UUID entityUuid, double worth) {
		createHologram((Item) Bukkit.getEntity(entityUuid), worth);
	}
	//Use the item version if possible for speed
	public static void createHologram(Item item, double worth) {
		plugin.tracking.add(item.getUniqueId());
    	plugin.amountMap.put(item.getUniqueId(), worth);
    	plugin.itemScheduleDatabase.put(item.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {public void run() {
			if(plugin.tracking.contains(item.getUniqueId()))
				HolographicDisplaysImplementation.handleTick(item);
			else{
				Bukkit.getScheduler().cancelTask(plugin.itemScheduleDatabase.get(item.getUniqueId()));
				plugin.itemScheduleDatabase.remove(item.getUniqueId());
				if(HolographicDisplaysImplementation.holographDatabase.containsKey(item.getUniqueId())) {
					HolographicDisplaysImplementation.holographDatabase.get(item.getUniqueId()).delete();
					HolographicDisplaysImplementation.holographDatabase.remove(item.getUniqueId());
				}
			}
		}}, 0L, 1L));
	}
	
	public static void destroyHologram(UUID entityUuid) {
		if (!plugin.tracking.contains(entityUuid)) return;
		Hologram holo = holographDatabase.get(entityUuid);
		holo.delete();
		plugin.tracking.remove(entityUuid);
    	plugin.amountMap.remove(entityUuid);
	}
	
	public static void updateHologram(UUID entityUuid, int newStackSize) {
		if(!holographDatabase.containsKey(entityUuid))
			return;
		
		updateHologram((Item) Bukkit.getEntity(entityUuid), newStackSize);
	}
	//Use the item version if possible for speed
	public static void updateHologram(Item item, int newStackSize) {
		if(!holographDatabase.containsKey(item.getUniqueId()))
			return;
		
		Hologram holo = holographDatabase.get(item.getUniqueId());
		holo.clearLines();
		holo.appendTextLine(ChatColor.GREEN + "$" + ChatColor.GOLD + plugin.getStringFormattedDecimal(plugin.amountMap.get(item.getUniqueId())*newStackSize));
	}
	
	public static void handleTick(UUID entityUuid){
		handleTick((Item) Bukkit.getEntity(entityUuid));
	}
	//Use item version if possible for speed
	public static void handleTick(Item item) {
		if(!holographDatabase.containsKey(item.getUniqueId())){
			if(plugin.getServer().getEntity(item.getUniqueId()) == null) {
				return;
			}
			Hologram holo = HologramsAPI.createHologram(plugin, plugin.getServer().getEntity(item.getUniqueId()).getLocation().clone().add(0, 1, 0));
			holo.appendTextLine(ChatColor.GREEN + "$" + ChatColor.GOLD + plugin.getStringFormattedDecimal(plugin.amountMap.get(item.getUniqueId())*item.getItemStack().getAmount()));
			holographDatabase.put(item.getUniqueId(), holo);
		}else{
			if(plugin.getServer().getEntity(item.getUniqueId()) == null) {
				holographDatabase.get(item.getUniqueId()).delete();
				holographDatabase.remove(item.getUniqueId());
			}
			else {
				Hologram holo = holographDatabase.get(item.getUniqueId());
				holo.teleport(item.getLocation().clone().add(0, 1, 0));
			}
		}
	}
	
	public static void handleMerge(UUID entityUUIDDestroyed, UUID entityUUIDStaying) {
		//If neither item has a hologram than plugin ignores event
		if(!(holographDatabase.containsKey(entityUUIDDestroyed) || holographDatabase.containsKey(entityUUIDStaying)))
			return;
		
		handleMerge((Item) Bukkit.getEntity(entityUUIDDestroyed), (Item) Bukkit.getEntity(entityUUIDStaying));
	}
	//Use item version if possible for speed
	public static void handleMerge(Item itemDestroyed, Item itemStaying) {
		//If neither item has a hologram than plugin ignores event
				if(!(holographDatabase.containsKey(itemDestroyed.getUniqueId()) || holographDatabase.containsKey(itemStaying.getUniqueId())))
					return;
				
				// If both items have holograms, destroy old item's and update new item's
				if(holographDatabase.containsKey(itemDestroyed.getUniqueId()) && holographDatabase.containsKey(itemStaying.getUniqueId())){
					destroyHologram(itemDestroyed.getUniqueId());
					updateHologram(itemStaying, itemDestroyed.getItemStack().getAmount()+itemStaying.getItemStack().getAmount());
				}
				
				//If only the new item has a hologram, update it
				else if(holographDatabase.containsKey(itemStaying.getUniqueId())) {
					updateHologram(itemStaying, itemDestroyed.getItemStack().getAmount()+itemStaying.getItemStack().getAmount());
				}
				
				//If only the old item has a hologram, destroy and create an updated hologram on the new one
				else if(holographDatabase.containsKey(itemDestroyed.getUniqueId())) {
					if(itemDestroyed == null || itemDestroyed.getType().equals(Material.AIR))
		                return;
		            if(!itemDestroyed.getItemStack().hasItemMeta())
		            	return;
		            NamespacedKey key = new NamespacedKey(plugin, "value");
		            if(!itemDestroyed.getItemStack().getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.DOUBLE))
		            	return;
		            
		            double worth = itemDestroyed.getItemStack().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
					destroyHologram(itemDestroyed.getUniqueId());
					createHologram(itemStaying, worth*(itemDestroyed.getItemStack().getAmount()+itemStaying.getItemStack().getAmount()));
				}
	}
	
}
