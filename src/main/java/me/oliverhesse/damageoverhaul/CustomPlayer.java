package me.oliverhesse.damageoverhaul;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.rmi.server.UID;
import java.util.UUID;

public class CustomPlayer {
    private final Player player;
    private final Plugin plugin;

    //used if player already has had their pdc set
    public CustomPlayer(Plugin plugin, Player player){
        this.player = player;
        this.plugin = plugin;
    }
    //creates new data for the player
    public CustomPlayer(Plugin plugin,Player player,double BaseDefence,double baseHealth,double healthScale,float baseSpeed){
        this.player = player;
        this.plugin = plugin;
        AttributeInstance maxHealthAttribute = this.player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute == null) {

            return;

        }
        maxHealthAttribute.setBaseValue(baseHealth);
        this.player.setHealthScale(healthScale);
        this.player.setWalkSpeed(baseSpeed);
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "entityModified"), PersistentDataType.BOOLEAN, true);

        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "Defence"), PersistentDataType.DOUBLE, BaseDefence);
        //creates resistances
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "HeatResistance"), PersistentDataType.DOUBLE, 0d);
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "SlashResistance"), PersistentDataType.DOUBLE, 0d);
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "PierceResistance"), PersistentDataType.DOUBLE, 0d);
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "BludgeonResistance"), PersistentDataType.DOUBLE, 0d);
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "NormalResistance"), PersistentDataType.DOUBLE, 0d);

        //create vulnerability
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "HeatVulnerability"), PersistentDataType.DOUBLE, 0d);
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "SlashVulnerability"), PersistentDataType.DOUBLE, 0d);
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "PierceVulnerability"), PersistentDataType.DOUBLE, 0d);
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "BludgeonVulnerability"), PersistentDataType.DOUBLE, 0d);
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "NormalVulnerability"), PersistentDataType.DOUBLE, 0d);


    }

    //updates the players total defence
    public boolean updateDefence(double defenceChange){
        Double currentDefence = this.player.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "Defence"),PersistentDataType.DOUBLE);
        if(currentDefence == null){
            return false;
        }
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "Defence"),PersistentDataType.DOUBLE,currentDefence-defenceChange);
        return true;
    }
    //updates the players current health
    public void updateHealth(double healthChange){
        this.player.setHealth(this.player.getHealth()+healthChange);

    }
    public boolean updateMaxHealth(double healthChange){
        AttributeInstance maxHealthAttribute = this.player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute == null) {
            return false;

        }
        double maxHealth = maxHealthAttribute.getValue();
        maxHealthAttribute.setBaseValue(maxHealth+healthChange);
        return true;
    }
    public void updateSpeed(float speedChange){
        this.player.setWalkSpeed(this.player.getWalkSpeed()+speedChange);
    }
    public boolean updateResistance(double resistanceChange,DamageTypeEnum damageType){
        Double currentResistance = this.player.getPersistentDataContainer().get(new NamespacedKey(this.plugin, damageType.asString()+"Resistance"), PersistentDataType.DOUBLE);
        if(currentResistance == null){
            return false;
        }
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, damageType.asString()+"Resistance"), PersistentDataType.DOUBLE, currentResistance+resistanceChange);
        return true;
    }
    public boolean updateVulnerability(double vulnerabilityChange,DamageTypeEnum damageType){
        Double currentVulnerability = this.player.getPersistentDataContainer().get(new NamespacedKey(this.plugin, damageType.asString()+"Vulnerability"), PersistentDataType.DOUBLE);
        if(currentVulnerability == null){
            return false;
        }
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin, damageType.asString()+"Vulnerability"), PersistentDataType.DOUBLE, currentVulnerability+vulnerabilityChange);
        return true;
    }
    public Double getResistance(DamageTypeEnum damageType){
        return this.player.getPersistentDataContainer().get(new NamespacedKey(this.plugin, damageType.asString()+"Resistance"), PersistentDataType.DOUBLE);

    }
    public Double getVulnerability(DamageTypeEnum damageType){
        return this.player.getPersistentDataContainer().get(new NamespacedKey(this.plugin, damageType.asString()+"Vulnerability"), PersistentDataType.DOUBLE);

    }
    public Double getDefence(){
        return this.player.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "Defence"), PersistentDataType.DOUBLE);
    }
    public void setLastDamageType(DamageTypeEnum damageType, UUID enemyID,Double damage){
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin,"enemyID"+enemyID.toString()),PersistentDataType.STRING,damageType.asString());
        this.player.getPersistentDataContainer().set(new NamespacedKey(this.plugin,"damage"+enemyID.toString()),PersistentDataType.DOUBLE,damage);
    }
    public DamageTypeEnum getLastDamageType(UUID enemyID){
        String damageType = this.player.getPersistentDataContainer().get(new NamespacedKey(this.plugin,"enemyID"+enemyID.toString()),PersistentDataType.STRING);

        if(damageType == null){
            return null;
        }
        return DamageTypeEnum.typeFromString(damageType);

    }
    public Double getLastDamageAmount(UUID enemyID){
        return this.player.getPersistentDataContainer().get(new NamespacedKey(this.plugin,"damage"+enemyID.toString()),PersistentDataType.DOUBLE);
    }
    public void removeLastDamageType(UUID enemyID){
        this.player.getPersistentDataContainer().remove(new NamespacedKey(this.plugin,"enemyID"+enemyID.toString()));
        this.player.getPersistentDataContainer().remove(new NamespacedKey(this.plugin,"damage"+enemyID.toString()));
    }

    public Player getPlayer(){
        return this.player;
    }

}
