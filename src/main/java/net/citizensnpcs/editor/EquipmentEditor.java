package net.citizensnpcs.editor;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

public class EquipmentEditor extends Editor {
    private final NPC npc;
    private final Player player;

    public EquipmentEditor(Player player, NPC npc) {
        this.player = player;
        this.npc = npc;
    }

    @Override
    public void begin() {
        Messaging.sendTr(player, Messages.EQUIPMENT_EDITOR_BEGIN);
    }

    @Override
    public void end() {
        Messaging.sendTr(player, Messages.EQUIPMENT_EDITOR_END);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (event.getMessage().equals("helmet")
                && !event.getPlayer().hasPermission("citizens.npc.edit.equip.any-helmet")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    if (!event.getPlayer().isValid())
                        return;
                    ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
                    if (hand.getType() == Material.AIR || hand.getAmount() <= 0) {
                        return;
                    }
                    npc.getTrait(Equipment.class).set(EquipmentSlot.HELMET,
                            new ItemStack(event.getPlayer().getInventory().getItemInMainHand().getType(), 1));
                    hand.setAmount(hand.getAmount() - 1);
                    event.getPlayer().getInventory().setItemInMainHand(hand);
                }
            });
        }
        if (event.getMessage().equals("offhand")
                && !event.getPlayer().hasPermission("citizens.npc.edit.equip.offhand")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    if (!event.getPlayer().isValid())
                        return;
                    ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
                    if (hand.getType() == Material.AIR || hand.getAmount() <= 0) {
                        return;
                    }
                    npc.getTrait(Equipment.class).set(EquipmentSlot.OFF_HAND,
                            new ItemStack(event.getPlayer().getInventory().getItemInMainHand().getType(), 1));
                    hand.setAmount(hand.getAmount() - 1);
                    event.getPlayer().getInventory().setItemInMainHand(hand);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR && Editor.hasEditor(event.getPlayer())) {
            event.setUseItemInHand(Result.DENY);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!npc.isSpawned() || !event.getPlayer().equals(player)
                || !npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())))
            return;

        Equipper equipper = EQUIPPERS.get(npc.getEntity().getType());
        if (equipper == null) {
            equipper = new GenericEquipper();
        }
        equipper.equip(event.getPlayer(), npc);
        event.setCancelled(true);
    }

    private static final Map<EntityType, Equipper> EQUIPPERS = Maps.newEnumMap(EntityType.class);

    static {
        EQUIPPERS.put(EntityType.PIG, new PigEquipper());
        EQUIPPERS.put(EntityType.SHEEP, new SheepEquipper());
        EQUIPPERS.put(EntityType.ENDERMAN, new EndermanEquipper());
        EQUIPPERS.put(EntityType.HORSE, new HorseEquipper());
    }
}