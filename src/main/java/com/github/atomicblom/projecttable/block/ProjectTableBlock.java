package com.github.atomicblom.projecttable.block;

import com.github.atomicblom.projecttable.inventory.ProjectTableContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Created by codew on 4/01/2016.
 */
public class ProjectTableBlock extends HorizontalBlock
{
    private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("projecttable:container.projecttable");

    public ProjectTableBlock(Properties properties) {
        super(properties);
        setDefaultState(getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));

    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    final VoxelShape TableTop = blockBenchVoxelShape(0.5, 11, 0.5, 15, 2, 15);
    final VoxelShape LegA = blockBenchVoxelShape(12.5, 0, 1.5, 2,  11, 2);
    final VoxelShape LegB = blockBenchVoxelShape(1.5, 0, 1.5, 2, 11, 2);
    final VoxelShape LegC = blockBenchVoxelShape(1.5, 0, 12.5, 2, 11, 2);
    final VoxelShape LegD = blockBenchVoxelShape(12.5, 0, 12.5, 2, 11, 2);
    final VoxelShape AABB = VoxelShapes.or(TableTop, LegA, LegB, LegC, LegD);

    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return AABB;
    }

    private VoxelShape blockBenchVoxelShape(double x, double y, double z, double width, double height, double depth) {
        return Block.makeCuboidShape(x, y, z, x + width, y + height, z + depth);
    }

    @Override
    @Deprecated
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return ActionResultType.SUCCESS;
        } else {
            player.openContainer(state.getContainer(worldIn, pos));
            //player.addStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
            //FIXME: Create Project Table Stat
            return ActionResultType.CONSUME;
        }
    }

    @Override
    @Deprecated
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        return new SimpleNamedContainerProvider((id, playerInventory, playerEntity) -> new ProjectTableContainer(id, playerInventory), CONTAINER_NAME);
    }
}