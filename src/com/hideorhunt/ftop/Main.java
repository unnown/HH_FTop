package com.hideorhunt.ftop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.google.common.collect.Lists;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener
{    
	@SuppressWarnings("unused")
	private Logger log = null;
	private Economy economy = null;
	private Essentials essentials = null;
	
	private static JavaPlugin plugin;
	private static SettingsManager settings = SettingsManager.getInstance();
	
	private HashMap<Faction, List<Block>> facBlocks = new HashMap<Faction, List<Block>>();
	private List<Material> expensiveBlocks = new ArrayList<Material>();

    @SuppressWarnings("serial")
	@Override
    public void onEnable() {
	    setPlugin(this);
	    Main.settings.setup(this);
	    saveDefaultConfig();
	    log = new Logger();

	    setupEssentials();
	    setupEconomy();
        getServer().getPluginManager().registerEvents(this, this);

        // TODO load from config..
        expensiveBlocks = new ArrayList<Material>() {
		{
			add(Material.IRON_BLOCK);
			add(Material.GOLD_BLOCK);
			add(Material.EMERALD_BLOCK);
			add(Material.DIAMOND_BLOCK);
			add(Material.HOPPER);
			add(Material.TNT);
			add(Material.MOB_SPAWNER);
			add(Material.BEACON);
		}};	        
        
        for (String key : Main.settings.getBlockData().getKeys(false))
        {
        	String[] k = Main.settings.getBlockData().getString(key).split(":");        	
			final Location kl = new Location(Bukkit.getWorld(k[0]), Integer.parseInt(k[1]), Integer.parseInt(k[2]), Integer.parseInt(k[3]));
			Faction fac = Factions.getInstance().getFactionById(k[4]);
			
			List<Block> blocks = new ArrayList<Block>();
			if (facBlocks.containsKey(fac)) {
				blocks = facBlocks.get(fac);
			}
			blocks.add(kl.getBlock());
			facBlocks.put(fac, blocks);
        }
               
        Logger.debug(ChatColor.DARK_GREEN + "Activated.");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        // Only new blocks please
        for (Iterator<String> localIterator1 = Main.settings.getBlockData().getKeys(false).iterator(); localIterator1.hasNext(); Main.settings.saveBlockData())
        {
        	String key = (String)localIterator1.next();
        	Main.settings.getBlockData().set(key, null);
        }        
        
		int i = 0;		
		for (Faction key : facBlocks.keySet()) {
			
			List<Block> entry = facBlocks.get(key);

			for (Block blck : entry) {
				Location loc = blck.getLocation();
				String val = loc.getWorld().getName() + ":" + 
						loc.getBlockX() + ":" + 
						loc.getBlockY() + ":" + 
						loc.getBlockZ() + ":" +
						key.getId();
				
				Main.settings.getBlockData().set(i + "", val);
				i++;
			}
		}
		
		Main.settings.saveBlockData();
    }	
	
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {    	
    	Player player = (Player)event.getPlayer();

    	if (expensiveBlocks.contains(event.getBlock().getType())) {

    		FPlayer fplayer = FPlayers.getInstance().getByPlayer(player); 
		    if(fplayer.hasFaction()) {	

				List<Block> blocks = new ArrayList<Block>();
				Faction fac = fplayer.getFaction();
				
				if (facBlocks.containsKey(fac)) {
					blocks = facBlocks.get(fac);
				}
				
				blocks.add(event.getBlock());
				facBlocks.put(fac, blocks);        	
				this.changeValue(event.getBlock(), fac, true);
		    }
    	}
    }
	
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
    	if (expensiveBlocks.contains(event.getBlock().getType())) {    	
			for (Faction key : facBlocks.keySet()) {    			
				List<Block> entry = facBlocks.get(key);
				
				Block toRemove = null;
				for (Block blck : entry) {
					if (event.getBlock().getLocation().equals(blck.getLocation())) {
						toRemove = blck;
						break;
					}
				}
				
				if (toRemove != null) {
					entry.remove(toRemove);
					facBlocks.put(key, entry);
					this.changeValue(toRemove, key, false);
					return;
				}
	    	}
    	}
    }  
    
    @EventHandler(ignoreCancelled = true)
	public void BlockExplode(BlockExplodeEvent event) {
		for (Block eventblck : event.blockList()) {
			if (expensiveBlocks.contains(event.getBlock().getType())) {
				for (Faction key : facBlocks.keySet()) {    			
					List<Block> entry = facBlocks.get(key);
					
					Block toRemove = null;
					for (Block blck : entry) {
						if (eventblck.getLocation().equals(blck.getLocation())) {
							toRemove = blck;
							break;
						}
					}
					
					if (toRemove != null) {
						entry.remove(toRemove);
						facBlocks.put(key, entry);
						this.changeValue(toRemove, key, false);
						return;
					}
		    	}
			}
		}
	}    
    
    @EventHandler(ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent event) {
		Block blockFrom = event.getBlock();
		Block blockTo = event.getToBlock();

		boolean isLiquid = blockFrom.getType() == Material.LAVA 
				|| blockFrom.getType() == Material.STATIONARY_LAVA
				|| blockFrom.getType() == Material.WATER
				|| blockFrom.getType() == Material.STATIONARY_WATER;
		
		if (isLiquid) {
			if (expensiveBlocks.contains(event.getBlock().getType())) {
				for (Faction key : facBlocks.keySet()) {    			
					List<Block> entry = facBlocks.get(key);
					
					Block toRemove = null;
					for (Block blck : entry) {
						if (blockTo.getLocation().equals(blck.getLocation())) {
							toRemove = blck;
							break;
						}
					}
					
					if (toRemove != null) {
						entry.remove(toRemove);
						facBlocks.put(key, entry);
						this.changeValue(toRemove, key, false);
						return;
					}
		    	}
			}
		}
	}    
        
    @SuppressWarnings("deprecation")
	private void changeValue(Block block, Faction fac, Boolean add) {
    	BigDecimal totalWorth = BigDecimal.ZERO;

    	for (ItemStack stack : block.getDrops()) {
	    	if (stack != null && stack.getType() != Material.AIR) {    		
		        if (stack.getAmount() > 0) {	        	
		        	try {	        		
			    		BigDecimal worth = essentials.getWorth().getPrice(essentials, stack).multiply(new BigDecimal(stack.getAmount()));                	
			            totalWorth = totalWorth.add(worth);		            
		        	} catch (Exception ex) {}	        	
		        }
	
		        if (totalWorth != BigDecimal.ZERO) {	
		        	if (add) economy.depositPlayer(fac.getAccountId(), Double.parseDouble(totalWorth.toString()));    
		        	else economy.withdrawPlayer(fac.getAccountId(), Double.parseDouble(totalWorth.toString()));      
		        }
	    	}
    	}
    }
    
    private boolean setupEssentials() 
    {    	
    	Plugin essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
    	if (essentialsPlugin.isEnabled() && (essentialsPlugin instanceof Essentials)) this.essentials = (Essentials) essentialsPlugin;
    	else Bukkit.getPluginManager().disablePlugin(this);
    	return this.essentials != null;    	
    }    
    
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}    
	
	public static String color(String s)
	{
	    return ChatColor.translateAlternateColorCodes('&', s);
	}
	
    public static List<String> color(List<String> lore) {
        ArrayList<String> newLore = Lists.newArrayList();
        for (String loretxt : lore) {
        	newLore.add(color(loretxt));
        }
        return newLore;
    }	
	
	public static JavaPlugin getPlugin()
	{
	    return plugin;
	}
	  
	public static void setPlugin(JavaPlugin plugin)
	{
		Main.plugin = plugin;
	}	  
}
