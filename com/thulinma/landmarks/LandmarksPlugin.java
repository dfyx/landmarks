package com.thulinma.landmarks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LandmarksPlugin extends JavaPlugin {
  private static final String LOG_PREFIX = "[Landmarks] ";
  private static final String CHAT_PREFIX = "[Landmarks] ";

  private String markersFile = "../dynmap/web/markers.json";
  private Logger log;
  private FileConfiguration configuration;
  private HashMap<String, JSONObject> markers = new HashMap<String, JSONObject>();

  public void onDisable() {
    saveJSON();

    log.info(LOG_PREFIX + "Landmarks disabled.");
  }

  public void onEnable() {
    log = Logger.getLogger("Minecraft");

    configuration = this.getConfig();
    markersFile = configuration.getString("markersfile", markersFile);

    loadJSON();

    PluginDescriptionFile pdfFile = getDescription();
    log.info(LOG_PREFIX + "Landmarks " + pdfFile.getVersion() + " enabled.");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    if (command.getName().equals("landmark")){
      if (!(sender instanceof Player)) {
        sender.sendMessage(CHAT_PREFIX + "This command cannot be used from the console");
        return true;
      }
      Player player = (Player) sender;
      if (args.length < 1){return false;}
      //build landmark name from args 0+
      String name = args[0];
      for (int i = 1; i < args.length; i++){name += " " + args[i];}
      if (isMarker(name)){
        if (!canEdit(name, sender)){
          sender.sendMessage(CHAT_PREFIX + "You are not allowed to modify \""+name+"\".");
          return true;
        }
        JSONObject marker = markers.get(name);
        setProperty(marker, "world", player.getLocation().getWorld().getName());
        setProperty(marker, "x", player.getLocation().getBlockX());
        setProperty(marker, "y", player.getLocation().getBlockY());
        setProperty(marker, "z", player.getLocation().getBlockZ());
        saveJSON();
        player.sendMessage(CHAT_PREFIX + "Landmark location for \""+name+"\" updated.");
      }else{
        if (!canCreate(sender)){
          sender.sendMessage(CHAT_PREFIX + "You are not allowed to add landmarks.");
          return true;
        }
        JSONObject marker = new JSONObject();
        setProperty(marker, "name", name);
        setProperty(marker, "world", player.getLocation().getWorld().getName());
        setProperty(marker, "x", player.getLocation().getBlockX());
        setProperty(marker, "y", player.getLocation().getBlockY());
        setProperty(marker, "z", player.getLocation().getBlockZ());
        setProperty(marker, "owner", player.getName());
        setProperty(marker, "type", "Default");
        markers.put(name, marker);
        saveJSON();
        player.sendMessage(CHAT_PREFIX + "New default-type landmark \""+name+"\" created.");
      }
      return true;
    }

    if (command.getName().equals("landmarktyped")){
      if (!(sender instanceof Player)) {
        sender.sendMessage(CHAT_PREFIX + "This command cannot be used from the console");
        return true;
      }
      Player player = (Player) sender;
      if (args.length < 2){return false;}
      String type = args[0];
      //build landmark name from args 1+
      String name = args[1];
      for (int i = 2; i < args.length; i++){name += " " + args[i];}
      if (isMarker(name)){
        if (!canEdit(name, sender)){
          sender.sendMessage(CHAT_PREFIX + "You are not allowed to modify \""+name+"\".");
          return true;
        }
        JSONObject marker = markers.get(name);
        setProperty(marker, "world", player.getLocation().getWorld().getName());
        setProperty(marker, "x", player.getLocation().getBlockX());
        setProperty(marker, "y", player.getLocation().getBlockY());
        setProperty(marker, "z", player.getLocation().getBlockZ());
        setProperty(marker, "type", type);
        saveJSON();
        player.sendMessage(CHAT_PREFIX + "Landmark location and type for \""+name+"\" updated.");
      }else{
        if (!canCreate(sender)){
          sender.sendMessage(CHAT_PREFIX + "You are not allowed to add landmarks.");
          return true;
        }
        JSONObject marker = new JSONObject();
        setProperty(marker, "name", name);
        setProperty(marker, "world", player.getLocation().getWorld().getName());
        setProperty(marker, "x", player.getLocation().getBlockX());
        setProperty(marker, "y", player.getLocation().getBlockY());
        setProperty(marker, "z", player.getLocation().getBlockZ());
        setProperty(marker, "owner", player.getName());
        setProperty(marker, "type", type);
        markers.put(name, marker);
        saveJSON();
        player.sendMessage(CHAT_PREFIX + "New "+type+"-type landmark \""+name+"\" created.");
      }
      return true;
    }
    if (command.getName().equals("landmarksettype")){
      if (args.length < 2){return false;}
      String type = args[0];
      //build landmark name from args 1+
      String name = args[1];
      for (int i = 2; i < args.length; i++){name += " " + args[i];}
      if (isMarker(name)){
        if (!canEdit(name, sender)){
          sender.sendMessage(CHAT_PREFIX + "You are not allowed to modify \""+name+"\".");
          return true;
        }
        JSONObject marker = markers.get(name);
        setProperty(marker, "type", type);
        saveJSON();
        sender.sendMessage(CHAT_PREFIX + "Landmark \""+name+"\" type updated to \""+type+"\".");
      }else{
        sender.sendMessage(CHAT_PREFIX + "No such landmark: "+name);
        return true;
      }
      return true;
    }
    if (command.getName().equals("landmarklist")){
      for (Object obj : markers.values()) {
        JSONObject marker = (JSONObject) obj;
        sender.sendMessage(CHAT_PREFIX + "["+marker.get("type").toString()+"] "+marker.get("name").toString()+" at ("+marker.get("x").toString()+", "+marker.get("y").toString()+", "+marker.get("z").toString()+")");
      }
      return true;
    }
    if (command.getName().equals("landmarkdelete")){
      if (args.length < 1){return false;}
      //build landmark name from args 0+
      String name = args[0];
      for (int i = 1; i < args.length; i++){name += " " + args[i];}
      if (isMarker(name)){
        if (!canEdit(name, sender)){
          sender.sendMessage(CHAT_PREFIX + "You are not allowed to modify \""+name+"\".");
          return true;
        }
        markers.remove(name);
        saveJSON();
        sender.sendMessage(CHAT_PREFIX + "Landmark \""+name+"\" deleted.");
      }else{
        sender.sendMessage(CHAT_PREFIX + "No such landmark: "+name);
        return true;
      }
      return true;
    }
    if (command.getName().equals("landmarklook")){
      if (!(sender instanceof Player)) {
        sender.sendMessage(CHAT_PREFIX + "This command cannot be used from the console");
        return true;
      }
      Player player = (Player) sender;
      if (args.length < 1){return false;}
      //build landmark name from args 0+
      String name = args[0];
      for (int i = 1; i < args.length; i++){name += " " + args[i];}
      if (isMarker(name)){
        if (!canLook(name, sender)){
          sender.sendMessage(CHAT_PREFIX + "You are not allowed to do that.");
          return true;
        }
        JSONObject marker = markers.get(name);
        player.teleport(lookAt(player.getLocation(), Double.parseDouble(marker.get("x").toString()), Double.parseDouble(marker.get("y").toString()), Double.parseDouble(marker.get("z").toString())));
      }else{
        sender.sendMessage(CHAT_PREFIX + "No such landmark: "+name);
        return true;
      }
      return true;
    }
    if (command.getName().equals("landmarkwarp")){
      if (!(sender instanceof Player)) {
        sender.sendMessage(CHAT_PREFIX + "This command cannot be used from the console");
        return true;
      }
      Player player = (Player) sender;
      if (args.length < 1){return false;}
      //build landmark name from args 0+
      String name = args[0];
      for (int i = 1; i < args.length; i++){name += " " + args[i];}
      if (isMarker(name)){
        if (!canWarp(name, sender)){
          sender.sendMessage(CHAT_PREFIX + "You are not allowed to do that.");
          return true;
        }
        JSONObject marker = markers.get(name);
        player.teleport(new Location(Bukkit.getWorld(marker.get("world").toString()), Double.parseDouble(marker.get("x").toString()), Double.parseDouble(marker.get("y").toString()), Double.parseDouble(marker.get("z").toString())));
      }else{
        sender.sendMessage(CHAT_PREFIX + "No such landmark: "+name);
        return true;
      }
      return true;
    }




    return false;
  }


  private boolean isMarker(String name) {
    return (markers.containsKey(name));
  }

  private boolean canEdit(String name, CommandSender sender) {
    if (!markers.containsKey(name)){return false;}
    if (sender.hasPermission("landmarks.modify.all")){return true;}
    if (sender.hasPermission("landmarks.modify."+name.toLowerCase().replace(" ", "_"))){return true;}
    if (markers.get(name).get("owner").equals(((Player)sender).getName()) && sender.hasPermission("landmarks.modify.own")){return true;}
    return false;
  }

  private boolean canLook(String name, CommandSender sender) {
    if (!markers.containsKey(name)){return false;}
    if (sender.hasPermission("landmarks.look.all")){return true;}
    if (sender.hasPermission("landmarks.look."+name.toLowerCase().replace(" ", "_"))){return true;}
    if (markers.get(name).get("owner").equals(((Player)sender).getName()) && sender.hasPermission("landmarks.look.own")){return true;}
    return false;
  }

  private boolean canWarp(String name, CommandSender sender) {
    if (!markers.containsKey(name)){return false;}
    if (sender.hasPermission("landmarks.warp.all")){return true;}
    if (sender.hasPermission("landmarks.warp."+name.toLowerCase().replace(" ", "_"))){return true;}
    if (markers.get(name).get("owner").equals(((Player)sender).getName()) && sender.hasPermission("landmarks.warp.own")){return true;}
    return false;
  }

  private boolean canCreate(CommandSender sender) {
    if (sender.hasPermission("landmarks.add")){return true;}
    return false;
  }

  @SuppressWarnings("unchecked") //prevent warning for j.put()
  private void setProperty(JSONObject j, String property, Object value){
    j.put(property, value);
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
        if (!marker.containsKey("type")){
          setProperty(marker, "type", "Default");
        }
        if (marker.containsKey("name")){
          markers.put((String) marker.get("name"), marker);
        }
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

  @SuppressWarnings("unchecked") //prevent eclipse whining about data.add()

  private void saveJSON() {
    //put data in an array
    JSONArray data = new JSONArray();
    for (JSONObject marker : markers.values()) {
      data.add(marker);
    }

    //attempt to write array to file
    try {
      File file = new File(markersFile);
      if (!file.isAbsolute()) {
        getDataFolder().mkdirs();
        file = new File(getDataFolder(), markersFile);
      }
      File parent = file.getParentFile();
      if(!parent.exists() && !parent.mkdirs()){
          throw new IllegalStateException("Couldn't create dir: " + parent);
      }
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      writer.write(data.toString());
      writer.close();

      log.info(LOG_PREFIX + "Saved " + file.getAbsolutePath());
    } catch (IOException e) {
      log.severe(LOG_PREFIX + "Error writing markers file.");
    }
  }

  //lookAt function by bergerkiller
  public static Location lookAt(Location loc, Location lookat) {
    //Clone the loc to prevent applied changes to the input loc
    loc = loc.clone();

    // Values of change in distance (make it relative)
    double dx = lookat.getX() - loc.getX();
    double dy = lookat.getY() - loc.getY();
    double dz = lookat.getZ() - loc.getZ();

    // Set yaw
    if (dx != 0) {
      // Set yaw start value based on dx
      if (dx < 0) {
        loc.setYaw((float) (1.5 * Math.PI));
      } else {
        loc.setYaw((float) (0.5 * Math.PI));
      }
      loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
    } else if (dz < 0) {
      loc.setYaw((float) Math.PI);
    }

    // Get the distance from dx/dz
    double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

    // Set pitch
    loc.setPitch((float) -Math.atan(dy / dxz));

    // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
    loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
    loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

    return loc;
  }

  //lookAt function by bergerkiller
  public static Location lookAt(Location loc, double dx, double dy, double dz) {
    //Clone the loc to prevent applied changes to the input loc
    loc = loc.clone();

    // Values of change in distance (make it relative)
    dx -= loc.getX();
    dy -= loc.getY();
    dz -= loc.getZ();

    // Set yaw
    if (dx != 0) {
      // Set yaw start value based on dx
      if (dx < 0) {
        loc.setYaw((float) (1.5 * Math.PI));
      } else {
        loc.setYaw((float) (0.5 * Math.PI));
      }
      loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
    } else if (dz < 0) {
      loc.setYaw((float) Math.PI);
    }

    // Get the distance from dx/dz
    double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

    // Set pitch
    loc.setPitch((float) -Math.atan(dy / dxz));

    // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
    loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
    loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

    return loc;
  }

}
