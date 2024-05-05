package me.oliverhesse.damageoverhaul;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.units.qual.C;

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
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "PierceDamage"), PersistentDataType.DOUBLE, 30d);
        item.setItemMeta(meta);
        logItemPDCKeys(item,plugin);
        event.getPlayer().getInventory().addItem(item);
        event.getPlayer().updateInventory(); // Update the player's inventory to reflect the changes
        CustomPlayer playerWrapper = new CustomPlayer(plugin,event.getPlayer(),200d,500d,20d,0.2f);
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
            plugin.getLogger().info( player.getHealth()+" took damage");
            plugin.getLogger().info( player.getHealth()+" took "+ event.getDamage()+"Damage");
            DamageOverhaul.sendStatActionBar(player,-event.getDamage());
        }else{
            if(CustomMob.isCustomMob(plugin,(LivingEntity) event.getEntity())){
                CustomMob currentMob = new CustomMob(plugin,(LivingEntity) event.getEntity());
                currentMob.updateDisplayHealth(-event.getDamage());
            }
        }
    }
}
