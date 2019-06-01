package me.coco0325;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Skillitem extends JavaPlugin implements CommandExecutor {

    public static File DataFile, TempFile;
    public static YamlConfiguration Data, Temp;

    public static String prefix, item_not_exist, item_exists, command_help, wrong_slot, save_success;
    public static ArrayList<Integer> check_slots;
    public static ItemStack default_item = new ItemStack(Material.AIR, 1);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DataFile = new File(getDataFolder(), "itemdata.yml");
        Data = new YamlConfiguration();
        TempFile = new File(getDataFolder(), "temp.yml");
        Temp = new YamlConfiguration();
        loadFile(DataFile, Data,  "items");
        loadFile(TempFile, Temp,  "data");
        loadTemp();
        getServer().getPluginManager().registerEvents(new Listeners(),this);
        prefix = getConfig().getString("messages.plugin_prefix").replaceAll("&", "§");
        item_not_exist = prefix+getConfig().getString("messages.item_does_not_exist").replaceAll("&", "§");
        item_exists = prefix+getConfig().getString("messages.item_exists").replaceAll("&", "§");
        command_help = prefix+getConfig().getString("messages.help").replaceAll("&", "§");
        wrong_slot = prefix+getConfig().getString("messages.wrong_slot").replaceAll("&", "§");
        save_success = prefix+getConfig().getString("messages.save_success").replaceAll("&", "§");
        check_slots = (ArrayList<Integer>)getConfig().getIntegerList("settings.slots");
        default_item.setType(Material.getMaterial(getConfig().getString("settings.default_item.ID")));
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.getMaterial(getConfig().getString("settings.default_item.ID")));
        meta.setDisplayName(getConfig().getString("settings.default_item.name").replaceAll("&", "§"));
        ArrayList<String> lore = new ArrayList<String>();
        lore = (ArrayList<String>)getConfig().getStringList("settings.default_item.lore");
        lore.replaceAll(e -> e.replaceAll("&", "§"));
        lore.set(1, lore.get(1)+"§c§o§r");
        meta.setLore(lore);
        default_item.setItemMeta(meta);
    }

    @Override
    public void onDisable() {
        try{
            for(String uuid : Listeners.tempitems.keySet()){
                Temp.set("data."+uuid, Listeners.tempitems.get(uuid));
            }
            if(Temp != null){
                Temp.save(TempFile);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadTemp() {
        try{
            for(String uuid : Temp.getConfigurationSection("data").getKeys(false)){
                HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
                for(String slot : Temp.getConfigurationSection("data."+uuid).getKeys(false)){
                    items.put(Integer.parseInt(slot), Temp.getItemStack("data."+uuid+"."+slot));
                }
                Listeners.tempitems.put(uuid, items);
            }
        }catch (Exception e){
        }
    }

    private void loadFile(File File, YamlConfiguration config, String main){
        if (!File.exists()) {
            File.getParentFile().mkdirs();
            try {
                config.save(File);
                config.load(File);
                config.createSection(main);
                config.save(File);
            }catch (IOException | InvalidConfigurationException e){
                e.printStackTrace();
            }
        }else{
            try {
                config.load(File);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("skillitem")){
            if(sender instanceof Player){
                Player player = (Player)sender;
                if(args.length == 0) {
                    sender.sendMessage(command_help);
                    return false;
                } else if(args[0].equalsIgnoreCase("reload")){
                    if(!sender.hasPermission("skillitem.reload")) return false;
                    loadFile(DataFile, Data, "items");
                    reloadConfig();;
                } else if(args[0].equalsIgnoreCase("get")){
                    if(args.length < 3){
                        sender.sendMessage(command_help);
                        return false;
                    }
                    Integer slot = Integer.valueOf(args[2])-1;
                    if(!check_slots.contains(Integer.valueOf(args[2]))){
                        sender.sendMessage(wrong_slot);
                        return false;
                    }
                    String id = args[1];
                    if(!sender.hasPermission("skillitem.get."+id)) return false;
                    if(Utils.isItemExist(id)){
                        ItemStack item = ((ItemStack)Skillitem.Data.get("items."+id)).clone();
                        if(item.hasItemMeta() && item.getItemMeta().hasLore()){
                            ItemMeta meta = ((ItemStack)Skillitem.Data.get("items."+id)).getItemMeta();
                            List<String> lore = meta.getLore();
                            lore.set(0, "§c§"+slot+"§r"+lore.get(0));
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        }else{
                            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item.getType());
                            List<String> lore = meta.getLore();
                            lore.set(0, "§c§"+slot+"§r"+lore.get(0));
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        }
                        player.getInventory().setItem(slot, item);
                    }else{
                        sender.sendMessage(item_not_exist);
                    }
                }else if(args[0].equalsIgnoreCase("save")){
                    if(!sender.hasPermission("skillitem.save")) return false;
                    if(!Utils.isItemExist(args[1])){
                        Utils.createItem(player.getInventory().getItemInMainHand(), args[1]);
                        sender.sendMessage(save_success);
                    }else{
                        sender.sendMessage(item_exists);
                    }
                }else if(args[0].equalsIgnoreCase("delete")){
                    if(!sender.hasPermission("skillitem.delete")) return false;
                    if(Utils.isItemExist(args[1])){
                        Utils.deleteItem(args[1]);
                    }else{
                        sender.sendMessage(item_not_exist);
                    }
                }
            }
        }
        return false;
    }
}
