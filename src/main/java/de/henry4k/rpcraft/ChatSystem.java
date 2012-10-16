package de.henry4k.rpcraft;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.ChatColor;
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

	public static String FormatCode( ChatColor code ) {
		return
			Character.toString((char)ChatColor.COLOR_CHAR) +
			Character.toString(code.getChar());
	}

	/**
	 * 0.0 is black and 1.0 is white; linear transition
	 */
	public static ChatColor GetGreyscaleColor( float factor ) {
		if(factor < 0.33f)      return ChatColor.DARK_GRAY;
		else if(factor < 0.66f) return ChatColor.GRAY;
		else                   return ChatColor.WHITE;
	}

	public static String DestroyText( String text, float destructionFactor ) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < text.length(); ++i) {
			// TODO
			if(Math.random() < destructionFactor) {
				builder.append( text.charAt(i) );
			}
			else {
				builder.append('.');
			}
		}
		return builder.toString();
	}

	/* TODO
	 * Die Funktion muss wissen,
	 * wie der "prefix" String aussieht bzw wie er verändert wird.
	 * - Lou shouts: ...
	 * - Someone shouts: ...
	 * - The solution beginns to bubble.. (hier gibts keine Nachricht..)
	 * - usw.
	 */

	// TODO: "prefix" looks misfitting
	public void localSound( Location originLocation, String prefix, String text, float maxDistance ) {
		World originWorld = originLocation.getWorld();
		Player[] players = plugin.getServer().getOnlinePlayers();
		Player curPlayer = null;
		float  distance = 0.0f;
		float  visibillity = 0.0f;
		String modifiedText = "";

		prefix = ChatColor.stripColor(prefix);
		text   = ChatColor.stripColor(text);
		
		for(int i = 0; i < players.length; ++i) {
			curPlayer = players[i];
			
			if(curPlayer.getWorld() != originWorld) {
				continue;
			}
			
			distance = (float)curPlayer.getLocation().distance(originLocation);
			
			if(distance >= maxDistance) {
				continue;
			}
			
			visibillity = 1.0f - distance/maxDistance;
			modifiedText = text;
			
			if(visibillity < 0.5f) {
				modifiedText = DestroyText(modifiedText, visibillity*2.0f);
			}
			
			curPlayer.sendRawMessage(
				FormatCode(GetGreyscaleColor(visibillity))+
				prefix+
				modifiedText
			);
		}
	}

	public String genString( char ch, int amount ) {
		StringBuilder builder = new StringBuilder();
		for(; amount > 0; --amount) {
			builder.append(ch);
		}
		return builder.toString();
	}

	public boolean chat( Player player, ChatMode mode, String message ) {
		if(CheckPermission(mode, player) == false) {
			// TODO: Inform the player that they has insufficient permissions.
			player.sendMessage("You have insufficient permissions to use "+mode);
			return false;
		}

		// More code goes here.
		// plugin.getServer().broadcastMessage(player.getName()+" "+mode+": "+message);
		
		switch(mode) {
			case OOC:
				plugin.getServer().broadcastMessage(
						FormatCode(ChatColor.YELLOW)+
						"[OOC] "+
						FormatCode(ChatColor.RESET)+
						player.getPlayerListName()+
						": "+
						message
				);
				break;

			case TALK:
				char lastChar = message.charAt( message.length()-1 );
				String verb = null;
				switch(lastChar) {
					case '?':
						verb = "asks";
						break;

					case '!':
						verb = "exclaims";
						break;

					default:
						verb = "says";
				}

				localSound(player.getLocation(), player.getPlayerListName()+" "+verb+": ", message, 30);
				break;

			case WHISPER:
				localSound(player.getLocation(), player.getPlayerListName()+" whispers: ", message, 10);
				break;

			case SHOUT:
				localSound(player.getLocation(), player.getPlayerListName()+" shouts: ", message.toUpperCase()+genString('!', 1+(int)Math.random()*2), 70);
				break;

			case EMOTE:
				localSound(player.getLocation(), player.getPlayerListName()+" "+message, "", 30);
				break;

			default:
				throw new Error("Oh Shit!");
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
