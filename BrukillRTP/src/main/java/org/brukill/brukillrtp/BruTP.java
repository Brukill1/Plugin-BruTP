package org.brukill.brukillrtp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class BruTP extends JavaPlugin implements Listener {
    private int teleportDelay;
    private String teleportTitle;
    private String teleportSubtitle;
    private String teleportMessage;
    private int minY;
    private int maxY;

    @Override
    public void onEnable() {
        getLogger().info("Плагин запущен!");

        // Загрузка конфигурации
        saveDefaultConfig();
        loadConfiguration();

        // Регистрация событий
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rtp")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                loadConfiguration();
                sender.sendMessage(ChatColor.GREEN + "Конфигурация плагина успешно перезагружена.");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.GREEN + "Список команд плагина BruTP:");
                sender.sendMessage(ChatColor.GOLD + "/rtp - Телепортирует вас на случайное место");
                sender.sendMessage(ChatColor.GOLD + "/rtp reload - Перезагружает конфигурацию плагина");
                sender.sendMessage(ChatColor.GOLD + "/rtp help - Отображает список всех команд плагина");
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;

                String formattedTitle = ChatColor.translateAlternateColorCodes('&', teleportTitle);
                String formattedSubtitle = ChatColor.translateAlternateColorCodes('&', teleportSubtitle);
                player.sendTitle(formattedTitle, formattedSubtitle, 10, teleportDelay * 20, 10);

                Bukkit.getScheduler().runTaskLater(this, () -> teleportPlayer(player), teleportDelay * 20L);
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            Bukkit.getScheduler().runTaskLater(this, () -> player.removePotionEffect(PotionEffectType.BLINDNESS), 60);
        }
    }

    private void teleportPlayer(Player player) {
        Location location = getRandomLocation(player.getWorld());
        player.teleport(location);
        player.setFallDistance(0);
        player.setHealth(player.getMaxHealth());

        String formattedMessage = ChatColor.translateAlternateColorCodes('&', teleportMessage)
                .replace("%x%", String.valueOf(location.getBlockX()))
                .replace("%y%", String.valueOf(location.getBlockY()))
                .replace("%z%", String.valueOf(location.getBlockZ()));
        player.sendMessage(formattedMessage);

        player.sendTitle("", ChatColor.BLACK.toString(), 0, 40, 20);
        Bukkit.getScheduler().runTaskLater(this, () -> player.resetTitle(), 60);
    }

    private Location getRandomLocation(World world) {
        int x = getRandomCoordinate();
        int z = getRandomCoordinate();
        int y = world.getHighestBlockYAt(x, z);

        return new Location(world, x + 0.5, y, z + 0.5);
    }

    private int getRandomCoordinate() {
        return (int) (Math.random() * 20000) - 10000;
    }

    private void loadConfiguration() {
        FileConfiguration config = getConfig();
        teleportDelay = config.getInt("teleport-delay", 3);
        teleportTitle = config.getString("teleport-title", "&6Телепортация");
        teleportSubtitle = config.getString("teleport-subtitle", "&7Подождите...");
        teleportMessage = config.getString("teleport-message", "&aВы были телепортированы на координаты: &e%x%, %y%, %z%");
        minY = config.getInt("min-y", 60);
        maxY = config.getInt("max-y", 80);
    }
}