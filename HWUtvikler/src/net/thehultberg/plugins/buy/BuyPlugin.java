package net.thehultberg.plugins.buy;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class of plugin.
 * @author Hultberg
 *
 */
public class BuyPlugin extends JavaPlugin {
	
	/**
	 * The config handler.
	 */
	public ConfigHandler confighandler;
	
	/**
	 * Minecraft logger
	 */
    public static final Logger log = Logger.getLogger("Minecraft");
	
	public BuyPlugin(){
		super();
		
		confighandler = new ConfigHandler(this);
	}
	
	/**
	 * This is fired when plugin starts.
	 */
	public void onEnable(){
		log.log(Level.INFO, "Plugin starter...");
		getDataFolder().mkdirs();
		
		this.confighandler.load(); // Load config.
		this.confighandler.trimProds(); // Trim prods listen.
		this.confighandler.printHelpFile(); // Print help file.
		
		//this.

		log.log(Level.INFO, "Plugin startet.");
	}
	
	/**
	 * This is fired when plugin shutsdown.
	 */
	public void onDisable(){
		log.log(Level.INFO, "Plugin avslutter.");
	}
	
	/**
	 * Når en kommando fyres.
	 * Betalt er via gull i inventorien.
	 */
    @SuppressWarnings({ "rawtypes", "deprecation" })
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if(cmd.getName().equalsIgnoreCase("buy")){
    		if(isPlayer(sender)){ // kan kun sendes av player.
    			Player p = (Player) sender;
    			if(args.length == 0){
    				// Se produkter.
    				p.sendMessage(ChatColor.BLUE + "--------------"
    						+ ChatColor.WHITE + "/buy"
    						+ ChatColor.BLUE + "--------------");
    				p.sendMessage(ChatColor.GRAY + "Bruk "
    						+ ChatColor.GOLD + "/buy "
    						+ ChatColor.DARK_GREEN + "[produkt-navn] [antall]"
    	    				+ ChatColor.GRAY + " for å kjøpe noe.");
    				Iterator it = this.confighandler.getProds().entrySet().iterator();
    		        while (it.hasNext()) {
    		            Map.Entry pairs = (Map.Entry)it.next();
    		            Product i = (Product) pairs.getValue();
    		            if(i != null){
    		            	p.sendMessage(i.getCustomName() + "" + ChatColor.YELLOW + " - " + i.getPerPrice() + "stk for " + i.getPrice() + "g.");
    		            }
    		            it.remove(); // Ungå CurrentModificationException elns.    		            
    		        }
    			} else {
    				if(args.length == 1 && args[0].equalsIgnoreCase("reloadConfig")){
    					// Reload config.
    					if(p.isOp()){
    						this.reloadConfig();
    						this.confighandler.load(); // Load config.
    						this.confighandler.trimProds(); // Trim prods listen.
    						this.confighandler.printHelpFile(); // Print help file.
    						p.sendMessage(ChatColor.DARK_GREEN + "Config er reloaded.");
    					} else {
    						p.sendMessage(ChatColor.DARK_RED + "Du har ikke tilgang til å reloade config.");
    					}
    				} else if(args.length >= 1){ // Kjøper
    					Product pro = this.confighandler.getProds().get(args[0].toLowerCase().trim());
    					if(pro != null){
    						int amountWant = 0;
    						if(args.length == 2){
    							try {
            						amountWant = Integer.parseInt(args[1]);
            					} catch(NumberFormatException n){
            	    				p.sendMessage(ChatColor.RED + "Verdien "+args[1]+" er i ugyldig format");
            	    				return true;
            	    			}
    						} else {
    							amountWant = pro.getPerPrice();
    						}
    						
    						ItemStack itemToGive = new ItemStack(pro.getItemId());
    						if(pro.getDamage() != 0){
    							itemToGive.setDurability(pro.getDamage());
    						}
    						
    						// Kalkuler pris.
    						float leftover1 = amountWant % pro.getPerPrice();
    						int leftover = Math.round(leftover1);
    						itemToGive.setAmount(amountWant - leftover);
    						
    						float temp1 = itemToGive.getAmount() / pro.getPerPrice();
    						float price_d = temp1 * pro.getPrice();        			
    						int price = Math.round(price_d);
    						
    						// Finn ut hvor mye gull spilleren har..
    						int playerHas = 0;
    						
    						// Scan inv.
    						for(ItemStack item : p.getInventory().getContents()){
    	    					if(item != null && item.getTypeId() != 0){
    	    						if(item.getTypeId() == 266){
    	    							playerHas += item.getAmount();
    	    						}
    	    					}
    	    				}
    						
    						if(itemToGive.getAmount() == 0){
    							p.sendMessage(ChatColor.RED + "Antallet kan ikke være null, eller under "+pro.getPerPrice()+".");
    							return true;
    						}
    						
    						// Check inventory
    						int space = this.getFreeSpace(p, itemToGive);
    						if(itemToGive.getAmount() <= space){
    							// Has space
    							if(playerHas >= price){
    								// Can afford.    								
    								// Se inn i fremtiden...
    								int toget = playerHas - price;
    								if(toget > 0){
    									int spaceGold = this.getFreeSpace(p, new ItemStack(Material.GOLD_INGOT, toget)); 
    									p.getInventory().remove(266);
    									if(toget <= spaceGold){ // Har fyrn plass i inventorien til gullet?
        									p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, toget));    										
    									} else {
    		    							p.sendMessage(ChatColor.RED + "Du har ikke nok plass i din inventory til veksle gullet.");
    		    							p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, playerHas)); // Return gullet.
    									}
    								}     							
    								p.getInventory().addItem(itemToGive);
    								p.updateInventory(); // Liker calle denne enda, så slipper vi problemer.
    								p.sendMessage(ChatColor.DARK_GREEN + "Du kjøpte " + ChatColor.WHITE + itemToGive.getAmount()
    										+ ChatColor.DARK_GREEN + "x" + ChatColor.WHITE + pro.getCustomName()
    										+ ChatColor.DARK_GREEN + " for " + ChatColor.WHITE + price + ChatColor.DARK_GREEN + " gull.");
    							} else {
    								p.sendMessage(ChatColor.RED + "Du har ikke nok gull i din inventory.");
    							}
    						} else {
    							p.sendMessage(ChatColor.RED + "Du har ikke nok plass i din inventory for dette produktet.");
    						}
    					} else {
							p.sendMessage(ChatColor.RED + "Fant ikke produktet '" + ChatColor.WHITE + args[0] + ChatColor.RED +"'");    						
    					}
    				}
    			}
    		}
    		return true;
    	}
    	return false;
    }
    
    /**
     * Finner ut hvor mye plass det er i en spillers inv.
     * @param p Spilleren.
     * @param material ItemStacken som det skal være plass til.
     * @return
     */
    private short getFreeSpace(Player p, ItemStack material) {
        short space = 0;
        for (ItemStack it : p.getInventory().getContents()) {
            if (it == null || it.getType() == Material.AIR) {
                space += 64;
            } else if (it.getTypeId() == material.getTypeId()
                    && it.getDurability() == material.getDurability()) {
                space += 64 - it.getAmount();
            }
        }
        return space;
    }
    
    public static boolean isPlayer(CommandSender sender){
    	if(!(sender instanceof Player)){
			return false;
		} else {
			return true;
		}
    }
    
}
