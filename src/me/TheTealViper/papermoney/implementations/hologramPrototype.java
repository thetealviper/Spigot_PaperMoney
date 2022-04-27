package me.TheTealViper.papermoney.implementations;

import java.util.UUID;

import org.bukkit.entity.Item;

import me.TheTealViper.papermoney.PaperMoney;

public class hologramPrototype {
	public static PaperMoney plugin = null;
	
	public static void createHologram(UUID entityUuid, double worth) {
		if(plugin.foundDecentHolograms) {
			DecentHologramsImplementation.createHologram(entityUuid, worth);
		}else if(plugin.foundHolographicDisplays) {
			HolographicDisplaysImplementation.createHologram(entityUuid, worth);
		} else {
			//Do Nothing ATM
		}
	}
	//Use the item version if possible for speed
	public static void createHologram(Item item, double worth) {
		if(plugin.foundDecentHolograms) {
			DecentHologramsImplementation.createHologram(item, worth);
		}else if(plugin.foundHolographicDisplays) {
			HolographicDisplaysImplementation.createHologram(item, worth);
		} else {
			//Do Nothing ATM
		}
	}
	
	public static void destroyHologram(UUID entityUuid) {
		if(plugin.foundDecentHolograms) {
			DecentHologramsImplementation.destroyHologram(entityUuid);
		}else if(plugin.foundHolographicDisplays) {
			HolographicDisplaysImplementation.destroyHologram(entityUuid);
		} else {
			//Do Nothing ATM
		}
	}
	
	public static void updateHologram(UUID entityUuid, int newStackSize) {
		if(plugin.foundDecentHolograms) {
			DecentHologramsImplementation.updateHologram(entityUuid, newStackSize);
		}else if(plugin.foundHolographicDisplays) {
			HolographicDisplaysImplementation.updateHologram(entityUuid, newStackSize);
		} else {
			//Do Nothing ATM
		}
	}
	//Use the item version if possible for speed
	public static void updateHologram(Item item, int newStackSize) {
		if(plugin.foundDecentHolograms) {
			DecentHologramsImplementation.updateHologram(item, newStackSize);
		}else if(plugin.foundHolographicDisplays) {
			HolographicDisplaysImplementation.updateHologram(item, newStackSize);
		} else {
			//Do Nothing ATM
		}
	}
	
	public static void handleTick(UUID entityUuid){
		if(plugin.foundDecentHolograms) {
			DecentHologramsImplementation.handleTick(entityUuid);
		}else if(plugin.foundHolographicDisplays) {
			HolographicDisplaysImplementation.handleTick(entityUuid);
		} else {
			//Do Nothing ATM
		}
	}
	//Use item version if possible for speed
	public static void handleTick(Item item) {
		if(plugin.foundDecentHolograms) {
			DecentHologramsImplementation.handleTick(item);
		}else if(plugin.foundHolographicDisplays) {
			HolographicDisplaysImplementation.handleTick(item);
		} else {
			//Do Nothing ATM
		}
	}
	
	public static void handleMerge(UUID entityUUIDDestroyed, UUID entityUUIDStaying) {
		if(plugin.foundDecentHolograms) {
			DecentHologramsImplementation.handleMerge(entityUUIDDestroyed, entityUUIDStaying);
		}else if(plugin.foundHolographicDisplays) {
			HolographicDisplaysImplementation.handleMerge(entityUUIDDestroyed, entityUUIDStaying);
		} else {
			//Do Nothing ATM
		}
	}
	//Use item version if possible for speed
	public static void handleMerge(Item itemDestroyed, Item itemStaying) {
		if(plugin.foundDecentHolograms) {
			DecentHologramsImplementation.handleMerge(itemDestroyed, itemStaying);
		}else if(plugin.foundHolographicDisplays) {
			HolographicDisplaysImplementation.handleMerge(itemDestroyed, itemStaying);
		} else {
			//Do Nothing ATM
		}
	}
	
}
