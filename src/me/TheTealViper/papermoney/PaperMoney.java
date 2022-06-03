package me.TheTealViper.papermoney;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.TheTealViper.papermoney.implementations.DecentHologramsImplementation;
import me.TheTealViper.papermoney.implementations.HolographicDisplaysImplementation;
import me.TheTealViper.papermoney.implementations.hologramPrototype;
import me.TheTealViper.papermoney.util.PluginFile;
import me.TheTealViper.papermoney.util.UtilityEquippedJavaPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
 
public class PaperMoney extends UtilityEquippedJavaPlugin implements Listener{
    public String prefix;
    public String help;
    public Economy econ;
    public List<UUID> tracking = new ArrayList<UUID>();
    public Map<UUID, Double> amountMap = new HashMap<UUID, Double>();
    public Map<UUID, Integer> itemScheduleDatabase = new HashMap<UUID, Integer>();
    PluginFile MESSAGES;
    public boolean foundHolographicDisplays = false;
	public boolean foundDecentHolograms = false;
//	public HolographicDisplaysImplementation HDI;
//	public DecentHologramsImplementation DHI;
 
	//Fixed bug with hologram only showing for paper
	//Fixed bug allowing custom textured player heads (1.18.2 only)
	//Known issue with DecentHolograms spamming Q leaves phantom holograms that don't actually exist. Some players see them differently.
	
    public void onEnable(){
    	if(Bukkit.getServer().getPluginManager().getPlugin("DecentHolograms") != null) {
			foundDecentHolograms = true;
//			DHI = new DecentHologramsImplementation();
			Bukkit.getLogger().info("PaperMoney running in DecentHolograms mode.");
		}else if(Bukkit.getServer().getPluginManager().getPlugin("HolographicDisplays") != null) {
			foundHolographicDisplays = true;
//			HDI = new HolographicDisplaysImplementation();
			Bukkit.getLogger().info("PaperMoney running in HolographicDisplays mode.");
		}else {
			//Do Nothing ATM
			Bukkit.getLogger().info("PaperMoney didn't find a proper hologram plugin.");
		}
    	StartupPlugin(this, "42464");
    	
    	ensureFileExists("messages_pt-br.yml");
    	ensureFileExists("messages.yml");
		MESSAGES = new PluginFile(this, "messages.yml");
    	
    	hologramPrototype.plugin = this;
    	HolographicDisplaysImplementation.plugin = this;
    	DecentHologramsImplementation.plugin = this;
        prefix = makeColors(MESSAGES.getString("Prefix")) + " ";
        help = makeColors(MESSAGES.getString("Help")) + " ";
        if(!setupEconomy()){
            Bukkit.getLogger().severe("You need to add Vault to your server.");
        }
    }
   
    public void onDisable(){
        getLogger().info("PaperMoney from TheTealViper shutting down. Bshzzzzzz");
    }
   
    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
    	if(!getConfig().getBoolean("Use_Holograms_If_Possible"))
			return;
    	
