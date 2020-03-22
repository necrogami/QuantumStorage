package net.gigabit101.quantumstorage.blocks;

import net.gigabit101.quantumstorage.QuantumStorage;
import net.gigabit101.quantumstorage.tiles.TileTank;
import net.gigabit101.quantumstorage.tiles.chests.TileChestGold;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockTank extends ContainerBlock
{
    private static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    public BlockTank()
    {
        super(Properties.create(Material.IRON).hardnessAndResistance(2.0F));
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn)
    {
        return new TileTank();
    }
    
    @Nonnull
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }
    
    @Override
    public ActionResultType onBlockActivated(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult traceResult)
    {
         FluidUtil.interactWithFluidHandler(player, hand, world, pos, traceResult.getFace());
        
//        if (!world.isRemote)
//        {
//            NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) world.getTileEntity(pos), pos);
//            return true;
//        }
        return ActionResultType.FAIL;
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }
    
    @Override
    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity tile, ItemStack stack)
    {
        TileTank tileEntity = (TileTank) tile;

        float xOffset = world.rand.nextFloat() * 0.8F + 0.1F;
        float yOffset = world.rand.nextFloat() * 0.8F + 0.1F;
        float zOffset = world.rand.nextFloat() * 0.8F + 0.1F;

        ItemStack stacknbt = (tileEntity).getDropWithNBT();
        int amountToDrop = Math.min(world.rand.nextInt(21) + 10, stacknbt.getCount());

        ItemEntity entityitem = new ItemEntity(world, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, stacknbt.split(amountToDrop));
        world.addEntity(entityitem);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, entity, stack);
        TileTank qsu = (TileTank) world.getTileEntity(pos);

        if (stack.hasTag())
        {
            qsu.readFromNBTWithoutCoords(stack.getTag().getCompound("tileEntity"));
        }
        world.notifyBlockUpdate(pos, state, state, 3);
    }
    
    @Override
    public void addInformation(ItemStack p_190948_1_, @Nullable IBlockReader p_190948_2_, List<ITextComponent> tooltip, ITooltipFlag p_190948_4_)
    {
        tooltip.add(new TranslationTextComponent(TextFormatting.RED + "WIP"));
    }
}
