//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.volmit.fuse.screen;

import java.util.Optional;

import com.mojang.logging.LogUtils;
import com.volmit.fuse.screen.widget.WorkspaceListWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.world.gen.GeneratorOptions;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorkspaceScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final GeneratorOptions DEBUG_GENERATOR_OPTIONS = new GeneratorOptions((long)"test1".hashCode(), true, false);
    protected final Screen parent;
    private ButtonWidget unlinkButton;
    private ButtonWidget launchButton;
    private ButtonWidget editButton;
    private ButtonWidget settingsButton;
    protected TextFieldWidget searchBox;
    private WorkspaceListWidget levelList;

    public WorkspaceScreen(Screen parent) {
        super(Text.translatable("Workspace"));
        this.parent = parent;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    public void tick() {
        this.searchBox.tick();
    }

    protected void init() {
        this.searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 22, 200, 20, this.searchBox, Text.translatable("selectWorld.search"));
        this.searchBox.setChangedListener((search) -> {
            this.levelList.setSearch(search);
        });
        this.levelList = new WorkspaceListWidget(this, this.client, this.width, this.height, 48, this.height - 64, 36, this.searchBox.getText(), this.levelList);
        this.addSelectableChild(this.searchBox);
        this.addSelectableChild(this.levelList);
        this.launchButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("Launch"), (button) -> {
            // TODO: LAUNCH
        }).dimensions(this.width / 2 - 154, this.height - 52, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("Add Project"), (button) -> {
            // TODO: ADD PROJECT
        }).dimensions(this.width / 2 + 4, this.height - 52, 150, 20).build());
        this.editButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.of("Edit"), (button) -> {
            // TODO EDIT
        }).dimensions(this.width / 2 - 154, this.height - 28, 72, 20).build());
        this.unlinkButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.of("Unlink"), (button) -> {
            // TODO: UNLINK
        }).dimensions(this.width / 2 - 76, this.height - 28, 72, 20).build());
        this.settingsButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.of("Settings"), (button) -> {
            // TODO: SETTINGS
        }).dimensions(this.width / 2 + 4, this.height - 28, 72, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 + 82, this.height - 28, 72, 20).build());
        this.worldSelected(false);
        this.setInitialFocus(this.searchBox);
    }

    private Optional<WorldListWidget.WorldEntry> getSelectedAsOptional() {
        return Optional.empty();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers) ? true : this.searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    public void close() {
        this.client.setScreen(this.parent);
    }

    public boolean charTyped(char chr, int modifiers) {
        return this.searchBox.charTyped(chr, modifiers);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.levelList.render(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void worldSelected(boolean active) {
        this.launchButton.active = active;
        this.unlinkButton.active = active;
        this.editButton.active = active;
    }

    public void removed() {

    }

    public void workspaceSelected(boolean b)
    {

    }
}