    	ItemStack item = e.getItemDrop().getItemStack();
    	if((foundDecentHolograms || foundHolographicDisplays)
    			&& (item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(getLoadEnhancedItemstackFromConfig().KEY_VALUE, PersistentDataType.DOUBLE))){
    		NamespacedKey key = new NamespacedKey(this, "value");
            if(item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
            	double worth = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
    			hologramPrototype.createHologram(e.getItemDrop(), worth);
    		}
    	}
    }
    
    @EventHandler
    public void onItemMerge(ItemMergeEvent e) {
    	if(!getConfig().getBoolean("Use_Holograms_If_Possible"))
			return;
    	
    	hologramPrototype.handleMerge(e.getEntity(), e.getTarget());
    }
    
    @EventHandler
    public void onPickup(EntityPickupItemEvent e){
    	if(!e.getEntityType().equals(EntityType.PLAYER))
    		return;
    	if(!getConfig().getBoolean("Use_Holograms_If_Possible"))
			return;
    	
    	UUID u = e.getItem().getUniqueId();
    	hologramPrototype.destroyHologram(u);
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
    	if(!getConfig().getBoolean("Drop_All_On_Death"))
    		return;
    	if(econ == null){
            getLogger().severe("MISSING VAULT");
            return;
        }
        double bal = econ.getBalance(Bukkit.getOfflinePlayer(e.getEntity().getUniqueId()));
        if(bal < getConfig().getDouble("Min_Amount")) {
        	return;
        }
        
        ItemStack item = getLoadEnhancedItemstackFromConfig().getItem(getConfig().getConfigurationSection("Item"));
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(this, "value");
        meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, bal);
        meta.setDisplayName(makeColors(formatWithSyntax(getConfig().getString("Item_Name"), e.getEntity(), bal)));
        List<String> lore = getConfig().getStringList("Item_Lore");
        if(lore != null){
            int i = 0;
            for(String s : lore){
                s = makeColors(formatWithSyntax(s, e.getEntity(), bal));
                lore.set(i, s);
                i++;
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        econ.withdrawPlayer(Bukkit.getOfflinePlayer(e.getEntity().getUniqueId()), bal);
        e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), item);
    }
    
    @SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){
            Player p = (Player) sender;
            boolean explain = false;
            boolean perms = false;
            if(label.equalsIgnoreCase("pmoney") || label.equalsIgnoreCase("papermoney")){
                if(args.length == 0){
                    explain = true;
                }else if(args.length == 1){
                	if(args[0].equalsIgnoreCase("test") && p.getName().equals("TheTealViper")) {
                		Bukkit.broadcastMessage(p.getInventory().getItemInMainHand().getItemMeta().getCustomModelData() + "");
                	}
                	if(args[0].equalsIgnoreCase("reload")) {
                		if(p.hasPermission("papermoney.reload")){
                			p.sendMessage(prefix + "Plugin configs reloaded!");
                			reloadConfig();
                			MESSAGES.reload();
                			prefix = makeColors(MESSAGES.getString("Prefix")) + " ";
                	        help = makeColors(MESSAGES.getString("Help")) + " ";
                		} else
                			perms = true;
                	}else if(args[0].equalsIgnoreCase("split") && getConfig().getBoolean("Enable_Money_Splitting")) {
                		if(p.hasPermission("papermoney.split")){
                			p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Split_Explain"), p, -1)));
                            return false;
                		} else
                			perms = true;
                	}else {
                		explain = true;
                	}
                }else if(args.length == 2){
                    if(args[0].equalsIgnoreCase("make")){
                        if(p.hasPermission("papermoney.make")){
                            if(!args[1].matches("^[0-9,.]+$")){
                                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Make_Explain"), p, -1)));
                                return false;
                            }
                            if(p.getInventory().firstEmpty() == -1){
                                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Full_Inventory_Warning"), p, -1)));
                                return false;
                            }
                            if(Double.valueOf(args[1]) < getConfig().getDouble("Min_Amount")) {
                            	p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Value_Must_Meet_Minimum_Warning"), p, Double.valueOf(args[1]))));
                            	return false;
                            }
                           
                            while(args[1].contains(",")){
                                args[1] = args[1].replace(",", "");
                            }
                            ItemStack item = getLoadEnhancedItemstackFromConfig().getItem(getConfig().getConfigurationSection("Item"));
                            ItemMeta meta = item.getItemMeta();
                            NamespacedKey key = new NamespacedKey(this, "value");
                            meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, roundToSigFigs(Double.valueOf(args[1]), getConfig().getInt("Maximum_Decimal_Places")));
                            meta.setDisplayName(makeColors(formatWithSyntax(getConfig().getString("Item_Name"), p, roundToSigFigs(Double.valueOf(args[1]), getConfig().getInt("Maximum_Decimal_Places")))));
                            List<String> lore = getConfig().getStringList("Item_Lore");
                            if(lore != null){
                                int i = 0;
                                for(String s : lore){
                                    s = makeColors(formatWithSyntax(s, p, roundToSigFigs(Double.valueOf(args[1]), getConfig().getInt("Maximum_Decimal_Places"))));
                                    lore.set(i, s);
                                    i++;
                                }
                                meta.setLore(lore);
                            }
                            item.setItemMeta(meta);
                            p.getInventory().addItem(item);
                        } else
                        	perms = true;
                    }else if(args[0].equalsIgnoreCase("take")){
                        if(p.hasPermission("papermoney.take")){
                            if(!args[1].matches("^[0-9,.]+$")){
                                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Take_Explain"), p, -1)));
                                return false;
                            }
                            if(p.getInventory().firstEmpty() == -1){
                                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Full_Inventory_Warning"), p, -1)));
                                return false;
                            }
                            if(econ == null){
                                getLogger().severe("MISSING VAULT");
                                return false;
                            }
                            if(!econ.has(p.getName(), Double.valueOf(args[1]))){
                                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Not_Enough_Money_Warning"), p, Double.valueOf(args[1]))));
                                return false;
                            }
                            if(Double.valueOf(args[1]) < getConfig().getDouble("Min_Amount")) {
                            	p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Value_Must_Meet_Minimum_Warning"), p, Double.valueOf(args[1]))));
                            	return false;
                            }
                           
                            while(args[1].contains(",")){
                                args[1] = args[1].replace(",", "");
                            }
                            ItemStack item = getLoadEnhancedItemstackFromConfig().getItem(getConfig().getConfigurationSection("Item"));
                            ItemMeta meta = item.getItemMeta();
                            NamespacedKey key = new NamespacedKey(this, "value");
                            meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, roundToSigFigs(Double.valueOf(args[1]), getConfig().getInt("Maximum_Decimal_Places")));
                            meta.setDisplayName(makeColors(formatWithSyntax(getConfig().getString("Item_Name"), p, roundToSigFigs(Double.valueOf(args[1]), getConfig().getInt("Maximum_Decimal_Places")))));
                            List<String> lore = getConfig().getStringList("Item_Lore");
                            if(lore != null){
                                int i = 0;
                                for(String s : lore){
                                    s = makeColors(formatWithSyntax(s, p, roundToSigFigs(Double.valueOf(args[1]), getConfig().getInt("Maximum_Decimal_Places"))));
                                    lore.set(i, s);
                                    i++;
                                }
                                meta.setLore(lore);
                            }
                            item.setItemMeta(meta);
                            p.getInventory().addItem(item);
                            econ.withdrawPlayer(p.getName(), Double.valueOf(args[1]));
                        } else
                        	perms = true;
                    }else if(args[0].equalsIgnoreCase("split") && getConfig().getBoolean("Enable_Money_Splitting")){
                        if(p.hasPermission("papermoney.split")){
                        	HandlePaperSplit(p, Arrays.copyOfRange(args, 1, args.length));
                        } else
                        	perms = true;
                    }else{
                        explain = true;
                    }
                }else if(args.length == 3){
                    if(args[0].equalsIgnoreCase("take")){
                        if(p.hasPermission("papermoney.take.others")){
                            if(!Bukkit.getOfflinePlayer(args[2]).isOnline()){
                                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Player_Offline_Warning"), p, -1)));
                                return false;
                            }
                            if(!args[1].matches("^[0-9,.]+$")){
                                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_TakeFromAnother_Explain"), p, -1)));
                                return false;
                            }
                            if(p.getInventory().firstEmpty() == -1){
                                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Full_Inventory_Warning"), p, Double.valueOf(args[1]))));
                                return false;
                            }
                            if(econ == null){
                                getLogger().severe("MISSING VAULT");
                                return false;
                            }
                            if(!econ.has(args[2], Double.valueOf(args[1]))){
                                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_AnotherNot_Enough_Money_Warning"), p, Double.valueOf(args[1]))));
                                return false;
                            }
                            if(Double.valueOf(args[1]) < getConfig().getDouble("Min_Amount")) {
                            	p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Value_Must_Meet_Minimum_Warning"), p, Double.valueOf(args[1]))));
                            	return false;
                            }
                           
                            while(args[1].contains(",")){
                                args[1] = args[1].replace(",", "");
                            }
                            ItemStack item = getLoadEnhancedItemstackFromConfig().getItem(getConfig().getConfigurationSection("Item"));
                            ItemMeta meta = item.getItemMeta();
                            NamespacedKey key = new NamespacedKey(this, "value");
                            meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, roundToSigFigs(Double.valueOf(args[1]), getConfig().getInt("Maximum_Decimal_Places")));
                            meta.setDisplayName(makeColors(formatWithSyntax(getConfig().getString("Item_Name"), p, roundToSigFigs(Double.valueOf(args[1]), getConfig().getInt("Maximum_Decimal_Places")))));
                            List<String> lore = getConfig().getStringList("Item_Lore");
                            if(lore != null){
                                int i = 0;
                                for(String s : lore){
                                    s = makeColors(formatWithSyntax(s, p, roundToSigFigs(Double.valueOf(args[1]), getConfig().getInt("Maximum_Decimal_Places"))));
                                    lore.set(i, s);
                                    i++;
                                }
                                meta.setLore(lore);
                            }
                            item.setItemMeta(meta);
                            p.getInventory().addItem(item);
                            econ.withdrawPlayer(args[2], Double.valueOf(args[1]));
                        } else
                        	perms = true;
                    }else if(args[0].equalsIgnoreCase("split") && getConfig().getBoolean("Enable_Money_Splitting")){
                        if(p.hasPermission("papermoney.split")){
                        	HandlePaperSplit(p, Arrays.copyOfRange(args, 1, args.length));
                        } else
                        	perms = true;
                    }else{
                        explain = true;
                    }
                }else {
                	if(args[0].equalsIgnoreCase("split") && getConfig().getBoolean("Enable_Money_Splitting")) {
                		if(p.hasPermission("papermoney.split")){
                			HandlePaperSplit(p, Arrays.copyOfRange(args, 1, args.length));
                        } else
                        	perms = true;
                	}else {
                		explain = true;
                	}
                }
            }
            if(explain && !perms){
                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Player_Command_Title"), p, -1)));
                if(p.hasPermission("papermoney.take"))
                    p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Take_Explain"), p, -1)));
                if(p.hasPermission("papermoney.split") && getConfig().getBoolean("Enable_Money_Splitting"))
                	p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Split_Explain"), p, -1)));
                if(p.hasPermission("papermoney.make") || p.hasPermission("papermoney.take.others") || p.hasPermission("papermoney.reload"))
                    p.sendMessage(prefix + "Staff Commands:");
                if(p.hasPermission("papermoney.make"))
                    p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Make_Explain"), p, -1)));
                if(p.hasPermission("papermoney.take.others"))
                    p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_TakeFromAnother_Explain"), p, -1)));
                if(p.hasPermission("papermoney.reload"))
                    p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Reload_Explain"), p, -1)));
            }
            if(perms){
                p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Missing_Permissions"), p, -1)));
            }
        }
        return false;
    }
    
    @SuppressWarnings("deprecation")
	@EventHandler
    public void onClick(PlayerInteractEvent e){
    	NamespacedKey key = new NamespacedKey(this, "value");
    	if(e.getHand() == null || e.getHand().equals(EquipmentSlot.OFF_HAND))
    		return;
    	if(e.getItem() != null && e.getItem().hasItemMeta() && e.getItem().getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.DOUBLE))
    		e.setCancelled(true);
    	else
    		return;
    	
        if((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && !getConfig().getBoolean("Disable_Right_Click_Deposit")){
        	ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
            if(item == null || item.getType().equals(Material.AIR))
                return;
            if(item.hasItemMeta()){
                if(item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
                	double worth = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
                	econ.depositPlayer(e.getPlayer().getName(), worth);
                    e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                    e.getPlayer().sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Deposited"), e.getPlayer(), worth)));
                    if(!getConfig().getString("Sound").equalsIgnoreCase("none")) {
                    	e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.valueOf(getConfig().getString("Sound")), 1f, 1f);
                    }
                    return;
                }
            }
        }
    }
   
    @EventHandler
    public void onDualWield(PlayerSwapHandItemsEvent e) {
    	ItemStack item = e.getMainHandItem();
        if(item == null || item.getType().equals(Material.AIR))
            return;
        if(item.hasItemMeta()){
            NamespacedKey key = new NamespacedKey(this, "value");
            if(item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
            	double worth = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE) * item.getAmount();
            	ItemStack offItem = e.getOffHandItem();
                if(offItem == null || offItem.getType().equals(Material.AIR))
                    return;
                if(offItem.hasItemMeta()){
                    if(offItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
                    	double offWorth = offItem.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE) * offItem.getAmount();
                    	
                    	ItemStack newMoneyItem = getLoadEnhancedItemstackFromConfig().getItem(getConfig().getConfigurationSection("Item"));
                        ItemMeta meta = newMoneyItem.getItemMeta();
                        NamespacedKey key2 = new NamespacedKey(this, "value");
                        meta.getPersistentDataContainer().set(key2, PersistentDataType.DOUBLE, worth+offWorth);
                        meta.setDisplayName(makeColors(formatWithSyntax(getConfig().getString("Item_Name"), e.getPlayer(), worth+offWorth)));
                        List<String> lore = getConfig().getStringList("Item_Lore");
                        if(lore != null){
                            int j = 0;
                            for(String s : lore){
                                s = makeColors(formatWithSyntax(s, e.getPlayer(), worth+offWorth));
                                lore.set(j, s);
                                j++;
                            }
                            meta.setLore(lore);
                        }
                        newMoneyItem.setItemMeta(meta);
                        
                        e.getPlayer().getInventory().setItemInOffHand(newMoneyItem);
                    	e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    	e.setCancelled(true);
                    }
                }
            }
        }
    }
    
    public static String makeColors(String s){
        String replaced = ChatColor.translateAlternateColorCodes('&', s.replaceAll("\\\\", " "));
        return replaced;
    }
   
    @SuppressWarnings("deprecation")
	public String formatWithSyntax(String s, Player p, double worth){
        String replacer = "%pm_player_money%";
        while(s.contains(replacer)){
            s = s.replace(replacer, numberFormatter(econ.getBalance(p.getName())));
        }
        replacer = "%pm_player_name%";
        while(s.contains(replacer)){
            s = s.replace(replacer, p.getName());
        }
        replacer = "%pm_IDENTIFIER%";
        while(s.contains(replacer)){
            s = s.replace(replacer, convertToInvisibleString("PAPERMONEY") + convertToInvisibleString(String.valueOf(worth)));
        }
        if(worth > -1){
            replacer = "%pm_bill_worth%";
            while(s.contains(replacer)){
                s = s.replace(replacer, numberFormatter(worth));
            }
        }
        replacer = "%pm_prefix%";
        while(s.contains(replacer)){
            s = s.replace(replacer, prefix);
        }
        replacer = "%pm_help%";
        while(s.contains(replacer)){
            s = s.replace(replacer, help);
        }
        replacer = "%pm_minimum%";
        while(s.contains(replacer)){
            s = s.replace(replacer, numberFormatter(getConfig().getDouble("Min_Amount")));
        }
        
        s = PlaceholderAPI.setPlaceholders(p, s);
        
        return s;
    }
    
    private Double roundToSigFigs(double number, int decimalPlaces) {
    	return Math.floor(number * Math.pow(10, decimalPlaces))/Math.pow(10, decimalPlaces);
    }
   
    public String getStringFormattedDecimal(double worth) {
    	DecimalFormat df = new DecimalFormat();
    	df.setMaximumFractionDigits(getConfig().getInt("Maximum_Decimal_Places"));
    	return df.format(worth);
		//return String.format("%. "+getConfig().getInt("Maximum_Decimal_Places")+"d", String.valueOf(worth));
	}
    
    private String numberFormatter(double number){
        NumberFormat myFormat = NumberFormat.getInstance();
        myFormat.setGroupingUsed(true);
        return myFormat.format(number);
    }
   
