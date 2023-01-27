//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.volmit.fuse.screen.widget;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.volmit.fuse.screen.WorkspaceScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorkspaceListWidget extends AlwaysSelectedEntryListWidget<WorkspaceListWidget.Entry> {
    static final Logger LOGGER = LogUtils.getLogger();
    static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    static final Identifier UNKNOWN_SERVER_LOCATION = new Identifier("textures/misc/unknown_server.png");
    static final Identifier WORLD_SELECTION_LOCATION = new Identifier("textures/gui/world_selection.png");
    static final Text FROM_NEWER_VERSION_FIRST_LINE;
    static final Text FROM_NEWER_VERSION_SECOND_LINE;
    static final Text SNAPSHOT_FIRST_LINE;
    static final Text SNAPSHOT_SECOND_LINE;
    static final Text LOCKED_TEXT;
    static final Text CONVERSION_TOOLTIP;
    private final WorkspaceScreen parent;
    private CompletableFuture<List<LevelSummary>> levelsFuture;
    @Nullable
    private List<LevelSummary> levels;
    private String search;
    private final LoadingEntry loadingEntry;

    public WorkspaceListWidget(WorkspaceScreen parent, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, String search, @Nullable WorkspaceListWidget oldWidget) {
        super(client, width, height, top, bottom, itemHeight);
        this.parent = parent;
        this.loadingEntry = new LoadingEntry(client);
        this.search = search;
        if (oldWidget != null) {
            this.levelsFuture = oldWidget.levelsFuture;
        } else {
            this.levelsFuture = this.loadLevels();
        }

        this.show(this.tryGet());
    }

    @Nullable
    private List<LevelSummary> tryGet() {
        try {
            return (List)this.levelsFuture.getNow(null);
        } catch (CancellationException | CompletionException var2) {
            return null;
        }
    }

    void load() {
        this.levelsFuture = this.loadLevels();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        List<LevelSummary> list = this.tryGet();
        if (list != this.levels) {
            this.show(list);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void show(@Nullable List<LevelSummary> levels) {
        if (levels == null) {
            this.showLoadingScreen();
        } else {
            this.showSummaries(this.search, levels);
        }

        this.levels = levels;
    }

    public void setSearch(String search) {
        if (this.levels != null && !search.equals(this.search)) {
            this.showSummaries(search, this.levels);
        }

        this.search = search;
    }

    private CompletableFuture<List<LevelSummary>> loadLevels() {
        LevelStorage.LevelList levelList;
        try {
            levelList = this.client.getLevelStorage().getLevelList();
        } catch (LevelStorageException var3) {
            LOGGER.error("Couldn't load level list", var3);
            this.showUnableToLoadScreen(var3.getMessageText());
            return CompletableFuture.completedFuture(List.of());
        }

        if (levelList.isEmpty()) {
            //CreateWorldScreen.create(this.client, (Screen)null);
            // Go to create screen TODO: Make this work
            return CompletableFuture.completedFuture(List.of());
        } else {
            return this.client.getLevelStorage().loadSummaries(levelList).exceptionally((throwable) -> {
                this.client.setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Couldn't load level list"));
                return List.of();
            });
        }
    }

    private void showSummaries(String search, List<LevelSummary> summaries) {
        this.clearEntries();
        search = search.toLowerCase(Locale.ROOT);
        Iterator var3 = summaries.iterator();

        while(var3.hasNext()) {
            LevelSummary levelSummary = (LevelSummary)var3.next();
            if (this.shouldShow(search, levelSummary)) {
                this.addEntry(new WorkspaceEntry(this, levelSummary));
            }
        }

        this.narrateScreenIfNarrationEnabled();
    }

    private boolean shouldShow(String search, LevelSummary summary) {
        return summary.getDisplayName().toLowerCase(Locale.ROOT).contains(search) || summary.getName().toLowerCase(Locale.ROOT).contains(search);
    }

    private void showLoadingScreen() {
        this.clearEntries();
        this.addEntry(this.loadingEntry);
        this.narrateScreenIfNarrationEnabled();
    }

    private void narrateScreenIfNarrationEnabled() {
        this.parent.narrateScreenIfNarrationEnabled(true);
    }

    private void showUnableToLoadScreen(Text message) {
        this.client.setScreen(new FatalErrorScreen(Text.translatable("selectWorld.unable_to_load"), message));
    }

    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 20;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    protected boolean isFocused() {
        return this.parent.getFocused() == this;
    }

    public void setSelected(@Nullable Entry entry) {
        super.setSelected(entry);
        this.parent.workspaceSelected(entry != null && entry.isAvailable());
    }

    protected void moveSelection(EntryListWidget.MoveDirection direction) {
        this.moveSelectionIf(direction, Entry::isAvailable);
    }

    public Optional<WorkspaceEntry> getSelectedAsOptional() {
        Entry entry = (Entry)this.getSelectedOrNull();
        if (entry instanceof WorkspaceEntry workspaceEntry) {
            return Optional.of(workspaceEntry);
        } else {
            return Optional.empty();
        }
    }

    public WorkspaceScreen getParent() {
        return this.parent;
    }

    public void appendNarrations(NarrationMessageBuilder builder) {
        if (this.children().contains(this.loadingEntry)) {
            this.loadingEntry.appendNarrations(builder);
        } else {
            super.appendNarrations(builder);
        }
    }

    static {
        FROM_NEWER_VERSION_FIRST_LINE = Text.translatable("selectWorld.tooltip.fromNewerVersion1").formatted(Formatting.RED);
        FROM_NEWER_VERSION_SECOND_LINE = Text.translatable("selectWorld.tooltip.fromNewerVersion2").formatted(Formatting.RED);
        SNAPSHOT_FIRST_LINE = Text.translatable("selectWorld.tooltip.snapshot1").formatted(Formatting.GOLD);
        SNAPSHOT_SECOND_LINE = Text.translatable("selectWorld.tooltip.snapshot2").formatted(Formatting.GOLD);
        LOCKED_TEXT = Text.translatable("selectWorld.locked").formatted(Formatting.RED);
        CONVERSION_TOOLTIP = Text.translatable("selectWorld.conversion.tooltip").formatted(Formatting.RED);
    }

    @Environment(EnvType.CLIENT)
    public static class LoadingEntry extends Entry {
        private static final Text LOADING_LIST_TEXT = Text.translatable("selectWorld.loading_list");
        private final MinecraftClient client;

        public LoadingEntry(MinecraftClient client) {
            this.client = client;
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int i = (this.client.currentScreen.width - this.client.textRenderer.getWidth(LOADING_LIST_TEXT)) / 2;
            Objects.requireNonNull(this.client.textRenderer);
            int j = y + (entryHeight - 9) / 2;
            this.client.textRenderer.draw(matrices, LOADING_LIST_TEXT, (float)i, (float)j, 16777215);
            String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
            int k = (this.client.currentScreen.width - this.client.textRenderer.getWidth(string)) / 2;
            Objects.requireNonNull(this.client.textRenderer);
            int l = j + 9;
            this.client.textRenderer.draw(matrices, string, (float)k, (float)l, 8421504);
        }

        public Text getNarration() {
            return LOADING_LIST_TEXT;
        }

        public boolean isAvailable() {
            return false;
        }
    }

    @Environment(EnvType.CLIENT)
    public final class WorkspaceEntry extends Entry implements AutoCloseable {
        private static final int field_32435 = 32;
        private static final int field_32436 = 32;
        private static final int field_32437 = 0;
        private static final int field_32438 = 32;
        private static final int field_32439 = 64;
        private static final int field_32440 = 96;
        private static final int field_32441 = 0;
        private static final int field_32442 = 32;
        private final MinecraftClient client;
        private final WorkspaceScreen screen;
        private final LevelSummary level;
        private final Identifier iconLocation;
        @Nullable
        private Path iconPath;
        private long time;

        public WorkspaceEntry(WorkspaceListWidget levelList, LevelSummary level) {
            this.client = levelList.client;
            this.screen = levelList.getParent();
            this.level = level;
            String string = level.getName();
            String var10004 = Util.replaceInvalidChars(string, Identifier::isPathCharacterValid);
            this.iconLocation = new Identifier("minecraft", "worlds/" + var10004 + "/" + Hashing.sha1().hashUnencodedChars(string) + "/icon");
            this.iconPath = level.getIconPath();
            if (!Files.isRegularFile(this.iconPath, new LinkOption[0])) {
                this.iconPath = null;
            }
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String string = this.level.getDisplayName();
            String var10000 = this.level.getName();
            String string2 = var10000 + " (" + WorkspaceListWidget.DATE_FORMAT.format(new Date(this.level.getLastPlayed())) + ")";
            if (StringUtils.isEmpty(string)) {
                var10000 = I18n.translate("selectWorld.world", new Object[0]);
                string = var10000 + " " + (index + 1);
            }

            Text text = this.level.getDetails();
            this.client.textRenderer.draw(matrices, string, (float)(x + 32 + 3), (float)(y + 1), 16777215);
            TextRenderer var17 = this.client.textRenderer;
            float var10003 = (float)(x + 32 + 3);
            Objects.requireNonNull(this.client.textRenderer);
            var17.draw(matrices, string2, var10003, (float)(y + 9 + 3), 8421504);
            var17 = this.client.textRenderer;
            var10003 = (float)(x + 32 + 3);
            Objects.requireNonNull(this.client.textRenderer);
            int var10004 = y + 9;
            Objects.requireNonNull(this.client.textRenderer);
            var17.draw(matrices, text, var10003, (float)(var10004 + 9 + 3), 8421504);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if ((Boolean)this.client.options.getTouchscreen().getValue() || hovered) {
                RenderSystem.setShaderTexture(0, WorkspaceListWidget.WORLD_SELECTION_LOCATION);
                DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int i = mouseX - x;
                boolean bl = i < 32;
                int j = bl ? 32 : 0;
                if (this.level.isLocked()) {
                    DrawableHelper.drawTexture(matrices, x, y, 96.0F, (float)j, 32, 32, 256, 256);
                    if (bl) {
                        this.screen.setTooltip(this.client.textRenderer.wrapLines(WorkspaceListWidget.LOCKED_TEXT, 175));
                    }
                } else if (this.level.requiresConversion()) {
                    DrawableHelper.drawTexture(matrices, x, y, 96.0F, (float)j, 32, 32, 256, 256);
                    if (bl) {
                        this.screen.setTooltip(this.client.textRenderer.wrapLines(WorkspaceListWidget.CONVERSION_TOOLTIP, 175));
                    }
                } else if (this.level.isDifferentVersion()) {
                    DrawableHelper.drawTexture(matrices, x, y, 32.0F, (float)j, 32, 32, 256, 256);
                    if (this.level.isFutureLevel()) {
                        DrawableHelper.drawTexture(matrices, x, y, 96.0F, (float)j, 32, 32, 256, 256);
                        if (bl) {
                            this.screen.setTooltip(ImmutableList.of(WorkspaceListWidget.FROM_NEWER_VERSION_FIRST_LINE.asOrderedText(), WorkspaceListWidget.FROM_NEWER_VERSION_SECOND_LINE.asOrderedText()));
                        }
                    } else if (!SharedConstants.getGameVersion().isStable()) {
                        DrawableHelper.drawTexture(matrices, x, y, 64.0F, (float)j, 32, 32, 256, 256);
                        if (bl) {
                            this.screen.setTooltip(ImmutableList.of(WorkspaceListWidget.SNAPSHOT_FIRST_LINE.asOrderedText(), WorkspaceListWidget.SNAPSHOT_SECOND_LINE.asOrderedText()));
                        }
                    }
                } else {
                    DrawableHelper.drawTexture(matrices, x, y, 0.0F, (float)j, 32, 32, 256, 256);
                }
            }

        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return true;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        public void close() {

        }

        @Override
        public Text getNarration() {
            return Text.of("");
        }

        public void play() {
        }

        public void edit() {
        }

        public void deleteIfConfirmed() {
        }

        public void recreate() {
        }
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> implements AutoCloseable {
        public Entry() {
        }

        public abstract boolean isAvailable();

        public void close() {
        }
    }
}
