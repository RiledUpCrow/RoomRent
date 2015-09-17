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

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import pl.betoncraft.betonquest.BetonQuest;

/**
 * Handles showing the room by the NPC.
 * 
 * @author Jakub Sapalski
 */
public class ShowingHandler implements Listener {
	
	private String endText;
	private Player player;
	private Location loc;
	private final NPC npc;

	public ShowingHandler(Player player, Location loc, int npcId,
			String startText, String endText) {
		this.endText = endText;
		this.player = player;
		this.loc = loc;
		this.npc = CitizensAPI.getNPCRegistry().getById(npcId);
		if (npc == null) {
			BetonQuest.getInstance().getLogger().warning("NPC with ID "
					+ npcId + " does not exist");
			return;
		}
		if (!npc.getNavigator().isNavigating()) {
			Bukkit.getPluginManager().registerEvents(this,
					RoomRent.getPlugin(RoomRent.class));
			player.sendMessage(startText);
			npc.getNavigator().setTarget(loc);
		}
	}
	
	@EventHandler
	public void onNavigationEnd(final NavigationCompleteEvent e) {
		if (!e.getNPC().equals(npc)) return;
		player.sendMessage(endText);
		npc.getNavigator().setTarget(loc);
		npc.getNavigator().setPaused(true);
		new BukkitRunnable() {
			@Override
			public void run() {
				npc.getNavigator().setPaused(false);
			}
		}.runTaskLater(BetonQuest.getInstance(), 5 * 20);
		HandlerList.unregisterAll(this);
	}
	
}
