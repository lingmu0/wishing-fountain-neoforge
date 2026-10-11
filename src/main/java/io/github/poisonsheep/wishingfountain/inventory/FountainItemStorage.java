package io.github.poisonsheep.wishingfountain.inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
public final class FountainItemStorage {
    private ItemStack stack = ItemStack.EMPTY;
    public FountainItemStorage(int slots) {
        if (slots != 1) throw new IllegalArgumentException("Fountain storage has one slot");
    }
    private static void checkSlot(int slot) {
        if (slot != 0) throw new IndexOutOfBoundsException(slot);
    }
    public ItemStack getStackInSlot(int slot) { checkSlot(slot); return stack; }
    public void setStackInSlot(int slot, ItemStack value) { checkSlot(slot); stack = value; }
    public ItemStack insertItem(int slot, ItemStack incoming, boolean simulate) {
        checkSlot(slot);
        if (incoming.isEmpty()) return ItemStack.EMPTY;
        if (!stack.isEmpty() && !ItemStack.isSameItemSameTags(stack, incoming)) return incoming;
        int amount = Math.min(incoming.getCount(), Math.min(8, incoming.getMaxStackSize()) - stack.getCount());
        if (amount <= 0) return incoming;
        if (!simulate) {
            if (stack.isEmpty()) stack = incoming.copyWithCount(amount);
            else stack.grow(amount);
        }
        return amount == incoming.getCount() ? ItemStack.EMPTY : incoming.copyWithCount(incoming.getCount() - amount);
    }
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        checkSlot(slot);
        if (amount <= 0 || stack.isEmpty()) return ItemStack.EMPTY;
        int extracted = Math.min(amount, stack.getCount());
        ItemStack result = stack.copyWithCount(extracted);
        if (!simulate) { stack.shrink(extracted); if (stack.isEmpty()) stack = ItemStack.EMPTY; }
        return result;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        net.minecraft.nbt.ListTag items = new net.minecraft.nbt.ListTag();
        if (!stack.isEmpty()) {
            CompoundTag entry = stack.save(new CompoundTag());
            entry.putInt("Slot", 0);
            items.add(entry);
        }
        tag.put("Items", items);
        tag.putInt("Size", 1);
        return tag;
    }
    public void deserializeNBT(CompoundTag tag) {
        var items = tag.getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
        stack = items.isEmpty() ? ItemStack.EMPTY : ItemStack.of(items.getCompound(0));
    }

}
