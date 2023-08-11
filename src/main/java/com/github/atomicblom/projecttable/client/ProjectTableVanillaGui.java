package com.github.atomicblom.projecttable.client;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.github.atomicblom.projecttable.client.model.ProjectTableRecipeInstance;
import com.github.atomicblom.projecttable.inventory.ProjectTableContainer;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.system.MathUtil;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ProjectTableVanillaGui extends AbstractContainerScreen<ProjectTableContainer> {

    private static final Component showOnlyCraftableComponentText = MutableComponent.create(new TranslatableContents("gui.projecttable:project_table.show_only_craftable"));
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(ProjectTableMod.MODID.toLowerCase(), "textures/gui/sscraftingtablegui.png");

    private static final int CRAFT_BUTTON_HEIGHT = 20;
    private static final int CRAFT_BUTTON_COUNT = 5;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final int timesInventoryChanged;
    private final Inventory playerInventory;
    private EditBox searchField;
    private Collection<ProjectTableRecipeInstance> recipeList;
    private List<ProjectTableRecipeInstance> filteredList;
    private Future<?> _filterFuture = null;
    private int _filterFutureId = 0;
    private Checkbox showOnlyCraftableCheckbox;
    private long _offset = 0;
    private CraftButton[] buttons = new CraftButton[CRAFT_BUTTON_COUNT];

    public ProjectTableVanillaGui(ProjectTableContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 318;
        imageHeight = 227;

        recipeList = Lists.newArrayList();
        filteredList = Lists.newArrayList();
        this.playerInventory = menu.getPlayerInventory();
        this.timesInventoryChanged = inventory.getTimesChanged();
    }

    @Override
    protected void init() {
        super.init();

        searchField = new EditBox(font, leftPos + 9, topPos + 9, 149, font.lineHeight, MutableComponent.create(new TranslatableContents("gui.projecttable:project_table.search")));
        this.searchField.setCanLoseFocus(false);
        this.searchField.setTextColor(-1);
        this.searchField.setTextColorUneditable(-1);
        this.searchField.setBordered(false);
        this.searchField.setMaxLength(60);
        this.searchField.setResponder(this::onNameChanged);
        this.searchField.setValue("");
        this.addRenderableWidget(this.searchField);
        this.setInitialFocus(this.searchField);
        this.searchField.setEditable(false);

        int j = this.font.width(showOnlyCraftableComponentText);
        showOnlyCraftableCheckbox = new Checkbox(218, 9, j + 24, 20, showOnlyCraftableComponentText, false);
        this.addRenderableWidget(showOnlyCraftableCheckbox);

        for (int i = 0; i < CRAFT_BUTTON_COUNT; i++ ){
            buttons[i] = new CraftButton(i,0, i * CRAFT_BUTTON_HEIGHT, 284, CRAFT_BUTTON_HEIGHT, playerInventoryTitle, this::craftButtonPressed);
        }

        executor.submit(this::createRecipeList);
    }

    private void craftButtonPressed(Button button) {
        var index = ((CraftButton)button).getIndex();
    }

    private void onNameChanged(String s) {
        triggerCreateFilteredList();
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int p_97809_, int p_97810_) {
        //this.font.draw(poseStack, showOnlyCraftableComponentText, 164, 8, 0x404040);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        blit(poseStack, x, y, this.getBlitOffset(), 0.0F, 0.0F, this.imageWidth, this.imageHeight, 384, 384);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        var maximumOffset = filteredList.size() * CRAFT_BUTTON_HEIGHT;
        if (_offset >= (maximumOffset - (CRAFT_BUTTON_COUNT * CRAFT_BUTTON_HEIGHT))) {
            _offset = (maximumOffset - (CRAFT_BUTTON_COUNT * CRAFT_BUTTON_HEIGHT));
        }
        if (_offset < 0) {
            _offset = 0;
        }

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int itemStartY = y + 23;
        int itemStartX = x + 8;

        var firstItemIndex = (_offset / maximumOffset) / CRAFT_BUTTON_HEIGHT;
        var buttonIndex = 0;
        for (int i = (int)firstItemIndex; i < CRAFT_BUTTON_COUNT && i < filteredList.size(); i++, buttonIndex++) {
            var firstItem = filteredList.get(i).getRecipe().getOutput().stream().findFirst().get();
            buttons[i].x = itemStartX;
            buttons[i].y = itemStartY + i * CRAFT_BUTTON_HEIGHT;
            buttons[i].render(poseStack, mouseX, mouseY, partialTicks);
            this.itemRenderer.blitOffset = 100f;
            if (!firstItem.isEmpty()) {
                this.itemRenderer.renderAndDecorateFakeItem(firstItem, itemStartX + 2, itemStartY + i * CRAFT_BUTTON_HEIGHT + 2);
                this.itemRenderer.renderGuiItemDecorations(this.font, firstItem, itemStartX + 2, itemStartY + i * CRAFT_BUTTON_HEIGHT + 2);
            }

            this.itemRenderer.blitOffset = 0f;

        }

        RenderSystem.enableDepthTest();

    }

    @Override
    public boolean keyPressed(int p_97878_, int p_97879_, int p_97880_) {
        if (p_97878_ == 256) {
            this.minecraft.player.closeContainer();
        }

        return !this.searchField.keyPressed(p_97878_, p_97879_, p_97880_) && !this.searchField.canConsumeInput() ? super.keyPressed(p_97878_, p_97879_, p_97880_) : true;
    }

    private void createRecipeList() {

        Collection<ProjectTableRecipe> recipes = Lists.newArrayList(ProjectTableManager.INSTANCE.getRecipes());
        this.recipeList = recipes.parallelStream()
                .filter(Objects::nonNull) // WTF... where are null recipes sneaking in?
                .map(ProjectTableRecipeInstance::new)
                .sorted((a, b) -> a.getRecipeName().compareToIgnoreCase(b.getRecipeName()))
                .sequential()
                .collect(Collectors.toList());
        searchField.setEditable(true);
        searchField.setFocus(true);

        triggerCreateFilteredList();
    }

    private void triggerCreateFilteredList() {
        ++_filterFutureId;
        if (_filterFuture != null) {
            _filterFuture.cancel(true);
        }
        _filterFuture = executor.submit(() -> this.createFilteredList(_filterFutureId));
    }

    private void createFilteredList(int id)
    {
        var showOnlyCraftable = showOnlyCraftableCheckbox.selected();
        //long startTime = new Date().getTime();
        String text = searchField != null ? searchField.getValue() : "";
        final String searchText = text.toLowerCase();

        //Copy the Player inventory to guard against changes.
        Inventory inventoryCopy = new Inventory(playerInventory.player);
        for (int slot = 0; slot < playerInventory.getContainerSize(); slot++) {
            inventoryCopy.setItem(slot, playerInventory.getItem(slot).copy());
        }

        final List<ItemStack> compactedInventoryItems = ProjectTableManager.INSTANCE.getCompactedInventoryItems(inventoryCopy);
        List<ProjectTableRecipeInstance> localFilteredList = this.recipeList
                .parallelStream()
                .takeWhile(f -> id == _filterFutureId)
                .map(r -> updateRecipeInstance(r, compactedInventoryItems))
                .filter(f -> filterRecipeInstance(f, searchText, showOnlyCraftable))
                .sorted((a, b) -> a.getRecipeName().compareToIgnoreCase(b.getRecipeName()))
                .sequential()
                .collect(Collectors.toList());

        //final long endTime = new Date().getTime();
        if (_filterFutureId == id) {
            //this.recipeListGuiComponent.setItems(localFilteredList);
            filteredList = localFilteredList;

            //ProjectTableMod.logger.info("List filtered in {}ms", endTime - startTime);
        } /*else { //Performance profiling
            ProjectTableMod.logger.info("List filtered cancelled after {}ms", endTime - startTime);
        }*/
    }

    private ProjectTableRecipeInstance updateRecipeInstance(ProjectTableRecipeInstance recipeInstance, List<ItemStack> compactedInventory) {
        final boolean canCraft = ProjectTableManager.INSTANCE.canCraftRecipe(recipeInstance.getRecipe(), compactedInventory);
        recipeInstance.setCanCraft(canCraft);

        //FIXME: Implement this when we support GameStages or something similar.
        recipeInstance.setIsLocked(false);
        return recipeInstance;
    }

    private boolean filterRecipeInstance(ProjectTableRecipeInstance f, String searchText, boolean showOnlyCraftable) {
        return (!showOnlyCraftable || f.canCraft()) &&
                !f.isLocked() &&
                (searchText.isEmpty() || f.getRecipeName().toLowerCase().contains(searchText));
    }

    private class CraftButton extends Button {

        private final int index;

        public CraftButton(int index, int x, int y, int width, int height, Component label, OnPress onPress) {
            super(x, y, width, height, label, onPress);
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}

