package com.mikedeejay2.simplestack.runnables;

import com.mikedeejay2.simplestack.SimpleStack;
import com.mikedeejay2.simplestack.util.CancelUtils;
import com.mikedeejay2.simplestack.util.MoveUtils;
import com.mikedeejay2.simplestack.util.StackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom item stacking runnable for stacking unstackable items when they are on
 * the ground.
 *
 * @author Mikedeejay2
 */
public class GroundItemStacker extends BukkitRunnable {
	
	private final SimpleStack plugin;
	
	public GroundItemStacker(SimpleStack plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		if (!plugin.config().processGroundItems()) {
			return;
		}
		List<World> worlds = Bukkit.getWorlds();
		List<Item>  items  = new ArrayList<>();
		for (World world : worlds) {
			for (Item item : world.getEntitiesByClass(Item.class)) {
				ItemStack stack = item.getItemStack();
				if (CancelUtils.cancelStackCheck(plugin, stack)) {
					continue;
				}
				items.add(item);
			}
		}
		for (Item item : items) {
			if (item.isDead() || item.getItemStack().getType() == Material.AIR) {
				continue;
			}
			ItemStack stack    = item.getItemStack();
			Material  material = stack.getType();
			if (StackUtils.getMaxAmount(plugin, stack) == material.getMaxStackSize()) {
				continue;
			}
			List<Entity> nearby = item.getNearbyEntities(1, 1, 1);
			for (Entity entity : nearby) {
				if (!(entity instanceof Item)) {
					continue;
				}
				Item      newItem  = (Item)entity;
				ItemStack newStack = newItem.getItemStack();
				if (!stack.isSimilar(newStack)) {
					continue;
				}
				MoveUtils.mergeItems(plugin, newStack, stack);
				if (!newStack.getType().isAir()) {
					newItem.setItemStack(newStack);
				}
				if (!stack.getType().isAir()) {
					item.setItemStack(stack);
				}
			}
		}
	}
	
}
