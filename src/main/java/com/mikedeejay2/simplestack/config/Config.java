package com.mikedeejay2.simplestack.config;

import com.mikedeejay2.simplestack.SimpleStack;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Config class for holding all configuration values for Simple Stack and
 * managing file saving / loading.
 *
 * @author Mikedeejay2
 */
public class Config {
	
	private final SimpleStack       plugin;
	private final FileConfiguration accessor;
	
	//Variables
	// List mode of the material list. Either Blacklist of Whitelist.
	private ListMode               listMode;
	// Material list of the config (Item Type list in config)
	private List<Material>         materialList;
	// Item amounts based on the item's material (Item Type amounts list in config)
	private Map<Material, Integer> itemAmounts;
	// Unique items list from the unique_items.json
	private List<ItemStack>        uniqueItemList;
	// The max amount for all items in minecraft
	private int                    maxAmount;
	// Whether custom hopper stacking occurs or not
	private boolean                hopperMovement;
	// Whether custom ground stacking occurs or not
	private boolean                groundStacks;
	// Whether the creative middle click dragging should always create a full stack
	private boolean                creativeDrag;
	
	// Internal config data
	// The unique items json file
	private YamlConfiguration uniqueItems;
	
	public Config(SimpleStack plugin) {
		this.plugin = plugin;
		accessor = plugin.getConfig();
		if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
			plugin.saveDefaultConfig();
			plugin.reloadConfig();
		}
		loadData();
	}
	
	public FileConfiguration getAccessor() {
		return accessor;
	}
	
	/**
	 * Load all data from the config files into this class.
	 */
	private void loadData() {
		
		loadDefaultAmount();
		loadListMode();
		loadMaterialList();
		loadItemList();
		loadItemAmounts();
		loadHopperMovement();
		loadGroundStacks();
		loadCreativeDrag();
	}
	
	/**
	 * Load creative item dragging into <tt>creativeDrag</tt> variable of this config
	 */
	private void loadCreativeDrag() {
		creativeDrag = accessor.getBoolean("Creative Item Dragging");
	}
	
	/**
	 * Load hopper movement into the <tt>hopperMovement</tt> variable of this config
	 */
	private void loadHopperMovement() {
		hopperMovement = accessor.getBoolean("Hopper Movement Checks");
	}
	
	/**
	 * Load ground stacking into the <tt>groundStacks</tt> variable of this config
	 */
	private void loadGroundStacks() {
		groundStacks = accessor.getBoolean("Ground Stacking Checks");
	}
	
	/**
	 * Load the default max amount into the <tt>maxAmount</tt> variable for this config
	 */
	private void loadDefaultAmount() {
		maxAmount = accessor.getInt("Default Max Amount");
		if (maxAmount > 64 || maxAmount <= 0) {
			maxAmount = 64;
			plugin.getLogger().warning(accessor.getString("simplestack.warnings.invalid_max_amount"));
		}
	}
	
	/**
	 * Load item amounts into the <tt>itemAmounts</tt> map for this config
	 */
	private void loadItemAmounts() {
		itemAmounts = new HashMap<>();
		ConfigurationSection section      = accessor.getConfigurationSection("Item Amounts");
		Set<String>          materialList = section.getKeys(false);
		for (String mat : materialList) {
			Material material = Material.matchMaterial(mat);
			if (material == null && !mat.equals("Example Item")) {
				plugin.getLogger().warning(accessor.getString("simplestack.warnings.invalid_material").replace("{MAT}", mat));
				continue;
			}
			int amount = section.getInt(mat);
			if (amount == 0 || amount > 64) {
				plugin.getLogger().warning(accessor.getString("simplestack.warnings.number_outside_of_range").replace("{MAT}", mat));
				continue;
			}
			if (material != null) {
				itemAmounts.put(material, amount);
			}
		}
	}
	
	/**
	 * Load the list mode into the <tt>ListMode</tt> variable for this config
	 */
	private void loadListMode() {
		String listMode = accessor.getString("List Mode");
		try {
			this.listMode = ListMode.valueOf(listMode.toUpperCase().replace(" ", "_"));
		} catch (Exception e) {
			plugin.getLogger().warning(accessor.getString("simplestack.warnings.invalid_list_mode").replace("{MODE}", listMode));
			this.listMode = ListMode.BLACKLIST;
		}
	}
	
	/**
	 * Load the material list into the <tt>materialList</tt> list for this config
	 */
	private void loadMaterialList() {
		List<String> matList = accessor.getStringList("Item Types");
		materialList = new ArrayList<>();
		
		for (String mat : matList) {
			Material material = Material.matchMaterial(mat);
			if (material == null && !mat.equals("Example Item")) {
				plugin.getLogger().warning(accessor.getString("simplestack.warnings.invalid_material").replace("{MAT}", mat));
				continue;
			}
			if (material == null) {
				continue;
			}
			materialList.add(material);
		}
	}
	
	/**
	 * Load the unique items list into the <tt>uniqueItems</tt> list for this config
	 */
	private void loadItemList() {
		uniqueItemList = new ArrayList<>();
		File uniqueItemsFile = new File("unique_items.yml");
		if (!uniqueItemsFile.exists()) {
			return;
		}
		this.uniqueItems = YamlConfiguration.loadConfiguration(uniqueItemsFile);
		List<ItemStack> itemList = uniqueItems.getMapList("items").stream().map(map -> ItemStack.deserialize((Map<String, Object>)map))
				.collect(Collectors.toList());
		if (itemList.isEmpty()) {
			return;
		}
		
		for (ItemStack item : itemList) {
			if (item == null || item.getType().isAir()) {
				plugin.getLogger().warning(accessor.getString("simplestack.warnings.invalid_unique_item"));
				continue;
			}
			uniqueItemList.add(item);
		}
	}
	
	/**
	 * Returns whether a material has a custom amount set in the config or not.
	 *
	 * @param material The material to search for
	 *
	 * @return If this item has a custom amount set or not
	 */
	public boolean hasCustomAmount(Material material) {
		return itemAmounts.containsKey(material);
	}
	
	/**
	 * Get the custom amount of an item that has been set in the config.
	 * A check is required before running this commands, see hasCustomAmount.
	 *
	 * @param item The item to get the custom amount for
	 *
	 * @return The custom amount for this item.
	 */
	public int getAmount(ItemStack item) {
		boolean containsUnique   = containsUniqueItem(item);
		boolean containsMaterial = containsMaterial(item.getType());
		boolean hasCustomAmount  = hasCustomAmount(item.getType());
		if (containsUnique) {
			return getUniqueItem(item).getAmount();
		} else if (hasCustomAmount) {
			return itemAmounts.get(item.getType());
		} else if ((getListMode() == ListMode.WHITELIST && containsMaterial) || (getListMode() == ListMode.BLACKLIST && !containsMaterial)) {
			return getMaxAmount();
		} else {
			return item.getMaxStackSize();
		}
	}
	
	/**
	 * Overridden method from <tt>DataFile</tt> that saves the current config file to the disk.
	 * This method also saves the "unique_items.json" file that this config file controls.
	 *
	 * @return Whether the file save was successful or not
	 */
	public boolean saveToDisk() {
		accessor.set("List Mode", listMode == ListMode.BLACKLIST ? "Blacklist" : "Whitelist");
		List<String> materials = new ArrayList<>();
		for (Material material : materialList) {
			if (material == null) {
				continue;
			}
			materials.add(material.toString());
		}
		accessor.set("Item Types", materials);
		accessor.set("Default Max Amount", maxAmount);
		
		accessor.set("Item Amounts", null);
		ConfigurationSection itemAmtAccessor = accessor.getConfigurationSection("Item Amounts");
		for (Map.Entry<Material, Integer> entry : itemAmounts.entrySet()) {
			Material material = entry.getKey();
			if (material == null || material == Material.AIR) {
				continue;
			}
			int    amount      = entry.getValue();
			String materialStr = material.toString();
			itemAmtAccessor.set(materialStr, amount);
		}
		
		accessor.set("Hopper Movement Checks", hopperMovement);
		accessor.set("Ground Stacking Checks", groundStacks);
		accessor.set("Creative Item Dragging", creativeDrag);
		
		if (!uniqueItemList.isEmpty()) {
			File uniqueItemsFile = new File(plugin.getDataFolder(), "unique_items.yml");
			if (uniqueItems == null) {
				uniqueItems = new YamlConfiguration();
			}
			uniqueItems.set("items", uniqueItemList);
			try {
				uniqueItems.save(uniqueItemsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		plugin.saveConfig();
		
		return true;
	}
	
	/**
	 * Overridden method from <tt>DataFile</tt> that resets the config to its default state.
	 *
	 * @return Whether the reset was successful or not
	 */
	public boolean resetFromJar() {
		plugin.saveResource("config.yml", true);
		plugin.reloadConfig();
		return true;
	}
	
	public boolean reload() {
		plugin.reloadConfig();
		return true;
	}
	
	/**
	 * Get the Material list's <tt>ListMode</tt>.
	 * The list mode is either:
	 * <ul>
	 *     <li>Whitelist</li>
	 *     <li>Blacklist</li>
	 * </ul>
	 *
	 * @return The current <tt>ListMode</tt>
	 */
	public ListMode getListMode() {
		return listMode;
	}
	
	/**
	 * Get a list of the materials from the config
	 *
	 * @return The list of materials
	 */
	public List<Material> getMaterialList() {
		return materialList;
	}
	
	/**
	 * Get a map of material to item amount
	 *
	 * @return A map of material to item amount
	 */
	public Map<Material, Integer> getItemAmounts() {
		return itemAmounts;
	}
	
	/**
	 * Return whether the config contains a unique item that matches the item specified
	 *
	 * @param item The <tt>ItemStack</tt> to search for
	 *
	 * @return Whether the item was found in the config
	 */
	public boolean containsUniqueItem(ItemStack item) {
		for (ItemStack curItem : uniqueItemList) {
			if (!curItem.isSimilar(item)) {
				continue;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Get a unique item from the config based off of a reference item of the same properties
	 *
	 * @param item The <tt>ItemStack</tt> to find in the config
	 *
	 * @return The <tt>ItemStack</tt> found with the same properties in the config
	 */
	public ItemStack getUniqueItem(ItemStack item) {
		for (ItemStack curItem : uniqueItemList) {
			if (!curItem.isSimilar(item)) {
				continue;
			}
			return curItem;
		}
		return null;
	}
	
	/**
	 * Return whether the material list contains a specific material or not
	 *
	 * @param material The material to search for
	 *
	 * @return Whether the material was found or not
	 */
	public boolean containsMaterial(Material material) {
		return materialList.contains(material);
	}
	
	/**
	 * Return whether the item amounts list contains a custom amount for a material or not
	 *
	 * @param material The material to search for
	 *
	 * @return Whether a custom amount for the material was found or not
	 */
	public boolean containsItemAmount(Material material) {
		return itemAmounts.containsKey(material);
	}
	
	/**
	 * Get the default max amount for items
	 *
	 * @return The default max amount for items
	 */
	public int getMaxAmount() {
		return maxAmount;
	}
	
	/**
	 * Add a unique item to the config at the player's request. <p>
	 * This method does not save the config, only modifies it.
	 *
	 * @param player The player that requested the action
	 * @param item   The item to add to the config
	 */
	public void addUniqueItem(Player player, ItemStack item) {
		removeUniqueItem(player, item);
		uniqueItemList.add(item);
	}
	
	/**
	 * Add a material to the config at the player's request. <p>
	 * This method does not save the config, only modifies it.
	 *
	 * @param player   The player that requested the action
	 * @param material The material to add to the config
	 *
	 * @return Whether the action was successful or not
	 */
	public boolean addMaterial(Player player, Material material) {
		if (containsMaterial(material)) {
			player.sendMessage(accessor.getString("simplestack.warnings.material_already_exists"));
			return false;
		}
		materialList.add(material);
		return true;
	}
	
	/**
	 * Removes a unique item from the config at the player's request. <p>
	 * This method does not save the config, only modifies it.
	 *
	 * @param player The player that requested the action
	 * @param item   The item to from from the config
	 *
	 * @return Whether the action was successful or not
	 */
	public boolean removeUniqueItem(Player player, ItemStack item) {
		for (ItemStack curItem : uniqueItemList) {
			if (!item.equals(curItem)) {
				continue;
			}
			uniqueItemList.remove(curItem);
			break;
		}
		return true;
	}
	
	/**
	 * Removes a material from the config at the player's request. <p>
	 * This method does not save the config, only modifies it.
	 *
	 * @param player   The player that requested the action
	 * @param material The material to remove from the config
	 *
	 * @return Whether the action was successful or not
	 */
	public boolean removeMaterial(Player player, Material material) {
		materialList.remove(material);
		return true;
	}
	
	/**
	 * Set the <tt>ListMode</tt> of the config. <p>
	 * This method does not save the config, only modifies it.
	 *
	 * @param newMode The new <tt>ListMode</tt> to use in the config
	 */
	public void setListMode(ListMode newMode) {
		this.listMode = newMode;
	}
	
	/**
	 * Add a material and custom amount to the config at the player's request. <p>
	 * This method does not save the config, only modifies it.
	 *
	 * @param player   The player that requested the action
	 * @param material The material to add to the config
	 * @param amount   The new max amount of the item
	 */
	public void addCustomAmount(Player player, Material material, int amount) {
		if (hasCustomAmount(material)) {
			removeCustomAmount(player, material);
		}
		itemAmounts.put(material, amount);
	}
	
	/**
	 * Removes a material from the custom amount list at the player's request. <p>
	 * This method does not save the config, only modifies it.
	 *
	 * @param player   The player that requested the action
	 * @param material The material to remove from the config
	 */
	public void removeCustomAmount(Player player, Material material) {
		if (!hasCustomAmount(material)) {
			player.sendMessage(accessor.getString("simplestack.warnings.custom_amount_does_not_exist"));
			return;
		}
		itemAmounts.remove(material);
	}
	
	/**
	 * Set a new max stack amount for items
	 *
	 * @param maxAmount The new max stack amount
	 */
	public void setMaxAmount(int maxAmount) {
		this.maxAmount = maxAmount;
	}
	
	/**
	 * Returns whether hoppers should process custom stacking or not
	 *
	 * @return Should process hoppers
	 */
	public boolean shouldProcessHoppers() {
		return hopperMovement;
	}
	
	/**
	 * Set whether hoppers should be processed for unstackables or not
	 *
	 * @param hopperMovement The new hopper processing state
	 */
	public void setHopperMovement(boolean hopperMovement) {
		this.hopperMovement = hopperMovement;
	}
	
	/**
	 * Get the list of unique items from the config
	 *
	 * @return The list of unique items
	 */
	public List<ItemStack> getUniqueItemList() {
		return uniqueItemList;
	}
	
	/**
	 * Set a new material list for the config
	 *
	 * @param materialList The new list of materials to use
	 */
	public void setMaterialList(List<Material> materialList) {
		this.materialList.clear();
		for (Material material : materialList) {
			if (material == null) {
				continue;
			}
			this.materialList.add(material);
		}
	}
	
	/**
	 * Set the unique items list of the config to a new list
	 *
	 * @param uniqueItemList The new items list to use
	 */
	public void setUniqueItemList(List<ItemStack> uniqueItemList) {
		this.uniqueItemList = uniqueItemList;
	}
	
	/**
	 * Set a new item amounts list for the config
	 *
	 * @param itemAmounts The new item amounts list
	 */
	public void setItemAmounts(Map<Material, Integer> itemAmounts) {
		this.itemAmounts = itemAmounts;
	}
	
	/**
	 * Get whether ground items should be processed to stack unstackables
	 *
	 * @return The ground stacking state
	 */
	public boolean processGroundItems() {
		return groundStacks;
	}
	
	/**
	 * Set whether the config should process ground item movements for unstackables or not
	 *
	 * @param groundStacks The new state for ground item stacking
	 */
	public void setGroundStacks(boolean groundStacks) {
		this.groundStacks = groundStacks;
	}
	
	/**
	 * Get whether a creative inventory drag event should always create full stacks or not
	 *
	 * @return Creative drag state
	 */
	public boolean shouldCreativeDrag() {
		return creativeDrag;
	}
	
	/**
	 * Set whether a creative inventory drag event should always create full stacks or not
	 *
	 * @param creativeDrag The new creative drag state
	 */
	public void setCreativeDrag(boolean creativeDrag) {
		this.creativeDrag = creativeDrag;
	}
}
