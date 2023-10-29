package me.TheTealViper.papermoney.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LoadEnhancedItemstackFromConfig implements Listener{
	private UtilityEquippedJavaPlugin plugin = null;
	public NamespacedKey KEY_VALUE, KEY_DAMAGE, KEY_FORCESTACK;
	
	//TODO
		//- Make stacksize work
		//- Make isSimilar() check persistentinfo
		//- Custom durability
		//- placeholder support (custom+placeholderapi)
	
	/**
	 * id: DIRT
	 * amount: 1
	 * name: "Dirt"
	 * lore:
	 *  - "1"
	 *  - "2"
	 * enchantments:
	 * 	- "arrowdamage:1"
	 *  - "arrowfire:1"
	 *  - "arrowinfinite:1"
	 *  - "arrowknockback:1"
	 *  - "damage:1"
	 *  - "digspeed:1"
	 *  - "durability:1"
	 *  - "fireaspect:1"
	 *  - "knockback:1"
	 *  - "lootbonusblock:1"
	 *  - "lootbonusmob:1"
	 *  - "luck:1"
	 *  - "protectionfall:1"
	 *  - "protectionfire:1"
	 *  - "silktouch:1"
	 * tags:
	 *  - "playerskullskin:SKINVALUE" //Do note that skulls will NOT stack properly or be considered "similar" because different UUID. Use Enhanced for UUID tracking.
	 *  - "vanilladurability:256"
	 *  - "unbreakable:true"
	 *  - "custommodeldata:1234567"
	 *  - "damage:100" //Enhanced Only
	 *  - "forcestack:100" //Enhanced Only
	 *  - "fakeenchant:true" //Adds enchant glow to item without any enchantments
	 * flags:
	 *  - "HIDE_ATTRIBUTES"
	 *  - "HIDE_DESTROYS"
	 *  - "HIDE_ENCHANTS"
	 *  - "HIDE_PLACED_ON"
	 *  - "HIDE_POTION_EFFECTS"
	 *  - "HIDE_UNBREAKABLE"
	 */
	
	public LoadEnhancedItemstackFromConfig(UtilityEquippedJavaPlugin plugin){
		this.plugin = plugin;
		KEY_VALUE = new NamespacedKey(plugin, "value");
		KEY_DAMAGE = new NamespacedKey(plugin, "damage");
		KEY_FORCESTACK = new NamespacedKey(plugin, "forcestack");
		
		//Register packet listener to handle some max stack size things
//		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SET_SLOT) {
//            @Override
//            public void onPacketSending(PacketEvent event) {
//            	PacketContainer packet = event.getPacket();
//            	StructureModifier<ItemStack> itemStructureModifier = packet.getItemModifier();
//            	ItemStack item = itemStructureModifier.read(0);
//        		if(item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(KEY_FORCESTACK, PersistentDataType.INTEGER))
//        			return;
//        		
//        		if(item.getAmount() > item.getItemMeta().getPersistentDataContainer().get(KEY_FORCESTACK, PersistentDataType.INTEGER)) {
//        			//Trigger a necessary inventory cleanup
//        			ItemStack clone = item.clone();
//        			clone.setAmount(1); //Set amount to 0 so we don't actually add anything, just cleanup what's there
//        			cleanInventoryOfItem(event.getPlayer(), event.getPlayer().getOpenInventory().getTopInventory(), clone);
//        			cleanInventoryOfItem(event.getPlayer(), event.getPlayer().getOpenInventory().getBottomInventory(), clone);
//        		}
//            }
//        });
	}
	
	public ItemStack getItem(ConfigurationSection sec) {
		//Null check
		if(sec == null)
			return null;
		ItemStack item = null;
		boolean modifiedMetaSoApply = false;
		
		//Handle ID
		item = (sec == null || !sec.contains("id")) ? null : new ItemStack(Material.getMaterial(sec.getString("id")));
		
		//Initiate Meta
		ItemMeta meta = item.getItemMeta();
		
		//Handle amount
		if(sec.contains("amount")) item.setAmount(sec.getInt("amount"));
		
		//Handle name
		if(sec.contains("name")) {meta.setDisplayName(plugin.getStringUtils().makeColors(sec.getString("name"))); modifiedMetaSoApply = true;}
		
		//Handle lore
		if(sec.contains("lore")) {
			List<String> dummy = sec.getStringList("lore");
			List<String> lore = new ArrayList<String>();
			for(String s : dummy) {
				lore.add(plugin.getStringUtils().makeColors(s));
			}
			meta.setLore(lore);
			modifiedMetaSoApply = true;
		}
		
		//Handle enchantments
		if(sec.contains("enchantments")) {
			List<String> enchantmentStrings = sec.getStringList("enchantments");
			for(String enchantmentString : enchantmentStrings) {
				String enchantmentName = enchantmentString.split(":")[0];
				int enchantmentLevel = Integer.valueOf(enchantmentString.split(":")[1]);
				switch(enchantmentName) {
					case "arrowdamage":
						meta.addEnchant(Enchantment.ARROW_DAMAGE, enchantmentLevel, true);
						break;
					case "arrowfire":
						meta.addEnchant(Enchantment.ARROW_FIRE, enchantmentLevel, true);
						break;
					case "arrowinfinite":
						meta.addEnchant(Enchantment.ARROW_INFINITE, enchantmentLevel, true);
						break;
					case "arrowknockback":
						meta.addEnchant(Enchantment.ARROW_KNOCKBACK, enchantmentLevel, true);
						break;
					case "damage":
						meta.addEnchant(Enchantment.DAMAGE_ALL, enchantmentLevel, true);
						break;
					case "digspeed":
						meta.addEnchant(Enchantment.DIG_SPEED, enchantmentLevel, true);
						break;
					case "durability":
						meta.addEnchant(Enchantment.DURABILITY, enchantmentLevel, true);
						break;
					case "fireaspect":
						meta.addEnchant(Enchantment.FIRE_ASPECT, enchantmentLevel, true);
						break;
					case "knockback":
						meta.addEnchant(Enchantment.KNOCKBACK, enchantmentLevel, true);
						break;
					case "lootbonusblock":
						meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, enchantmentLevel, true);
						break;
					case "lootbonusmob":
						meta.addEnchant(Enchantment.LOOT_BONUS_MOBS, enchantmentLevel, true);
						break;
					case "luck":
						meta.addEnchant(Enchantment.LUCK, enchantmentLevel, true);
						break;
					case "protectionfall":
						meta.addEnchant(Enchantment.PROTECTION_FALL, enchantmentLevel, true);
						break;
					case "protectionfire":
						meta.addEnchant(Enchantment.PROTECTION_FALL, enchantmentLevel, true);
						break;
					case "silktouch":
						meta.addEnchant(Enchantment.SILK_TOUCH, enchantmentLevel, true);
						break;
				}
			}
			modifiedMetaSoApply = true;
		}
		
		//Handle vanilla tags
		if(sec.contains("tags")) {
			for(String tagString : sec.getStringList("tags")) {
				String[] tagStringProcessed = tagString.split(":");
				String tag = tagStringProcessed[0];
				String value = tagStringProcessed[1];
				switch(tag) {
					case "playerskullskin":
						JsonObject o = new JsonParser().parse(new String(Base64.decodeBase64(value))).getAsJsonObject();
						String skinUrl = o.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
						SkullMeta skullMeta = (SkullMeta) meta;
						PlayerProfile profile = Bukkit.createPlayerProfile(UUID.nameUUIDFromBytes(skinUrl.getBytes()));
						PlayerTextures textures = profile.getTextures();
						try {
							textures.setSkin(new URL(skinUrl));
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					    profile.setTextures(textures);
					    skullMeta.setOwnerProfile(profile);
					    meta = skullMeta;
						break;
					case "vanilladurability":
						Damageable dam = (Damageable) meta;
						dam.setDamage(Integer.valueOf(value));
						meta = (ItemMeta) dam;
						break;
					case "unbreakable":
						meta.setUnbreakable(Boolean.valueOf(value));
						break;
					case "custommodeldata":
						meta.setCustomModelData(Integer.valueOf(value));
						break;
					case "fakeenchant":
						ItemstackUtils.addEnchantmentGlow(meta);
						break;
				}
			}
			modifiedMetaSoApply = true;
		}
		
		//Handle vanilla flags
		if(sec.contains("flags")){
			for(String s : sec.getStringList("flags")){
				meta.addItemFlags(ItemFlag.valueOf(s));
			}
			modifiedMetaSoApply = true;
		}
		
		//The below order is important so the item put in the databases is the actual key item
		if(modifiedMetaSoApply) item.setItemMeta(meta);
		//Handle enhanced tags
		if(sec.contains("tags")) {
			for(String tagString : sec.getStringList("tags")) {
				String[] tagStringProcessed = tagString.split(":");
				String tag = tagStringProcessed[0];
				String value = tagStringProcessed[1];
				switch(tag) {
					case "damage":
						meta = item.getItemMeta();
                        meta.getPersistentDataContainer().set(KEY_DAMAGE, PersistentDataType.DOUBLE, Double.valueOf(value));
						item.setItemMeta(meta);
						break;
					case "forcestack":
						meta = item.getItemMeta();
                        meta.getPersistentDataContainer().set(KEY_FORCESTACK, PersistentDataType.INTEGER, Integer.valueOf(value));
						item.setItemMeta(meta);
						break;
				}
			}
		}
		return item;
	}
	
	public boolean isSimilar(ItemStack item1, ItemStack item2) {
		if(item2.getType() != item1.getType())
			return false;
		if(item2.hasItemMeta() != item1.hasItemMeta())
			return false;
		if(item2.hasItemMeta()) {
			ItemMeta item1Meta = item1.getItemMeta();
			ItemMeta item2Meta = item2.getItemMeta();
			
			if (item2Meta.hasDisplayName() != item1Meta.hasDisplayName())
				return false;
			if(item2Meta.hasDisplayName()) {
				if(!item2Meta.getDisplayName().equals(item1Meta.getDisplayName()))
					return false;
			}
			if (item2Meta.hasLore() != item1Meta.hasLore())
				return false;
			if (item2Meta.hasLore()) {
				for(int i = 0;i < item2Meta.getLore().size();i++) {
					if(!item2Meta.getLore().get(i).equals(item1Meta.getLore().get(i)))
						return false;
				}
			}
			if (item2Meta.hasEnchants() != item1Meta.hasEnchants())
				return false;
			if (item2Meta.hasEnchants()) {
                if (item2Meta.getEnchants().size() != item1Meta.getEnchants().size()) {
                    return false;
                }
                for (Entry<Enchantment, Integer> enchantInfo : item1Meta.getEnchants().entrySet()) {
                    if (item2Meta.getEnchantLevel(enchantInfo.getKey()) != item1Meta.getEnchantLevel(enchantInfo.getKey())) {
                        return false;
                    }
                }
            }
			if (item2Meta.getItemFlags().size() != item1Meta.getItemFlags().size())
				return false;
			for (ItemFlag flag : item2Meta.getItemFlags()) { //We can do this because we already know the itemflag list size is the same
                if (!item1Meta.hasItemFlag(flag)) {
                    return false;
                }
            }
			if((item2Meta instanceof Damageable) != (item1Meta instanceof Damageable))
				return false;
			if(item2Meta instanceof Damageable) {
				Damageable dam1 = (Damageable) item1Meta;
				Damageable dam2 = (Damageable) item2Meta;
				if(dam1.hasDamage() != dam2.hasDamage())
					return false;
				if(dam2.hasDamage()) {
					if(dam2.getDamage() != dam1.getDamage())
						return false;
				}
			}
			if(item2Meta.hasCustomModelData() != item1Meta.hasCustomModelData())
				return false;
			if(item2Meta.hasCustomModelData()) {
				if(item2Meta.getCustomModelData() != item1Meta.getCustomModelData())
					return false;
			}
			//Check persistent storage key/values for this plugin
			if(item2Meta.getPersistentDataContainer().has(KEY_VALUE, PersistentDataType.DOUBLE) != item1Meta.getPersistentDataContainer().has(KEY_VALUE, PersistentDataType.DOUBLE))
				return false;
			if(item2Meta.getPersistentDataContainer().has(KEY_VALUE, PersistentDataType.DOUBLE)) {
				//You have to EXPLICITLY make these getters into double variables or it breaks EVERYTHING if the values differ. I DON'T KNOW WHY.
				double val1 = item1Meta.getPersistentDataContainer().get(KEY_VALUE, PersistentDataType.DOUBLE);
				double val2 = item2Meta.getPersistentDataContainer().get(KEY_VALUE, PersistentDataType.DOUBLE);
				if(val2 != val1)
					return false;
			}
			if(item2Meta.getPersistentDataContainer().has(KEY_DAMAGE, PersistentDataType.DOUBLE) != item1Meta.getPersistentDataContainer().has(KEY_DAMAGE, PersistentDataType.DOUBLE))
				return false;
			if(item2Meta.getPersistentDataContainer().has(KEY_DAMAGE, PersistentDataType.DOUBLE)) {
				//You have to EXPLICITLY make these getters into double variables or it breaks EVERYTHING if the values differ. I DON'T KNOW WHY.
				double val1 = item1Meta.getPersistentDataContainer().get(KEY_DAMAGE, PersistentDataType.DOUBLE);
				double val2 = item2Meta.getPersistentDataContainer().get(KEY_DAMAGE, PersistentDataType.DOUBLE);
				if(val2 != val1)
					return false;
			}
			if(item2Meta.getPersistentDataContainer().has(KEY_FORCESTACK, PersistentDataType.INTEGER) != item1Meta.getPersistentDataContainer().has(KEY_FORCESTACK, PersistentDataType.INTEGER))
				return false;
			if(item2Meta.getPersistentDataContainer().has(KEY_FORCESTACK, PersistentDataType.INTEGER)) {
				if(item2Meta.getPersistentDataContainer().get(KEY_FORCESTACK, PersistentDataType.INTEGER) != item1Meta.getPersistentDataContainer().get(KEY_FORCESTACK, PersistentDataType.INTEGER))
					return false;
			}
		}
		return true;
	}
	
	public void giveCustomItem(Player p, Inventory inv, ItemStack item){
		int amount = item.getAmount();//Start at the item's amount because they're picking it up
		Inventory bufferInv = Bukkit.createInventory(null, 36);
		for(int i = 0;i < 36;i++) {
			bufferInv.setItem(i, item);
		}
		bufferInv.setContents(Arrays.copyOfRange(inv.getContents(), 0, 36));
		List<Integer> removedSlots = new ArrayList<Integer>();
		for(int i = 0;i < 36;i++){
			if(bufferInv.getItem(i) != null && isSimilar(bufferInv.getItem(i),item)){
				amount += bufferInv.getItem(i).getAmount();
				bufferInv.getItem(i).setAmount(0);
				removedSlots.add(i);
			}
		}
		int stackSize = getForceStackSize(item);
		while(amount > 0){
			if(amount > stackSize){
				ItemStack temp = item.clone();
				temp.setAmount(stackSize);
				if(!removedSlots.isEmpty()) {
					bufferInv.setItem(removedSlots.get(0), temp);
					removedSlots.remove(0);
				} else if(bufferInv.firstEmpty() != -1)
					bufferInv.setItem(bufferInv.firstEmpty(), temp);
				else {
					p.getWorld().dropItem(p.getLocation(), temp);
				}
				amount -= stackSize;
			}else{
				ItemStack temp = item.clone();
				temp.setAmount(amount);
				if(!removedSlots.isEmpty()) {
					bufferInv.setItem(removedSlots.get(0), temp);
					removedSlots.remove(0);
				} else if(bufferInv.firstEmpty() != -1)
					bufferInv.setItem(bufferInv.firstEmpty(), temp);
				else {
					p.getWorld().dropItem(p.getLocation(), temp);
				}
				amount = 0;
			}
		}
		ItemStack[] bufferArray = new ItemStack[inv.getContents().length > bufferInv.getContents().length ? inv.getContents().length : bufferInv.getContents().length];
		System.arraycopy(inv.getContents(), 0, bufferArray, 0, inv.getContents().length);
		System.arraycopy(bufferInv.getContents(), 0, bufferArray, 0, bufferInv.getContents().length);
		inv.setContents(bufferArray);
	}
	public void cleanInventoryOfItem(Player p, Inventory inv, ItemStack item) {
		//Formats an inventory holding items with custom stack sizes
		int amount = 0;
		Inventory bufferInv = Bukkit.createInventory(null, 36);
		for(int i = 0;i < 36;i++) {
			bufferInv.setItem(i, item);
		}
		bufferInv.setContents(Arrays.copyOfRange(inv.getContents(), 0, 36));
		List<Integer> removedSlots = new ArrayList<Integer>();
		for(int i = 0;i < 36;i++){
			if(bufferInv.getItem(i) != null && isSimilar(bufferInv.getItem(i),item)){
				amount += bufferInv.getItem(i).getAmount();
				bufferInv.getItem(i).setAmount(0);
				removedSlots.add(i);
			}
		}
		int stackSize = getForceStackSize(item);
		while(amount > 0){
			if(amount > stackSize){
				ItemStack temp = item.clone();
				temp.setAmount(stackSize);
				if(!removedSlots.isEmpty()) {
					bufferInv.setItem(removedSlots.get(0), temp);
					removedSlots.remove(0);
				} else if(bufferInv.firstEmpty() != -1)
					bufferInv.setItem(bufferInv.firstEmpty(), temp);
				else {
					p.getWorld().dropItem(p.getLocation(), temp);
				}
				amount -= stackSize;
			}else{
				ItemStack temp = item.clone();
				temp.setAmount(amount);
				if(!removedSlots.isEmpty()) {
					bufferInv.setItem(removedSlots.get(0), temp);
					removedSlots.remove(0);
				} else if(bufferInv.firstEmpty() != -1)
					bufferInv.setItem(bufferInv.firstEmpty(), temp);
				else {
					p.getWorld().dropItem(p.getLocation(), temp);
				}
				amount = 0;
			}
		}
		ItemStack[] bufferArray = new ItemStack[inv.getContents().length > bufferInv.getContents().length ? inv.getContents().length : bufferInv.getContents().length];
		System.arraycopy(inv.getContents(), 0, bufferArray, 0, inv.getContents().length);
		System.arraycopy(bufferInv.getContents(), 0, bufferArray, 0, bufferInv.getContents().length);
		inv.setContents(bufferArray);
	}
	
	public int getForceStackSize(ItemStack item) {
		if(!item.hasItemMeta()) return -1;
		ItemMeta meta = item.getItemMeta();
		if(!meta.getPersistentDataContainer().has(KEY_FORCESTACK, PersistentDataType.INTEGER)) return -1;
		return meta.getPersistentDataContainer().get(KEY_FORCESTACK, PersistentDataType.INTEGER);
	}
	
	public double getDamageAmount(ItemStack item) {
		if(!item.hasItemMeta()) return -1;
		ItemMeta meta = item.getItemMeta();
		if(!meta.getPersistentDataContainer().has(KEY_DAMAGE, PersistentDataType.DOUBLE)) return -1;
		return meta.getPersistentDataContainer().get(KEY_DAMAGE, PersistentDataType.DOUBLE);
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e){
		if(e.getDamager() instanceof Player){
			Player p = (Player) e.getDamager();
			ItemStack item = p.getInventory().getItemInMainHand();
			if(p.getInventory().getItemInMainHand() != null && !p.getInventory().getItemInMainHand().getType().equals(Material.AIR)){
				if(item.hasItemMeta()) {
					if(item.getItemMeta().getPersistentDataContainer().has(KEY_DAMAGE, PersistentDataType.DOUBLE)) {
						e.setDamage(item.getItemMeta().getPersistentDataContainer().get(KEY_DAMAGE, PersistentDataType.DOUBLE));
						return;
					}
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = false)
	public void onPickup(EntityPickupItemEvent e){
		if(!e.getEntityType().equals(EntityType.PLAYER))
			return;
		
		Player p = (Player) e.getEntity();
		ItemStack item = e.getItem().getItemStack();
		if(item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(KEY_FORCESTACK, PersistentDataType.INTEGER))
			return;
		
		e.setCancelled(true);
		e.getItem().remove();
		giveCustomItem(p, p.getInventory(), item);
	}
}
