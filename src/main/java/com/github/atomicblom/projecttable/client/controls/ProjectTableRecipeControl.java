package com.github.atomicblom.projecttable.client.controls;

import com.github.atomicblom.projecttable.Logger;
import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.github.atomicblom.projecttable.client.mcgui.GuiRenderer;
import com.github.atomicblom.projecttable.client.mcgui.GuiTexture;
import com.github.atomicblom.projecttable.client.mcgui.IGuiTemplate;
import com.github.atomicblom.projecttable.client.mcgui.IModelView;
import com.github.atomicblom.projecttable.client.mcgui.controls.ButtonControl;
import com.github.atomicblom.projecttable.client.mcgui.util.Rectangle;
import com.github.atomicblom.projecttable.client.model.ProjectTableRecipeInstance;
import com.github.atomicblom.projecttable.gui.events.IRecipeCraftingEventListener;
import com.github.atomicblom.projecttable.util.ItemStackUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.locale.Language;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ProjectTableRecipeControl extends ButtonControl implements IGuiTemplate<ProjectTableRecipeControl>, IModelView<ProjectTableRecipeInstance>
{
    private final GuiTexture craftableTexture;
    private final GuiTexture uncraftableTexture;
    private final String toolIndicatorLocalized;

    private ProjectTableRecipeInstance recipeInstance = null;
    private ProjectTableRecipe recipe = null;
    private ArrayList<List<ItemStack>> recipeIngredients = null;

    public ProjectTableRecipeControl(GuiRenderer guiRenderer, GuiTexture craftableTexture, GuiTexture uncraftableTexture)
    {
        super(guiRenderer, new Rectangle(0, 0, craftableTexture.getBounds().getWidth(), craftableTexture.getBounds().getHeight()));
        this.craftableTexture = craftableTexture;
        this.uncraftableTexture = uncraftableTexture;

        setDefaultTexture(craftableTexture);
        setDisabledTexture(uncraftableTexture);
        setHoverTexture(craftableTexture);
        setPressedTexture(uncraftableTexture);

        toolIndicatorLocalized = Language.getInstance().getOrDefault("gui.projecttable:project_table.tool_indicator");
    }

    @Override
    public void draw(PoseStack PoseStack) {
        if (recipeInstance == null || recipe == null || recipeIngredients == null) { return; }
        setDisabled(!recipeInstance.canCraft());

        super.draw(PoseStack);

        final GuiRenderer guiRenderer = getGuiRenderer();
        PoseStack.pushPose();
        PoseStack.translate(0, 0, 200);
        RenderSystem.disableDepthTest();
        final ImmutableList<ItemStack> output = recipe.getOutput();
        final ItemStack outputItemStack = output.get(0);
        if (output.size() == 1 && !outputItemStack.isEmpty())
        {
            guiRenderer.renderItem(this, outputItemStack, 2, 3);

            int itemCount = outputItemStack.getCount();

            final String craftedItemCount = String.format("%d", itemCount);
            final int textWidth = guiRenderer.getStringWidth(craftedItemCount);

            PoseStack.pushPose();
            PoseStack.translate(0, 0, 200);
            if (itemCount > 0) {
                guiRenderer.drawStringWithShadow(PoseStack, this, craftedItemCount, 16 - textWidth + 2, 12, 16777215);
            }
            var displayName = Language.getInstance().getOrDefault(recipe.getDisplayName().getString());
            guiRenderer.drawStringWithShadow(PoseStack, this, displayName, 2 + 20, 8, 16777215);
            PoseStack.popPose();
        }

        final int inputItemCount = recipeIngredients.size();

        for (int j = 0; j < inputItemCount; ++j) {
            final IIngredient inputIngredient = recipe.getInput().get(j);
            final List<ItemStack> possibleItems = recipeIngredients.get(j);

            if (possibleItems.size() == 0) {
                continue;
            }

            final ClientLevel world = Minecraft.getInstance().level;
            assert world != null;
            final long totalWorldTime = world.getGameTime();
            final int renderedItem = (int)((totalWorldTime / 20) % possibleItems.size());

            int quantityConsumed = inputIngredient.getQuantityConsumed();
            final String requiredItemCount = String.format("%d", quantityConsumed);
            final int textWidth = guiRenderer.getStringWidth(requiredItemCount);

            final int border = 1;
            final int padding = 2;
            final int itemSize = 16;

            guiRenderer.renderItem(this, possibleItems.get(renderedItem), getBounds().getWidth() - border - (itemSize + padding + 2) * (j + border), padding + border);

            PoseStack.pushPose();
            PoseStack.translate(0, 0, 200);
            if (quantityConsumed > 0) {
                guiRenderer.drawStringWithShadow(PoseStack, this, requiredItemCount, getBounds().getWidth() - border - (itemSize + padding + 2) * j - textWidth - border, 12, 16777215);
            } else {
                guiRenderer.drawStringWithShadow(PoseStack, this, toolIndicatorLocalized, getBounds().getWidth() - border - (itemSize + padding + 2) * j - textWidth - border, 12, 16777215);
            }
            PoseStack.popPose();
        }
        PoseStack.popPose();

        RenderSystem.enableDepthTest();
    }

    public ProjectTableRecipeInstance getRecipe()
    {
        return recipeInstance;
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
        this.recipe = recipeInstance != null ? recipeInstance.getRecipe() : null;
        if (recipe == null || recipe.getInput() == null) {
            recipeIngredients = null;
            return;
        }

        final int inputItemCount = recipe.getInput().size();
        recipeIngredients = new ArrayList<>(inputItemCount);
        for (int j = 0; j < inputItemCount; ++j) {
            final IIngredient inputIngredient = recipe.getInput().get(j);
            final List<ItemStack> possibleItems = ItemStackUtils.getAllSubtypes(inputIngredient.getItemStacks());
            if (possibleItems.size() == 0) {
                ProjectTableMod.logger.error("Unable to get all item subtypes for input " + j + " in " + recipe.getId() + " from source " + recipe.getSource());
            }

            recipeIngredients.add(j, possibleItems);
        }
    }

    @Override
    public ProjectTableRecipeInstance getModel() {
        return this.recipeInstance;
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

    public void removeOnRecipeCraftingEventListener(IRecipeCraftingEventListener listener) {
        recipeCraftingEventListeners.remove(listener);
    }
}