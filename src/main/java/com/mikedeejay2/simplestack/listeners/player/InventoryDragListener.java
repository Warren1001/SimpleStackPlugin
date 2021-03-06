package com.mikedeejay2.simplestack.listeners.player;

import com.mikedeejay2.simplestack.SimpleStack;
import com.mikedeejay2.simplestack.util.CancelUtils;
import com.mikedeejay2.simplestack.util.MoveUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for Inventory Drag events
 *
 * @author Mikedeejay2
 */
public class InventoryDragListener implements Listener {
	
	private final SimpleStack plugin;
	
	public InventoryDragListener(SimpleStack plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * This method properly distributes un-stackable items that have been stacked evenly
	 * across the inventory slots. This fixes the 1 per item bug that was happening
	 * before this was implemented.
	 *
	 * @param event The event being activated
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void inventoryDragEvent(InventoryDragEvent event) {
        if (event.getType() != DragType.EVEN) {
            return;
        }
		InventoryView inventoryView = event.getView();
        if (event.getInventory() instanceof BrewerInventory || event.getInventory() instanceof BeaconInventory) {
            return;
        }
		Player player = (Player)inventoryView.getPlayer();
        if (CancelUtils.cancelPlayerCheck(plugin, player)) {
            return;
        }
		
		ItemStack cursor = event.getOldCursor();
        if (CancelUtils.cancelStackCheck(plugin, cursor)) {
            return;
        }
        if (CancelUtils.cancelGUICheck(plugin, event.getInventory(), cursor)) {
            return;
        }
		GameMode gameMode = player.getGameMode();
		if (gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE) {
			MoveUtils.dragItemsSurvival(plugin, event, inventoryView, player, cursor);
		} else {
			if (plugin.config().shouldCreativeDrag()) {
				MoveUtils.dragItemsCreative(plugin, event, inventoryView, player, cursor);
			} else {
				MoveUtils.dragItemsSurvival(plugin, event, inventoryView, player, cursor);
			}
		}
		
		player.updateInventory();
		event.setCancelled(true);
	}
	
}
