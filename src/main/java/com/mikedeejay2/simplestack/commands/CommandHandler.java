package com.mikedeejay2.simplestack.commands;

import com.mikedeejay2.simplestack.SimpleStack;
import com.mikedeejay2.simplestack.config.Config;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandHandler implements CommandExecutor {
	
	private final SimpleStack plugin;
	private final Config      config;
	
	public CommandHandler(SimpleStack plugin) {
		this.plugin = plugin;
		config = plugin.config();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("additem")) {
				if (!sender.hasPermission("simplestack.additem")) {
					sender.sendMessage(format("simplestack.warnings.no_permission"));
					return true;
				}
				if (!(sender instanceof Player)) {
					sender.sendMessage(format("simplestack.warnings.must_be_player"));
					return true;
				}
				Player    player   = (Player)sender;
				ItemStack heldItem = player.getInventory().getItemInMainHand();
				if (heldItem.getType() == Material.AIR) {
					player.sendMessage(format("simplestack.warnings.held_item_required"));
					return true;
				}
				config.addUniqueItem(player, heldItem);
				config.saveToDisk();
				player.sendMessage(format("simplestack.commands.additem.success"));
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
			} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
				if (!sender.hasPermission("simplestack.reload")) {
					sender.sendMessage(format("simplestack.warnings.no_permission"));
					return true;
				}
				config.reload();
				sender.sendMessage(format("simplestack.reload.success"));
				if (!(sender instanceof Player)) {
					return true;
				}
				Player player = (Player)sender;
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
			} else if (args[0].equalsIgnoreCase("removeitem")) {
				if (!sender.hasPermission("simplestack.removeitem")) {
					sender.sendMessage(format("simplestack.warnings.no_permission"));
					return true;
				}
				if (!(sender instanceof Player)) {
					sender.sendMessage(format("simplestack.warnings.must_be_player"));
					return true;
				}
				Player    player   = (Player)sender;
				ItemStack heldItem = player.getInventory().getItemInMainHand();
				if (heldItem.getType() == Material.AIR) {
					player.sendMessage(format("simplestack.warnings.held_item_required"));
					return true;
				}
				config.removeUniqueItem(player, heldItem);
				config.saveToDisk();
				player.sendMessage(format("simplestack.commands.removeitem.success"));
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
			} else if (args[0].equalsIgnoreCase("reset")) {
				if (!sender.hasPermission("simplestack.reset")) {
					sender.sendMessage(format("simplestack.warnings.no_permission"));
					return true;
				}
				config.resetFromJar();
				sender.sendMessage(format("simplestack.reset.success"));
				if (!(sender instanceof Player)) {
					return true;
				}
				Player player = (Player)sender;
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
			} else if (args[0].equalsIgnoreCase("setamount")) {
				if(!sender.hasPermission("simplestack.setamount")) {
					sender.sendMessage(format("simplestack.warnings.no_permission"));
					return true;
				}
				if (args.length != 2) {
					sender.sendMessage(format("simplestack.commands.setamount.format"));
					return true;
				}
				if (!NumberUtils.isNumber(args[1])) {
					sender.sendMessage(ChatColor.RED + args[1] + " is not a number.");
					return true;
				}
				int amount = Integer.parseInt(args[1]);
				if (amount < 0) {
					sender.sendMessage(ChatColor.RED + args[1] + " must be greater than 0.");
					return true;
				}
				Player    player = (Player)sender;
				ItemStack item   = player.getInventory().getItemInMainHand();
				if (item.getType() == Material.AIR) {
					player.sendMessage(format("simplestack.warnings.held_item_required"));
					return true;
				}
				item.setAmount(amount);
				player.sendMessage(format("simplestack.commands.setamount.success"));
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1f);
			}
			return true;
		}
		return false;
	}
	
	private String format(String key) {
		return ChatColor.translateAlternateColorCodes('&', config.getAccessor().getString(key));
	}
	
}