//  private void copy(InputStream in, File file) {
//      try {
//          OutputStream out = new FileOutputStream(file);
//          byte[] buf = new byte[1024];
//          int len;
//          while((len=in.read(buf))>0){
//              out.write(buf,0,len);
//          }
//          out.close();
//          in.close();
//      } catch (Exception e) {
//          e.printStackTrace();
//      }
//  }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
   
    
    public static String convertToInvisibleString(String s) {
        String hidden = "";
        for (char c : s.toCharArray()) hidden += "§"+c;
        return hidden;
    }
    public static String convertBack(String s){
        //String converted = ChatColor.stripColor(s);
        String converted = s.replaceAll("§", "");
        return converted;
    }
    
    private void HandlePaperSplit(Player p, String[] args) {
    	ItemStack item = p.getInventory().getItemInMainHand();
        if(item == null || item.getType().equals(Material.AIR)) {
            //Do nothing
        	p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Split_NotPaperMoney"), p, -1)));
        }else{
        	if(item.hasItemMeta()){
                NamespacedKey key = new NamespacedKey(this, "value");
                if(item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
                	double worthInHand = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE) * item.getAmount();
                	try
                	{
					  double[] amountTyped = new double[args.length];
					  double totalTyped = 0;
					  for(int i = 0;i < args.length;i++) {
						  amountTyped[i] = Double.parseDouble(args[i]);
						  amountTyped[i] = roundToSigFigs(amountTyped[i], getConfig().getInt("Maximum_Decimal_Places"));
						  totalTyped += amountTyped[i];
					  }
					  if(totalTyped < worthInHand) {
						  int emptyInvSlots = 1; //Start at 1 because the paper in their hand counts
						  for(int i = 0; i < 36;i++){
							  if(p.getInventory().getItem(i) == null || p.getInventory().getItem(i).getType().equals(Material.AIR)){
								  emptyInvSlots++;
							  }
						  }
						  int inventorySlotsNecessary = args.length;
						  if(totalTyped < worthInHand)
						  	  inventorySlotsNecessary++;
						  if(emptyInvSlots < inventorySlotsNecessary) {
							  //Not enough inv room to split
							  p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Split_MissingInv"), p, -1)));
						  }else {
							  //All checks have gone through. Give them their money.
							  p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
							  for(int i = 0;i < inventorySlotsNecessary;i++) {
								  if(i+1 > args.length) {
									    //Leftover money
									    ItemStack newMoneyItem = getLoadEnhancedItemstackFromConfig().getItem(getConfig().getConfigurationSection("Item"));
			                            ItemMeta meta = newMoneyItem.getItemMeta();
			                            NamespacedKey key2 = new NamespacedKey(this, "value");
			                            meta.getPersistentDataContainer().set(key2, PersistentDataType.DOUBLE, worthInHand-totalTyped);
			                            meta.setDisplayName(makeColors(formatWithSyntax(getConfig().getString("Item_Name"), p, worthInHand-totalTyped)));
			                            List<String> lore = getConfig().getStringList("Item_Lore");
			                            if(lore != null){
			                                int j = 0;
			                                for(String s : lore){
			                                    s = makeColors(formatWithSyntax(s, p, worthInHand-totalTyped));
			                                    lore.set(j, s);
			                                    j++;
			                                }
			                                meta.setLore(lore);
			                            }
			                            newMoneyItem.setItemMeta(meta);
			                            p.getInventory().addItem(newMoneyItem);
								  }else {
									    //Arg specific money
									    ItemStack newMoneyItem = getLoadEnhancedItemstackFromConfig().getItem(getConfig().getConfigurationSection("Item"));
			                            ItemMeta meta = newMoneyItem.getItemMeta();
			                            NamespacedKey key2 = new NamespacedKey(this, "value");
			                            meta.getPersistentDataContainer().set(key2, PersistentDataType.DOUBLE, amountTyped[i]);
			                            meta.setDisplayName(makeColors(formatWithSyntax(getConfig().getString("Item_Name"), p, amountTyped[i])));
			                            List<String> lore = getConfig().getStringList("Item_Lore");
			                            if(lore != null){
			                                int j = 0;
			                                for(String s : lore){
			                                    s = makeColors(formatWithSyntax(s, p, amountTyped[i]));
			                                    lore.set(j, s);
			                                    j++;
			                                }
			                                meta.setLore(lore);
			                            }
			                            newMoneyItem.setItemMeta(meta);
			                            p.getInventory().addItem(newMoneyItem);
								  }
							  }
						  }
					  }else {
						  //Worth less than split amount
						  p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Split_BiggerValue"), p, -1)));
					  }
                	}
                	catch(NumberFormatException e)
                	{
                		//Not a double
                		p.sendMessage(makeColors(formatWithSyntax(MESSAGES.getString("PMoney_Split_NonNumeric"), p, -1)));
                	}
                }
            }
        }
    }
    
    public void ensureFileExists(String filename) {
    	if(!new File("plugins/PaperMoney/" + filename).exists()) {
    		Bukkit.getLogger().info("Copying File: " + filename);
			try {
				InputStream inStream = getResource(filename);
				File targetFile = new File("plugins/PaperMoney/" + filename);
			    OutputStream outStream = new FileOutputStream(targetFile);
			    byte[] buffer = new byte[inStream.available()];
			    inStream.read(buffer);
			    outStream.write(buffer);
			    outStream.close();
			    inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
    
}