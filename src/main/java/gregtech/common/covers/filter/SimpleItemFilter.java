package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Consumer;

public class SimpleItemFilter extends AbstractItemFilter implements ISlottedItemFilter {

    private static final int MAX_MATCH_SLOTS = 9;

    protected ItemStackHandler itemFilterSlots;
    protected boolean ignoreDamage = true;
    protected boolean ignoreNBT = true;
    protected int maxStackSize = 1;

    public SimpleItemFilter() {
        this.itemFilterSlots = new ItemStackHandler(MAX_MATCH_SLOTS) {
            @Override
            public int getSlotLimit(int slot) {
                return maxStackSize;
            }
        };
    }

    protected void setIgnoreDamage(boolean ignoreDamage) {
        this.ignoreDamage = ignoreDamage;
        markDirty();
    }

    protected void setIgnoreNBT(boolean ignoreNBT) {
        this.ignoreNBT = ignoreNBT;
        markDirty();
    }

    public ItemStackHandler getItemFilterSlots() {
        return itemFilterSlots;
    }

    public boolean isIgnoreDamage() {
        return ignoreDamage;
    }

    public boolean isIgnoreNBT() {
        return ignoreNBT;
    }

    @Override
    public int getMaxMatchSlots() {
        return MAX_MATCH_SLOTS;
    }

    @Override
    public int matchItemStack(ItemStack itemStack) {
        return itemFilterMatch(getItemFilterSlots(), isIgnoreDamage(), isIgnoreNBT(), itemStack);
    }

    @Override
    public boolean testItemStack(ItemStack itemStack) {
        return matchItemStack(itemStack) != -1;
    }

    @Override
    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    @Override
    public int getMaxStackSize() {
        return maxStackSize;
    }

    @Override
    public int getSlotStackSize(int slotIndex) {
        return itemFilterSlots.getStackInSlot(slotIndex).getCount();
    }

    @Override
    public int getTotalOccupiedHeight() {
        return 36;
    }

    @Override
    public void initUI(int y, Consumer<Widget> widgetGroup) {
        for (int i = 0; i < 9; i++) {
            widgetGroup.accept(new PhantomSlotWidget(itemFilterSlots, i, 10 + 18 * (i % 3), y + 18 * (i / 3)).setBackgroundTexture(GuiTextures.SLOT));
        }
        widgetGroup.accept(new ToggleButtonWidget(74, y, 20, 20, GuiTextures.BUTTON_FILTER_DAMAGE,
            () -> ignoreDamage, this::setIgnoreDamage).setTooltipText("cover.item_filter.ignore_damage"));
        widgetGroup.accept(new ToggleButtonWidget(99, y, 20, 20, GuiTextures.BUTTON_FILTER_NBT,
            () -> ignoreNBT, this::setIgnoreNBT).setTooltipText("cover.item_filter.ignore_nbt"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setTag("ItemFilter", itemFilterSlots.serializeNBT());
        tagCompound.setBoolean("IgnoreDamage", ignoreDamage);
        tagCompound.setBoolean("IgnoreNBT", ignoreNBT);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        this.itemFilterSlots.deserializeNBT(tagCompound.getCompoundTag("ItemFilter"));
        this.ignoreDamage = tagCompound.getBoolean("IgnoreDamage");
        this.ignoreNBT = tagCompound.getBoolean("IgnoreNBT");
    }

    public static int itemFilterMatch(IItemHandler filterSlots, boolean ignoreDamage, boolean ignoreNBTData, ItemStack itemStack) {
        for (int i = 0; i < filterSlots.getSlots(); i++) {
            ItemStack filterStack = filterSlots.getStackInSlot(i);
            if (!filterStack.isEmpty() && areItemsEqual(ignoreDamage, ignoreNBTData, filterStack, itemStack)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean areItemsEqual(boolean ignoreDamage, boolean ignoreNBTData, ItemStack filterStack, ItemStack itemStack) {
        if (ignoreDamage) {
            if (!filterStack.isItemEqualIgnoreDurability(itemStack)) {
                return false;
            }
        } else if (!filterStack.isItemEqual(itemStack)) {
            return false;
        }
        return ignoreNBTData || ItemStack.areItemStackTagsEqual(filterStack, itemStack);
    }
}
