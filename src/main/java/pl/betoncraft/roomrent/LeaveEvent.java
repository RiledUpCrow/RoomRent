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

import pl.betoncraft.betonquest.Instruction;
import pl.betoncraft.betonquest.InstructionParseException;
import pl.betoncraft.betonquest.api.QuestEvent;
import pl.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Makes the player leave his room.
 * 
 * @author Jakub Sapalski
 */
public class LeaveEvent extends QuestEvent {

	private RoomSet set;

	public LeaveEvent(Instruction instruction) throws InstructionParseException {
		super(instruction);
		set = RoomRent.getPlugin(RoomRent.class).getRoomSets().get(instruction.next());
		if (set == null) {
			throw new InstructionParseException("There is no such set as '" + instruction.current() + "'");
		}
	}

	@Override
	public void run(String playerID) {
		set.removeRenter(PlayerConverter.getPlayer(playerID));
	}

}
