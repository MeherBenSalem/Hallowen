package com.nightbeam.hallowed.client;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.config.Configs;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

/**
 * The Hallowed HUD layer: the violet vignette of the Witching Hour, the title cards (dusk, midnight, the Horseman)
 * and the Spook trick's flash. Title cards are a dark band across the screen with wide-spaced capitals between two
 * ember-orange rules, a jack o'lantern at each end.
 */
public final class NightOverlay {
    private static final Identifier VIGNETTE = Hallowed.id("textures/gui/vignette.png");
    private static final Identifier SPOOK = Hallowed.id("textures/gui/spook.png");
    private static final Identifier ORNAMENT = Hallowed.id("textures/gui/ornament.png");
    private static final int EMBER = 0xFF9A3C;
    private static final int PALE = 0xF2DDBA;
    private static final int BLOOD = 0xE0453A;

    public enum Banner {
        DUSK("dusk", 120, 2.4f, 34, EMBER, 0.45f),
        WITCHING("witching", 170, 3.4f, 50, EMBER, 0.7f),
        HORSEMAN("horseman", 150, 3.2f, 48, BLOOD, 0.7f),
        FELLED("felled", 150, 3.2f, 48, EMBER, 0.7f),
        LEFT("left", 110, 2.4f, 34, PALE, 0.5f);

        final String key;
        final int length;
        final float scale;
        final int band;
        final int color;
        final float bandAlpha;

        Banner(String key, int length, float scale, int band, int color, float bandAlpha) {
            this.key = key;
            this.length = length;
            this.scale = scale;
            this.band = band;
            this.color = color;
            this.bandAlpha = bandAlpha;
        }
    }

    private static final Deque<Banner> QUEUE = new ArrayDeque<>();
    private static Banner current;
    private static int currentStart;
    private static int ticks;
    private static int spookStart = -1000;
    private static int sparkleStart = -1000;

    private NightOverlay() {
    }

    public static void tick() {
        ticks++;
        if (current != null && ticks - currentStart >= current.length) current = null;
        if (current == null && !QUEUE.isEmpty()) {
            current = QUEUE.poll();
            currentStart = ticks;
        }
    }

    public static void reset() {
        QUEUE.clear();
        current = null;
        spookStart = sparkleStart = -1000;
    }

    public static void banner(Banner banner) {
        if (!Configs.client().banners) return;
        if (QUEUE.size() < 3) QUEUE.add(banner);
    }

    public static void spook() {
        spookStart = ticks;
    }

    public static void sparkle() {
        sparkleStart = ticks;
    }

