package me.oliverhesse.damageoverhaul;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

abstract class DamageMethods {

    public static Double calculatePlayerDamage(Plugin plugin, DamageTypeEnum damageType, Entity target, double baseDamage, Player player, double scaleFactor){
        PersistentDataContainer enemyPDC  = target.getPersistentDataContainer();
        Double defence = 100d; //base defence of enemies
        Double resistance= 0d;
        Double vulnerability= 0d;
        if(enemyPDC.has(new NamespacedKey(plugin,"entityModified"), PersistentDataType.BOOLEAN)){
            //entity is modified use normal formula
            //get new defence

            defence = enemyPDC.get(new NamespacedKey(plugin,"defence"),PersistentDataType.DOUBLE);
            if(defence == null){

                return null;
            }

            resistance = enemyPDC.get(new NamespacedKey(plugin,damageType.asString()+"Resistance"),PersistentDataType.DOUBLE);
            if(resistance == null){

                return null;
            }

            vulnerability = enemyPDC.get(new NamespacedKey(plugin,damageType.asString()+"Vulnerability"),PersistentDataType.DOUBLE);
            if(vulnerability == null){

                return null;
            }


        }

        if(damageType == DamageTypeEnum.BLEED){
            //halve defence
            defence = defence/2;
        }
        if(damageType == DamageTypeEnum.ALMIGHTY){
            defence = 0d;
        }

        if(damageType == DamageTypeEnum.SLASH){
            //they dealt slash damage apply bleed
            //currently bleed is a bit broken. the last applied bleed will always override the currently applied bleed
            Integer taskId =  enemyPDC.get(new NamespacedKey(plugin,"hasBleed"),PersistentDataType.INTEGER);
            if(taskId != null) {
                //cancel current task
                Bukkit.getScheduler().cancelTask(taskId);
            }
            //create new DOT
            DamageOverTime newDOT = new DamageOverTime(plugin, (LivingEntity) target, DamageTypeEnum.BLEED, baseDamage, 5, player,true);
            int newTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, newDOT, 20L, 20L);
            newDOT.setTaskId(newTaskId);
            enemyPDC.set(new NamespacedKey(plugin, "hasBleed"), PersistentDataType.INTEGER, newTaskId);

        }
        plugin.getLogger().info("base Damage: "+baseDamage);
        Double damage = (baseDamage/(1+(defence/scaleFactor)))*(1-resistance/100)*(1+vulnerability/100);
        plugin.getLogger().info("final Damage: "+damage);
        plugin.getLogger().info("enemy defence: "+defence);

        return  damage;


    }
    public double calculateMobDamage(DamageTypeEnum damageType, Entity target, double baseDamage, LivingEntity damagee){
        return 2d;
    }
}
