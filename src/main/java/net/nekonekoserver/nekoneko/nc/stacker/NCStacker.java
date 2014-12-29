package net.nekonekoserver.nekoneko.nc.stacker;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import java.util.List;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class NCStacker extends JavaPlugin implements Listener {
   
    private final PluginManager plm = getServer().getPluginManager();
    private List<String> allow;
    private NoCheatPlus NCP;
    @Override
    public void onEnable() {
        super.onEnable();
        
        saveDefaultConfig();
        
        plm.registerEvents(this, this);
        allow = getConfig().getStringList("AllowWorlds");
        if (plm.getPlugin("NoCheatPlus") != null) {
            NCP = (NoCheatPlus) getServer().getPluginManager().getPlugin("NoCheatPlus");
        } else {
            NCP = null;
        }
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
    }
    
    @EventHandler
    public void onRideEntity(PlayerInteractEntityEvent event) {
        if (!event.isCancelled()) {
            Player stacker = event.getPlayer();
            if (stacker.hasPermission("ncstacker.use")) { 
                if (stacker.isSneaking()) {
                    return;
                }
                for (String allowWorld : allow) {
                    if (stacker.getWorld().getName().equals(allowWorld)) {
                        if (stacker.getVehicle() == null) {
                            Entity ride = event.getRightClicked();
                            if (ride != null) {
                                if ((ride instanceof LivingEntity)) {
                                    if (ride instanceof LivingEntity) {
                                        if (((LivingEntity)ride).isCustomNameVisible()) {
                                            return;
                                        }
                                    }
                                    while (ride.getVehicle() != null) {
                                        ride = ride.getVehicle();
                                    }
                                    if (ride.equals(stacker)) {
                                        return;
                                    }
                                    Entity top = stacker;
                                    while (top.getPassenger() != null) {
                                        top = top.getPassenger();
                                    }
                                    top.setPassenger(ride);
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void ThrowEntity(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }
        final Player player = event.getPlayer();
        if (player.getVehicle() == null) {
            final Entity entity = player.getPassenger();
            if (entity != null) {
                player.eject();
                final Entity entityStack = entity.getPassenger();
                if (entityStack != null) {
                    entity.eject();
                    entityStack.leaveVehicle();
                    getServer().getScheduler().scheduleSyncDelayedTask(this, new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.setPassenger(entityStack);
                        }
                    }, 2L);
                }
                NCPExemptionManager.exemptPermanently(entity.getEntityId(), CheckType.MOVING_SURVIVALFLY);
                velocity(entity, player.getLocation().getDirection(), 1.8D, false, 0.0D, 0.3D, 2.0D, false);
                NCPExemptionManager.unexempt(entity.getEntityId(), CheckType.MOVING_SURVIVALFLY);
            }
        }
    }

    private void velocity(Entity entity, Vector vector, double multiply, boolean set, double base, double add, double max, boolean boost) {
        if ((Double.isNaN(vector.getX())) || (Double.isNaN(vector.getY())) || (Double.isNaN(vector.getZ())) || (vector.length() == 0.0D)) {
            return;
        }

        if (set) {
            vector.setY(base);
        }

        vector.normalize();
        vector.multiply(multiply);

        vector.setY(vector.getY() + add);

        if (vector.getY() > max) {
          vector.setY(max);
        }
        if ((boost) && (isGrounded(entity))) {
          vector.setY(vector.getY() + 0.2D);
        }

        entity.setFallDistance(0.0F);
        entity.setVelocity(vector);
    }
    
    private boolean isGrounded(Entity entity) {
        if (entity instanceof CraftEntity) {
            return ((CraftEntity)entity).getHandle().onGround;
        } else {
            return false;
        }
    }
}
