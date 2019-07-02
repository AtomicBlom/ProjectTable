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
import com.github.atomicblom.projecttable.client.mcgui.events.ICheckboxPressedEventListener;
import com.github.atomicblom.projecttable.client.mcgui.events.IItemMadeVisibleEventListener;
import com.github.atomicblom.projecttable.client.model.ProjectTableRecipeInstance;
import com.github.atomicblom.projecttable.gui.events.IRecipeCraftingEventListener;
import com.github.atomicblom.projecttable.inventory.ProjectTableContainer;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacket;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.Rectangle;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectTableGui extends McGUI
{
    private static final String LOCATION = "textures/gui/";
    private static final String FILE_EXTENSION = ".png";

    private final GuiTexture guiTexture = new GuiTexture(getResourceLocation("sscraftingtablegui"), 384, 384);
    private final InventoryPlayer playerInventory;
    private int timesInventoryChanged;
    private GuiTextField searchField = null;
    private Collection<ProjectTableRecipeInstance> recipeList;
    private final List<ProjectTableRecipeInstance> filteredList;
    private ScrollPaneControl recipeListGuiComponent = null;
    private ScrollbarControl scrollbarGuiComponent = null;
    private GuiRenderer guiRenderer;
    private static boolean showOnlyCraftable = true;
    private CheckboxControl showOnlyCraftableComponent;

    public ProjectTableGui(InventoryPlayer playerInventory) {
        super(new ProjectTableContainer(playerInventory));
        recipeList = Lists.newArrayList();
        filteredList = Lists.newArrayList();
        this.playerInventory = playerInventory;
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
    public void initGui()
    {
        xSize = 175;
        ySize = 227;
        super.initGui();
        xSize = 317;
        ySize = 227;

        createRecipeList();

        searchField = new GuiTextField(0, fontRenderer, guiLeft + 9 - (317 - 175) / 2, guiTop + 9, 149, fontRenderer.FONT_HEIGHT);
        searchField.setMaxStringLength(60);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setVisible(true);
        searchField.setTextColor(16777215);
        searchField.setFocused(true);

        createComponents();

        setRecipeRenderText();
    }

    private void createRecipeList() {

        Collection<ProjectTableRecipe> recipes = Lists.newArrayList(ProjectTableManager.INSTANCE.getRecipes());
        //Copy the Player inventory to guard against changes.
        InventoryPlayer inventoryCopy = new InventoryPlayer(playerInventory.player);
        inventoryCopy.copyInventory(playerInventory);
        //Temporary Item List:

        this.recipeList = recipes.parallelStream().map((recipe) -> {
                    ProjectTableRecipeInstance recipeInstance = new ProjectTableRecipeInstance(recipe);

                    final boolean canCraft = ProjectTableManager.INSTANCE.canCraftRecipe(recipe, inventoryCopy);

                    recipeInstance.setCanCraft(canCraft);
                    //FIXME: Implement this when we support GameStages or something similar.
                    recipeInstance.setIsLocked(false);

                    return recipeInstance;
                })
                .sorted((a, b) -> a.getRecipe().getDisplayName().compareToIgnoreCase(b.getRecipe().getDisplayName()))
                .sequential()
                .collect(Collectors.toList());

        createFilteredList();
    }

    private void createFilteredList()
    {

        String text = searchField != null ? searchField.getText() : "";
        filteredList.clear();

        final String searchText = text.toLowerCase();

        synchronized (filteredList) {
            this.recipeList.parallelStream().filter(f ->
                    (!showOnlyCraftable || f.canCraft()) &&
                            !f.isLocked() &&
                            (searchText.isEmpty() || f.getRecipe().getDisplayName().toLowerCase().contains(searchText))
            )
            .sorted((a, b) -> a.getRecipe().getDisplayName().compareToIgnoreCase(b.getRecipe().getDisplayName()))
            .sequential()
            .collect(Collectors.toCollection(() -> filteredList));
        }
    }

    protected void createComponents()
    {
        guiRenderer = new GuiRenderer(mc, mc.getTextureManager(), fontRenderer, itemRender);

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
        showOnlyCraftableComponent.addOnButtonPressedEventListener(new ShowOnlyCraftableEventListener());

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

        templateRecipeControl.addOnRecipeCraftingEventListener(new RecipeCraftingEventListener());
        recipeListGuiComponent.addOnFireItemMadeEventListener(new RecipeMadeVisibleEventListener());
        timesInventoryChanged = playerInventory.getTimesChanged();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (playerInventory.getTimesChanged() != timesInventoryChanged) {
            timesInventoryChanged = playerInventory.getTimesChanged();
            createRecipeList();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseZ)
    {
        fontRenderer.drawString(I18n.format("gui.projecttable:project_table.show_only_craftable"), 105, 10, 0x404040);
    }

    protected void setRecipeRenderText()
    {
        for (final ProjectTableRecipeInstance recipeInstance : recipeList)
        {
            final ProjectTableRecipe projectTableRecipe = recipeInstance.getRecipe();
            if (projectTableRecipe.getRenderText() == null) {
                String proposedName = projectTableRecipe.getDisplayName();

                if (fontRenderer.getStringWidth(proposedName) > 64) {
                    while (fontRenderer.getStringWidth(proposedName + "...") > 64) {
                        proposedName = proposedName.substring(0, proposedName.length() - 2);
                    }
                    proposedName += "...";
                }

                projectTableRecipe.setRenderText(proposedName);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        searchField.drawTextBox();
        guiRenderer.notifyTextureChanged();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!checkHotbarKeys(keyCode))
        {
            if (searchField.textboxKeyTyped(typedChar, keyCode))
            {
                createFilteredList();
            }
            else
            {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    private void craftRecipe(ProjectTableRecipe recipe) {
        ProjectTableMod.network.sendToServer(new ProjectTableCraftPacket(recipe));
        ProjectTableManager.INSTANCE.craftRecipe(recipe, playerInventory);
        this.timesInventoryChanged = playerInventory.getTimesChanged();
    }

    private class RecipeCraftingEventListener implements IRecipeCraftingEventListener
    {
        @Override
        public void onRecipeCrafting(ProjectTableRecipe recipe)
        {
            craftRecipe(recipe);
        }
    }

    private class ShowOnlyCraftableEventListener implements ICheckboxPressedEventListener
    {
        @Override
        public void onCheckboxPressed(CheckboxControl button, boolean value) {
            showOnlyCraftable = value;
            createFilteredList();
        }
    }

    private class RecipeMadeVisibleEventListener implements IItemMadeVisibleEventListener<ProjectTableRecipeInstance, ProjectTableRecipeControl>
    {
        @Override
        public void onItemMadeVisible(ScrollPaneControl scrollPaneControl, ProjectTableRecipeControl projectTableRecipeControl, ProjectTableRecipeInstance projectTableRecipe)
        {
            final boolean canCraft = ProjectTableManager.INSTANCE.canCraftRecipe(projectTableRecipe.getRecipe(), playerInventory);
            projectTableRecipe.setCanCraft(canCraft);
        }
    }
}