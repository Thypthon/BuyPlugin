package net.thehultberg.plugins.buy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * The config handler.
 * @author Hultberg
 *
 */
public class ConfigHandler {

	public BuyPlugin plugin;
	
	/**
	 * Produkter er i denne listen.
	 */
	private List<String> configProds; // The config defined list of prods.
	private HashMap<String, Product> prods; // Listen med produkter, ferdig behandlet.
	
	public ConfigHandler(BuyPlugin instance){
		this.plugin = instance;
		
		prods = new HashMap<String, Product>();
	}
	
	/**
	 * Load the config file.
	 */
	public void load(){
		List<String> exampleProds = Arrays.asList("341-Slimeball-1-4", "17:3-Jungel Wood-1-8");
		plugin.getConfig().addDefault("BuyPlugin.Products", exampleProds);
		
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		plugin.reloadConfig();
		
		this.configProds = plugin.getConfig().getStringList("BuyPlugin.Products");
	}
	
	/**
	 * Skriver en fil med hvordan du definerer produkter.
	 */
	public void printHelpFile(){
		String fileName = "BuyPluginHELP.txt";
		try {
			File f = new File(fileName);
			if(!f.exists()){
				String content = "For å definere et produkt skriver du i dette formatet."
						+ " [id]:[evnt damage]-[navn]-[pris]-[får per pris]";
				 
				BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
		        out.write(content);
		        out.close();
			}
		} catch (IOException e){
			System.out.println("En feil sjedde ved print av hjelpe filen.");
		}
	}
	
	/**
	 * Gjør produkter bedre for søk når en bruker skal kjøpe noe. Gjør om custom name til f.eks. (fra: Jungel Wood) jungel$wood
	 */
	public void trimProds(){
		HashMap<String, Product> trimedProds = new HashMap<String, Product>();
		for(String prod : this.getConfigProds()){
			try {
				String[] splitedProd = prod.split("-+");
				
				// START - Handle ID
				String[] splitedId = splitedProd[0].split(":");
				int prodId = Integer.parseInt(splitedId[0]);	
				short damage = 0;
				if(splitedId.length == 2){ // her er damage defined.
					damage = Short.decode(splitedId[1]);
				}
				// SLUTT - Handle ID
				
				String customName = splitedProd[1].toLowerCase().replaceAll(" ", "").trim();
				int pricePer = Integer.parseInt(splitedProd[2]);
				int getPer = Integer.parseInt(splitedProd[3]);
				
				trimedProds.put(customName, new Product(splitedProd[1].replaceAll(" ", ""), pricePer, getPer, prodId, damage));
			} catch(NumberFormatException ex){
				// Hm... må bare ignorere produktet da.
				String[] splitedProd = prod.split("-"); // Forsøke å hente navn...
				String customName = splitedProd[1];
				BuyPlugin.log.log(Level.WARNING, "Produktet '"+customName+"' hadde en feil i formatering og er ignorert.");
			}
		}
		
		prods = trimedProds;
	}

	public List<String> getConfigProds() {
		return configProds;
	}

	public HashMap<String, Product> getProds() {
		return prods;
	}
	
}
