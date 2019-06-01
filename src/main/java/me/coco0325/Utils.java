package me.coco0325;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static void createItem(ItemStack i, String id){
        ItemStack item = i.clone();
        if(item.hasItemMeta()){
            ItemMeta meta = i.getItemMeta();
            if(meta.hasLore()){
                List<String> lore = meta.getLore();
                lore.set(0, lore.get(0)+"§c§o§r");
                meta.setLore(lore);
            }else{
                List<String> lore = new ArrayList<>();
                lore.add("§c§o§r");
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }else{
            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item.getType());
            List<String> lore = new ArrayList<>();
            lore.add("§c§o§r");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        Bukkit.getConsoleSender().sendMessage(String.valueOf(Skillitem.Data));
        try {
            Skillitem.Data.set("items."+id, item);
            Skillitem.Data.save(Skillitem.DataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isItemExist(String id){
        if(Skillitem.Data != null){
            if(Skillitem.Data.getConfigurationSection("items") != null){
                if(Skillitem.Data.getConfigurationSection("items").getKeys(false).contains(id)){
                    return true;
                }
            }
        }
        return false;
    }

    public static void deleteItem(String id){
        Skillitem.Data.set("items."+id, null);
    }
    
    public static  void checkEmptySlot(Player p){
        if(!p.hasPermission("skillitem.ignore")){
            Inventory inv = p.getInventory();
            for(Integer slot : Skillitem.check_slots){
                slot--;
                ItemStack de = Skillitem.default_item.clone();
                ItemMeta meta = de.getItemMeta();
                List<String> lore = meta.getLore();
                lore.set(0, "§c§"+slot+"§r"+meta.getLore().get(0)+"§c§o§r");
                meta.setLore(lore);
                de.setItemMeta(meta);
                ItemStack item = inv.getItem(slot);
               if(!isSkillitem(item)){
                   p.getInventory().setItem(slot, de);
               }
            }
        }
    }

    public static Boolean isSkillitem(ItemStack item){
        if(item != null){
            if(item.hasItemMeta()){
                if(item.getItemMeta().hasLore()){
                    if(item.getItemMeta().getLore().get(0).endsWith("§c§o§r")){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
