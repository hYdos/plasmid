package net.gegy1000.plasmid.game.player;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class PlayerSnapshot {
    private final RegistryKey<World> dimension;
    private final Vec3d position;
    private final GameMode gameMode;
    private final DefaultedList<ItemStack> inventory;
    private final DefaultedList<ItemStack> enderInventory;
    private final Collection<StatusEffectInstance> potionEffects;

    private PlayerSnapshot(
            RegistryKey<World> dimension, Vec3d position,
            GameMode gameMode,
            DefaultedList<ItemStack> inventory,
            DefaultedList<ItemStack> enderInventory,
            Collection<StatusEffectInstance> potionEffects
    ) {
        this.dimension = dimension;
        this.position = position;
        this.gameMode = gameMode;
        this.inventory = inventory;
        this.enderInventory = enderInventory;
        this.potionEffects = potionEffects;
    }

    public static PlayerSnapshot take(ServerPlayerEntity player) {
        RegistryKey<World> dimension = player.world.getRegistryKey();
        Vec3d position = player.getPos();
        GameMode gameMode = player.interactionManager.getGameMode();

        DefaultedList<ItemStack> inventory = snapshotInventory(player.inventory);
        DefaultedList<ItemStack> enderInventory = snapshotInventory(player.getEnderChestInventory());

        List<StatusEffectInstance> potionEffects = player.getStatusEffects().stream()
                .map(StatusEffectInstance::new)
                .collect(Collectors.toList());

        return new PlayerSnapshot(dimension, position, gameMode, inventory, enderInventory, potionEffects);
    }

    public void restore(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld().getServer().getWorld(this.dimension);

        player.setGameMode(GameMode.ADVENTURE);

        this.restoreInventory(player.inventory, this.inventory);
        this.restoreInventory(player.getEnderChestInventory(), this.enderInventory);

        player.clearStatusEffects();
        for (StatusEffectInstance potionEffect : this.potionEffects) {
            player.addStatusEffect(potionEffect);
        }

        player.teleport(world, this.position.x, this.position.y, this.position.z, 0.0F, 0.0F);
        player.setGameMode(this.gameMode);

        player.setFireTicks(0);
        player.stopFallFlying();
    }

    private void restoreInventory(Inventory inventory, DefaultedList<ItemStack> from) {
        inventory.clear();
        for (int i = 0; i < from.size(); i++) {
            inventory.setStack(i, from.get(i));
        }
    }

    private static DefaultedList<ItemStack> snapshotInventory(Inventory inventory) {
        DefaultedList<ItemStack> copy = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < copy.size(); i++) {
            copy.set(i, inventory.getStack(i));
        }
        return copy;
    }
}
