package com.backported.util.handlers;

import com.backported.Base;
import com.backported.init.ModBlocks;
import com.backported.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

@EventBusSubscriber
public class RegistryHandler {
   @SubscribeEvent
   public static void onItemRegister(RegistryEvent.Register<Item> event) {
      event.getRegistry().registerAll((IForgeRegistryEntry[])ModItems.ITEMS.toArray(new Item[0]));
   }

   @SubscribeEvent
   public static void onBlockRegister(RegistryEvent.Register<Block> event) {
      event.getRegistry().registerAll((IForgeRegistryEntry[])ModBlocks.BLOCKS.toArray(new Block[0]));
   }

   @SubscribeEvent
   public static void onModelRegister(ModelRegistryEvent event) {
      for(Item item : ModItems.ITEMS) {
         Base.proxy.registerItemRenderer(item, 0, "inventory");
      }

   }
}
