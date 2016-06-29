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

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Updates rooms, removing the renters, changing the text on signs and saves the
 * data.
 * 
 * @author Jakub Sapalski
 */
public class RoomUpdater extends BukkitRunnable {

	private RoomRent plugin;

	/**
	 * Starts the room updater
	 * 
	 * @param plugin
	 *            main plugin instance
	 */
	public RoomUpdater(RoomRent plugin) {
		this.plugin = plugin;
		runTaskTimer(plugin, 0, 20 * 60);
	}

	@Override
	public void run() {
		for (RoomSet set : plugin.getRoomSets().values()) {
			set.update();
		}
	}

}
