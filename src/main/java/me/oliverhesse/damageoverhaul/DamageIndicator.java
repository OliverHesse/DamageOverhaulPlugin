package me.oliverhesse.damageoverhaul;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

abstract class DamageIndicator {

    public static void sendFakeArmorStand(Plugin plugin, Player player, Location location, String damage, Random random) {
        double yModifier = -1.75 + (random.nextDouble() * 1.75);
        double xModifier = -0.75 + (random.nextDouble() * 1);
        double zModifier = -0.75 + (random.nextDouble() * 1);
        //update location using offset
        location.setX(location.getX() + xModifier);
        location.setY(location.getY() + yModifier);
        location.setZ(location.getZ() + zModifier);

        // Create the armor stand
        Integer newId = random.nextInt(Integer.MAX_VALUE);

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        //set entity data
        var spawnPacketModifier = packet.getModifier();
        spawnPacketModifier.write(0, newId);
        spawnPacketModifier.write(1, UUID.randomUUID());
        spawnPacketModifier.write(3, location.getX());
        spawnPacketModifier.write(4, location.getY());
        spawnPacketModifier.write(5, location.getZ());
        packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);

        //create meta data packet
        PacketContainer packetMeta = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packetMeta.getIntegers().write(0,newId);
        List<WrappedDataValue> metadata = new ArrayList<>();
        Optional<?> name = Optional.of(WrappedChatComponent.fromChatMessage(damage)[0].getHandle());

        //set meta data values
        WrappedDataValue bitmask = new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class),(byte) 0x20);
        WrappedDataValue customName = new WrappedDataValue(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), name);
        WrappedDataValue customNameVisible = new WrappedDataValue(3, WrappedDataWatcher.Registry.get(Boolean.class), true);
        WrappedDataValue gravityState = new WrappedDataValue(5, WrappedDataWatcher.Registry.get(Boolean.class), true);

        metadata.add(customName);
        metadata.add(customNameVisible);
        metadata.add(bitmask);
        metadata.add(gravityState);

        packetMeta.getDataValueCollectionModifier().write(0, metadata);
        // Send the packet
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetMeta);


        // Remove the armor stand after 1 second
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> removeFakeArmorStand(player,newId), 20); // 20 ticks = 1 second
    }
    public static void removeFakeArmorStand(Player player,Integer id) {
        // Remove the armor stand
        PacketContainer destroyPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        List<Integer> allId = new ArrayList<>();
        allId.add(id);
        destroyPacket.getIntLists().write(0, allId);

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);


    }
}
