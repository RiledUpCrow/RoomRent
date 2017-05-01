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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.Instruction;
import pl.betoncraft.betonquest.InstructionParseException;
import pl.betoncraft.betonquest.api.QuestEvent;
import pl.betoncraft.betonquest.config.Config;
import pl.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Makes the Citizen NPC walk to the rented room, showing the way.
 * 
 * @author Jakub Sapalski
 */
public class ShowEvent extends QuestEvent {

	private String startText;
	private String endText;
	private int npcId;
	private RoomSet set;

	public ShowEvent(Instruction instruction) throws InstructionParseException {
		super(instruction);
		set = RoomRent.getPlugin(RoomRent.class).getRoomSets().get(instruction.next());
		if (set == null) {
			throw new InstructionParseException("There is no such set as '" + instruction.current() + "'");
		}
		npcId = instruction.getInt();
		startText = instruction.next().replace('_', ' ').replace('&', '§');
		endText = instruction.next().replace('_', ' ').replace('&', '§');
		for (String variable : BetonQuest.resolveVariables(startText + endText)) {
			BetonQuest.createVariable(Config.getPackages().get("default"), variable);
		}
	}

	@Override
	public void run(String playerID) {
		final Player player = PlayerConverter.getPlayer(playerID);
		final Location loc = set.getLocation(player);
		if (loc == null) {
			BetonQuest.getInstance().getLogger().warning("The player " + PlayerConverter.getName(playerID)
					+ " does not have a room rented in '" + set.getName() + "' set");
			return;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				new ShowingHandler(player, loc, npcId, startText, endText);
			}
		}.runTaskLater(BetonQuest.getInstance(), 20);
	}

}
