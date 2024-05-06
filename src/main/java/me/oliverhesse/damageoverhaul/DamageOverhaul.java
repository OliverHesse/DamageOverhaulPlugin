package me.oliverhesse.damageoverhaul;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
/*TODO look into damageSource to rewrite damage function to differentiate beteween DOT and normal this
  TODO this would be most likely done by implementing it into my DamageTypeEnum
*/
public final class DamageOverhaul extends JavaPlugin {
    private int taskId;
    @Override
    public void onEnable() {

        // Plugin startup logic
        //regiser commands
        getCommand("spawn").setExecutor(new CommandSpawn());

        //register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        //create task so i can display stats
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendStatActionBar(player,0d);
            }
        }, 0L, 40L); // 20 ticks = 1 second, so 2 seconds = 40 ticks


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getScheduler().cancelTask(taskId);
    }
    public static void sendStatActionBar(Player player,Double healthChange){
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if(maxHealthAttribute == null){

            return;
        }
        Integer displayHealth = (int) Math.ceil(player.getHealth()+healthChange);
        Integer displayMaxHealth = (int) Math.ceil(maxHealthAttribute.getValue());
        player.sendActionBar(Component.text().content(displayHealth+"/"+displayMaxHealth).color(NamedTextColor.RED));
    }

}
