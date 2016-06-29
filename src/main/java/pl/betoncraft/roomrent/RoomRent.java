/**
 * RoomRent - convenient room renting with BetonQuest
 * Copyright (C) 2015 Jakub "Co0sh" Sapalski
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.roomrent;

import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import pl.betoncraft.betonquest.BetonQuest;

/**
 * RoomRent main class.
 * 
 * @author Jakub Sapalski
 */
public class RoomRent extends JavaPlugin {

	private HashMap<String, RoomSet> roomSets = new HashMap<>();
	private Database database;

	@Override
	public void onEnable() {
		// save the default config
		saveDefaultConfig();
		// load all room sets from the configuration
		load();
		// start room updater
		new RoomUpdater(this);
		// register a command
		new RoomCommand(this);
		// register BetonQuest events and conditions
		BetonQuest.getInstance().registerEvents(getConfig().getString("types.rent_event"), RentEvent.class);
		BetonQuest.getInstance().registerEvents(getConfig().getString("types.leave_event"), LeaveEvent.class);
		BetonQuest.getInstance().registerConditions(getConfig().getString("types.room_condition"), RoomCondition.class);
		BetonQuest.getInstance().registerConditions(getConfig().getString("types.time_condition"), TimeCondition.class);
		BetonQuest.getInstance().registerConditions(getConfig().getString("types.free_condition"), FreeCondition.class);
		BetonQuest.getInstance().registerVariable(getConfig().getString("types.room_variable"), RoomVariable.class);
		// if Citizens is enabled, register 'show' event
		if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
			BetonQuest.getInstance().registerEvents(getConfig().getString("types.show_event"), ShowEvent.class);
		}
		// start metrics
		try {
			new Metrics(this).start();
		} catch (IOException e) {
		}
	}

	@Override
	public void onDisable() {
		database.save();
	}

	/**
	 * Reloads the plugin.
	 */
	public void reload() {
		database.save();
		reloadConfig();
		load();
	}

	/**
	 * @return the database object
	 */
	public Database getDB() {
		return database;
	}

	/**
	 * @return the hashmap containing all sets of rooms
	 */
	public HashMap<String, RoomSet> getRoomSets() {
		return roomSets;
	}

	/**
	 * Loads all the rooms and associated player data.
	 */
	private void load() {
		database = new Database(this);
		roomSets.clear();
		ConfigurationSection config = getConfig().getConfigurationSection("room_sets");
		if (config != null)
			for (String key : config.getKeys(false)) {
				roomSets.put(key, new RoomSet(this, key));
			}
	}

}
