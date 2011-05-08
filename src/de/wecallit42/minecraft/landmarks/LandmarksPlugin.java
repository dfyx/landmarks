package de.wecallit42.minecraft.landmarks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

public class LandmarksPlugin extends JavaPlugin {
	private static final String LOG_PREFIX = "[Landmarks] ";
	private static final String CHAT_PREFIX = "[Landmarks] ";

	private String markersFile = "markers.json";
	private Logger log;
	private Configuration configuration;
	private HashMap<String, JSONObject> markers = new HashMap<String, JSONObject>();
	
	private PermissionHandler permissions;

	public void onDisable() {
		saveJSON();

		log.info(LOG_PREFIX + "Landmarks disabled.");
	}

	public void onEnable() {
		log = Logger.getLogger("Minecraft");

		configuration = new Configuration(new File(this.getDataFolder(),
				"config.yml"));
		configuration.load();
		markersFile = configuration.getString("markersfile", markersFile);

		loadJSON();
		setupPermissions();

		PluginDescriptionFile pdfFile = getDescription();
		log.info(LOG_PREFIX + "Landmarks " + pdfFile.getVersion() + " enabled.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (!command.getName().equals("landmark"))
			return false;
		
		Player player = (Player) sender;
		
		if (args.length < 1)
			return false;

		if (args[0].equals("add")) {
			if (args.length < 2)
				return false;

			String name = args[1];
			for (int i = 2; i < args.length; i++) {
				name += " " + args[i];
			}

			addMarker(name, player);
			saveJSON();
			
			return true;
		} else if (args[0].equals("modify")) {
			if (args.length < 2)
				return false;

			String name = args[1];
			for (int i = 2; i < args.length; i++) {
				name += " " + args[i];
			}

			modifyMarker(name, player);
			saveJSON();
			
			return true;
		} else if (args[0].equals("set")) {
			if (args.length < 2)
				return false;

			String name = args[1];
			for (int i = 2; i < args.length; i++) {
				name += " " + args[i];
			}

			if(markers.containsKey(name)) {
				modifyMarker(name, player);
			} else {
				addMarker(name, player);
			}
			saveJSON();
			
			return true;
		} else if (args[0].equals("del") || args[0].equals("delete")
				|| args[0].equals("remove")) {
			if (args.length < 2)
				return false;

			String name = args[1];
			for (int i = 2; i < args.length; i++) {
				name += " " + args[i];
			}

			removeMarker(name, player);
			saveJSON();
			
			return true;
		}
		return false;
	}
	
	private void addMarker(String name, Player player) {
		if(markers.containsKey(name)) {
			player.sendMessage(CHAT_PREFIX + "There already is a marker called \"" + name + "\".");
		} else if (hasPermission(player, "landmarks.add")) {
			Location location = player.getLocation();

			JSONObject marker = new JSONObject();
			marker.put("name", name);
			marker.put("world", location.getWorld().getName());
			marker.put("x", location.getBlockX());
			marker.put("y", location.getBlockY());
			marker.put("z", location.getBlockZ());
			marker.put("owner", player.getName());
			marker.put("time", System.currentTimeMillis() / 1000);
			markers.put(name, marker);
			
			player.sendMessage(CHAT_PREFIX + "Marker \"" + name + "\" added.");
		} else {
			player.sendMessage(CHAT_PREFIX + "You are not allowed to add new markers.");
		}
	}
	
	private void modifyMarker(String name, Player player) {
		if (!markers.containsKey(name)) {
			player.sendMessage(CHAT_PREFIX + "There is no marker called \""
					+ name + "\".");
		} else if (hasPermission(player, "landmarks.modify.all")
				|| (markers.get(name).get("owner").equals(player.getName())
					&& hasPermission(player, "landmarks.modify.own"))) {
			Location location = player.getLocation();

			JSONObject marker = markers.get(name);
			marker.put("world", location.getWorld().getName());
			marker.put("x", location.getBlockX());
			marker.put("y", location.getBlockY());
			marker.put("z", location.getBlockZ());
			
			player.sendMessage(CHAT_PREFIX + "Marker \"" + name
					+ "\" modified.");
		} else {
			player.sendMessage(CHAT_PREFIX
					+ "You are not allowed to modify marker \"" + name + "\".");
		}
	}
	
	private void removeMarker(String name, Player player) {
		if (!markers.containsKey(name)) {
			player.sendMessage(CHAT_PREFIX + "There is no marker called \""
					+ name + "\".");
		} else if (hasPermission(player, "landmarks.remove.all")
				|| (markers.get(name).get("owner").equals(player.getName())
					&& hasPermission(player, "landmarks.remove.own"))) {
			markers.remove(name);
			player.sendMessage(CHAT_PREFIX + "Marker \"" + name
					+ "\" removed");
		} else {
			player.sendMessage(CHAT_PREFIX
					+ "You are not allowed to remove marker \"" + name + "\".");
		}
	}
	
	private void setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin(
				"Permissions");

		if (permissions == null) {
			if (test != null) {
				permissions = ((Permissions) test).getHandler();
			} else {
				log.info(LOG_PREFIX + "Permission system not detected, no checks");
			}
		}
	}
	
	private boolean hasPermission(Player player, String node) {
		if(permissions == null) {
			return true;
		} else {
			return permissions.has(player, node);
		}
	}

	private void loadJSON() {
		JSONParser parser = new JSONParser();
		try {
			File file = new File(markersFile);
			if (!file.isAbsolute()) {
				file = new File(getDataFolder(), markersFile);
			}

			log.info(LOG_PREFIX + "Loading " + file.getAbsolutePath());

			BufferedReader reader = new BufferedReader(new FileReader(file));
			JSONArray data = (JSONArray) parser.parse(reader);
			for (Object obj : data) {
				JSONObject marker = (JSONObject) obj;
				markers.put((String) marker.get("name"), marker);
			}
			reader.close();

			log.info(LOG_PREFIX + "Successfully loaded " + markers.size()
					+ " markers.");
		} catch (FileNotFoundException ex) {
			// Assume empty markers file
		} catch (ParseException ex) {
			log.severe(LOG_PREFIX + "The markers file has errors.");
		} catch (IOException ex) {
			log.severe(LOG_PREFIX + "Error reading markers file.");
		}
	}

	private void saveJSON() {
		File file = new File(markersFile);
		if (!file.isAbsolute()) {
			file = new File(getDataFolder(), markersFile);
		}

		JSONArray data = new JSONArray();
		for (JSONObject marker : markers.values()) {
			data.add(marker);
		}

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(data.toString());
			writer.close();

			log.info(LOG_PREFIX + "Saved " + file.getAbsolutePath());
		} catch (IOException e) {
			log.severe(LOG_PREFIX + "Error writing markers file.");
		}
	}
}
