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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

/**
 * Represents a set of rooms.
 * 
 * @author Jakub Sapalski
 */
public class RoomSet {

	private World world;
	private String name;
	private HashMap<String, SingleRoom> rooms = new HashMap<>();

	/**
	 * Loads from the configuration a room set with given name. 
	 * 
	 * @param plugin
	 * 				main plugin instance
	 * @param name
	 * 				name of the set
	 */
	public RoomSet(RoomRent plugin, String name) {
		this.name = name;
		world = Bukkit.getWorld(plugin.getConfig().getString(
				"room_sets." + name + ".world"));
		for (String key : plugin.getConfig()
				.getConfigurationSection("room_sets." + name + ".rooms")
				.getKeys(false)) {
			try {
				String loc = plugin.getConfig()
						.getString("room_sets." + name + ".rooms." + key);
				rooms.put(key, new SingleRoom(plugin, world, key, loc));
			} catch (RoomException e) {
				plugin.getLogger().severe(e.getMessage());
			}
		}
		plugin.getLogger().info("Loaded " + rooms.size() + " rooms in '"
				+ name + "' set.");
	}
	
	/**
	 * @return true if this set contains at leas one free room
	 */
	public boolean hasRoom() {
		for (SingleRoom room : rooms.values()) {
			if (room.isFree()) return true;
		}
		return false;
	}
	
	/**
	 * Rents a first available room for the player or adds time to existing one.
	 * 
	 * @param player
	 * 				the renter of the room
	 * @param time
	 * 				duration of the renting
	 */
	public void rentRoom(OfflinePlayer player, long time) {
		// first check if the player has a room in this set, and if so
		// make the duration longer
		for (SingleRoom room : rooms.values()) {
			if (room.getRenter() != null && room.getRenter().equals(player)) {
				room.addTime(time);
				return;
			}
		}
		// if the player does not have a room, rent him a new one
		for (SingleRoom room : rooms.values()) {
			if (room.isFree()) {
				room.addRenter(player, time);
				return;
			}
		}
		// if there is no free room, make a note in the console
		Logger.getLogger("RoomRent").warning(
				"There are no more free rooms in the '" + name + "' set for "
				+ player.getName()
				+ ". You should use a condition to check for that"
				+ " and block renting a room.");
	}
	
	/**
	 * Checks if the set has a room rented by this player.
	 * 
	 * @param player
	 * 				player to check
	 * @return true if the player has a room in this set
	 */
	public boolean containsRenter(OfflinePlayer player) {
		for (SingleRoom room : rooms.values()) {
			if (room.getRenter() != null && room.getRenter().equals(player)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the location of the sign of the player's room.
	 * 
	 * @param player
	 * 				the renter of the room
	 * @return the location of the sign or null if the player is not a renter
	 */
	public Location getLocation(OfflinePlayer player) {
		for (SingleRoom room : rooms.values()) {
			if (room.getRenter() != null && room.getRenter().equals(player)){
				return room.getSign().getLocation();
			}
		}
		return null;
	}
	
	/**
	 * Calculates time left to termination of the rent.
	 * 
	 * @param player
	 * 				player to check
	 * @return -1 if the player does not have a room or time to termination
	 */
	public long getTimeLeft(OfflinePlayer player) {
		for (SingleRoom room : rooms.values()) {
			if (room.getRenter() != null && room.getRenter().equals(player)) {
				return room.getTime() - new Date().getTime();
			}
		}
		return -1;
	}
	
	/**
	 * Removes a renter from this set.
	 * 
	 * @param player
	 * 				player to remove
	 */
	public void removeRenter(OfflinePlayer player) {
		for (SingleRoom room : rooms.values()) {
			if (room.getRenter() != null && room.getRenter().equals(player)) {
				room.removeRenter();
				room.update();
				return;
			}
		}
	}
	
	/**
	 * @return updates all rooms in this set
	 */
	public void update() {
		for (SingleRoom room : rooms.values()) {
			room.update();
		}
	}

	/**
	 * @return the name of the set
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the rooms in this set
	 */
	public Collection<SingleRoom> getRooms() {
		return rooms.values();
	}

}
