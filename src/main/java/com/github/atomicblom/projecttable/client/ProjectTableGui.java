package com.github.atomicblom.projecttable.client;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.github.atomicblom.projecttable.client.controls.ProjectTableRecipeControl;
import com.github.atomicblom.projecttable.client.mcgui.GuiRenderer;
import com.github.atomicblom.projecttable.client.mcgui.GuiSubTexture;
import com.github.atomicblom.projecttable.client.mcgui.GuiTexture;
import com.github.atomicblom.projecttable.client.mcgui.McGUI;
import com.github.atomicblom.projecttable.client.mcgui.controls.CheckboxControl;
import com.github.atomicblom.projecttable.client.mcgui.controls.ScrollPaneControl;
import com.github.atomicblom.projecttable.client.mcgui.controls.ScrollbarControl;
import com.github.atomicblom.projecttable.client.mcgui.controls.TexturedPaneControl;
import com.github.atomicblom.projecttable.client.mcgui.util.Rectangle;
import com.github.atomicblom.projecttable.client.model.ProjectTableRecipeInstance;
import com.github.atomicblom.projecttable.inventory.ProjectTableContainer;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacket;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectTableGui extends McGUI<ProjectTableContainer>
{
    private static final ITextComponent showOnlyCraftableComponentText = new TranslationTextComponent("gui.projecttable:project_table.show_only_craftable");
    private static final String LOCATION = "textures/gui/";
    private static final String FILE_EXTENSION = ".png";

    private final GuiTexture guiTexture = new GuiTexture(getResourceLocation("sscraftingtablegui"), 384, 384);
    private final PlayerInventory playerInventory;
    private int timesInventoryChanged;
    private TextFieldWidget searchField = null;
    private Collection<ProjectTableRecipeInstance> recipeList;
    private final List<ProjectTableRecipeInstance> filteredList;
    private ScrollPaneControl<ProjectTableRecipeInstance, ProjectTableRecipeControl> recipeListGuiComponent = null;
    private ScrollbarControl scrollbarGuiComponent = null;
    private GuiRenderer guiRenderer;
    private boolean showOnlyCraftable = false;
    private CheckboxControl showOnlyCraftableComponent;



    public ProjectTableGui(ProjectTableContainer screenContainer, PlayerInventory inv, ITextComponent title) {
        super(screenContainer, inv, title);

        xSize = 318;
        ySize = 227;

        recipeList = Lists.newArrayList();
        filteredList = Lists.newArrayList();
        this.playerInventory = screenContainer.getPlayerInventory();
        this.timesInventoryChanged = playerInventory.getTimesChanged();
    }

    protected ResourceLocation getResourceLocation(String path)
    {
        return getResourceLocation(ProjectTableMod.MODID.toLowerCase(), LOCATION + path + FILE_EXTENSION);
    }

    protected ResourceLocation getResourceLocation(String modID, String path)
    {
        return new ResourceLocation(modID, path);
    }

    @Override
    protected String getInventoryName() {
        return "Project Table";
    }

    @Override
    public void init()
    {
        //xSize = 175;
        //ySize = 227;
        super.init();
        //xSize = 317;
        //ySize = 227;

        createRecipeList();

        //searchField = new TextFieldWidget(font, guiLeft + 9 - (317 - 175) / 2, guiTop + 9, 149, font.FONT_HEIGHT, new TranslationTextComponent("gui.projecttable:project_table.search"));
        searchField = new TextFieldWidget(font, guiLeft + 9, guiTop + 9, 149, font.FONT_HEIGHT, new TranslationTextComponent("gui.projecttable:project_table.search"));
        searchField.setMaxStringLength(60);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setVisible(true);
        searchField.setTextColor(16777215);
        searchField.setFocused2(true);

        createComponents();

        setRecipeRenderText();
    }

    private void createRecipeList() {

        Collection<ProjectTableRecipe> recipes = Lists.newArrayList(ProjectTableManager.INSTANCE.getRecipes());
        this.recipeList = recipes.parallelStream()
                .map(ProjectTableRecipeInstance::new)
                .sorted((a, b) -> a.getRecipe().getDisplayName().toString().compareToIgnoreCase(b.getRecipe().getDisplayName().toString()))
                .sequential()
                .collect(Collectors.toList());

        createFilteredList();
    }

    private void createFilteredList()
    {
        String text = searchField != null ? searchField.getText() : "";
        filteredList.clear();

        final String searchText = text.toLowerCase();

        //Copy the Player inventory to guard against changes.
        PlayerInventory inventoryCopy = new PlayerInventory(playerInventory.player);
        inventoryCopy.copyInventory(playerInventory);

        synchronized (filteredList) {
            this.recipeList.parallelStream()
                .map(r -> updateRecipeInstance(r, inventoryCopy))
                .filter(f -> filterRecipeInstance(f, searchText))
                .sorted((a, b) -> a.getRecipe().getDisplayName().getString().compareToIgnoreCase(b.getRecipe().getDisplayName().toString()))
                .sequential()
                .collect(Collectors.toCollection(() -> filteredList));
        }
    }

    private ProjectTableRecipeInstance updateRecipeInstance(ProjectTableRecipeInstance recipeInstance, PlayerInventory inventoryCopy) {
        final boolean canCraft = ProjectTableManager.INSTANCE.canCraftRecipe(recipeInstance.getRecipe(), inventoryCopy);
        recipeInstance.setCanCraft(canCraft);

        //FIXME: Implement this when we support GameStages or something similar.
        recipeInstance.setIsLocked(false);
        return recipeInstance;
    }

    private boolean filterRecipeInstance(ProjectTableRecipeInstance f, String searchText) {
        return (!showOnlyCraftable || f.canCraft()) &&
                !f.isLocked() &&
                (searchText.isEmpty() || f.getRecipe().getDisplayName().getString().toLowerCase().contains(searchText));
    }

    protected void createComponents()
    {
        guiRenderer = new GuiRenderer(this);

        final GuiTexture guiBackground = new GuiSubTexture(guiTexture, new Rectangle(0, 0, 317, 227));
        final GuiTexture inactiveHandle = new GuiSubTexture(guiTexture, new Rectangle(318, 0, 12, 15));
        final GuiTexture activeHandle = new GuiSubTexture(guiTexture, new Rectangle(318 + 12, 0, 12, 15));
        final GuiTexture craftableSubtexture = new GuiSubTexture(guiTexture, new Rectangle(0, 227, 284, 23));
        final GuiTexture uncraftableSubtexture = new GuiSubTexture(guiTexture, new Rectangle(0, 227 + 23, 284, 23));
        final GuiTexture checkboxBackground = new GuiSubTexture(guiTexture, new Rectangle(330, 15, 12, 12));
        final GuiTexture checkboxActive = new GuiSubTexture(guiTexture, new Rectangle(318, 15, 12, 12));

        setRootControl(new TexturedPaneControl(guiRenderer, 317, 227, guiBackground));
        scrollbarGuiComponent = new ScrollbarControl(guiRenderer, activeHandle, inactiveHandle);
        scrollbarGuiComponent.setLocation(298, 24);
        scrollbarGuiComponent.setSize(14, 115);

        showOnlyCraftableComponent = new CheckboxControl(guiRenderer);
        showOnlyCraftableComponent.setDefaultTexture(checkboxBackground);
        showOnlyCraftableComponent.setDisabledTexture(checkboxBackground);
        showOnlyCraftableComponent.setPressedTexture(checkboxBackground);
        showOnlyCraftableComponent.setActiveOverlayTexture(checkboxActive);
        showOnlyCraftableComponent.setLocation(164, 8);
        showOnlyCraftableComponent.setSize(12, 12);
        showOnlyCraftableComponent.setValue(showOnlyCraftable);

        final ProjectTableRecipeControl templateRecipeControl = new ProjectTableRecipeControl(guiRenderer, craftableSubtexture, uncraftableSubtexture);
        recipeListGuiComponent = new ScrollPaneControl<ProjectTableRecipeInstance, ProjectTableRecipeControl>(guiRenderer, 330, 23*5)
                .setScrollbar(scrollbarGuiComponent)
                .setItemRendererTemplate(templateRecipeControl)
                .setVisibleItemCount(5)
                .setItems(filteredList);
        recipeListGuiComponent.setLocation(8, 24);

        addChild(recipeListGuiComponent);
        addChild(scrollbarGuiComponent);
        addChild(showOnlyCraftableComponent);

        templateRecipeControl.addOnRecipeCraftingEventListener(this::craftRecipe);
        showOnlyCraftableComponent.addOnButtonPressedEventListener((button, value) -> {
            showOnlyCraftable = value;
            createFilteredList();
        });
        recipeListGuiComponent.addOnFireItemMadeVisibleEventListener((scrollPaneControl, projectTableRecipeControl, projectTableRecipe) -> {
            final boolean canCraft = ProjectTableManager.INSTANCE.canCraftRecipe(projectTableRecipe.getRecipe(), playerInventory);
            projectTableRecipe.setCanCraft(canCraft);
        });
        timesInventoryChanged = playerInventory.getTimesChanged();
    }

    @Override
    public void tick() {
        super.tick();

        if (playerInventory.getTimesChanged() != timesInventoryChanged) {
            timesInventoryChanged = playerInventory.getTimesChanged();
            createFilteredList();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseZ)
    {
        font.func_238407_a_(matrixStack, showOnlyCraftableComponentText, 175, 10, 0xE0E0E0);
    }

    protected void setRecipeRenderText()
    {
        //FIXME: we can probably defer this until the element is displayed.
        for (final ProjectTableRecipeInstance recipeInstance : recipeList)
        {
            final ProjectTableRecipe projectTableRecipe = recipeInstance.getRecipe();
            if (projectTableRecipe.getRenderText() == null) {
                String proposedName = projectTableRecipe.getDisplayName().toString();

                if (font.getStringWidth(proposedName) > 64) {
                    while (font.getStringWidth(proposedName + "...") > 64) {
                        proposedName = proposedName.substring(0, proposedName.length() - 2);
                    }
                    proposedName += "...";
                }

                projectTableRecipe.setRenderText(proposedName);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        guiRenderer.notifyTextureChanged();
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        //if (!this.func_195363_d(typedChar, keyCode)) //checkHotbar...
        //{
            if (searchField.charTyped(typedChar, keyCode))
            {
                createFilteredList();
            }
            else
            {
                return super.charTyped(typedChar, keyCode);
            }
        //}
        return true;
    }

    private void craftRecipe(ProjectTableRecipe recipe) {
        ProjectTableMod.network.sendToServer(new ProjectTableCraftPacket(recipe));
        ProjectTableManager.INSTANCE.craftRecipe(recipe, playerInventory);

        //Can probably just remove this.
//        for (ProjectTableRecipeInstance projectTableRecipe : this.recipeListGuiComponent.getVisibleItems()) {
//            final boolean canCraft = ProjectTableManager.INSTANCE.canCraftRecipe(projectTableRecipe.getRecipe(), playerInventory);
//            projectTableRecipe.setCanCraft(canCraft);
//        }
        //this.timesInventoryChanged = playerInventory.getTimesChanged();
    }
}