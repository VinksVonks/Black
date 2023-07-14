package ee.vinku.black;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import ee.vinku.black.Commands.BlackMarketSkullCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public final class BlackMarketSkull extends JavaPlugin implements Listener {
    private ConsoleCommandSender clogger;
    public WorldGuardHook wghook;

    public void onEnable() {
        clogger = getServer().getConsoleSender();
        clogger.sendMessage(ChatColor.AQUA + "---------------------------------------");
        clogger.sendMessage(ChatColor.GREEN + "      Et ma paremini näeksin          ");
        clogger.sendMessage(ChatColor.AQUA + "---------------------------------------");
        //this.getLogger().warning("This plugin uses EssentialsX and WorldGuard, make sure those are installed.");
        getServer().getPluginManager().registerEvents(this, this);

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        getLogger().info("getCommand('reloadconfig'): " + getCommand("reloadconfig"));

        BlackMarketSkullCommand blackMarketSkullCommand = new BlackMarketSkullCommand(this);
        getCommand("blackmarketskull").setExecutor(blackMarketSkullCommand);
        getCommand("blackmarketskull").setTabCompleter(blackMarketSkullCommand);

        this.wghook = new WorldGuardHook(this);



    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = event.getEntity().getKiller();

        double percentage = getConfig().getDouble("percentage");
        if (new Random().nextDouble() > percentage) {
            if (killer != null) {
                if (this.wghook.isLocApplicable(player.getLocation(), killer)) return;
                ItemStack item = getHead();
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
    }

    public ItemStack getHead() {
        return createSkull(getConfig().getString("pvpTexture"), "pvp");
    }

    public ItemStack createSkull(String texture, String id) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        if (texture.isEmpty()) return head;

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), null);

        // Set the display name and lore of the item
        headMeta.displayName(colorize(getConfig().getString("displayName")));
        headMeta.lore(Collections.singletonList(colorize(getConfig().getString("lore"))));

        profile.getProperties().put("textures", new Property("textures", getConfig().getString("prefix") + texture));

        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
            error.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }
    public Component colorize(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(ChatColor.translateAlternateColorCodes('&', text));
    }
    @Override
    public void onDisable() {
        clogger.sendMessage(ChatColor.DARK_RED + "---------------------------------------");
        clogger.sendMessage(ChatColor.GOLD + "                Goodbye!                   ");
        clogger.sendMessage(ChatColor.DARK_RED + "---------------------------------------");
    }
}
