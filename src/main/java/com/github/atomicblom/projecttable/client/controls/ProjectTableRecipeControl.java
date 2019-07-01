package com.github.atomicblom.projecttable.client.controls;

import com.github.atomicblom.projecttable.Logger;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.github.atomicblom.projecttable.client.mcgui.GuiRenderer;
import com.github.atomicblom.projecttable.client.mcgui.GuiTexture;
import com.github.atomicblom.projecttable.client.mcgui.IGuiTemplate;
import com.github.atomicblom.projecttable.client.mcgui.IModelView;
import com.github.atomicblom.projecttable.client.mcgui.controls.ButtonControl;
import com.github.atomicblom.projecttable.client.model.ProjectTableRecipeInstance;
import com.github.atomicblom.projecttable.gui.events.IRecipeCraftingEventListener;
import com.github.atomicblom.projecttable.util.ItemStackUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ProjectTableRecipeControl extends ButtonControl implements IGuiTemplate<ProjectTableRecipeControl>, IModelView<ProjectTableRecipeInstance>
{
    private final GuiTexture craftableTexture;
    private final GuiTexture uncraftableTexture;
    private ProjectTableRecipeInstance recipeInstance = null;

    public ProjectTableRecipeControl(GuiRenderer guiRenderer, GuiTexture craftableTexture, GuiTexture uncraftableTexture)
    {
        super(guiRenderer, new Rectangle(0, 0, craftableTexture.getBounds().getWidth(), craftableTexture.getBounds().getHeight()));
        this.craftableTexture = craftableTexture;
        this.uncraftableTexture = uncraftableTexture;

        setDefaultTexture(craftableTexture);
        setDisabledTexture(uncraftableTexture);
        setHoverTexture(craftableTexture);
        setPressedTexture(uncraftableTexture);
    }

    @Override
    public void draw() {
        if (recipeInstance == null) { return; }
        super.draw();

        setDisabled(!recipeInstance.canCraft());
        final ProjectTableRecipe recipe = recipeInstance.getRecipe();

        final GuiRenderer guiRenderer = getGuiRenderer();

        GlStateManager.enableRescaleNormal();
        final ImmutableList<ItemStack> output = recipe.getOutput();
        final ItemStack outputItemStack = output.get(0);
        if (output.size() == 1 && !outputItemStack.isEmpty())
        {
            RenderHelper.enableGUIStandardItemLighting();
            guiRenderer.renderItem(this, outputItemStack, 2, 3);
            RenderHelper.disableStandardItemLighting();

            int itemCount = outputItemStack.getCount();

            final String craftedItemCount = String.format("%d", itemCount);
            final int textWidth = guiRenderer.getStringWidth(craftedItemCount);

            GlStateManager.depthFunc(GL11.GL_ALWAYS);
            if (itemCount > 0) {
                guiRenderer.drawStringWithShadow(this, craftedItemCount, 16 - textWidth + 2, 12, 16777215);
            }
            GlStateManager.depthFunc(GL11.GL_LEQUAL);


            guiRenderer.drawStringWithShadow(this, recipe.getDisplayName(), 2 + 20, 8, 16777215);
        }

        final int inputItemCount = recipe.getInput().size();

        for (int j = 0; j < inputItemCount; ++j) {
            final IIngredient inputIngredient = recipe.getInput().get(j);

            final List<ItemStack> possibleItems = ItemStackUtils.getAllSubtypes(inputIngredient.getItemStacks());

            final long totalWorldTime = Minecraft.getMinecraft().world.getTotalWorldTime();
            final int renderedItem = (int)((totalWorldTime / 20) % possibleItems.size());

            int quantityConsumed = inputIngredient.getQuantityConsumed();
            final String requiredItemCount = String.format("%d", quantityConsumed);
            final int textWidth = guiRenderer.getStringWidth(requiredItemCount);

            final int border = 1;
            final int padding = 2;
            final int itemSize = 16;

            guiRenderer.renderItem(this, possibleItems.get(renderedItem), getBounds().getWidth() - border - (itemSize + padding) * (j + border), padding + border);

            GlStateManager.depthFunc(GL11.GL_ALWAYS);
            if (quantityConsumed > 0) {
                guiRenderer.drawStringWithShadow(this, requiredItemCount, getBounds().getWidth() - border - (itemSize + padding) * j - textWidth - border, 12, 16777215);
            } else {
                guiRenderer.drawStringWithShadow(this, "T", getBounds().getWidth() - border - (itemSize + padding) * j - textWidth - border, 12, 16777215);
            }
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
        }

        GlStateManager.disableRescaleNormal();
    }

    public ProjectTableRecipeInstance getRecipe()
    {
        return recipeInstance;
    }

    public void setRecipe(ProjectTableRecipeInstance recipeInstance)
    {
        this.recipeInstance = recipeInstance;
    }

    @Override
    public ProjectTableRecipeControl construct()
    {
        final ProjectTableRecipeControl concreteControl = new ProjectTableRecipeControl(getGuiRenderer(), craftableTexture, uncraftableTexture);

        concreteControl.recipeCraftingEventListeners = recipeCraftingEventListeners;

        return concreteControl;
    }

    @Override
    public void setModel(ProjectTableRecipeInstance recipeInstance)
    {
        this.recipeInstance = recipeInstance;
    }

    @Override
    protected void onButtonPressed() {
        onRecipeCraftingInternal();
    }

    /////////////////////////////////////////////////////////////////////////////
    // On Recipe Crafting Event Handling
    /////////////////////////////////////////////////////////////////////////////

    private void onRecipeCraftingInternal() {
        onRecipeCrafting();

        fireRecipeCraftingEvent();
    }

    protected void onRecipeCrafting() {
    }

    private void fireRecipeCraftingEvent()
    {
        for (final IRecipeCraftingEventListener eventListener : recipeCraftingEventListeners)
        {
            try {
                eventListener.onRecipeCrafting(recipeInstance.getRecipe());
            } catch (final RuntimeException e) {
                Logger.warning("Exception in an IRecipeCraftingEventListener %s", e);
            }
        }
    }

    private List<IRecipeCraftingEventListener> recipeCraftingEventListeners = new ArrayList<>(1);

    @SuppressWarnings("unused")
    public void addOnRecipeCraftingEventListener(IRecipeCraftingEventListener listener) {
        recipeCraftingEventListeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeOnRecipeCraftingEventListener(IRecipeCraftingEventListener listener) {
        recipeCraftingEventListeners.remove(listener);
    }
}