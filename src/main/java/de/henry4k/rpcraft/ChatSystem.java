package de.henry4k.rpcraft;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ChatSystem implements CommandExecutor, Listener {
	private RpCraft plugin = null;

	public ChatSystem( RpCraft plugin ) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public static ChatMode GetModeByString( String cmd ) {
		cmd = cmd.toLowerCase();
		if(cmd.equals("ooc"))     return ChatMode.OOC;
		if(cmd.equals("say"))     return ChatMode.TALK;
		if(cmd.equals("whisper")) return ChatMode.WHISPER;
		if(cmd.equals("shout"))   return ChatMode.SHOUT;
		if(cmd.equals("me"))      return ChatMode.EMOTE;
		return null;
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

	public static String DestroyText( String text, float destructionFactor, char destructionChar ) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < text.length(); ++i) {
			// TODO
			if(Math.random() < destructionFactor) {
				builder.append( text.charAt(i) );
			}
			else {
				builder.append(destructionChar);
			}
		}
		return builder.toString();
	}
	
	public void localChat( Player player, String prefix, String text, float maxDistance ) {
		prefix = player.getPlayerListName()+prefix;
		
		localSound(player.getLocation(), prefix, text, maxDistance);
		
		if(player.hasPermission("rpcraft.radio") && plugin.getPlayerSettings(player).getRadioMicrophone() ) {
			radioChat(player, prefix, text);
		}
	}

	public String destroyMessage( String prefix, String message, float destruction, char destructionChar ) {
		if(destruction > 0.5) {
			return prefix+DestroyText(message, destruction, destructionChar);
		}
		else {
			return DestroyText(message, destruction, destructionChar);
		}
	}
	
	public void localSound( Location originLocation, String prefix, String text, float maxDistance ) {
		World originWorld = originLocation.getWorld();
		Player[] players = plugin.getServer().getOnlinePlayers();
		Player curPlayer = null;
		float  distance = 0.0f;
		float  visibillity = 0.0f;

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
			
			curPlayer.sendRawMessage(
				FormatCode(GetGreyscaleColor(visibillity))+
				destroyMessage(prefix, text, (visibillity >= 0.5f)?(1.0f):(visibillity*2.0f), '.')
			);
		}
	}

	public void radioChat( Player originPlayer, String prefix, String message ) {
		int channel = plugin.getPlayerSettings(originPlayer).getRadioChannel();
		Player[] players = plugin.getServer().getOnlinePlayers();
		Player curPlayer = null;
		int curChannel = 0;
		float destruction = 0.0f;
		
		for(int i = 0; i < players.length; ++i) {
			curPlayer = players[i];

			if(curPlayer == originPlayer) {
				continue;
			}

			curChannel = plugin.getPlayerSettings(curPlayer).getRadioChannel();
			
			if(curChannel == 0) {
				continue;
			}

			destruction = 1.0f - ((float)Math.abs(channel - curChannel) / 10.0f);

			if(curPlayer.getWorld() != originPlayer.getWorld()) {
				destruction += 0.5f;
				if(destruction > 1.0f) {
					destruction = 1.0f;
				}
			}
			
			curPlayer.sendRawMessage(
				FormatCode(ChatColor.GRAY)+
				"[RADIO] "+
				FormatCode(ChatColor.RESET)+
				destroyMessage(prefix, message, destruction, '/')
			);
		}
	}

	public boolean chat( Player player, ChatMode mode, String message ) {
		if(CheckPermission(mode, player) == false) {
			// TODO: Inform the player that they has insufficient permissions.
			player.sendMessage("You have insufficient permissions to use "+mode);
			return false;
		}
		
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

				localChat(player, " "+verb+": ", message, 30);
				break;

			case WHISPER:
				localChat(player, " whispers: ", message, 10);
				break;

			case SHOUT:
				localChat(player, " shouts: ", message.toUpperCase(), 70);
				break;

			case EMOTE:
				localChat(player, " "+message, "", 30);
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

	static Matcher RadioChannelMatcher = Pattern.compile("([0-9]{1,3})\\.([0-9])").matcher("");

	/**
	 * Parses a string of the form XXX.Y
	 * Returns -1 on error or XXXY on success.
	 */
	public static int RadioChannelByString( String str ) {
		RadioChannelMatcher.reset(str);
		if(RadioChannelMatcher.matches()) {
			return
				Integer.valueOf(RadioChannelMatcher.group(1))*10 +
				Integer.valueOf(RadioChannelMatcher.group(2));
		}
		else {
			return -1;
		}
	}

	public static String RadioChannelToString( int ch ) {
		return ""+(ch/10)+"."+(ch - (ch/10*10));
	}
	
	public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args ) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("Only players can use this command!");
			return false;
		}
		Player player = (Player)sender;
		
		ChatMode mode = GetModeByString(cmd.getName());
		if(mode != null) {
			String message = mergeArguments(args);
			
			if(args.length == 0) {
				plugin.getPlayerSettings(player).setChatMode(mode);
			}
			else {
				return chat(player, mode, message);
			}
		}
		
		// When its not a chat mode
		// try the other commands:

		if(cmd.getName().equalsIgnoreCase("radio")) {
			if(args.length == 0) {
				if(plugin.getPlayerSettings(player).getRadioMicrophone()) {
					sender.sendMessage("Your microphone is enabled.");
				}
				else {
					sender.sendMessage("Your microphone is disabled.");
				}
			}
			else {
				if(args[0].equals("0")) {
					plugin.getPlayerSettings(player).setRadioMicrophone(false);
					sender.sendMessage("You switch your microphone off.");
				}
				else {
					plugin.getPlayerSettings(player).setRadioMicrophone(true);
					sender.sendMessage("You switch your microphone on.");
				}
			}
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("radiochannel")) {
			if(args.length == 0) {
				sender.sendMessage(
					"You are currently speaking on channel "+
					RadioChannelToString( plugin.getPlayerSettings(player).getRadioChannel() )
				);
			}
			else {
				int ch = RadioChannelByString(args[0]);

				if(ch != -1) {
					sender.sendMessage("You are speaking now on channel "+RadioChannelToString(ch));
					plugin.getPlayerSettings(player).setRadioChannel(ch);
				}
				else {
					sender.sendMessage("Radio channels have the form XXX.X");
				}
			}
			return true;
		}

		return false;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerChat( PlayerChatEvent event ) {
		Player player  = event.getPlayer();
		String message = event.getMessage();
		ChatMode mode  = plugin.getPlayerSettings(player).getChatMode();
		
		if(chat(player, mode, message)) {
			event.setCancelled(true);
		}
	}
}
