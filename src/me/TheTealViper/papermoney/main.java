package me.TheTealViper.papermoney;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
 
@SuppressWarnings("deprecation")
public class main extends JavaPlugin implements Listener{
    public String prefix;
    public String help;
    public Economy econ;
    List<UUID> tracking = new ArrayList<UUID>();
    Map<UUID, Double> amountMap = new HashMap<UUID, Double>();
    Map<UUID, Integer> itemScheduleDatabase = new HashMap<UUID, Integer>();
 
    public void onEnable(){
    	holographShit.plugin = this;
        getLogger().info("PaperMoney from TheTealViper enabling!");
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        prefix = makeColors(getConfig().getString("Prefix")) + " ";
        help = ChatColor.RESET + "" + ChatColor.GRAY;
        if(!setupEconomy()){
            Bukkit.getLogger().severe("You need to add Vault to your server.");
        }
    }
   
    public void onDisable(){
        getLogger().info("PaperMoney from TheTealViper shutting down. Bshzzzzzz");
    }
   
    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
    	ItemStack item = e.getItemDrop().getItemStack();
    	if(Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays") && item.getType().equals(Material.PAPER) && item.hasItemMeta() && item.getItemMeta().hasLore()){
    		List<String> lore = item.getItemMeta().getLore();
    		boolean isPaper = false;
            double worth = 0;
            for(String s : lore){
                if(s.contains(convertToInvisibleString("PAPERMONEY"))){
                    isPaper = true;
                    String withoutKnowledges = convertBack(s.replace(convertToInvisibleString("PAPERMONEY"), ""));
                    if(withoutKnowledges.contains(".")){
                        int i = 0;
                        int foundIt = 0;
                        for(char c : withoutKnowledges.toCharArray()){
                            if(c == '.'){
                                foundIt = i;
                            }
                            i++;
                        }
                        withoutKnowledges = withoutKnowledges.substring(0, foundIt);
                    }
                    worth = Double.valueOf(withoutKnowledges.replace("§", ""));
                }
            }
    		if(isPaper){
    			tracking.add(e.getItemDrop().getUniqueId());
    	    	amountMap.put(e.getItemDrop().getUniqueId(), worth);
    	    	itemScheduleDatabase.put(e.getItemDrop().getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {public void run() {
    				if(tracking.contains(e.getItemDrop().getUniqueId()))
    					holographShit.handle(e.getItemDrop().getUniqueId());
    				else{
    					Bukkit.getScheduler().cancelTask(itemScheduleDatabase.get(e.getItemDrop().getUniqueId()));
    					itemScheduleDatabase.remove(e.getItemDrop().getUniqueId());
    					if(holographShit.holographDatabase.containsKey(e.getItemDrop().getUniqueId())) {
    						holographShit.holographDatabase.get(e.getItemDrop().getUniqueId()).delete();
        					holographShit.holographDatabase.remove(e.getItemDrop().getUniqueId());
    					}
    				}
    			}}, 0L, 1L));
    		}
    	}
    }
    
    @EventHandler
    public void onPickup(PlayerPickupItemEvent e){
    	UUID u = e.getItem().getUniqueId();
    	tracking.remove(u);
    	amountMap.remove(u);
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){
            Player p = (Player) sender;
            boolean explain = false;
            boolean perms = false;
            if(label.equalsIgnoreCase("pmoney") || label.equalsIgnoreCase("papermoney")){
                if(args.length == 0){
                    explain = true;
                }
                if(args.length == 1){
                    explain = true;
                }
                if(args.length == 2){
                    if(args[0].equalsIgnoreCase("make")){
                        if(p.hasPermission("papermoney.make")){
                            if(!args[1].matches("^[0-9,.]+$")){
                                p.sendMessage(prefix + "/pmoney make (amount)" + help + " - Makes a note with a certain value.");
                                return false;
                            }
                            if(p.getInventory().firstEmpty() == -1){
                                p.sendMessage(prefix + "Your inventory is full.");
                                return false;
                            }
                            if(Double.valueOf(args[1]) < getConfig().getDouble("Min_Amount")) {
                            	p.sendMessage(prefix + "You must use a value greater or equal to " + getConfig().getDouble("Min_Amount"));
                            	return false;
                            }
                           
                            while(args[1].contains(",")){
                                args[1] = args[1].replace(",", "");
                            }
                            ItemStack item = new ItemStack(Material.PAPER, 1);
                            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.STICK);
                            meta.setDisplayName(makeColors(formatWithSyntax(getConfig().getString("Item_Name"), p, Double.valueOf(args[1]))));
                            List<String> lore = getConfig().getStringList("Item_Lore");
                            if(lore != null){
                                int i = 0;
                                for(String s : lore){
                                    s = makeColors(formatWithSyntax(s, p, Double.valueOf(args[1])));
                                    lore.set(i, s);
                                    i++;
                                }
                                meta.setLore(lore);
                            }
                            item.setItemMeta(meta);
                            p.getInventory().addItem(item);
                        }
                    }else if(args[0].equalsIgnoreCase("take")){
                        if(p.hasPermission("papermoney.take")){
                            if(!args[1].matches("^[0-9,.]+$")){
                                p.sendMessage(prefix + "/pmoney take (amount)" + help + " - Turns money from your bank into a note.");
                                return false;
                            }
                            if(p.getInventory().firstEmpty() == -1){
                                p.sendMessage(prefix + "Your inventory is full.");
                                return false;
                            }
                            if(econ == null){
                                getLogger().severe("MISSING VAULT");
                                return false;
                            }
                            if(!econ.has(p.getName(), Double.valueOf(args[1]))){
                                p.sendMessage(prefix + ChatColor.RED + "You don't have that much money!");
                                return false;
                            }
                            if(Double.valueOf(args[1]) < getConfig().getDouble("Min_Amount")) {
                            	p.sendMessage(prefix + "You must use a value greater or equal to " + getConfig().getDouble("Min_Amount"));
                            	return false;
                            }
                           
                            while(args[1].contains(",")){
                                args[1] = args[1].replace(",", "");
                            }
                            ItemStack item = new ItemStack(Material.PAPER, 1);
                            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.STICK);
                            meta.setDisplayName(makeColors(formatWithSyntax(getConfig().getString("Item_Name"), p, Double.valueOf(args[1]))));
                            List<String> lore = getConfig().getStringList("Item_Lore");
                            if(lore != null){
                                int i = 0;
                                for(String s : lore){
                                    s = makeColors(formatWithSyntax(s, p, Double.valueOf(args[1])));
                                    lore.set(i, s);
                                    i++;
                                }
                                meta.setLore(lore);
                            }
                            item.setItemMeta(meta);
                            p.getInventory().addItem(item);
                            econ.withdrawPlayer(p.getName(), Double.valueOf(args[1]));
                        }
                    }else{
                        explain = true;
                    }
                }
                if(args.length == 3){
                    if(args[0].equalsIgnoreCase("take")){
                        if(p.hasPermission("papermoney.take.others")){
                            if(!Bukkit.getOfflinePlayer(args[2]).isOnline()){
                                p.sendMessage(prefix + "That player is not currently online.");
                                return false;
                            }
                            if(!args[1].matches("^[0-9,.]+$")){
                                p.sendMessage(prefix + "/pmoney take (amount) (playerName)" + help + " - Turns money from another's bank into a note.");
                                return false;
                            }
                            if(p.getInventory().firstEmpty() == -1){
                                p.sendMessage(prefix + "Your inventory is full.");
                                return false;
                            }
                            if(econ == null){
                                getLogger().severe("MISSING VAULT");
                                return false;
                            }
                            if(!econ.has(args[2], Double.valueOf(args[1]))){
                                p.sendMessage(prefix + ChatColor.RED + "They don't have that much money!");
                                return false;
                            }
                            if(Double.valueOf(args[1]) < getConfig().getDouble("Min_Amount")) {
                            	p.sendMessage(prefix + "You must use a value greater or equal to " + getConfig().getDouble("Min_Amount"));
                            	return false;
                            }
                           
                            while(args[1].contains(",")){
                                args[1] = args[1].replace(",", "");
                            }
                            ItemStack item = new ItemStack(Material.PAPER, 1);
                            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.STICK);
                            meta.setDisplayName(makeColors(formatWithSyntax(getConfig().getString("Item_Name"), p, Double.valueOf(args[1]))));
                            List<String> lore = getConfig().getStringList("Item_Lore");
                            if(lore != null){
                                int i = 0;
                                for(String s : lore){
                                    s = makeColors(formatWithSyntax(s, p, Double.valueOf(args[1])));
                                    lore.set(i, s);
                                    i++;
                                }
                                meta.setLore(lore);
                            }
                            item.setItemMeta(meta);
                            p.getInventory().addItem(item);
                            econ.withdrawPlayer(args[2], Double.valueOf(args[1]));
                        }
                    }else{
                        explain = true;
                    }
                }
            }
            if(explain && !perms){
                p.sendMessage(prefix + "Commands:");
                if(p.hasPermission("papermoney.take"))
                    p.sendMessage("/pmoney take (amount)" + help + " - Turns money from your bank into a note.");
                if(p.hasPermission("papermoney.make") || p.hasPermission("papermoney.take.others"))
                    p.sendMessage(prefix + "Staff Commands:");
                if(p.hasPermission("papermoney.make"))
                    p.sendMessage("/pmoney make (amount)" + help + " - Makes a note with a certain value.");
                if(p.hasPermission("papermoney.take.others"))
                    p.sendMessage("/pmoney take (amount) (playerName)" + help + " - Turns money from another's bank into a note.");
            }
            if(perms){
                p.sendMessage(prefix + "You don't have the perms!");
            }
        }
        return false;
    }
   
    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(e.getItem() == null || e.getItem().getType().equals(Material.AIR))
                return;
            if(e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasLore() && e.getItem().getItemMeta().hasDisplayName()){
                List<String> lore = e.getItem().getItemMeta().getLore();
                boolean isReal = false;
                double worth = 0;
                for(String s : lore){
                    if(s.contains(convertToInvisibleString("PAPERMONEY"))){
                        isReal = true;
                        String withoutKnowledges = convertBack(s.replace(convertToInvisibleString("PAPERMONEY"), ""));
                        if(withoutKnowledges.contains(".")){
                            int i = 0;
                            int foundIt = 0;
                            for(char c : withoutKnowledges.toCharArray()){
                                if(c == '.'){
                                    foundIt = i;
                                }
                                i++;
                            }
                            withoutKnowledges = withoutKnowledges.substring(0, foundIt + 1);
                        }
                        worth = Double.valueOf(withoutKnowledges.replace("§", ""));
                    }
                }
                if(isReal){
                    econ.depositPlayer(e.getPlayer().getName(), worth);
                    e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                    e.getPlayer().sendMessage(prefix + "$" + numberFormatter(worth) + " was deposited into your bank.");
                    if(!getConfig().getString("Sound").equalsIgnoreCase("none")) {
                    	e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.valueOf(getConfig().getString("Sound")), 1f, 1f);
                    }
                    return;
                }
            }
        }
    }
   
    public static String makeColors(String s){
        String replaced = ChatColor.translateAlternateColorCodes('&', s.replaceAll("\\\\", " "));
        return replaced;
    }
   
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
        return s;
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
        for (char c : s.toCharArray()) hidden += ChatColor.COLOR_CHAR+""+c;
        return hidden;
    }
    public static String convertBack(String s){
        //String converted = ChatColor.stripColor(s);
        String converted = s.replaceAll("�", "");
        return converted;
    }
}
