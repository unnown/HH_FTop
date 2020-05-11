package com.hideorhunt.ftop;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class SettingsManager
{
  static SettingsManager instance = new SettingsManager();
  Plugin p;
  FileConfiguration BlockData;
  File BlockDataFile;
  
  public static SettingsManager getInstance()
  {
    return instance;
  }
  
  public void setup(Plugin p)
  {
    if (!p.getDataFolder().exists()) {
      p.getDataFolder().mkdir();
    }
    this.BlockDataFile = new File(p.getDataFolder(), "blockdata.yml");
    if (!this.BlockDataFile.exists()) {
      try
      {
        this.BlockDataFile.createNewFile();
      }
      catch (IOException e)
      {
        Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create blockdata.yml!");
      }
    }
    this.BlockData = YamlConfiguration.loadConfiguration(this.BlockDataFile);
    
    try
    {    
    	this.BlockDataFile.delete();
    	this.BlockDataFile.createNewFile();
    }
    catch (IOException e)
    {
      Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create blockdata.yml!");
    }    
  }
  
  public FileConfiguration getBlockData()
  {
    return this.BlockData;
  }
  
  public void saveBlockData()
  {
    try
    {
      this.BlockData.save(this.BlockDataFile);
    }
    catch (IOException e)
    {
      Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save blockdata.yml!");
    }
  }
}
