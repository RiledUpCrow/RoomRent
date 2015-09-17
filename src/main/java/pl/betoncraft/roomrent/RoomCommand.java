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

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * The main command used to create rooms.
 * 
 * @author Jakub Sapalski
 */
public class RoomCommand implements CommandExecutor {
	
	private final RoomRent plugin;

	public RoomCommand(RoomRent plugin) {
		this.plugin = plugin;
		plugin.getCommand("room").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("room")) {
			if (args.length == 0) {
				showHelp(sender, label);
				return true;
			}
			if (args[0].equalsIgnoreCase("reload")) {
				plugin.reload();
				return true;
			}
			if (args[0].equalsIgnoreCase("add")) {
				if (sender instanceof Player) {
					// set and region name must be provided
					if (args.length != 3) {
						sender.sendMessage("§cIncorrect amount of arguments: "
								+ "/roomrent add <set> <region>");
						return true;
					}
					Player player = ((Player) sender);
					// region must exist
					if (WorldGuardPlugin.inst().getRegionManager(
							player.getWorld())
							.getRegion(args[2]) == null) {
						sender.sendMessage("§cThis region does not exist!");
						return true;
					}
					// the player must look at the sign
					Set<Material> transparent = new HashSet<Material>();
					transparent.add(Material.AIR);
					Block target = player.getTargetBlock(transparent, 15);
					if (target.getType() != Material.SIGN_POST &&
							target.getType() != Material.WALL_SIGN) {
						sender.sendMessage("§cYou have to look at the sign!");
						return true;
					}
					// check if the world does not conflict with configuration
					String world = plugin.getConfig()
							.getString("room_sets." + args[1] + ".world");
					if (world == null) {
						plugin.getConfig().set("room_sets." + args[1] +
								".world", player.getWorld().getName());
					} else if (!world.equals(target.getWorld().getName())){
						sender.sendMessage("§3This set is on another world!");
						return true;
					}
					// adding region to the config
					plugin.getConfig().set("room_sets." + args[1] + ".rooms." +
							args[2],
							target.getLocation().getBlockX() + ";" +
							target.getLocation().getBlockY() + ";" +
							target.getLocation().getBlockZ());
					plugin.saveConfig();
					sender.sendMessage("§2Region successfully added!");
					return true;
				}
				// the console cannot add rooms
				sender.sendMessage("Please edit the files manually and "
						+ "use /roomrent reload");
				return true;
			}
			showHelp(sender, label);
			return true;
		}
		return false;
	}

	/**
	 * Shows the help to the player.
	 */
	private void showHelp(CommandSender sender, String label) {
		sender.sendMessage("§eTo add the room to a set use a command '§b/"
				+ label + " add <set> <region>§e', where §b<set>§e is a name"
				+ " of a set and §b<region>§e is a name of the WorldGuard"
				+ " region. You have to look at the sign, which will become "
				+ "an information board for the room.");
		sender.sendMessage("§eIf you want to reload the saved configuration,"
				+ " use '§b/" + label + " reload§e' command.");
	}

}
