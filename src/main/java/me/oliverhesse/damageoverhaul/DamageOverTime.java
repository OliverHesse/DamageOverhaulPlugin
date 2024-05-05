package me.oliverhesse.damageoverhaul;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Random;


public class DamageOverTime implements Runnable{
    private final Plugin plugin;
    private final LivingEntity target;
    private final DamageTypeEnum damageType;
    private final double damage;
    private final int numberOfTicks;
    private int currentTick = 0;
    private final Entity damagee;
    private int taskId;
    private boolean isPlayer;
    private Random random;
    @Override
    public void run() {
        if (this.getTicksLeft() > 0) {
            this.getTarget().damage(this.damage);
            if(isPlayer){
            DamageIndicator.sendFakeArmorStand(this.plugin,(Player) this.damagee,this.target.getLocation(), String.valueOf(Math.ceil(this.damage)),random);
            }
            this.incrTick();
        } else {
            this.cancel(); // Stop the task when it has run the specified number of times
        }
    }
    public void setTaskId(int taskId){
        this.taskId = taskId;
    }
    private void cancel() {
        Bukkit.getScheduler().cancelTask(this.taskId);
    }
    public DamageOverTime(Plugin plugin, LivingEntity target, DamageTypeEnum damageType, double damage, int numberOfTicks, Entity damagee,boolean isPlayer){
        this.plugin = plugin;
        this.target = target;
        this.damageType = damageType;
        this.damage = damage;
        this.numberOfTicks = numberOfTicks;
        this.damagee = damagee;
        this.isPlayer = isPlayer;
        this.random = new Random();
    }
    public LivingEntity getTarget(){
        return this.target;
    }

    public Entity getDamagee() {
        return damagee;
    }

    public DamageTypeEnum getType(){
        return this.damageType;
    }
    public double getDamage(){
        return this.damage;
    }
    public int getTicksLeft(){
        return this.numberOfTicks-this.currentTick;
    }
    public void incrTick(){
        this.currentTick++;
    }
}
