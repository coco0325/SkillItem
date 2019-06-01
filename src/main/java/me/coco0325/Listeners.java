package me.coco0325;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;


import java.util.HashMap;
import java.util.Iterator;

public class Listeners implements Listener {

    public static HashMap<String, HashMap<Integer, ItemStack>> tempitems =
            new HashMap<String, HashMap<Integer, ItemStack>>() ;

    @EventHandler
    public void onPlayerClickItem(InventoryClickEvent e){
        if(e.getInventory().getType().equals(InventoryType.CRAFTING)){
            if(Skillitem.check_slots.contains(e.getRawSlot()-35)){
                if(!e.getWhoClicked().hasPermission("skillitem.ignore")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Utils.checkEmptySlot(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        if(!e.getPlayer().hasPermission("skillitem.ignore")){
            ItemStack item = e.getItemDrop().getItemStack();
            if(Utils.isSkillitem(item)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent e){
        String uuid = e.getPlayer().getUniqueId().toString();
        try{
            if(tempitems.get(uuid).keySet().size() != 0){
                Iterator<Integer> it = tempitems.get(uuid).keySet().iterator();
                while(it.hasNext()){
                    Integer slot = it.next();
                    if(slot <= 8 && slot >= 0){
                        e.getPlayer().getInventory().setItem(slot, tempitems.get(uuid).get(slot));
                    }
                }
                tempitems.remove(uuid);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        Utils.checkEmptySlot(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(!e.getKeepInventory()){
            HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
            for(ItemStack item : e.getDrops()){
                if(Utils.isSkillitem(item)){
                                Integer slot = Character.getNumericValue(item.getItemMeta().getLore().get(0).charAt(3));
                                items.put(slot, item.clone());
                                item.setType(Material.AIR);
                }
            }
            tempitems.put(e.getEntity().getUniqueId().toString(), items);
        }
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e){
        if(Utils.isSkillitem(e.getOffHandItem())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent e){
        if(Utils.isSkillitem(e.getPlayer().getInventory().getItemInMainHand())){
            e.setCancelled(true);
        }
    }
}
