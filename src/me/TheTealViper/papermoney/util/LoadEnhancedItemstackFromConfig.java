package me.TheTealViper.papermoney.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class LoadEnhancedItemstackFromConfig implements Listener{
	public Map<String, ItemStack> enhancedItemInfo = new HashMap<String, ItemStack>();
	public Map<ItemStack, Integer> damageInfo = new HashMap<ItemStack, Integer>();
	public Map<ItemStack, Integer> forceStackInfo = new HashMap<ItemStack, Integer>();
	
	//TODO
		//- Custom durability
		//- Custom skull w/ continuous UUID
	
	/**
	 * id: DIRT
	 * amount: 1
	 * name: "Dirt"
	 * lore:
	 *  - "1"
	 *  - "2"
	 * enchantments:
	 *  - UPDATED ENCHANTMENT LIST FOUND @ https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html
	 * 	- "aquaaffinity:1"
	 *  - "baneofarthropods:1"
	 *  - "blastprotection:1"
	 *  - "channeling:1"
	 *  - "cleaving:1"
	 *  - "curseofbinding:1"
	 *  - "curseofvanishing:1"
	 *  - "depthstrider:1"
	 *  - "efficiency:1"
	 *  - "featherfalling:1"
	 *  - "fireaspect:1"
	 *  - "fireprotection:1"
	 *  - "flame:1"
	 *  - "fortune:1"
	 *  - "frostwalker:1"
	 * 	- "impaling:1"
	 *  - "infinity:1"
	 *  - "knockback:1"
	 *  - "looting:1"
	 *  - "loyalty:1"
	 *  - "luckofthesea:1"
	 *  - "lure:1"
	 *  - "mending:1"
	 *  - "multishot:1"
	 *  - "piercing:1"
	 *  - "power:1"
	 *  - "projectileprotection:1"
	 *  - "protection:1"
	 *  - "punch:1"
	 *  - "quickcharge:1"
	 * 	- "respiration:1"
	 *  - "riptide:1"
	 *  - "sharpness:1"
	 *  - "silktouch:1"
	 *  - "smite:1"
	 *  - "soulspeed:1"
	 *  - "sweepingedge:1"
	 *  - "swiftsneak:1"
	 *  - "thorns:1"
	 *  - "unbreaking:1"
	 *  - "windburst:1"
	 * tags:
	 *  - "textureskull:SKINVALUE"
	 *  - "playerskull:PLAYERNAME"
	 *  - "vanilladurability:256"
	 *  - "unbreakable:true"
	 *  - "custommodeldata:1234567"
	 *  - "enchantglow:true"
	 *  - "damage:20" //WIP
	 *  - "forcestack:5" //WIP
	 * flags:
	 *  - "HIDE_ATTRIBUTES"
	 *  - "HIDE_DESTROYS"
	 *  - "HIDE_ENCHANTS"
	 *  - "HIDE_PLACED_ON"
	 *  - "HIDE_POTION_EFFECTS"
	 *  - "HIDE_UNBREAKABLE"
	 * attributes:
	 *  - "ATTRIBUTE:VALUE:OPERATION"
	 *  - "ATTRIBUTE:VALUE:OPERATION:SLOT"
	 *  - ATTRIBUTE NAMES FOUND @ https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html 
	 *  - ATTRIBUTE OPERATIONS FOUND @ https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/AttributeModifier.Operation.html
	 *  - ATTRIBUTE SLOTS FOUND @ https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlot.html
	 * custompersistentdata:
	 *  - "namespace:key:value"
	 */
	
	public LoadEnhancedItemstackFromConfig(UtilityEquippedJavaPlugin plugin){
	}

	public ItemStack getItem(String key) {
		return enhancedItemInfo.get(key).clone();
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack loadItem(String itemIdentifier, ConfigurationSection sec) {
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
		if(sec.contains("name")) {
			meta.setDisplayName(StringUtils.makeColors(sec.getString("name")));
			modifiedMetaSoApply = true;
		}
		
		//Handle lore
		if(sec.contains("lore")) {
			List<String> dummy = sec.getStringList("lore");
			List<String> lore = new ArrayList<String>();
			for(String s : dummy) {
				lore.add(StringUtils.makeColors(s));
			}
			meta.setLore(lore);
			modifiedMetaSoApply = true;
		}
		
		//Handle enchantments
		if(sec.contains("enchantments")) {
			List<String> enchantmentStrings = sec.getStringList("enchantments");
			for(String enchantmentString : enchantmentStrings) {
				String enchantmentName = enchantmentString.split(":")[0].replaceAll(" ", "").replaceAll("_", "").toLowerCase();
				int enchantmentLevel = Integer.valueOf(enchantmentString.split(":")[1]);
				//Loop through enchantments, see if name matches, apply if does
				for (Enchantment ench : Enchantment.values()) {
					String enchantmentNameParsed = ench.toString().split(":")[1];
					enchantmentNameParsed = enchantmentNameParsed.substring(0, enchantmentNameParsed.length()-1);
					if (enchantmentNameParsed.replaceAll(" ", "").replaceAll("_", "").toLowerCase().equals(enchantmentName)) {
						meta.addEnchant(ench, enchantmentLevel, true);
						break;
					}
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
					case "textureskull":
					    SkullMeta skullMeta = (SkullMeta) meta;
				        PlayerProfile pp = Bukkit.createPlayerProfile(UUID.fromString("9c1917c9-95e1-4042-8f9c-f5cc653d266b")); //Random UUID representing heads made from this plugin.
				        PlayerTextures pt = pp.getTextures();
				        try {
							pt.setSkin(new URL("http://textures.minecraft.net/texture/" + value));
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        pp.setTextures(pt);
				        skullMeta.setOwnerProfile(pp);
					    meta = skullMeta;
						break;
					case "playerskull":
						SkullMeta skullMeta2 = (SkullMeta) meta;
				        skullMeta2.setOwningPlayer(Bukkit.getOfflinePlayer(value));
					    meta = skullMeta2;
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
					case "enchantglow":
						meta.setEnchantmentGlintOverride(true);
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
		
		//Handle vanilla attributes
		if(sec.contains("attributes")){
			for(String s : sec.getStringList("attributes")){
				String[] args = s.split(":");
				if(args.length == 3) {
					meta.addAttributeModifier(Attribute.valueOf(args[0].toUpperCase()), new AttributeModifier("test", Double.valueOf(args[1]), Operation.valueOf(args[2].toUpperCase())));
				}else if(args.length == 4) {
					for(String slot : args[3].split(",")) {
						meta.addAttributeModifier(Attribute.valueOf(args[0].toUpperCase()), new AttributeModifier(UUID.randomUUID(), "test", Double.valueOf(args[1]), Operation.valueOf(args[2].toUpperCase()), EquipmentSlot.valueOf(slot.toUpperCase())));
					}
				}else {
					//User messed up formatting
				}
			}
			modifiedMetaSoApply = true;
		}
		
		//Handle custom persistent data
		if (sec.contains("custompersistentdata")) {
			for (String s : sec.getStringList("custompersistentdata")) {
				String[] args = s.split(":");
				String namespace = args[0];
				String k = args[1];
				String v = args[2];
				NamespacedKey nk = new NamespacedKey(namespace, k);
				meta.getPersistentDataContainer().set(nk, PersistentDataType.STRING, v);
			}
		}
		
		//The below order is important so the item put in the databases is the actual key item
		if(modifiedMetaSoApply) item.setItemMeta(meta);
		enhancedItemInfo.put(itemIdentifier, item.clone());
		//Handle enhanced tags
				if(sec.contains("tags")) {
					for(String tagString : sec.getStringList("tags")) {
						String[] tagStringProcessed = tagString.split(":");
						String tag = tagStringProcessed[0];
						String value = tagStringProcessed[1];
						switch(tag) {
							case "damage":
								damageInfo.put(item.clone(), Integer.valueOf(value));
								break;
							case "forcestack":
								forceStackInfo.put(item.clone(), Integer.valueOf(value));
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
			if(item2Meta.getPersistentDataContainer().getKeys().size() != item1Meta.getPersistentDataContainer().getKeys().size())
				return false;
			for (NamespacedKey nk : item2Meta.getPersistentDataContainer().getKeys()) {
				if (!item1Meta.getPersistentDataContainer().has(nk) || !item1Meta.getPersistentDataContainer().get(nk, PersistentDataType.STRING).equals(item2Meta.getPersistentDataContainer().get(nk, PersistentDataType.STRING)))
					return false;
			}
		}
		return true;
	}
	
	public void giveCustomItem(Player p, ItemStack item){
		int amount = item.getAmount();//Start at the item's amount because they're picking it up
		for(int i = 0;i < 36;i++){
			if(p.getInventory().getItem(i) != null && isSimilar(p.getInventory().getItem(i),item)){
				amount += p.getInventory().getItem(i).getAmount();
				p.getInventory().getItem(i).setAmount(0);
			}
		}
		int stackSize = getForceStackSize(item);
		while(amount > 0){
			if(amount > stackSize){
				ItemStack temp = item.clone();
				temp.setAmount(stackSize);
				p.getInventory().setItem(p.getInventory().firstEmpty(), temp);
				amount -= stackSize;
			}else{
				ItemStack temp = item.clone();
				temp.setAmount(amount);
				p.getInventory().setItem(p.getInventory().firstEmpty(), temp);
				amount = 0;
			}
		}
	}
	
	public String getKeyItemString(ItemStack item) {
		for(String key : enhancedItemInfo.keySet()) {
			ItemStack keyItem = enhancedItemInfo.get(key);
			if(isSimilar(item, keyItem))
				return key;
		}
		return null;
	}
	public ItemStack getKeyItem(ItemStack item) {
		for(String key : enhancedItemInfo.keySet()) {
			ItemStack keyItem = enhancedItemInfo.get(key);
			if(isSimilar(item, keyItem))
				return keyItem;
		}
		return null;
	}
	
	public int getForceStackSize(ItemStack item) {
		for(ItemStack keyItem : forceStackInfo.keySet()) {
			if(isSimilar(item, keyItem))
				return forceStackInfo.get(keyItem);
		}
		return -1;
	}
	
	public int getDamageAmount(ItemStack item) {
		for(ItemStack keyItem : damageInfo.keySet()) {
			if(isSimilar(item, keyItem))
				return damageInfo.get(keyItem);
		}
		return -1;
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e){
		if(e.getDamager() instanceof Player){
			Player p = (Player) e.getDamager();
			ItemStack item = p.getInventory().getItemInMainHand();
			if(p.getInventory().getItemInMainHand() != null && !p.getInventory().getItemInMainHand().getType().equals(Material.AIR)){
				for(ItemStack i : damageInfo.keySet()){
					if(item.isSimilar(i)){
						e.setDamage(damageInfo.get(i));
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
		ItemStack forceStack = null;
//		ItemStack keyItem = null;
		for(ItemStack i : forceStackInfo.keySet()){
			if(isSimilar(e.getItem().getItemStack(), i)){
				forceStack = e.getItem().getItemStack();
//				keyItem = i;
			}
		}
		if(forceStack != null){
			e.setCancelled(true);
			e.getItem().remove();
			giveCustomItem(p, forceStack);
		}
	}
}
