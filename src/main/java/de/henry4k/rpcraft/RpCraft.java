package de.henry4k.rpcraft;

import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.Player;

public class RpCraft extends JavaPlugin {
	protected Map<Player, PlayerSettings> playerSettings = null;
	protected ChatSystem chatSystem = null;

	@Override
	public void onEnable() {
		saveDefaultConfig(); // TODO: Ah? What kind of magic is this?
		
		playerSettings = new java.util.HashMap<Player, PlayerSettings>();
		chatSystem = new ChatSystem(this);

		getCommand("ooc")         .setExecutor(chatSystem);
		getCommand("say")         .setExecutor(chatSystem);
		getCommand("whisper")     .setExecutor(chatSystem);
		getCommand("shout")       .setExecutor(chatSystem);
		getCommand("me")          .setExecutor(chatSystem);
		getCommand("radio")       .setExecutor(chatSystem);
		getCommand("radiochannel").setExecutor(chatSystem);

		// PluginManager pm = getServer().getPluginManager();
		// pm.registerEvents(new RpCraftPlayerListener(this), this);

		getLogger().info("RpCraft loaded!");
	}

	@Override
	public void onDisable() {
		playerSettings = null;
		chatSystem = null;
		// I'm not quite sure whether reloading a plugin means
		// that just onDisable() and onEnable() gets called.
		// TODO: Check this and adjust the code if needed.

		getLogger().info("RpCraft unloaded!");
	}
	
	public PlayerSettings getPlayerSettings( Player player ) {
		if(playerSettings.containsKey(player)) {
			return playerSettings.get(player);
		}
		else {
			PlayerSettings settings = new PlayerSettings();
			playerSettings.put(player, settings);
			return settings;
		}
	}
}
