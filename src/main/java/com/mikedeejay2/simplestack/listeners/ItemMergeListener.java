package com.mikedeejay2.simplestack.listeners;

import com.mikedeejay2.simplestack.SimpleStack;
import com.mikedeejay2.simplestack.util.MoveUtils;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for Item Merge events
 *
 * @author Mikedeejay2
 */
public class ItemMergeListener implements Listener {
	
	private final SimpleStack plugin;
	
	public ItemMergeListener(SimpleStack plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Patches bug where stackable items that are set to a lower value than regular
	 * can be merged into a larger number when dropped.
	 *
	 * @param event The event being activated
	 */
	@EventHandler
	public void itemMergeEvent(ItemMergeEvent event) {
		Item resultItem = event.getEntity();
		Item targetItem = event.getTarget();
		event.setCancelled(true);
		ItemStack resultStack = resultItem.getItemStack();
		ItemStack targetStack = targetItem.getItemStack();
		MoveUtils.mergeItems(plugin, resultStack, targetStack);
	}
	
}
