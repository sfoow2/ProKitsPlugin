package me.sfoow.prokits.Ect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import revxrsal.commands.annotation.Command;

import java.util.HashMap;
import java.util.UUID;

import static me.sfoow.prokits.Ect.RegionUtil.isInRegion;
import static me.sfoow.prokits.Ect.utils.*;

public class ShulkerRooms implements Listener {

    // true = taken, false = free
    private static final boolean[] roomsTaken = new boolean[20];

    // roomId -> player UUID
    private static final HashMap<Byte, UUID> roomOwner = new HashMap<>();

    // player UUID -> roomId
    private static final HashMap<UUID, Byte> playerRoom = new HashMap<>();

    /* =========================
       INTERNAL HELPERS
       ========================= */

    private static byte getNextFreeRoom() {
        for (byte i = 0; i < roomsTaken.length; i++) {
            if (!roomsTaken[i]) {
                return i;
            }
        }
        return -1;
    }

    private static Location getRoomLocation(byte roomId) {
        return new Location(Bukkit.getWorld("spawn"),18.5,92,-73.5).add(new Vector(18 * roomId,0,0));
    }

    /* =========================
       PUBLIC API
       ========================= */

    /**
     * Sends a player to the next available room
     */
    public static boolean sendPlayerToRoom(Player player) {
        UUID uuid = player.getUniqueId();

        // Already in a room
        if (playerRoom.containsKey(uuid)) {
            Qwerty(player,"§cYou are already in a room.");
            SendNo(player);
            return false;
        }

        byte roomId = getNextFreeRoom();
        if (roomId == -1) {
            Qwerty(player,"§cNo free rooms available.");
            SendNo(player);
            return false;
        }

        // Mark room as taken
        roomsTaken[roomId] = true;
        roomOwner.put(roomId, uuid);
        playerRoom.put(uuid, roomId);

        // Teleport (null for now)
        Location loc = getRoomLocation(roomId);
        if (loc != null) {
            player.teleport(loc);
        }

        Qwerty(player,"§aYou have been sent to room §e#" + roomId);
        SendYes(player);
        return true;
    }

    /**
     * Removes a player from their room
     */
    public static boolean removePlayerFromRoom(Player player) {
        UUID uuid = player.getUniqueId();

        if (!playerRoom.containsKey(uuid)) {
            Qwerty(player,"§cYou are not in a room.");
            SendNo(player);
            return false;
        }

        byte roomId = playerRoom.get(uuid);

        // Free the room
        roomsTaken[roomId] = false;
        roomOwner.remove(roomId);
        playerRoom.remove(uuid);

        // Teleport out (null for now)
        // player.teleport(somewhere);

        Qwerty(player,"§aYou have left room §e#" + roomId);
        SendYes(player);
        return true;
    }

    /**
     * Force free a room (useful for quit events)
     */
    public static void removePlayerFromRoom(UUID uuid) {
        if (!playerRoom.containsKey(uuid)) return;

        byte roomId = playerRoom.get(uuid);

        roomsTaken[roomId] = false;
        roomOwner.remove(roomId);
        playerRoom.remove(uuid);
    }

    /* =========================
       OPTIONAL GETTERS
       ========================= */

    public static boolean isRoomTaken(byte roomId) {
        return roomsTaken[roomId];
    }

    public static Byte getPlayerRoom(Player player) {
        return playerRoom.get(player.getUniqueId());
    }


    @Command("shulker")
    public void ShulkerSendCommand(Player sender){
        if (isInRegion(sender.getLocation(),"spawn")){
            sendPlayerToRoom(sender);
        } else {
            Qwerty(sender,"&c&lYou can only do this in spawn!");
            SendNo(sender);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        removePlayerFromRoom(event.getPlayer().getUniqueId());
    }


}
