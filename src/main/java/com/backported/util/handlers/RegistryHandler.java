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

@EventBusSubscriber
public class RegistryHandler {

   @SubscribeEvent
   public static void onItemRegister(RegistryEvent.Register<Item> event) {
      // 直接传入 Item 数组，不需要强制转换
      event.getRegistry().registerAll(ModItems.ITEMS.toArray(new Item[0]));
   }

   @SubscribeEvent
   public static void onBlockRegister(RegistryEvent.Register<Block> event) {
      // 直接传入 Block 数组，不需要强制转换
      event.getRegistry().registerAll(ModBlocks.BLOCKS.toArray(new Block[0]));
   }

   @SubscribeEvent
   public static void onModelRegister(ModelRegistryEvent event) {
      for (Item item : ModItems.ITEMS) {
         Base.proxy.registerItemRenderer(item, 0, "inventory");
      }
   }

}
