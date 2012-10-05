package de.henry4k.rpcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

public class ChatSystem implements CommandExecutor {
	private RpCraft plugin = null;

	public ChatSystem( RpCraft plugin ) {
		this.plugin = plugin;
	}

	public boolean checkPermission( ChatMode mode, Player player ) {
		switch(mode) {
			case OOC:
				return player.hasPermission("rpcraft.ooc");

			case TALK:
			case WHISPER:
			case SHOUT:
			case EMOTE:
				return player.hasPermission("rpcraft.local");

			case RADIO:
				return player.hasPermission("rpcraft.radio");

			default:
				throw new Error("Something went wrong.");
		}
	}

	public boolean chat( Player player, ChatMode mode, String message ) {
		if(checkPermission(mode, player) == false) {
			// TODO: Inform the player that they has insufficient permissions.
			return false;
		}

		// More code goes here.

		return true;
	}

	@Override
	public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args ) {
		try {
			ChatMode mode = ChatMode.valueOf( cmd.getName().toUpperCase() );
			
			if(!(sender instanceof Player)) {
				sender.sendMessage("Only players can use this command!");
				return false;
			}
			Player player = (Player)sender;

			if(args.length == 0) {
				// GetPlayerSettings(sender).mode = cmd;
				// sender.sendRawMessage("Chat mode is now '"+cmd+"'");
			}
			else {
				// String message = // ...;
				// chat(player, mode, message);
			}
		}
		catch( IllegalArgumentException e ) {
			// silently ignored
		}
		
		// When its not a chat mode
		// try the other commands:
		
		if(cmd.getName().equalsIgnoreCase("radiochannel")) {
			// ...
			return true;
		}

		return false;
	}
}
