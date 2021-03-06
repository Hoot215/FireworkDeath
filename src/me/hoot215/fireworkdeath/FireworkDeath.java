/*
 * Display fireworks where players die.
 * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.hoot215.fireworkdeath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class FireworkDeath extends JavaPlugin implements Listener
  {
    private static final Color[] COLOURS = {Color.AQUA, Color.BLACK,
        Color.BLUE, Color.FUCHSIA, Color.GRAY, Color.GREEN, Color.LIME,
        Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE,
        Color.RED, Color.SILVER, Color.TEAL, Color.WHITE, Color.YELLOW};
    private Method world_getHandle = null;
    private Method nms_world_broadcastEntityEffect = null;
    private Method firework_getHandle = null;
    
    private static Method getMethod (Class<?> clazz, String method)
      {
        for (Method m : clazz.getMethods())
          {
            if (m.getName().equals(method))
              {
                return m;
              }
          }
        return null;
      }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath (PlayerDeathEvent event)
      {
        if (event.getEntity().hasPermission("fireworkdeath.firework"))
          {
            boolean flicker = new Random().nextBoolean();
            boolean trail = new Random().nextBoolean();
            FireworkEffect.Type type =
                FireworkEffect.Type.values()[new Random()
                    .nextInt(FireworkEffect.Type.values().length)];
            List<Color> colours = new ArrayList<Color>();
            List<Color> fades = new ArrayList<Color>();
            for (int i = 0; i < 3; i++)
              {
                colours.add(COLOURS[new Random().nextInt(COLOURS.length)]);
                fades.add(COLOURS[new Random().nextInt(COLOURS.length)]);
              }
            FireworkEffect effect =
                FireworkEffect.builder().flicker(flicker).trail(trail)
                    .with(type).withColor(colours).withFade(fades).build();
            World world = event.getEntity().getWorld();
            Firework firework =
                (Firework) world.spawnEntity(event.getEntity().getLocation(),
                    EntityType.FIREWORK);
            // Credit to codename_B
            Object nms_world = null;
            Object nms_firework = null;
            if (world_getHandle == null)
              {
                // get the methods of the craftbukkit objects
                world_getHandle = getMethod(world.getClass(), "getHandle");
                firework_getHandle =
                    getMethod(firework.getClass(), "getHandle");
              }
            try
              {
                nms_world = world_getHandle.invoke(world, (Object[]) null);
                nms_firework =
                    firework_getHandle.invoke(firework, (Object[]) null);
              }
            catch (InvocationTargetException e)
              {
                e.printStackTrace();
              }
            catch (IllegalAccessException e)
              {
                e.printStackTrace();
              }
            if (nms_world_broadcastEntityEffect == null)
              {
                nms_world_broadcastEntityEffect =
                    getMethod(nms_world.getClass(), "broadcastEntityEffect");
              }
            FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
            data.clearEffects();
            data.setPower(1);
            data.addEffect(effect);
            firework.setFireworkMeta(data);
            try
              {
                nms_world_broadcastEntityEffect.invoke(nms_world, new Object[] {
                    nms_firework, (byte) 17});
              }
            catch (IllegalAccessException e)
              {
                e.printStackTrace();
              }
            catch (InvocationTargetException e)
              {
                e.printStackTrace();
              }
            firework.remove();
          }
      }
    
    @Override
    public void onDisable ()
      {
        this.getLogger().info("Is now disabled");
      }
    
    @Override
    public void onEnable ()
      {
        this.getServer().getPluginManager().registerEvents(this, this);
        
        this.getLogger().info("Is now enabled");
      }
  }
