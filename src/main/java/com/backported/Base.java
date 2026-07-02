package com.backported;

import com.backported.proxy.CommonProxy;
import com.backported.util.handlers.FuelHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(
   modid = "backported",
   name = "Scaffolding Backported",
   version = "1.0.0",
   acceptedMinecraftVersions = "[1.12.2]"
)
public class Base {
   @Instance
   public static Base instance;
   @SidedProxy(
      clientSide = "com.backported.proxy.ClientProxy",
      serverSide = "com.backported.proxy.CommonProxy"
   )
   public static CommonProxy proxy;
   private static Logger logger;

   @EventHandler
   public static void preInit(FMLPreInitializationEvent event) {
      logger = event.getModLog();
   }

   @EventHandler
   public static void init(FMLInitializationEvent event) {
      GameRegistry.registerFuelHandler(new FuelHandler());
   }
}

