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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LandmarksPlugin extends JavaPlugin {
	private static final String LOG_PREFIX = "[Landmarks] ";
	private static final String CHAT_PREFIX = "[Landmarks] ";

	private String markersFile = "markers.json";
	private Logger log;
	private Configuration configuration;
	private HashMap<String, JSONObject> markers = new HashMap<String, JSONObject>();

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

		log.info(LOG_PREFIX + "Landmarks 0.1 enabled.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (!command.getName().equals("landmark"))
			return false;
		
		if (args.length < 1)
			return false;

		if (args[0].equals("set")) {
			if (args.length < 2)
				return false;

			String name = args[1];
			for (int i = 2; i < args.length; i++) {
				name += " " + args[i];
			}

			Location location = ((Player) sender).getLocation();

			JSONObject marker = new JSONObject();
			marker.put("name", (Object) name);
			marker.put("world", location.getWorld().getName());
			marker.put("x", location.getBlockX());
			marker.put("y", location.getBlockY());
			marker.put("z", location.getBlockZ());
			marker.put("owner", ((Player) sender).getName());
			marker.put("time", System.currentTimeMillis() / 1000);
			markers.put(name, marker);

			sender.sendMessage(CHAT_PREFIX + "Marker \"" + name + "\" set.");
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

			if (markers.containsKey(name)) {
				markers.remove(name);
				saveJSON();
				sender.sendMessage(CHAT_PREFIX + "Marker \"" + name
						+ "\" removed");
			} else {
				sender.sendMessage(CHAT_PREFIX + "Unknown marker \"" + name
						+ "\"");
			}
			return true;
		}
		return false;
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
