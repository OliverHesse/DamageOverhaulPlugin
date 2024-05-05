package me.oliverhesse.damageoverhaul;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

public class CustomMob {
    private final LivingEntity mob;
    private final Plugin plugin;
    private final NamedTextColor  nameColour = NamedTextColor.BLUE;
    public static boolean isCustomMob(Plugin plugin, LivingEntity mob){
        return mob.getPersistentDataContainer().has(new NamespacedKey(plugin, "entityModified"), PersistentDataType.BOOLEAN);
    }
    //u sed if mob already has had their pdc set
    public CustomMob(Plugin plugin, LivingEntity mob){
        this.mob = mob;
        this.plugin = plugin;
    }
    //creates new data for the mob


    public CustomMob(Plugin plugin,LivingEntity mob,double baseDefence,double baseHealth,float baseSpeed,String name,boolean NameTagVisible){
        this.mob = mob;
        this.plugin = plugin;
        AttributeInstance maxHealthAttribute = this.mob.getAttribute(Attribute.GENERIC_MAX_HEALTH);


        AttributeInstance mobBaseSpeed = this.mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (maxHealthAttribute == null || mobBaseSpeed == null) {
            return;
        }
        maxHealthAttribute.setBaseValue(baseHealth);
        mobBaseSpeed.setBaseValue(baseSpeed);
        this.mob.setHealth(baseHealth);
        int maxHealth = (int) Math.ceil(maxHealthAttribute.getValue());
        int currentHealth = (int) Math.ceil(this.mob.getHealth());
        Component newText = Component.text()
                .content(name).color(this.nameColour)
                .append(Component.text(" "+currentHealth+"/"+maxHealth,NamedTextColor.GREEN)).build();
        this.mob.customName(newText);
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "entityModified"), PersistentDataType.BOOLEAN, true);
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "entityName"), PersistentDataType.STRING, name);

        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "Defence"), PersistentDataType.DOUBLE, baseDefence);
        //creates resistances
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "HeatResistance"), PersistentDataType.DOUBLE, 0d);
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "SlashResistance"), PersistentDataType.DOUBLE, 0d);
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "PierceResistance"), PersistentDataType.DOUBLE, 0d);
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "BludgeonResistance"), PersistentDataType.DOUBLE, 0d);
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "NormalResistance"), PersistentDataType.DOUBLE, 0d);

        //create vulnerability
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "HeatVulnerability"), PersistentDataType.DOUBLE, 0d);
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "SlashVulnerability"), PersistentDataType.DOUBLE, 0d);
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "PierceVulnerability"), PersistentDataType.DOUBLE, 0d);
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "BludgeonVulnerability"), PersistentDataType.DOUBLE, 0d);
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "NormalVulnerability"), PersistentDataType.DOUBLE, 0d);
    }

    public CustomMob(Plugin plugin,LivingEntity mob,double baseDefence,double baseHealth,float baseSpeed,String name){
        this(plugin,mob,baseDefence,baseHealth,baseSpeed,name,false);
    }
    public void updateDisplayHealth(Double changeHealth){
        AttributeInstance maxHealthAttribute = this.mob.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        String name = this.mob.getPersistentDataContainer().get(new NamespacedKey(plugin,"entityName"),PersistentDataType.STRING);
        if(maxHealthAttribute == null || name == null){
            return;
        }
        int maxHealth = (int) Math.ceil(maxHealthAttribute.getValue());
        int currentHealth = (int) Math.ceil(this.mob.getHealth()+changeHealth);

        Component newText = Component.text()
                .content(name).color(this.nameColour)
                .append(Component.text(" "+currentHealth+"/"+maxHealth,NamedTextColor.GREEN)).build();
        this.mob.customName(newText);
    }


    public void setNameDisplayed(boolean state){
        this.mob.setCustomNameVisible(state);
    }
    //updates the mobs total defence
    public boolean updateDefence(double defenceChange){
        Double currentDefence = this.mob.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "Defence"),PersistentDataType.DOUBLE);
        if(currentDefence == null){
            return false;
        }
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "Defence"),PersistentDataType.DOUBLE,currentDefence-defenceChange);
        return true;
    }
    //updates the mobs current health
    public void updateHealth(double healthChange){
        this.mob.setHealth(this.mob.getHealth()+healthChange);

    }
    public boolean updateMaxHealth(double healthChange){
        AttributeInstance maxHealthAttribute = this.mob.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute == null) {
            return false;

        }
        double maxHealth = maxHealthAttribute.getValue();
        maxHealthAttribute.setBaseValue(maxHealth+healthChange);
        return true;
    }
    public void updateSpeed(float speedChange){

        AttributeInstance mobBaseSpeed = this.mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if ( mobBaseSpeed == null) {
            return;
        }
        mobBaseSpeed.setBaseValue(mobBaseSpeed.getValue()+speedChange);
    }
    public boolean updateResistance(double resistanceChange,DamageTypeEnum damageType){
        Double currentResistance = this.mob.getPersistentDataContainer().get(new NamespacedKey(this.plugin, damageType.asString()+"Resistance"), PersistentDataType.DOUBLE);
        if(currentResistance == null){
            return false;
        }
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, damageType.asString()+"Resistance"), PersistentDataType.DOUBLE, currentResistance+resistanceChange);
        return true;
    }
    public boolean updateVulnerability(double vulnerabilityChange,DamageTypeEnum damageType){
        Double currentVulnerability = this.mob.getPersistentDataContainer().get(new NamespacedKey(this.plugin, damageType.asString()+"Vulnerability"), PersistentDataType.DOUBLE);
        if(currentVulnerability == null){
            return false;
        }
        this.mob.getPersistentDataContainer().set(new NamespacedKey(this.plugin, damageType.asString()+"Vulnerability"), PersistentDataType.DOUBLE, currentVulnerability+vulnerabilityChange);
        return true;
    }
    public Double getResistance(DamageTypeEnum damageType){
        return this.mob.getPersistentDataContainer().get(new NamespacedKey(this.plugin, damageType.asString()+"Resistance"), PersistentDataType.DOUBLE);

    }
    public Double getVulnerability(DamageTypeEnum damageType){
        return this.mob.getPersistentDataContainer().get(new NamespacedKey(this.plugin, damageType.asString()+"Vulnerability"), PersistentDataType.DOUBLE);

    }
    public Double getDefence(){
        return this.mob.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "Defence"), PersistentDataType.DOUBLE);
    }
}