    /** Under the HUD: the vignette. */
    public static void renderUnder(GuiGraphicsExtractor graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !Configs.client().vignette) return;
        float strength = HallowedClient.intensity(partialTick) * 0.22f + HallowedClient.witching() * 0.33f;
        strength *= Math.min(1.2f, Configs.client().intensity);
        if (strength <= 0.01f) return;
        float pulse = 1 + 0.06f * Mth.sin((ticks + partialTick) * 0.05f);
        int width = graphics.guiWidth(), height = graphics.guiHeight();
        graphics.blit(RenderPipelines.GUI_TEXTURED, VIGNETTE, 0, 0, 0, 0, width, height, width, height,
                ARGB.colorFromFloat(Mth.clamp(strength * pulse, 0, 1), 0.36f, 0.12f, 0.42f));
    }

    /** Over the HUD: title cards, the Spook flash and the treat glow. */
    public static void renderOver(GuiGraphicsExtractor graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        float now = ticks + partialTick;
        int width = graphics.guiWidth(), height = graphics.guiHeight();
        graphics.nextStratum();
        if (current != null) renderBanner(graphics, minecraft.font, width, height, now - currentStart);
        if (now - sparkleStart < 20) {
            float a = 1 - (now - sparkleStart) / 20f;
            graphics.blit(RenderPipelines.GUI_TEXTURED, VIGNETTE, 0, 0, 0, 0, width, height, width, height,
                    ARGB.colorFromFloat(a * 0.35f, 1.0f, 0.7f, 0.25f));
        }
        if (now - spookStart < 26) renderSpook(graphics, width, height, now - spookStart);
    }

    private static void renderSpook(GuiGraphicsExtractor graphics, int width, int height, float age) {
        float flash = Math.max(0, 1 - age / 5f);
        if (flash > 0) graphics.fill(0, 0, width, height, argb(flash * 0.55f, 0xE8E2FF));
        float alpha = age < 3 ? age / 3f : 1 - smooth((age - 3) / 23f);
        float size = Math.min(width, height) * Mth.lerp(smooth(Math.min(1, age / 8f)), 0.55f, 1.05f);
        graphics.pose().pushMatrix();
        graphics.pose().translate(width / 2f - size / 2, height / 2f - size / 2 - size * 0.04f);
        graphics.pose().scale(size / 128f, size / 128f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SPOOK, 0, 0, 0, 0, 128, 128, 128, 128, ARGB.colorFromFloat(alpha * 0.92f, 1, 1, 1));
        graphics.pose().popMatrix();
    }

    private static void renderBanner(GuiGraphicsExtractor graphics, Font font, int width, int height, float age) {
        Banner banner = current;
        float in = smooth(Math.min(1, age / 16f));
        float out = 1 - smooth(Mth.clamp((age - (banner.length - 26)) / 26f, 0, 1));
        float alpha = in * out;
        if (alpha <= 0.02f) return;
        int cy = (int) (height * (banner == Banner.DUSK || banner == Banner.LEFT ? 0.28f : 0.36f));
        int band = banner.band;
        int slices = 48;
        for (int i = 0; i < slices; i++) {
            int x0 = width * i / slices, x1 = width * (i + 1) / slices;
            float edge = (float) Math.pow(Math.sin(Math.PI * (i + 0.5) / slices), 0.8);
            graphics.fill(x0, cy - band / 2, x1, cy + band / 2, argb(alpha * banner.bandAlpha * edge, 0x07040A));
            int rule = argb(alpha * 0.9f * edge * edge, banner.color);
            graphics.fill(x0, cy - band / 2 - 1, x1, cy - band / 2, rule);
            graphics.fill(x0, cy + band / 2, x1, cy + band / 2 + 1, rule);
        }
        Component title = Component.translatable("banner.hallowed." + banner.key + ".title");
        Component subtitle = Component.translatable("banner.hallowed." + banner.key + ".subtitle");
        float spacing = Mth.lerp(in, 5.0f, 1.8f);
        boolean hasSubtitle = !subtitle.getString().isEmpty();
        float titleY = cy - (hasSubtitle ? 7 : 0) - font.lineHeight * banner.scale / 2f;
        float titleWidth = drawSpaced(graphics, font, title, width / 2f, titleY, banner.scale, spacing, argb(alpha, banner.color));
        if (hasSubtitle) {
            drawSpaced(graphics, font, subtitle, width / 2f, cy + band / 2f - font.lineHeight * 1.3f - 2, 1.25f, 0.8f, argb(alpha * 0.92f, PALE));
        }
        int ornament = (int) (14 * banner.scale / 2.4f);
        int ox = (int) (titleWidth / 2 + 10 + ornament);
        int oy = (int) (titleY + font.lineHeight * banner.scale / 2f - ornament / 2f);
        int color = ARGB.colorFromFloat(alpha, 1, 1, 1);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ORNAMENT, width / 2 - ox - ornament / 2, oy, 0, 0, ornament, ornament, ornament, ornament, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ORNAMENT, width / 2 + ox - ornament / 2, oy, 0, 0, ornament, ornament, ornament, ornament, color);
    }

    /** Draws text letter by letter with extra spacing; returns its scaled width. */
    private static float drawSpaced(GuiGraphicsExtractor graphics, Font font, Component text, float cx, float y, float scale, float spacing,
            int color) {
        String raw = text.getString();
        float width = 0;
        for (int i = 0; i < raw.length(); i++) width += font.width(String.valueOf(raw.charAt(i))) + (i > 0 ? spacing : 0);
        if (((color >>> 24) & 0xFF) < 6) return width * scale;
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx - width * scale / 2f, y);
        graphics.pose().scale(scale, scale);
        float x = 0;
        for (int i = 0; i < raw.length(); i++) {
            String c = String.valueOf(raw.charAt(i));
            FormattedCharSequence seq = Component.literal(c).withStyle(text.getStyle()).getVisualOrderText();
            graphics.text(font, seq, (int) x, 0, color, true);
            x += font.width(c) + spacing;
        }
        graphics.pose().popMatrix();
        return width * scale;
    }

    private static float smooth(float t) {
        t = Mth.clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    private static int argb(float alpha, int rgb) {
        int a = Mth.clamp((int) (alpha * 255), 0, 255);
        return a << 24 | (rgb & 0xFFFFFF);
    }
}
