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
	
	public static ChatMode GetModeByString( String cmd ) {
		cmd = cmd.toLowerCase();
		if(cmd.equals("ooc"))     return ChatMode.OOC;
		if(cmd.equals("say"))     return ChatMode.TALK;
		if(cmd.equals("whisper")) return ChatMode.WHISPER;
		if(cmd.equals("shout"))   return ChatMode.SHOUT;
		if(cmd.equals("me"))      return ChatMode.EMOTE;
		if(cmd.equals("radio"))   return ChatMode.RADIO;
		throw new Error("Invalid chat mode");
	}

	public static boolean CheckPermission( ChatMode mode, Player player ) {
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
				throw new Error("Invalid ChatMode enum.");
		}
	}

	public FormatCode( ChatColor code ) {
		return
			Character.toString((char)ChatColor.COLOR_CHAR) +
			Character.toString(
	}
	
	public boolean chat( Player player, ChatMode mode, String message ) {
		if(CheckPermission(mode, player) == false) {
			// TODO: Inform the player that they has insufficient permissions.
			player.sendMessage("You have insufficient permissions to use "+mode);
			return false;
		}

		// More code goes here.
		plugin.getServer().broadcastMessage(player.getName()+" "+mode+": "+message);
		
		switch(mode) {
			case OOC:

		}

		return true;
	}
	
	public String mergeArguments( String[] args ) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < args.length; ++i) {
			if(i != 0)
				builder.append(" ");
			builder.append(args[i]);
		}
		return builder.toString();
	}
	
	public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args ) {
		try {
			ChatMode mode = GetModeByString(cmd.getName());
			
			if(!(sender instanceof Player)) {
				sender.sendMessage("Only players can use this command!");
				return false;
			}
			Player player = (Player)sender;
			String message = mergeArguments(args);
			
			if(args.length == 0) {
				plugin.getPlayerSettings(player).setChatMode(mode);
				sender.sendMessage("Chat mode is now "+mode+" (FAKE!)");
			}
			else {
				return chat(player, mode, message);
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
