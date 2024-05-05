package me.oliverhesse.damageoverhaul;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.joml.Vector3d;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class DamageListener implements Listener {

    private final Plugin plugin;
    private Random random;
    private final double scaleFactor = 1000;
    public DamageListener(Plugin plugin){
        this.plugin = plugin;
        this.random = new Random();
    }
    public Double calculateDamage(DamageTypeEnum damageType,Entity target,double baseDamage,Player player){
        //TODO fix do separate almighty damage and include armour strip/ignore
        plugin.getLogger().info( "Trying to deal " +damageType.asString()+" damage");
        PersistentDataContainer enemyPDC  = target.getPersistentDataContainer();
        Double defence = 100d;
        Double resistance= 0d;
        Double vulnerability= 0d;
        if(enemyPDC.has(new NamespacedKey(plugin,"entityModified"),PersistentDataType.BOOLEAN)){
            //entity is modified use normal formula
            //get new defence

            defence = enemyPDC.get(new NamespacedKey(plugin,"defence"),PersistentDataType.DOUBLE);
            if(defence == null){
                plugin.getLogger().info( "tried to deal modified damage but defence was not present");
                return null;
            }

            resistance = enemyPDC.get(new NamespacedKey(plugin,damageType.asString()+"Resistance"),PersistentDataType.DOUBLE);
            if(resistance == null){
                plugin.getLogger().info( "tried to deal modified damage but resistance was not present");
                return null;
            }

            vulnerability = enemyPDC.get(new NamespacedKey(plugin,damageType.asString()+"Vulnerability"),PersistentDataType.DOUBLE);
            if(vulnerability == null){
                plugin.getLogger().info( "tried to deal modified damage but vulnerability was not present");
                return null;
            }


        }

        Double damage = (baseDamage/(1+(defence/scaleFactor)))*(1-resistance/100)*(1+vulnerability/100);
        if(damageType == DamageTypeEnum.SLASH){
            //they dealt slash damage apply bleed
            //currently bleed is a bit broken. the last applied bleed will always override the currently applied bleed
            Integer taskId =  enemyPDC.get(new NamespacedKey(plugin,"hasBleed"),PersistentDataType.INTEGER);
            if(taskId != null) {
                //cancel current task
                Bukkit.getScheduler().cancelTask(taskId);
            }
            //create new DOT
            DamageOverTime newDOT = new DamageOverTime(this.plugin, (LivingEntity) target, DamageTypeEnum.BLEED, damage, 5, player,true);
            int newTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, newDOT, 20L, 20L);
            newDOT.setTaskId(newTaskId);
            enemyPDC.set(new NamespacedKey(plugin, "hasBleed"), PersistentDataType.INTEGER, newTaskId);

        }
        return  damage;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageEvent(EntityDamageByEntityEvent event){
        plugin.getLogger().info( "EntityDamageByEntityEvent was called");

        if (event.getDamager() instanceof Player player){
            CustomPlayer thisPlayer = new CustomPlayer(plugin,player);

            if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
                //not using fist
                PersistentDataContainer pdc  = player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
                logItemPDCKeys(pdc,plugin);
                if(pdc.has(new NamespacedKey(plugin,"itemModified"), PersistentDataType.BOOLEAN)){
                    //this item has modified statsplugin.getLogger().info( player.getName()+" is using a Modified Item");
                    event.setDamage(0);
                    Byte bitmask = pdc.get(new NamespacedKey(plugin,"damageTypesMask"),PersistentDataType.BYTE);
                    if(bitmask == null){
                        plugin.getLogger().info( player.getName()+" tried to deal modified damage but typeMask was not present");
                        return;
                    }
                    List<DamageTypeEnum> allDamageTypes = DamageTypeEnum.getTypes(bitmask);
                    for (DamageTypeEnum damageType : allDamageTypes) {
                        Double baseDamage = pdc.get(new NamespacedKey(plugin,damageType.asString()+"Damage"),PersistentDataType.DOUBLE);
                        if(baseDamage == null){
                            plugin.getLogger().info( player.getName()+" tried to deal modified damage but "+ damageType.asString()+"Damage was not present");
                            return;
                        }
                        Double damage = calculateDamage(damageType,event.getEntity(),baseDamage,player);
                        if(damage == null){
                            return;
                        }
                        event.setDamage(event.getDamage()+damage);
                        Integer displayDamage = (int) Math.ceil(damage);
                        DamageIndicator.sendFakeArmorStand(plugin,player,event.getEntity().getLocation(),displayDamage.toString(),random);
                    }

                }else{

                    //using unmodified item
                    Double damage = calculateDamage(DamageTypeEnum.NORMAL,event.getEntity(),event.getFinalDamage(),player);
                    if(damage == null){
                        return;
                    }
                    event.setDamage(event.getDamage()+damage);
                    Integer displayDamage = (int) Math.ceil(damage);
                    DamageIndicator.sendFakeArmorStand(plugin,player,event.getEntity().getLocation(),displayDamage.toString(),random);
                }
            }else{

                //using their fist
                Double damage = calculateDamage(DamageTypeEnum.NORMAL,event.getEntity(),event.getFinalDamage(),player);
                if(damage == null){
                    return;
                }
                event.setDamage(event.getDamage()+damage);
                Integer displayDamage = (int) Math.ceil(damage);
                DamageIndicator.sendFakeArmorStand(plugin,player,event.getEntity().getLocation(),displayDamage.toString(),random);
            }


        }

    }
    public void logItemPDCKeys(PersistentDataContainer pdc, Plugin plugin) {

        plugin.getLogger().info("PDC CHECKS");
        if (pdc.isEmpty()) {
            plugin.getLogger().info("PDC is empty.");
            return;
        }

        for (NamespacedKey key : pdc.getKeys()) {
            plugin.getLogger().info("Key: " + key);
        }
    }
    //only kinda works
    //spawns the armour stand just need to make it invisible with a nametag
    //might ignore this for now and move on to the rest of the plugin just show damage through text.



}
