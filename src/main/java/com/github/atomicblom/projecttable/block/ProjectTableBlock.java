package com.github.atomicblom.projecttable.block;

import com.github.atomicblom.projecttable.inventory.ProjectTableContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/**
 * Created by codew on 4/01/2016.
 */
public class ProjectTableBlock extends HorizontalDirectionalBlock
{
    private static final Component CONTAINER_NAME = MutableComponent.create(new TranslatableContents("projecttable:container.projecttable"));

    public ProjectTableBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH));

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getNearestLookingDirection().getOpposite());
    }

    final VoxelShape TableTop = blockBenchVoxelShape(0.5, 11, 0.5, 15, 2, 15);
    final VoxelShape LegA = blockBenchVoxelShape(12.5, 0, 1.5, 2,  11, 2);
    final VoxelShape LegB = blockBenchVoxelShape(1.5, 0, 1.5, 2, 11, 2);
    final VoxelShape LegC = blockBenchVoxelShape(1.5, 0, 12.5, 2, 11, 2);
    final VoxelShape LegD = blockBenchVoxelShape(12.5, 0, 12.5, 2, 11, 2);
    final VoxelShape AABB = Shapes.or(TableTop, LegA, LegB, LegC, LegD);

    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    private VoxelShape blockBenchVoxelShape(double x, double y, double z, double width, double height, double depth) {
        return Shapes.box(x / 16, y/ 16, z/ 16, (x + width) / 16, (y + height) / 16, (z + depth) / 16);
    }

    @Override
    @Deprecated
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(worldIn, pos));
            //player.addStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
            //FIXME: Create Project Table Stat
            return InteractionResult.CONSUME;
        }
    }

    @Override
    @Deprecated
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        return new SimpleMenuProvider((id, playerInventory, playerEntity) -> new ProjectTableContainer(id, playerInventory), CONTAINER_NAME);
    }
}