package com.backported.blocks;

import com.backported.init.ModBlocks;
import com.backported.init.ModItems;
import java.lang.reflect.Field;
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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
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
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockScaffolding extends Block {
    public static final PropertyInteger DISTANCE = PropertyInteger.create("distance", 0, 7);
    public static final PropertyBool BOTTOM = PropertyBool.create("bottom");
    
    protected static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.0F, 0.875F, 0.0F, 1.0F, 1.0F, 1.0F);
    protected static final AxisAlignedBB COLLISION_BOX_BOTTOM = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
    protected static final AxisAlignedBB LEG_BL = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 0.125F, 1.0F, 0.125F);
    protected static final AxisAlignedBB LEG_BR = new AxisAlignedBB(0.875F, 0.0F, 0.0F, 1.0F, 1.0F, 0.125F);
    protected static final AxisAlignedBB LEG_FL = new AxisAlignedBB(0.0F, 0.0F, 0.875F, 0.125F, 1.0F, 1.0F);
    protected static final AxisAlignedBB LEG_FR = new AxisAlignedBB(0.875F, 0.0F, 0.875F, 1.0F, 1.0F, 1.0F);

    // 反射获取 EntityLivingBase 内部的 isJumping 字段，用于安全检测玩家是否按下跳跃
    private static final Field isJumpingField = ReflectionHelper.findField(EntityLivingBase.class, "isJumping", "field_70703_bu");

    public BlockScaffolding() {
        super(Material.WOOD);
        this.setTranslationKey("scaffolding");
        this.setRegistryName("backported", "scaffolding");
        this.setHardness(0.0F);
        this.setResistance(0.0F);
        this.setSoundType(SoundType.WOOD);
        this.setLightOpacity(0);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(DISTANCE, 0).withProperty(BOTTOM, false));
        
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add((new ItemBlock(this)).setRegistryName("scaffolding"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 60;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DISTANCE, BOTTOM);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(DISTANCE, meta & 7).withProperty(BOTTOM, (meta & 8) == 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(DISTANCE);
        if (state.getValue(BOTTOM)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        int distance = this.getDistance(world, pos);
        if (distance == 7) {
            // 规避死循环危险：如果超过延伸距离，放置时直接以物品形式掉落，而不是产生会转化为方块的沙子实体
            if (!world.isRemote) {
                world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(this)));
            }
            return Blocks.AIR.getDefaultState();
        } else {
            return this.getDefaultState().withProperty(DISTANCE, distance).withProperty(BOTTOM, this.isBottomScaffolding(world, pos));
        }
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        RayTraceResult resultTop = this.rayTrace(pos, start, end, COLLISION_BOX);
        RayTraceResult resultFL = this.rayTrace(pos, start, end, LEG_FL);
        RayTraceResult resultFR = this.rayTrace(pos, start, end, LEG_FR);
        RayTraceResult resultBL = this.rayTrace(pos, start, end, LEG_BL);
        RayTraceResult resultBR = this.rayTrace(pos, start, end, LEG_BR);
        RayTraceResult resultBottom = this.rayTrace(pos, start, end, COLLISION_BOX_BOTTOM);
        RayTraceResult closestResult = null;
        double closestDistance = Double.MAX_VALUE;
        RayTraceResult[] results;

        if (state.getValue(BOTTOM)) {
            results = new RayTraceResult[]{resultFL, resultFR, resultBL, resultBR, resultTop, resultBottom};
        } else {
            results = new RayTraceResult[]{resultFL, resultFR, resultBL, resultBR, resultTop};
        }

        for (RayTraceResult result : results) {
            if (result != null) {
                double distance = result.hitVec.squareDistanceTo(start);
                if (distance < closestDistance) {
                    closestResult = result;
                    closestDistance = distance;
                }
            }
        }
        return closestResult;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (entityIn != null) {
            if (state.getValue(BOTTOM)) {
                if (entityIn.posY >= (double)pos.getY() + 1.0F && !entityIn.isSneaking() && entityIn.motionY <= 0.0F) {
                    super.addCollisionBoxToList(pos, entityBox, collidingBoxes, COLLISION_BOX); // 修复：显式调用父类方法
                }
                if (entityIn.posY >= (double)pos.getY() && entityIn.motionY <= 0.0F) {
                    super.addCollisionBoxToList(pos, entityBox, collidingBoxes, COLLISION_BOX_BOTTOM);
                }
            } else if (!(entityIn instanceof EntityPlayer) || !((EntityPlayer)entityIn).isSneaking()) {
                if (entityIn.posY >= (double)pos.getY() + 1.0F) {
                    super.addCollisionBoxToList(pos, entityBox, collidingBoxes, COLLISION_BOX);
                }
            }
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (entityIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entityIn;
            if (player.posX > (double)pos.getX() && player.posX < (double)(pos.getX() + 1) && player.posZ > (double)pos.getZ() && player.posZ < (double)(pos.getZ() + 1)) {
                if (!worldIn.isRemote) {
                    if (worldIn.getTotalWorldTime() % 10L == 0L && player.motionY != 0.0F) {
                        worldIn.playSound(null, player.getPosition(), SoundEvents.BLOCK_WOOD_STEP, SoundCategory.BLOCKS, 0.15F, 1.0F);
                    }
                }

                // 修复：替换 Keyboard 纯客户端检测，使用反射安全获取双端通用的跳跃状态字段
                boolean isJumping = false;
                try {
                    isJumping = isJumpingField.getBoolean(player);
                } catch (IllegalAccessException e) {
                    // 反射失败时的降级方案
                }

                if (player.isSneaking()) {
                    player.motionY = -0.15;
                    player.fallDistance = 0.0F;
                    player.onGround = true;
                } else if (isJumping) {
                    player.motionY = 0.2F;
                    player.fallDistance = 0.0F;
                } else {
                    // 处于脚手架内部时提供轻微浮空与慢速下滑阻力
                    player.motionY *= 0.5F;
                    if (player.motionY < -0.15) {
                        player.motionY = -0.15;
                    }
                    player.motionX *= 0.5F;
                    player.motionZ *= 0.5F;
                    player.fallDistance = 0.0F;
                }
            }
        }
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            int distance = this.getDistance(worldIn, pos);
            if (distance > 7) {
                worldIn.destroyBlock(pos, true);
                return;
            }

            worldIn.setBlockState(pos, state.withProperty(DISTANCE, distance).withProperty(BOTTOM, this.isBottomScaffolding(worldIn, pos)));
            if (worldIn.getBlockState(pos).getBlock() == this && !this.canStayAt(worldIn, pos)) {
                worldIn.destroyBlock(pos, true);
            }
        }
    }

    private int getDistance(World world, BlockPos pos) {
        if (world.getBlockState(pos.down()).isFullBlock() || world.getBlockState(pos.down()).getBlock() == this) {
            // 如果下方是实心方块或者是另一个脚手架，垂直链上的垂直距离为0
            if (world.getBlockState(pos.down()).getBlock() == this) {
                return world.getBlockState(pos.down()).getValue(DISTANCE);
            }
            return 0;
        } else {
            int minDistance = 7;
            for (EnumFacing facing : Plane.HORIZONTAL) {
                BlockPos neighborPos = pos.offset(facing);
                IBlockState neighborState = world.getBlockState(neighborPos);
                if (neighborState.getBlock() == this) {
                    minDistance = Math.min(minDistance, neighborState.getValue(DISTANCE) + 1);
                }
            }
            return minDistance;
        }
    }

    private boolean isBottomScaffolding(World world, BlockPos pos) {
        return !world.getBlockState(pos.down()).isFullBlock() && world.getBlockState(pos.down()).getBlock() != this;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!worldIn.isRemote) {
            int distance = this.getDistance(worldIn, pos);
            worldIn.setBlockState(pos, state.withProperty(DISTANCE, distance).withProperty(BOTTOM, this.isBottomScaffolding(worldIn, pos)));
        }
    }

    private boolean canStayAt(World worldIn, BlockPos pos) {
        if (this.getDistance(worldIn, pos) == 7) {
            return false;
        } else {
            IBlockState belowState = worldIn.getBlockState(pos.down());
            if (belowState.isFullBlock() || belowState.getBlock() == this) {
                return true;
            } else {
                int distance = worldIn.getBlockState(pos).getValue(DISTANCE);
                for (EnumFacing facing : Plane.HORIZONTAL) {
                    if (worldIn.getBlockState(pos.offset(facing)).getBlock() == this && distance > worldIn.getBlockState(pos.offset(facing)).getValue(DISTANCE)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).isFullBlock() || this.getDistance(worldIn, pos) < 7 || worldIn.getBlockState(pos.up()).getBlock() == this;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            ItemStack heldItem = playerIn.getHeldItem(hand);
            if (!(heldItem.getItem() instanceof ItemBlock)) {
                return false;
            } else {
                Block heldBlock = ((ItemBlock)heldItem.getItem()).getBlock();
                if (heldBlock != this) {
                    if (heldBlock.canPlaceBlockAt(worldIn, pos.offset(facing))) {
                        worldIn.playSound(null, pos.offset(facing), heldBlock.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                    return false;
                } else if (worldIn.getBlockState(pos.down()).getBlock() == Blocks.AIR && hitY <= 0.125F && hitX >= 0.125F && hitZ >= 0.125F && hitX <= 0.875F && hitZ <= 0.875F) {
                    pos = pos.down();
                    AxisAlignedBB aabb = (new AxisAlignedBB(pos)).grow(0.1);

                    for (EntityFallingBlock entity : worldIn.getEntitiesWithinAABB(EntityFallingBlock.class, aabb)) {
                        if (entity.getPosition().equals(pos)) {
                            return true;
                        }
                    }

                    worldIn.setBlockState(pos, this.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, playerIn, hand));
                    worldIn.playSound(null, pos, this.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                    if (!playerIn.isCreative()) {
                        playerIn.getHeldItem(hand).shrink(1);
                    }
                    return true;
                } else if (facing != EnumFacing.UP) {
                    // 修复：将引用的 ModBlocks.SCAFFOLDING 替换为通用的 this，保证动态注册正确性
                    while (worldIn.getBlockState(pos.up()).getBlock() == this) {
                        pos = pos.up();
                        if (pos.getY() > 255) {
                            return false;
                        }
                    }

                    pos = pos.up();
                    if (worldIn.getBlockState(pos).getBlock() == Blocks.AIR) {
                        worldIn.setBlockState(pos, this.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, playerIn, hand));
                        worldIn.playSound(null, pos, this.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                        if (!playerIn.isCreative()) {
                            playerIn.getHeldItem(hand).shrink(1);
                        }
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    EnumFacing playerFacing = playerIn.getHorizontalFacing();
                    int length = 1;

                    // 修复：替换为 this
                    while (worldIn.getBlockState(pos).getBlock() == this) {
                        pos = pos.offset(playerFacing);
                        length++;
                        if (length == 8) {
                            return true;
                        }
                    }

                    if (worldIn.getBlockState(pos).getBlock() == Blocks.AIR) {
                        AxisAlignedBB aabb = (new AxisAlignedBB(pos)).grow(0.1);

                        for (EntityFallingBlock entity : worldIn.getEntitiesWithinAABB(EntityFallingBlock.class, aabb)) {
                            if (entity.getPosition().equals(pos)) {
                                return true;
                            }
                        }

                        worldIn.setBlockState(pos, this.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, playerIn, hand));
                        worldIn.playSound(null, pos, this.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                        if (!playerIn.isCreative()) {
                            playerIn.getHeldItem(hand).shrink(1);
                        }
                    }
                    return true;
                }
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighborPos = pos.offset(facing);
            IBlockState neighborState = worldIn.getBlockState(neighborPos);
            if (neighborState.getBlock() == this) {
                worldIn.notifyNeighborsOfStateChange(neighborPos, this, true);
            }
        }
    }

    private void spawnFallingScaffolding(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            EntityFallingBlock fallingBlock = new EntityFallingBlock(world, (double)pos.getX() + 0.5F, (double)pos.getY(), (double)pos.getZ() + 0.5F, state);
            fallingBlock.fallTime = 1;
            fallingBlock.setHurtEntities(false);
            world.spawnEntity(fallingBlock);
        }
    }
}
