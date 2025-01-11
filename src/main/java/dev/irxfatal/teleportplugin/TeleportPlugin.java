package dev.irxfatal.teleportplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportPlugin extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        getCommand("tp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 3) {
            return true;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(args[0]);
            y = Double.parseDouble(args[1]);
            z = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            return true;
        }

        Location targetLocation = new Location(player.getWorld(), x, y, z);
        targetLocation.setYaw(player.getLocation().getYaw());
        targetLocation.setPitch(player.getLocation().getPitch());
        startTeleportAnimation(player, targetLocation);
        return true;
    }

    private void startTeleportAnimation(Player player, Location targetLocation) {
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 1.0f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks < 30) { // Faster animation
                    double radius = 1;
                    double height = ticks * 0.1; // Faster vertical movement
                    for (int i = 0; i < 10; i++) {
                        double angle = i * (Math.PI / 5) + ticks * 0.2; // Faster rotation
                        double xOffset = radius * Math.cos(angle);
                        double zOffset = radius * Math.sin(angle);
                        player.getWorld().spawnParticle(
                                Particle.END_ROD,
                                player.getLocation().add(xOffset, height, zOffset),
                                2,
                                0,
                                0,
                                0,
                                0
                        );
                    }

                    if (ticks % 10 == 0) { // More frequent sound
                        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 1.0f);
                    }

                    ticks++;
                } else {
                    player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, player.getLocation(), 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    player.teleport(targetLocation);

                    new BukkitRunnable() {
                        int fallTicks = 0;

                        @Override
                        public void run() {
                            if (fallTicks < 30) { // Faster animation
                                double radius = 1;
                                double height = 3 - (fallTicks * 0.1); // Faster vertical movement
                                for (int i = 0; i < 10; i++) {
                                    double angle = i * (Math.PI / 5) + fallTicks * 0.2; // Faster rotation
                                    double xOffset = radius * Math.cos(angle);
                                    double zOffset = radius * Math.sin(angle);
                                    targetLocation.getWorld().spawnParticle(
                                            Particle.END_ROD,
                                            targetLocation.clone().add(xOffset, height, zOffset),
                                            2,
                                            0,
                                            0,
                                            0,
                                            0
                                    );
                                }
                                fallTicks++;
                            } else {
                                cancel();
                            }
                        }
                    }.runTaskTimer(TeleportPlugin.this, 0, 1);

                    cancel();
                }
            }
        }.runTaskTimer(this, 0, 1);
    }
}
