package org.maski.pogic;

import java.io.File;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

import net.minecraft.server.*;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Pogic extends JavaPlugin {
	private final PogicPlayerListener playerListener = new PogicPlayerListener(this);
	private final PogicEntityListener entityListener = new PogicEntityListener(this);
	
    private static Logger l = Logger.getLogger("Minecraft.PogicPlugin");

    private ModManifest clientMods;
    private Map<Integer, String> entityMap = new HashMap<Integer, String>();

    private PogicHttpServer httpServer;
    
    public void onDisable() {
    	this.httpServer.stop();
    }

    public void onEnable() {
        l.info("Pogic on!");

        // FIXME
        try {
			clientMods = new ModManifest(new URL("http://localhost:8081/"));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
        catalogClientJars();

        this.httpServer = new PogicHttpServer(getClientModsFolder(), 8081);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Normal, this);

    }
    
    private File getClientModsFolder() {
    	return new File(this.getDataFolder(), "client");
    }
    
    private void catalogClientJars() {
        // look at *.jar found in my data directory / client
        // make sha2-256 of each
        // store in data structure
        if (!getClientModsFolder().exists()) {
        	getClientModsFolder().mkdirs();
        }
        for (File clientModJar : getClientModsFolder().listFiles()) {
        	if (!clientModJar.getName().endsWith(".jar")) continue;
        	clientMods.add(clientModJar);
        }
    }
    
	public int addEntity(Plugin plugin, String name, Class<? extends Entity> entity) {
		// FIXME keep my own list so I can remove all entity's this plugin has added when we disable the plugin
		int entityId = maxEntityId() + 1;
    	addEntityToEntityTypes(entityId, name, entity);
    	entityMap.put(entityId, name);
    	return entityId;
	}

	@SuppressWarnings({ "unchecked" })
	private int maxEntityId() {
		Map<Integer, Class<?>> entityIdsToClass;
		try {
			Field field = EntityTypes.class.getDeclaredField("c");
			field.setAccessible(true);
			entityIdsToClass = (Map<Integer, Class<?>>)field.get(null);
		} catch (IllegalArgumentException e1) {
			throw new RuntimeException(e1);
		} catch (SecurityException e1) {
			throw new RuntimeException(e1);
		} catch (IllegalAccessException e1) {
			throw new RuntimeException(e1);
		} catch (NoSuchFieldException e1) {
			throw new RuntimeException(e1);
		}
		return Collections.max(entityIdsToClass.keySet()).intValue();
	}

    @SuppressWarnings({ "rawtypes" })
	private void addEntityToEntityTypes(int entityId, String string, Class<? extends Entity> entity) {
		Class[] params = {Class.class, String.class, Integer.TYPE};
		Method method;
		try {
			method = EntityTypes.class.getDeclaredMethod("a", params);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		method.setAccessible(true);
		try {
			method.invoke(EntityTypes.class, entity, string, entityId);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void removeEntity(String string) {
	}
	
	public void spawnSomethingNew(Player player) {
		try {
		Block block = player.getTargetBlock(null, 20);
		Location loc = block.getLocation();
		int y = loc.getWorld().getHighestBlockYAt(loc);
		loc.setY(y);
		System.out.println("spawnSomethingNew " + loc);

		CraftWorld cw = (CraftWorld)player.getWorld();
		Entity newThing = EntityTypes.a("Creeper2", cw.getHandle());
		newThing.b(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		cw.getHandle().a(newThing);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

    private class PogicPlayerListener extends PlayerListener {
        private final Pogic plugin;

        public PogicPlayerListener(Pogic instance) {
            plugin = instance;
        }

        @Override
        public void onPlayerJoin(final PlayerEvent event) {
            System.out.println(event.getPlayer().getName() + " joined the server! :D");
        	((CraftPlayer)event.getPlayer()).getHandle().a.b(new Packet200Pogic(plugin.clientMods, plugin.entityMap));

        	Runnable task = new Runnable() { public void run() { plugin.spawnSomethingNew(event.getPlayer()); }};
        	getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 200);
        }
    }

    private class PogicEntityListener extends EntityListener {
        private final Pogic plugin;

        public PogicEntityListener(Pogic instance) {
            plugin = instance;
        }

		@Override
		public void onCreatureSpawn(CreatureSpawnEvent event) {
			//System.out.println("preventing the spawn of " + event.getEntity());
			event.setCancelled(true);
		}
    	
        
    	
    }

}
