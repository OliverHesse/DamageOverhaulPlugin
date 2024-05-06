package me.oliverhesse.damageoverhaul;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
//used to spawn custom mobs
//for now only spawn zombies
public class CommandSpawn implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player player){
            Plugin plugin = sender.getServer().getPluginManager().getPlugin("DamageOverhaul");
            Location newLocation = player.getLocation();
            newLocation.setX(newLocation.getX());
            LivingEntity newZombie = (LivingEntity) player.getWorld().spawnEntity(newLocation, EntityType.ZOMBIE);

            CustomMob customZombie = new CustomMob(plugin,newZombie,200,100,0.1f,"Custom Zombie");
            customZombie.setNameDisplayed(true);
            return true;
        }

        return false;
    }
}
