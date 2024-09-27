package net.fsmdev.automateditemframes.mixin;


import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.stream.IntStream;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin {

    @Shadow
    public abstract void setHeldItemStack(ItemStack stack);

    @Shadow
    public abstract ItemStack getHeldItemStack();

    @Inject(method="onPlace", at=@At("TAIL"))
    private void onPlace(CallbackInfo ci) {
        ItemFrameEntity itemFrame = (ItemFrameEntity) (Object) this;
        BlockPos blockPos = itemFrame.getAttachedBlockPos().offset(itemFrame.getFacing().getOpposite());
        BlockEntity blockEntity = itemFrame.getWorld().getBlockEntity(blockPos);
        BlockState blockState = itemFrame.getWorld().getBlockState(blockPos);
        if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof TrappedChestBlockEntity || blockEntity instanceof BarrelBlockEntity) {
            ChestType chestType = blockEntity instanceof BarrelBlockEntity ? ChestType.SINGLE : blockState.get(Properties.CHEST_TYPE);
            HashMap<String, Integer> itemStacks = new HashMap<>();
            IntStream.range(0, ((LootableContainerBlockEntity) blockEntity).size()).mapToObj(i -> ((LootableContainerBlockEntity) blockEntity).getStack(i)).filter(currentItemStack -> !currentItemStack.isEmpty()).forEach(currentItemStack -> itemStacks.put(currentItemStack.getTranslationKey(), itemStacks.getOrDefault(currentItemStack.getTranslationKey(), 0) + currentItemStack.getCount()));
            Direction facing = chestType == ChestType.SINGLE ? null : ChestBlock.getFacing(blockState);
            BlockEntity secondChest = chestType == ChestType.SINGLE ? null : itemFrame.getWorld().getBlockEntity(blockPos.add(facing.getVector()));
            if (chestType != ChestType.SINGLE && (secondChest instanceof ChestBlockEntity || secondChest instanceof TrappedChestBlockEntity)) {
                IntStream.range(0, ((LootableContainerBlockEntity) secondChest).size()).mapToObj(i -> ((LootableContainerBlockEntity) secondChest).getStack(i)).filter(currentItemStack -> !currentItemStack.isEmpty()).forEach(currentItemStack -> itemStacks.put(currentItemStack.getTranslationKey(), itemStacks.getOrDefault(currentItemStack.getTranslationKey(), 0) + currentItemStack.getCount()));
            }
            if (!itemStacks.isEmpty()) {
                Map.Entry<String, Integer> higherCountStack = Collections.max(itemStacks.entrySet(), Map.Entry.comparingByValue());
                for (int i = 0; i < ((LootableContainerBlockEntity) blockEntity).size(); i++) {
                    ItemStack currentItemStack = ((LootableContainerBlockEntity) blockEntity).getStack(i);
                    if (!currentItemStack.isEmpty()) {
                        if (currentItemStack.getTranslationKey().equals(higherCountStack.getKey())) {
                            setHeldItemStack(((LootableContainerBlockEntity) blockEntity).removeStack(i, 1));
                            break;
                        }
                    }
                }
                if (getHeldItemStack().isEmpty() && (secondChest instanceof ChestBlockEntity || secondChest instanceof TrappedChestBlockEntity)) {
                    for (int i = 0; i < ((LootableContainerBlockEntity) secondChest).size(); i++) {
                        ItemStack currentItemStack = ((LootableContainerBlockEntity) secondChest).getStack(i);
                        if (!currentItemStack.isEmpty()) {
                            if (currentItemStack.getTranslationKey().equals(higherCountStack.getKey())) {
                                setHeldItemStack(((LootableContainerBlockEntity) secondChest).removeStack(i, 1));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
