package ee.vinku.black;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.ess3.api.events.teleport.PreTeleportEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WorldGuardHook implements Listener {

    private final BlackMarketSkull plugin;
    private StateFlag BLOCK_PLUGIN_TP = new StateFlag("block-plugin-tp", false);
    private StateFlag MARKET_SKULL = new StateFlag("market-skull", false);
    // WorldGuard.getInstance()
    public WorldGuardHook(BlackMarketSkull plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.registerFlag();
    }

    /**
     * Checks if location is applicable for skull drop
     * @param loc location to check
     * @param player player used in testing
     * @return whether the location is applicable for skull drop
     */
    public boolean isLocApplicable(Location loc, Player player) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return query.testState(BukkitAdapter.adapt(loc),
                WorldGuardPlugin.inst().wrapPlayer(player),
                this.MARKET_SKULL);
    }

    private void registerFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(this.BLOCK_PLUGIN_TP);
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("block-plugin-tp");
            if (existing instanceof StateFlag) {
                this.BLOCK_PLUGIN_TP = (StateFlag) existing;
            } else {
                throw new RuntimeException("This should not have happend!");
            }
        }
        try {
            registry.register(this.MARKET_SKULL);
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("market-skull");
            if (existing instanceof StateFlag) {
                this.MARKET_SKULL = (StateFlag) existing;
            } else {
                throw new RuntimeException("This should not have happend!");
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN
                || event.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            if (query.testState(BukkitAdapter.adapt(event.getTo()),
                    WorldGuardPlugin.inst().wrapPlayer(event.getPlayer()),
                    this.BLOCK_PLUGIN_TP)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPrePlayerTeleport(PreTeleportEvent e) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        if (query.testState(BukkitAdapter.adapt(e.getTarget().getLocation()),
                WorldGuardPlugin.inst().wrapPlayer(e.getTeleporter().getBase()),
                this.BLOCK_PLUGIN_TP)) {
            e.setCancelled(true);
        }
    }
}
