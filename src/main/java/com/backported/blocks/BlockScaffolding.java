package com.backported.blocks;

import com.backported.init.ModBlocks;
import com.backported.init.ModItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class BlockScaffolding extends Block {
   public static final PropertyInteger DISTANCE = PropertyInteger.func_177719_a("distance", 0, 7);
   public static final PropertyBool BOTTOM = PropertyBool.func_177716_a("bottom");
   protected static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB((double)0.0F, (double)0.875F, (double)0.0F, (double)1.0F, (double)1.0F, (double)1.0F);
   protected static final AxisAlignedBB COLLISION_BOX_BOTTOM = new AxisAlignedBB((double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F, (double)0.125F, (double)1.0F);
   protected static final AxisAlignedBB LEG_BL = new AxisAlignedBB((double)0.0F, (double)0.0F, (double)0.0F, (double)0.125F, (double)1.0F, (double)0.125F);
   protected static final AxisAlignedBB LEG_BR = new AxisAlignedBB((double)0.875F, (double)0.0F, (double)0.0F, (double)1.0F, (double)1.0F, (double)0.125F);
   protected static final AxisAlignedBB LEG_FL = new AxisAlignedBB((double)0.0F, (double)0.0F, (double)0.875F, (double)0.125F, (double)1.0F, (double)1.0F);
   protected static final AxisAlignedBB LEG_FR = new AxisAlignedBB((double)0.875F, (double)0.0F, (double)0.875F, (double)1.0F, (double)1.0F, (double)1.0F);

   public BlockScaffolding() {
      super(Material.field_151575_d);
      this.func_149663_c("scaffolding");
      this.setRegistryName("backported", "scaffolding");
      this.func_149711_c(0.0F);
      this.func_149752_b(0.0F);
      this.func_149672_a(SoundType.field_185848_a);
      this.func_149713_g(0);
      this.func_149647_a(CreativeTabs.field_78031_c);
      this.func_180632_j(this.field_176227_L.func_177621_b().func_177226_a(DISTANCE, 0).func_177226_a(BOTTOM, false));
      ModBlocks.BLOCKS.add(this);
      ModItems.ITEMS.add((new ItemBlock(this)).setRegistryName("scaffolding"));
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer func_180664_k() {
      return BlockRenderLayer.CUTOUT_MIPPED;
   }

   public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
      return 60;
   }

   protected BlockStateContainer func_180661_e() {
      return new BlockStateContainer(this, new IProperty[]{DISTANCE, BOTTOM});
   }

   public IBlockState func_176203_a(int meta) {
      return this.func_176223_P().func_177226_a(DISTANCE, meta & 7).func_177226_a(BOTTOM, (meta & 8) == 0);
   }

   public int func_176201_c(IBlockState state) {
      int meta = (Integer)state.func_177229_b(DISTANCE);
      if ((Boolean)state.func_177229_b(BOTTOM)) {
         meta |= 8;
      }

      return meta;
   }

   public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
      int distance = this.getDistance(world, pos);
      if (distance == 7) {
         this.spawnFallingScaffolding(world, pos, this.func_176223_P());
         return Blocks.field_150350_a.func_176223_P();
      } else {
         return this.func_176223_P().func_177226_a(DISTANCE, distance).func_177226_a(BOTTOM, this.isBottomScaffolding(world, pos));
      }
   }

   public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
      return false;
   }

   public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
      return field_185505_j;
   }

   public RayTraceResult func_180636_a(IBlockState state, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
      RayTraceResult resultTop = this.func_185503_a(pos, start, end, COLLISION_BOX);
      RayTraceResult resultFL = this.func_185503_a(pos, start, end, LEG_FL);
      RayTraceResult resultFR = this.func_185503_a(pos, start, end, LEG_FR);
      RayTraceResult resultBL = this.func_185503_a(pos, start, end, LEG_BL);
      RayTraceResult resultBR = this.func_185503_a(pos, start, end, LEG_BR);
      RayTraceResult resultBottom = this.func_185503_a(pos, start, end, COLLISION_BOX_BOTTOM);
      RayTraceResult closestResult = null;
      double closestDistance = Double.MAX_VALUE;
      RayTraceResult[] results;
      if ((Boolean)state.func_177229_b(BOTTOM)) {
         results = new RayTraceResult[]{resultFL, resultFR, resultBL, resultBR, resultTop, resultBottom};
      } else {
         results = new RayTraceResult[]{resultFL, resultFR, resultBL, resultBR, resultTop};
      }

      for(RayTraceResult result : results) {
         if (result != null) {
            double distance = result.field_72307_f.func_72436_e(start);
            if (distance < closestDistance) {
               closestResult = result;
               closestDistance = distance;
            }
         }
      }

      return closestResult;
   }

   public AxisAlignedBB func_180646_a(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
      return field_185506_k;
   }

   public void func_185477_a(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
      if (entityIn != null) {
         if ((Boolean)state.func_177229_b(BOTTOM)) {
            if (entityIn.field_70163_u >= (double)pos.func_177956_o() + (double)1.0F && !entityIn.func_70093_af() && entityIn.field_70181_x <= (double)0.0F) {
               func_185492_a(pos, entityBox, collidingBoxes, COLLISION_BOX);
            }

            if (entityIn.field_70163_u >= (double)pos.func_177956_o() && entityIn.field_70181_x <= (double)0.0F) {
               func_185492_a(pos, entityBox, collidingBoxes, COLLISION_BOX_BOTTOM);
            }

         } else if (!(entityIn instanceof EntityPlayer) || !((EntityPlayer)entityIn).func_70093_af()) {
            if (entityIn.field_70163_u >= (double)pos.func_177956_o() + (double)1.0F) {
               func_185492_a(pos, entityBox, collidingBoxes, COLLISION_BOX);
            }

         }
      }
   }

   public void func_180634_a(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
      if (entityIn instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entityIn;
         if (player.field_70165_t > (double)pos.func_177958_n() && player.field_70165_t < (double)(pos.func_177958_n() + 1) && player.field_70161_v > (double)pos.func_177952_p() && player.field_70161_v < (double)(pos.func_177952_p() + 1)) {
            if (!worldIn.field_72995_K) {
               if (worldIn.func_82737_E() % 10L == 0L && player.field_70181_x != (double)0.0F) {
                  worldIn.func_184133_a((EntityPlayer)null, player.func_180425_c(), SoundEvents.field_187653_cW, SoundCategory.BLOCKS, 0.15F, 1.0F);
               }

               return;
            }

            if (player.func_70093_af()) {
               player.field_70181_x = -0.1;
               player.field_70143_R = 0.0F;
               player.field_70122_E = true;
            } else if (Keyboard.isKeyDown(Minecraft.func_71410_x().field_71474_y.field_74314_A.func_151463_i())) {
               player.field_70181_x = (double)0.2F;
               player.field_70143_R = 0.0F;
            } else {
               player.field_70181_x *= (double)0.5F;
               player.field_70159_w *= (double)0.5F;
               player.field_70179_y *= (double)0.5F;
               player.field_70143_R = 0.0F;
            }
         }
      }

   }

   public boolean func_185481_k(IBlockState state) {
      return true;
   }

   public boolean func_149662_c(IBlockState state) {
      return false;
   }

   public boolean func_149686_d(IBlockState state) {
      return false;
   }

   public boolean func_149637_q(IBlockState state) {
      return false;
   }

   public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (!worldIn.field_72995_K) {
         int distance = this.getDistance(worldIn, pos);
         if (distance > 7) {
            worldIn.func_175655_b(pos, true);
            return;
         }

         worldIn.func_175656_a(pos, state.func_177226_a(DISTANCE, distance).func_177226_a(BOTTOM, this.isBottomScaffolding(worldIn, pos)));
         if (worldIn.func_180495_p(pos).func_177230_c() == this && !this.canStayAt(worldIn, pos)) {
            worldIn.func_175655_b(pos, true);
         }
      }

   }

   private int getDistance(World world, BlockPos pos) {
      if (world.func_180495_p(pos.func_177977_b()).func_185896_q()) {
         return 0;
      } else {
         int minDistance = 7;

         for(EnumFacing facing : Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.func_177972_a(facing);
            IBlockState neighborState = world.func_180495_p(neighborPos);
            if (neighborState.func_177230_c() == this) {
               minDistance = Math.min(minDistance, (Integer)neighborState.func_177229_b(DISTANCE) + 1);
            }
         }

         return minDistance;
      }
   }

   private boolean isBottomScaffolding(World world, BlockPos pos) {
      return !world.func_180495_p(pos.func_177977_b()).func_185896_q();
   }

   public void func_180633_a(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
      if (!worldIn.field_72995_K) {
         int distance = this.getDistance(worldIn, pos);
         worldIn.func_175656_a(pos, state.func_177226_a(DISTANCE, distance).func_177226_a(BOTTOM, this.isBottomScaffolding(worldIn, pos)));
      }

   }

   private boolean canStayAt(World worldIn, BlockPos pos) {
      if (this.getDistance(worldIn, pos) == 7) {
         return false;
      } else {
         IBlockState belowState = worldIn.func_180495_p(pos.func_177977_b());
         if (belowState.func_185896_q()) {
            return true;
         } else {
            int distance = (Integer)worldIn.func_180495_p(pos).func_177229_b(DISTANCE);

            for(EnumFacing facing : Plane.HORIZONTAL) {
               if (worldIn.func_180495_p(pos.func_177972_a(facing)).func_177230_c() == this && distance > (Integer)worldIn.func_180495_p(pos.func_177972_a(facing)).func_177229_b(DISTANCE)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public boolean func_176196_c(World worldIn, BlockPos pos) {
      return worldIn.func_180495_p(pos.func_177977_b()).func_185896_q() || this.getDistance(worldIn, pos) < 7 || worldIn.func_180495_p(pos.func_177984_a()).func_177230_c() == this;
   }

   public boolean func_180639_a(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      if (worldIn.field_72995_K) {
         return true;
      } else {
         ItemStack heldItem = playerIn.func_184586_b(hand);
         if (!(heldItem.func_77973_b() instanceof ItemBlock)) {
            return false;
         } else {
            Block heldBlock = ((ItemBlock)heldItem.func_77973_b()).func_179223_d();
            if (heldBlock != this) {
               if (heldBlock.func_176196_c(worldIn, pos.func_177972_a(facing))) {
                  worldIn.func_184133_a((EntityPlayer)null, pos.func_177972_a(facing), heldBlock.func_185467_w().func_185841_e(), SoundCategory.BLOCKS, 1.0F, 1.0F);
               }

               return false;
            } else if (worldIn.func_180495_p(pos.func_177977_b()).func_177230_c() == Blocks.field_150350_a && (double)hitY <= (double)0.125F && (double)hitX >= (double)0.125F && (double)hitZ >= (double)0.125F && (double)hitX <= (double)0.875F && (double)hitZ <= (double)0.875F) {
               pos = pos.func_177977_b();
               AxisAlignedBB aabb = (new AxisAlignedBB(pos)).grow(0.1);

               for(EntityFallingBlock entity : worldIn.func_72872_a(EntityFallingBlock.class, aabb)) {
                  if (entity.func_180425_c().equals(pos)) {
                     return true;
                  }
               }

               worldIn.func_175656_a(pos, this.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, playerIn, hand));
               worldIn.func_184133_a((EntityPlayer)null, pos, this.field_149762_H.func_185841_e(), SoundCategory.BLOCKS, 1.0F, 1.0F);
               if (!playerIn.func_184812_l_()) {
                  playerIn.func_184586_b(hand).func_190918_g(1);
               }

               return true;
            } else if (facing != EnumFacing.UP) {
               while(worldIn.func_180495_p(pos.func_177984_a()).func_177230_c() == ModBlocks.SCAFFOLDING) {
                  pos = pos.func_177984_a();
                  if (pos.func_177956_o() > 255) {
                     return false;
                  }
               }

               pos = pos.func_177984_a();
               if (worldIn.func_180495_p(pos).func_177230_c() == Blocks.field_150350_a) {
                  worldIn.func_175656_a(pos, this.func_176223_P());
                  worldIn.func_184133_a((EntityPlayer)null, pos, this.field_149762_H.func_185841_e(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                  if (!playerIn.func_184812_l_()) {
                     playerIn.func_184586_b(hand).func_190918_g(1);
                  }

                  return true;
               } else {
                  return false;
               }
            } else {
               EnumFacing playerFacing = playerIn.func_174811_aO();
               int length = 1;

               while(worldIn.func_180495_p(pos).func_177230_c() == ModBlocks.SCAFFOLDING) {
                  pos = pos.func_177972_a(playerFacing);
                  ++length;
                  if (length == 8) {
                     return true;
                  }
               }

               if (worldIn.func_180495_p(pos).func_177230_c() == Blocks.field_150350_a) {
                  AxisAlignedBB aabb = (new AxisAlignedBB(pos)).grow(0.1);

                  for(EntityFallingBlock entity : worldIn.func_72872_a(EntityFallingBlock.class, aabb)) {
                     if (entity.func_180425_c().equals(pos)) {
                        return true;
                     }
                  }

                  worldIn.func_175656_a(pos, this.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, playerIn, hand));
                  worldIn.func_184133_a((EntityPlayer)null, pos, this.field_149762_H.func_185841_e(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                  if (!playerIn.func_184812_l_()) {
                     playerIn.func_184586_b(hand).func_190918_g(1);
                  }
               }

               return true;
            }
         }
      }
   }

   public void func_180663_b(World worldIn, BlockPos pos, IBlockState state) {
      super.func_180663_b(worldIn, pos, state);

      for(EnumFacing facing : EnumFacing.values()) {
         BlockPos neighborPos = pos.func_177972_a(facing);
         IBlockState neighborState = worldIn.func_180495_p(neighborPos);
         if (neighborState.func_177230_c() == this) {
            worldIn.func_175684_a(neighborPos, this, 1);
         }
      }

   }

   private void spawnFallingScaffolding(World world, BlockPos pos, IBlockState state) {
      if (!world.field_72995_K) {
         EntityFallingBlock fallingBlock = new EntityFallingBlock(world, (double)pos.func_177958_n() + (double)0.5F, (double)pos.func_177956_o(), (double)pos.func_177952_p() + (double)0.5F, state);
         fallingBlock.field_145812_b = 1;
         fallingBlock.func_189654_d(false);
         fallingBlock.func_145806_a(false);
         world.func_72838_d(fallingBlock);
      }

   }
}

