package com.backported.util.handlers;

import com.backported.init.ModBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.IFuelHandler;

public class FuelHandler implements IFuelHandler {
   public int getBurnTime(ItemStack fuel) {
      return fuel.func_77973_b() == Item.func_150898_a(ModBlocks.SCAFFOLDING) ? 50 : 0;
   }
}

