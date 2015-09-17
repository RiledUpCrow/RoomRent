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

import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * A single room in a RoomSet.
 * 
 * @author Jakub Sapalski
 */
public class SingleRoom {

	private RoomRent plugin;
	private ProtectedRegion region;
	private World world;
	private Sign sign;
	private OfflinePlayer renter;
	private long time;

	/**
	 * Creates a room with specified region and sign location.
	 *
	 * @param region
	 *            name of the WorldGuard region
	 * @param signLoc
	 *            location of the room sign
	 * @throws Exception
	 */
	public SingleRoom(RoomRent plugin, World world, String regionName,
			String signLoc) throws RoomException {
		this.world = world;
		this.plugin = plugin;
		region = WorldGuardPlugin.inst().getRegionManager(world)
				.getRegion(regionName);
		if (region == null) {
			throw new RoomException("Region with given name does not exist!");
		}
		sign = getBlock(signLoc, world);
		FileConfiguration config = plugin.getDB().getConfig();
		String rawRenter = config.getString(world.getName() + "." + regionName
				+ ".player");
		renter = (rawRenter == null) ? null : Bukkit.getOfflinePlayer(
				UUID.fromString(rawRenter));
		String rawTime = config.getString(world.getName() + "." + regionName
				+ ".time");
		time = (rawTime == null) ? -1 : Long.parseLong(rawTime);
	}
	
	public boolean isFree() {
		return renter == null && time < 0;
	}
	
	/**
	 * @return the region associated with this room
	 */
	public ProtectedRegion getRegion() {
		return region;
	}
	
	/**
	 * @return this room's sign
	 */
	public Sign getSign() {
		return sign;
	}
	
	/**
	 * @return the player owning the region
	 */
	public OfflinePlayer getRenter() {
		return renter;
	}
	
	/**
	 * Adds a player as the renter of the room.
	 * 
	 * @param renter
	 * 				player to add as the renter
	 * @param time
	 * 				time of the rent
	 */
	public void addRenter(OfflinePlayer renter, long time) {
		this.renter = renter;
		this.time = new Date().getTime() + time;
		plugin.getDB().getConfig().set(world.getName() + "." + region.getId()
				+ ".player",
				renter.getUniqueId().toString());
		plugin.getDB().getConfig().set(world.getName() + "." + region.getId()
				+ ".time", this.time);
		plugin.getDB().save();
		region.getMembers().getPlayerDomain().addPlayer(renter.getUniqueId());
		update();
	}
	
	/**
	 * Removes the current renter of the region. Does not update the signs!
	 */
	public void removeRenter() {
		plugin.getDB().getConfig().set(world.getName() + "." + region.getId(),
				null);
		plugin.getDB().save();
		region.getMembers().getPlayerDomain().removePlayer(renter.getUniqueId());
		time = -1;
		renter = null;
	}
	
	/**
	 * @return time of termination
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * Adds time of the renting.
	 * 
	 * @param time to add
	 */
	public void addTime(long time) {
		this.time += time;
		plugin.getDB().getConfig().set(world.getName() + "." + region.getId()
				+ ".time", this.time);
		plugin.getDB().save();
		update();
	}
	
	/**
	 * Removes the player if the time has passed and updates the sign.
	 */
	public void update() {
		if (renter == null && time < 0) {
			sign.setLine(1, plugin.getConfig().getString("time_messages.free"));
			sign.setLine(2, null);
			sign.update(true, false);
			return;
		}
		long current = new Date().getTime();
		String text = null;
		// if current time is grater than termination one, remove the player
		if (time < current) {
			removeRenter();
			update();
			return;
		} else {
			// generate nice string with amount of time
			long diff = time - current; 
			long day = 1000 * 60 * 60 * 24;
			long hour = 1000 * 60 * 60;
			long minute = 1000 * 60;
			if (diff > day) {
				text = String.valueOf((diff - (diff%day)) / day) + " "
						+ plugin.getConfig().getString("time_messages.day");
			} else if (diff > hour) {
				text = String.valueOf((diff - (diff%hour)) / hour) + " "
						+ plugin.getConfig().getString("time_messages.hour");
			} else if (diff > minute) {
				text = String.valueOf((diff - (diff%minute)) / minute) + " "
						+ plugin.getConfig().getString("time_messages.minute");
			} else {
				text = "1 " + plugin.getConfig().getString(
						"time_messages.minute");
			}
		}
		// set only two lines of the sign, so the rest can be freely modified
		sign.setLine(1, (renter == null) ? null : renter.getName());
		sign.setLine(2, (time < 0) ? null : text);
		sign.update(true, false);
	}

	/**
	 * Parses the location of the sign and return its block.
	 * 
	 * @param signLoc
	 *            location string of the sign (x;y;z;world)
	 * @throws Exception
	 *             when there is a problem with the location
	 * @return the block of the sign
	 */
	private Sign getBlock(String signLoc, World world) throws RoomException {
		String[] location = signLoc.split(";");
		if (location.length != 3) {
			throw new RoomException("Wrong location format");
		}
		int y = 0, x = 0, z = 0;
		try {
			x = Integer.parseInt(location[0]);
			y = Integer.parseInt(location[1]);
			z = Integer.parseInt(location[2]);
		} catch (NumberFormatException e) {
			throw new RoomException("Cannot parse coordinates");
		}
		Block block = new Location(world, x, y, z).getBlock();
		if (block != null && block.getType() != Material.SIGN_POST
				&& block.getType() != Material.WALL_SIGN) {
			throw new RoomException("The block is not a sign");
		}
		return (Sign) block.getState();
	}

}
