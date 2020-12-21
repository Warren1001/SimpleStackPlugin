package com.mikedeejay2.simplestack;

import com.mikedeejay2.simplestack.commands.CommandHandler;
import com.mikedeejay2.simplestack.config.Config;
import com.mikedeejay2.simplestack.listeners.*;
import com.mikedeejay2.simplestack.listeners.player.*;
import com.mikedeejay2.simplestack.runnables.GroundItemStacker;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Simple Stack plugin for Minecraft 1.14 - 1.16.4
 * If you find a bug, please report it to the Github:
 * https://github.com/Mikedeejay2/SimpleStackPlugin
 *
 * @author Mikedeejay2
 */
public final class SimpleStack extends JavaPlugin {
	
	// Permission for general Simple Stack use
	private final String permission = "simplestack.use";
	
	// The config of Simple Stack which stores all customizable data
	private Config config;
	
	@Override
	public void onEnable() {
		
		config = new Config(this);
		
		getCommand("simplestack").setExecutor(new CommandHandler(this));
		
		getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
		getServer().getPluginManager().registerEvents(new EntityPickupItemListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
		getServer().getPluginManager().registerEvents(new InventoryMoveItemListener(this), this);
		getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);
		getServer().getPluginManager().registerEvents(new PrepareAnvilListener(this), this);
		getServer().getPluginManager().registerEvents(new InventoryDragListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerBucketEmptyListener(this), this);
		getServer().getPluginManager().registerEvents(new ItemMergeListener(this), this);
		getServer().getPluginManager().registerEvents(new InventoryPickupItemListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerItemConsumeListener(this), this);
		getServer().getPluginManager().registerEvents(new PrepareSmithingListener(this), this);
		
		GroundItemStacker stacker = new GroundItemStacker(this);
		stacker.runTaskTimer(this, 0, 20);
	}
	
	@Override
	public void onDisable() {
		config.saveToDisk();
	}
	
	/**
	 * Get the simplestack.use permission as a string
	 *
	 * @return simplestack.use String permission
	 */
	public String getPermission() {
		return permission;
	}
	
	/**
	 * Get the Config file for Simple Stack
	 *
	 * @return The config of Simple Stack
	 */
	public Config config() {
		return config;
	}
	
}
