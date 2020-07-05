package net.gigabit101.quantumstorage.tiles;

import net.gigabit101.quantumstorage.util.inventory.ItemUtils;
import net.gigabit101.quantumstorage.containers.ContainerQSU;
import net.gigabit101.quantumstorage.init.QSBlocks;
import net.gigabit101.quantumstorage.inventory.DsuInventoryHandler;
import net.gigabit101.quantumstorage.network.VanillaPacketDispatcher;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Gigabit101 on 03/04/2017.
 */
public class TileQsu extends TileEntity implements INamedContainerProvider, ITickableTileEntity
{
    int INPUT = 0;
    int STORAGE = 1;
    int OUTPUT = 2;
    
    public DsuInventoryHandler inventory = new DsuInventoryHandler();
    
    public TileQsu()
    {
        super(QSBlocks.QSU_TILE.get());
    }
    
    @Override
    public void onLoad()
    {
        VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
    }
    
    @Override
    public void tick()
    {
        try
        {
            if (!inventory.getStackInSlot(INPUT).isEmpty())
            {
                if (inventory.getStackInSlot(STORAGE).isEmpty() && (inventory.getStackInSlot(OUTPUT).isEmpty() || ItemUtils.isItemEqual(inventory.getStackInSlot(INPUT), inventory.getStackInSlot(OUTPUT), true)))
                {
                    inventory.setStackInSlot(STORAGE, inventory.getStackInSlot(INPUT).copy());
                    inventory.setStackInSlot(INPUT, ItemStack.EMPTY);
                } else if (!inventory.getStackInSlot(STORAGE).isEmpty() && ItemUtils.isItemEqual(inventory.getStackInSlot(INPUT), inventory.getStackInSlot(STORAGE), true))
                {
                    inventory.getStackInSlot(STORAGE).grow(inventory.getStackInSlot(INPUT).getCount());
                    inventory.setStackInSlot(INPUT, ItemStack.EMPTY);
                }
            }
        
            if (!inventory.getStackInSlot(STORAGE).isEmpty())
            {
                int size = inventory.getStackInSlot(STORAGE).getMaxStackSize();
                if (inventory.getStackInSlot(OUTPUT) == ItemStack.EMPTY || inventory.getStackInSlot(OUTPUT).getCount() == 0)
                {
                    if (inventory.getStackInSlot(STORAGE).getCount() >= size)
                    {
                        inventory.setStackInSlot(OUTPUT, inventory.getStackInSlot(STORAGE).copy());
                        inventory.getStackInSlot(OUTPUT).setCount(size);
                        inventory.getStackInSlot(STORAGE).shrink(size);
                    } else
                    {
                        inventory.setStackInSlot(OUTPUT, inventory.getStackInSlot(STORAGE));
                        inventory.setStackInSlot(STORAGE, ItemStack.EMPTY);
                    }
                }
                if (inventory.getStackInSlot(STORAGE).getCount() != 0 && ItemUtils.isItemEqual(inventory.getStackInSlot(STORAGE), inventory.getStackInSlot(OUTPUT), true) && inventory.getStackInSlot(OUTPUT).getCount() <= size - 1)
                {
                    inventory.getStackInSlot(OUTPUT).grow(1);
                    inventory.getStackInSlot(STORAGE).shrink(1);
                }
            }
            if(world.getTileEntity(pos) != null)
            {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return LazyOptional.of(() -> inventory).cast();
        }
        return super.getCapability(cap, side);
    }
    
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }
    
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
    {
        super.onDataPacket(net, packet);
        this.deserializeNBT(packet.getNbtCompound());
    }

    @Override
    public void func_230337_a_(BlockState state, CompoundNBT compound)
    {
        super.func_230337_a_(state, compound);
        inventory.deserializeNBT(compound);
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT compound)
    {
        compound = super.write(compound);
        compound.merge(inventory.serializeNBT());
        return compound;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TranslationTextComponent("tile.qsu.name");
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity)
    {
        return new ContainerQSU(id, playerEntity.inventory, this);
    }
    
    public void writeToNBTWithoutCoords(CompoundNBT tagCompound)
    {
        tagCompound = super.write(tagCompound);
        if (inventory != null)
        {
            tagCompound.merge(inventory.serializeNBT());
        }
    }
    
    public void readFromNBTWithoutCoords(CompoundNBT compound)
    {
        inventory.deserializeNBT(compound);
    }
    
    public ItemStack getDropWithNBT()
    {
        CompoundNBT tileEntity = new CompoundNBT();
        ItemStack dropStack = new ItemStack(QSBlocks.QSU.get(), 1);
        writeToNBTWithoutCoords(tileEntity);
        dropStack.setTag(new CompoundNBT());
        dropStack.getTag().put("tileEntity", tileEntity);
        return dropStack;
    }
}