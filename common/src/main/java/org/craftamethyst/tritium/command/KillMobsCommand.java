package org.craftamethyst.tritium.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.function.Predicate;

public class KillMobsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("killmobs")
                        .requires(source -> source.hasPermission(4))
                        .executes(context -> killMobs(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> killMobs(context.getSource(), EntityArgument.getPlayers(context, "targets")))
                        )
        );
    }

    private static int killMobs(CommandSourceStack source, Player player) {
        return killMobs(source, java.util.Collections.singleton(player));
    }

    private static int killMobs(CommandSourceStack source, Collection<? extends Player> players) {
        int totalKilled = 0;

        for (Player player : players) {
            double range = 50.0;
            Predicate<Entity> isMonster = entity ->
                    entity instanceof Monster &&
                            !entity.getType().equals(EntityType.ENDER_DRAGON);

            Collection<Entity> monsters = player.level().getEntities(player, player.getBoundingBox().inflate(range), isMonster);
            int killed = 0;
            for (Entity mob : monsters) {
                mob.discard();
                killed++;
            }

            totalKilled += killed;

            if (killed > 0) {
                player.displayClientMessage(Component.literal("清除了 " + killed + " 个怪物"),false);
            } else {
                player.displayClientMessage(Component.literal("附近没有怪物"),false);
            }
        }

        return totalKilled;
    }
}