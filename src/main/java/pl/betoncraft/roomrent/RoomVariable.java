/**
 * RoomRent - convenient room renting with BetonQuest
 * Copyright (C) 2016 Jakub "Co0sh" Sapalski
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

import java.text.SimpleDateFormat;
import java.util.Date;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.Instruction;
import pl.betoncraft.betonquest.InstructionParseException;
import pl.betoncraft.betonquest.api.Variable;
import pl.betoncraft.betonquest.config.Config;
import pl.betoncraft.betonquest.utils.PlayerConverter;

/**
 * BetonQuest variable which gets replaced by varius information about a set.
 * 
 * @author Jakub Sapalski
 */
public class RoomVariable extends Variable {

	private RoomSet set;
	private Type type;
	private RoomRent plugin;

	public RoomVariable(Instruction instruction) throws InstructionParseException {
		super(instruction);
		plugin = RoomRent.getPlugin(RoomRent.class);
		set = plugin.getRoomSets().get(instruction.next());
		if (set == null) {
			throw new InstructionParseException("There is no such set as '" + instruction.current() + "'");
		}
		switch (instruction.next().toLowerCase()) {
		case "total":
			type = Type.TOTAL_ROOMS;
			break;
		case "free":
			type = Type.FREE_ROOMS;
			break;
		case "full":
			type = Type.FULL_ROOMS;
			break;
		case "left":
			type = Type.TIME_LEFT;
			break;
		case "date":
			type = Type.ENDING_DATE;
			break;
		default:
			throw new InstructionParseException("Unknown keyword: '" + instruction.current() + "'");
		}
	}

	@Override
	public String getValue(String playerID) {
		switch (type) {
		case TOTAL_ROOMS:
			return Integer.toString(set.getRooms().size());
		case FREE_ROOMS:
			int free = 0;
			for (SingleRoom room : set.getRooms()) {
				if (room.isFree()) {
					free++;
				}
			}
			return Integer.toString(free);
		case FULL_ROOMS:
			int full = 0;
			for (SingleRoom room : set.getRooms()) {
				if (!room.isFree()) {
					full++;
				}
			}
			return Integer.toString(full);
		case TIME_LEFT:
			String lang = BetonQuest.getInstance().getPlayerData(playerID).getLanguage();
			String daysWord = Config.getMessage(lang, "days");
			String hoursWord = Config.getMessage(lang, "hours");
			String minutesWord = Config.getMessage(lang, "minutes");
			String secondsWord = Config.getMessage(lang, "seconds");
			long timeLeft = set.getTimeLeft(PlayerConverter.getPlayer(playerID));
			long s = (timeLeft / (1000)) % 60;
			long m = (timeLeft / (1000 * 60)) % 60;
			long h = (timeLeft / (1000 * 60 * 60)) % 24;
			long d = (timeLeft / (1000 * 60 * 60 * 24));
			StringBuilder time = new StringBuilder();
			String[] words = new String[3];
			if (d > 0)
				words[0] = d + " " + daysWord;
			if (h > 0)
				words[1] = h + " " + hoursWord;
			if (m > 0)
				words[2] = m + " " + minutesWord;
			int count = 0;
			for (String word : words) {
				if (word != null)
					count++;
			}
			if (count == 0) {
				time.append(s + " " + secondsWord);
			} else if (count == 1) {
				for (String word : words) {
					if (word == null)
						continue;
					time.append(word);
				}
			} else if (count == 2) {
				boolean second = false;
				for (String word : words) {
					if (word == null)
						continue;
					if (second) {
						time.append(" " + word);
					} else {
						time.append(word + " " + Config.getMessage(lang, "and"));
						second = true;
					}
				}
			} else {
				time.append(words[0] + ", " + words[1] + " " + Config.getMessage(lang, "and ") + words[2]);
			}
			return time.toString();
		case ENDING_DATE:
			return new SimpleDateFormat(Config.getString("config.date_format"))
					.format(set.getTimeLeft(PlayerConverter.getPlayer(playerID)) + new Date().getTime());
		}
		return "";
	}

	private enum Type {
		TOTAL_ROOMS, FREE_ROOMS, FULL_ROOMS, TIME_LEFT, ENDING_DATE
	}

}
