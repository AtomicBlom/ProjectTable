package com.github.atomicblom.projecttable.block;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.gui.ModGUIs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Created by codew on 4/01/2016.
 */
public class ProjectTableBlock extends Block
{

    public ProjectTableBlock() {

        super(Material.WOOD);
        setHarvestLevel("axe", 1);

        setDefaultState(getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH));

    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, BlockHorizontal.FACING);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        final int orientation = (MathHelper.floor(placer.rotationYaw * 4.0f / 360.0f + 0.5)) & 3;
        final EnumFacing horizontal = EnumFacing.byHorizontalIndex(orientation);
        final IBlockState newState = worldIn.getBlockState(pos)
                .withProperty(BlockHorizontal.FACING, horizontal);

        worldIn.setBlockState(pos, newState, 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int meta = 0;
        EnumFacing value = state.getValue(BlockHorizontal.FACING);
        if (value == EnumFacing.UP || value == EnumFacing.DOWN) {
            value = EnumFacing.NORTH;
        }
        meta |= value.ordinal() - 2;
        return meta;
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta)
    {
        return super.getStateFromMeta(meta)
                .withProperty(BlockHorizontal.FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        playerIn.openGui(ProjectTableMod.instance, ModGUIs.PROJECT_TABLE.getID(), worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
}