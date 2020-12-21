package com.mikedeejay2.simplestack.listeners;

import com.mikedeejay2.simplestack.SimpleStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

/**
 * Listens for Item Spawn events. <p>
 * <b>Currently unused</b>
 *
 * @author Mikedeejay2
 */
public class ItemSpawnListener implements Listener {
	
	private final SimpleStack plugin;
	
	public ItemSpawnListener(SimpleStack plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * @param event The event being activated
	 */
	@EventHandler
	public void itemSpawnEvent(ItemSpawnEvent event) {
	
	}
	
}