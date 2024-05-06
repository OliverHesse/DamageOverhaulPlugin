package me.oliverhesse.damageoverhaul;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Random;

public class PlayerListener implements Listener {
    private final Plugin plugin;

    public PlayerListener(Plugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD, 1);

        ItemMeta meta = item.getItemMeta();
        //for now I will be making a sword with slash and pierce damage
        byte itemMask = (byte) (DamageTypeEnum.SLASH.getMask()|DamageTypeEnum.PIERCE.getMask());
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "itemModified"), PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "damageTypesMask"), PersistentDataType.BYTE, itemMask);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "SlashDamage"), PersistentDataType.DOUBLE, 20d);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "PierceDamage"), PersistentDataType.DOUBLE, 50d);
        item.setItemMeta(meta);
        logItemPDCKeys(item,plugin);
        event.getPlayer().getInventory().addItem(item);
        event.getPlayer().updateInventory(); // Update the player's inventory to reflect the changes
        CustomPlayer playerWrapper = new CustomPlayer(plugin,event.getPlayer(),200d,500d,20d,0.2f);
    }

    @EventHandler
    public void prePlayerAttack(PrePlayerAttackEntityEvent event){
        CustomPlayer player = new CustomPlayer(this.plugin, event.getPlayer());

        if(player.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR){
            PersistentDataContainer pdc  = player.getPlayer().getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
            if(pdc.has(new NamespacedKey(plugin,"itemModified"), PersistentDataType.BOOLEAN)){

                Byte bitmask = pdc.get(new NamespacedKey(plugin,"damageTypesMask"),PersistentDataType.BYTE);
                if(bitmask == null){
                    plugin.getLogger().info( player.getPlayer().getName()+" tried to deal modified damage but typeMask was not present");
                    return;
                }
                List<DamageTypeEnum> allDamageTypes = DamageTypeEnum.getTypes(bitmask);
                //we know the item was modified cancel attack event
                for (DamageTypeEnum damageType : allDamageTypes) {

                    LivingEntity target = (LivingEntity) event.getAttacked();
                    Double baseDamage = pdc.get(new NamespacedKey(plugin,damageType.asString()+"Damage"),PersistentDataType.DOUBLE);
                    plugin.getLogger().info("Base Damage: "+baseDamage );
                    if(baseDamage == null){
                        plugin.getLogger().info( player.getPlayer().getName()+" tried to deal modified damage but "+ damageType.asString()+"Damage was not present");
                        return;
                    }
                    plugin.getLogger().info("Base Damage: "+baseDamage );
                    player.setLastDamageType(damageType,event.getAttacked().getUniqueId(),baseDamage);
                    //The actual damage sent here is a bit bugged so i also save the damage to retrieve
                    target.damage(baseDamage,player.getPlayer());


                }
            }else{
                //item is not modified
                player.setLastDamageType(DamageTypeEnum.NORMAL,event.getAttacked().getUniqueId(),5d);
            }

        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageEvent(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player playerObj){
            CustomPlayer player = new CustomPlayer(this.plugin,playerObj);

            DamageTypeEnum lastDamageType = player.getLastDamageType(event.getEntity().getUniqueId());
            if(lastDamageType == null){
                plugin.getLogger().info(player.getPlayer().getName()+": something went wrong");
                return;
            }
            plugin.getLogger().info(player.getPlayer().getName()+" is trying to deal "+lastDamageType.asString());

            Double baseDamage = player.getLastDamageAmount(event.getEntity().getUniqueId());
            plugin.getLogger().info("Test base Damage: "+ baseDamage);
            Double finalDamage = DamageMethods.calculatePlayerDamage(this.plugin,lastDamageType,event.getEntity(),baseDamage,player.getPlayer(),1000);
            if(finalDamage == null){
                plugin.getLogger().info(player.getPlayer().getName()+": something went wrong calculating final damage");
                return;
            }

            String finalDamageText = String.valueOf((int) Math.ceil(finalDamage));
            event.setDamage(finalDamage);
            DamageIndicator.sendFakeArmorStand(this.plugin,player.getPlayer(),event.getEntity().getLocation(),finalDamageText,new Random());
            player.removeLastDamageType(event.getEntity().getUniqueId());
        }
    }
    public void logItemPDCKeys(ItemStack item, Plugin plugin) {
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        if (pdc.isEmpty()) {
            plugin.getLogger().info("PDC is empty.");
            return;
        }

        for (NamespacedKey key : pdc.getKeys()) {
            plugin.getLogger().info("Key: " + key);
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamaged(EntityDamageEvent event){
        if (event.getEntity() instanceof Player player){
            DamageOverhaul.sendStatActionBar(player,-event.getDamage());
        }else{
            if(CustomMob.isCustomMob(plugin,(LivingEntity) event.getEntity())){
                plugin.getLogger().info("enemy took: "+event.getDamage());
                CustomMob currentMob = new CustomMob(plugin,(LivingEntity) event.getEntity());
                currentMob.updateDisplayHealth(-event.getDamage());
            }
        }
    }


}