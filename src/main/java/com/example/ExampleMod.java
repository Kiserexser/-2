package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;
import org.joml.Matrix4f;
import java.awt.Color;
import java.util.Random;

public class ExampleMod implements ClientModInitializer {
    
    private static boolean menuOpen = false;
    private static final int GUI_WIDTH = 420;
    private static final int GUI_HEIGHT = 500;
    
    // ========== ЧИТЫ (15 функций) ==========
    private static boolean killAura = true;
    private static boolean velocity = true;
    private static boolean noFall = true;
    private static boolean speed = false;
    private static boolean fly = false;
    private static boolean autoSprint = true;
    private static boolean noSlow = true;
    private static boolean waterWalk = false;
    private static boolean step = false;
    private static boolean antiKnockback = false;
    private static boolean reach = true;
    private static boolean autoClicker = false;
    private static boolean fastPlace = false;
    private static boolean chestStealer = false;
    private static boolean noFallDamage = true;
    
    // ========== ВИЗУАЛЫ (15 функций) ==========
    private static boolean esp = true;
    private static boolean tracer = true;
    private static boolean healthBar = true;
    private static boolean nametags = true;
    private static boolean boxEsp = true;
    private static boolean fullBright = true;
    private static boolean noHurtCam = false;
    private static boolean chams = false;
    private static boolean glow = false;
    private static boolean skeleton = false;
    private static boolean fovCircle = true;
    private static boolean tracers = true;
    private static boolean wallHack = false;
    private static boolean xray = false;
    private static boolean noFireOverlay = false;
    
    private static float reachDistance = 3.4f;
    private static Random random = new Random();
    private static long lastAttack = 0;
    private static KeyBinding menuKey;
    private static MinecraftClient mc;
    
    // Для кликов
    private static long lastClickTime = 0;
    private static final long CLICK_DELAY = 150;
    
    @Override
    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();
        
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.lunxes.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "Lunxes Client"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (menuKey.wasPressed()) menuOpen = !menuOpen;
            
            // ========== ПРИМЕНЕНИЕ ЧИТОВ ==========
            if (fullBright) client.options.getGamma().setValue(100.0);
            else client.options.getGamma().setValue(1.0);
            
            if (noHurtCam && client.player.hurtTime > 0) client.player.hurtTime = 0;
            
            if (autoSprint && client.player.isOnGround() && !client.player.isSprinting()) {
                client.player.setSprinting(true);
            }
            
            if (killAura && !menuOpen && client.player.isOnGround()) {
                long now = System.currentTimeMillis();
                if (now - lastAttack < 1700 + random.nextInt(171)) return;
                
                Box box = client.player.getBoundingBox().expand(reachDistance);
                LivingEntity target = null;
                for (LivingEntity e : client.world.getEntitiesByClass(LivingEntity.class, box,
                        e -> e != client.player && e.isAlive() && !(e instanceof PlayerEntity && ((PlayerEntity)e).isCreative()))) {
                    if (client.player.distanceTo(e) <= reachDistance) {
                        target = e;
                        break;
                    }
                }
                if (target != null) {
                    float yaw = (float)(Math.toDegrees(Math.atan2(target.getX() - client.player.getX(), target.getZ() - client.player.getZ())));
                    float pitch = (float)(-Math.toDegrees(Math.atan2(target.getY() + 0.8 - client.player.getEyeY(),
                        Math.sqrt(Math.pow(target.getX() - client.player.getX(), 2) + Math.pow(target.getZ() - client.player.getZ(), 2)))));
                    yaw += (random.nextFloat() - 0.5f) * 3;
                    pitch += (random.nextFloat() - 0.5f) * 2;
                    client.player.setYaw(yaw);
                    client.player.setPitch(pitch);
                    client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                    client.interactionManager.attackEntity(client.player, target);
                    client.player.swingHand(Hand.MAIN_HAND);
                    lastAttack = now;
                }
            }
            
            if (velocity && !menuOpen && client.player.hurtTime > 0 && random.nextInt(10) == 0) {
                client.player.setVelocity(client.player.getVelocity().x * 0.6, client.player.getVelocity().y, client.player.getVelocity().z * 0.6);
            }
            if (antiKnockback && client.player.hurtTime > 0) {
                client.player.setVelocity(client.player.getVelocity().x * 0.1, client.player.getVelocity().y, client.player.getVelocity().z * 0.1);
            }
            if (noFall && client.player.fallDistance > 3) {
                client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                client.player.fallDistance = 0;
            }
            if (speed && client.player.isOnGround()) {
                Vec3d vel = client.player.getVelocity();
                client.player.setVelocity(vel.x * 1.3, vel.y, vel.z * 1.3);
            }
            if (fly && !client.player.isOnGround()) {
                client.player.getAbilities().allowFlying = true;
                client.player.getAbilities().isFlying = true;
            } else if (!fly && !client.player.isCreative()) {
                client.player.getAbilities().allowFlying = false;
                client.player.getAbilities().isFlying = false;
            }
        });
        
        HudRenderCallback.EVENT.register(this::renderMenu);
        WorldRenderEvents.LAST.register(this::onWorldRender);
    }
    
    private void renderMenu(MatrixStack matrices, float delta) {
        if (!menuOpen) return;
        
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        int x = w / 2 - GUI_WIDTH / 2;
        int y = h / 2 - GUI_HEIGHT / 2;
        
        // Фон (тёмно-серый, полупрозрачный)
        fill(matrices, x, y, x + GUI_WIDTH, y + GUI_HEIGHT, new Color(30, 30, 35, 240).getRGB());
        drawOutline(matrices, x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 2, new Color(80, 80, 100, 255).getRGB());
        
        // Заголовок
        drawString(matrices, "§l§fLunxes Client", x + 15, y + 12, new Color(255, 255, 255, 255).getRGB());
        drawString(matrices, "§7Click to toggle modules", x + 15, y + 28, new Color(150, 150, 170, 255).getRGB());
        
        fill(matrices, x + 10, y + 42, x + GUI_WIDTH - 10, y + 43, new Color(80, 80, 100, 255).getRGB());
        
        // ========== ЛЕВАЯ КОЛОНКА - ЧИТЫ ==========
        drawString(matrices, "§l§fCOMBAT & MOVEMENT", x + 15, y + 58, new Color(200, 200, 220, 255).getRGB());
        
        String[] combatNames = {"KillAura", "Velocity", "AntiKnockback", "NoFall", "Speed", "Fly", "AutoSprint", "NoSlow", "WaterWalk", "Step", "Reach", "AutoClicker", "FastPlace", "ChestStealer", "NoFallDamage"};
        boolean[] combatStates = {killAura, velocity, antiKnockback, noFall, speed, fly, autoSprint, noSlow, waterWalk, step, reach, autoClicker, fastPlace, chestStealer, noFallDamage};
        
        for (int i = 0; i < combatNames.length; i++) {
            int btnY = y + 76 + i * 14;
            // Белый если включен, серый если выключен
            Color col = combatStates[i] ? new Color(255, 255, 255, 255) : new Color(120, 120, 140, 255);
            drawString(matrices, (combatStates[i] ? "✔ " : "✘ ") + combatNames[i], x + 25, btnY, col.getRGB());
            
            // Обработка клика
            handleClick(x + 20, x + 140, btnY, btnY + 12, i, combatStates);
        }
        
        // ========== ПРАВАЯ КОЛОНКА - ВИЗУАЛЫ ==========
        drawString(matrices, "§l§fVISUALS", x + 220, y + 58, new Color(200, 200, 220, 255).getRGB());
        
        String[] visualNames = {"ESP", "BoxESP", "Tracers", "HealthBar", "Nametags", "FullBright", "NoHurtCam", "Chams", "Glow", "Skeleton", "FOV Circle", "WallHack", "XRay", "NoFireOverlay"};
        boolean[] visualStates = {esp, boxEsp, tracer, healthBar, nametags, fullBright, noHurtCam, chams, glow, skeleton, fovCircle, wallHack, xray, noFireOverlay};
        
        for (int i = 0; i < visualNames.length; i++) {
            int btnY = y + 76 + i * 14;
            Color col = visualStates[i] ? new Color(255, 255, 255, 255) : new Color(120, 120, 140, 255);
            drawString(matrices, (visualStates[i] ? "✔ " : "✘ ") + visualNames[i], x + 230, btnY, col.getRGB());
            
            handleClickVisual(x + 225, x + 360, btnY, btnY + 12, i, visualStates);
        }
        
        // Нижняя панель
        fill(matrices, x, y + GUI_HEIGHT - 32, x + GUI_WIDTH, y + GUI_HEIGHT, new Color(20, 20, 25, 250).getRGB());
        drawString(matrices, "§7Right Shift §8- §7Close Menu", x + 15, y + GUI_HEIGHT - 23, new Color(150, 150, 170, 255).getRGB());
        drawString(matrices, "§7Lunxes §8- §7v1.0", x + GUI_WIDTH - 90, y + GUI_HEIGHT - 23, new Color(150, 150, 170, 255).getRGB());
        
        // Обновляем переменные после кликов
        killAura = combatStates[0];
        velocity = combatStates[1];
        antiKnockback = combatStates[2];
        noFall = combatStates[3];
        speed = combatStates[4];
        fly = combatStates[5];
        autoSprint = combatStates[6];
        noSlow = combatStates[7];
        waterWalk = combatStates[8];
        step = combatStates[9];
        reach = combatStates[10];
        autoClicker = combatStates[11];
        fastPlace = combatStates[12];
        chestStealer = combatStates[13];
        noFallDamage = combatStates[14];
        
        esp = visualStates[0];
        boxEsp = visualStates[1];
        tracer = visualStates[2];
        healthBar = visualStates[3];
        nametags = visualStates[4];
        fullBright = visualStates[5];
        noHurtCam = visualStates[6];
        chams = visualStates[7];
        glow = visualStates[8];
        skeleton = visualStates[9];
        fovCircle = visualStates[10];
        wallHack = visualStates[11];
        xray = visualStates[12];
        noFireOverlay = visualStates[13];
    }
    
    private void handleClick(int x1, int x2, int y1, int y2, int index, boolean[] states) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < CLICK_DELAY) return;
        
        double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
        double mouseY = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();
        
        if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
            if (mc.mouse.wasLeftButtonClicked()) {
                states[index] = !states[index];
                lastClickTime = now;
            }
        }
    }
    
    private void handleClickVisual(int x1, int x2, int y1, int y2, int index, boolean[] states) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < CLICK_DELAY) return;
        
        double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
        double mouseY = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();
        
        if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
            if (mc.mouse.wasLeftButtonClicked()) {
                states[index] = !states[index];
                lastClickTime = now;
            }
        }
    }
    
    private void onWorldRender(WorldRenderEvents.LastContext ctx) {
        if (mc.player == null || mc.world == null) return;
        
        MatrixStack matrices = ctx.matrixStack();
        Camera camera = ctx.camera();
        Matrix4f posMat = ctx.positionMatrix();
        
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity)) continue;
            if (e.squaredDistanceTo(mc.player) > 2500) continue;
            if (e instanceof PlayerEntity && ((PlayerEntity)e).isCreative()) continue;
            
            if ((esp || boxEsp) && e instanceof LivingEntity) drawEspBox(e, matrices, posMat, camera);
            if (tracer && e instanceof LivingEntity) drawTracer(e, matrices, posMat, camera);
            if (healthBar && e instanceof LivingEntity) drawHealthBar((LivingEntity)e, matrices, posMat, camera);
            if (nametags) drawNametag(e, matrices, posMat, camera);
        }
    }
    
    private void drawEspBox(Entity e, MatrixStack matrices, Matrix4f posMat, Camera camera) {
        matrices.push();
        Vec3d camPos = camera.getPos();
        double x = e.getX() - camPos.x;
        double y = e.getY() - camPos.y;
        double z = e.getZ() - camPos.z;
        Box box = e.getBoundingBox();
        double w = (box.maxX - box.minX) / 2;
        double h = box.maxY - box.minY;
        matrices.translate(x, y, z);
        matrices.multiply(camera.getRotation());
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        // Белые линии для ESP
        buffer.vertex(posMat, -w, 0, -w).color(1,1,1,1).next();
        buffer.vertex(posMat, -w, 0, w).color(1,1,1,1).next();
        buffer.vertex(posMat, -w, 0, w).color(1,1,1,1).next();
        buffer.vertex(posMat, w, 0, w).color(1,1,1,1).next();
        buffer.vertex(posMat, w, 0, w).color(1,1,1,1).next();
        buffer.vertex(posMat, w, 0, -w).color(1,1,1,1).next();
        buffer.vertex(posMat, w, 0, -w).color(1,1,1,1).next();
        buffer.vertex(posMat, -w, 0, -w).color(1,1,1,1).next();
        buffer.vertex(posMat, -w, h, -w).color(1,1,1,1).next();
        buffer.vertex(posMat, -w, h, w).color(1,1,1,1).next();
        buffer.vertex(posMat, -w, h, w).color(1,1,1,1).next();
        buffer.vertex(posMat, w, h, w).color(1,1,1,1).next();
        buffer.vertex(posMat, w, h, w).color(1,1,1,1).next();
        buffer.vertex(posMat, w, h, -w).color(1,1,1,1).next();
        buffer.vertex(posMat, w, h, -w).color(1,1,1,1).next();
        buffer.vertex(posMat, -w, h, -w).color(1,1,1,1).next();
        tess.draw();
        matrices.pop();
    }
    
    private void drawTracer(Entity e, MatrixStack matrices, Matrix4f posMat, Camera camera) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Vec3d camPos = camera.getPos();
        buffer.vertex(posMat, 0, 0, 0).color(1,1,1,1).next();
        buffer.vertex(posMat, e.getX() - camPos.x, e.getY() + 1 - camPos.y, e.getZ() - camPos.z).color(1,1,1,1).next();
        tess.draw();
    }
    
    private void drawHealthBar(LivingEntity e, MatrixStack matrices, Matrix4f posMat, Camera camera) {
        Vec3d camPos = camera.getPos();
        double x = e.getX() - camPos.x;
        double y = e.getY() + e.getHeight() + 0.3 - camPos.y;
        double z = e.getZ() - camPos.z;
        float health = e.getHealth() / e.getMaxHealth();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(posMat, x - 0.3, y, z).color(0.1f,0.1f,0.1f,0.8f).next();
        buffer.vertex(posMat, x + 0.3, y, z).color(0.1f,0.1f,0.1f,0.8f).next();
        buffer.vertex(posMat, x + 0.3, y + 0.1, z).color(0.1f,0.1f,0.1f,0.8f).next();
        buffer.vertex(posMat, x - 0.3, y + 0.1, z).color(0.1f,0.1f,0.1f,0.8f).next();
        float r = 1 - health;
        float g = health;
        buffer.vertex(posMat, x - 0.3, y, z).color(r,g,0,0.9f).next();
        buffer.vertex(posMat, x - 0.3 + (0.6 * health), y, z).color(r,g,0,0.9f).next();
        buffer.vertex(posMat, x - 0.3 + (0.6 * health), y + 0.1, z).color(r,g,0,0.9f).next();
        buffer.vertex(posMat, x - 0.3, y + 0.1, z).color(r,g,0,0.9f).next();
        tess.draw();
    }
    
    private void drawNametag(Entity e, MatrixStack matrices, Matrix4f posMat, Camera camera) {
        Vec3d camPos = camera.getPos();
        double x = e.getX() - camPos.x;
        double y = e.getY() + e.getHeight() + 0.5 - camPos.y;
        double z = e.getZ() - camPos.z;
        String name = e.getName().getString();
        matrices.push();
        matrices.translate(x, y, z);
        matrices.multiply(camera.getRotation());
        matrices.scale(-0.02f, -0.02f, 0.02f);
        int width = mc.textRenderer.getWidth(name);
        fill(matrices, -width/2 - 2, -2, width/2 + 2, 10, new Color(0,0,0,150).getRGB());
        drawString(matrices, name, -width/2, 0, Color.WHITE.getRGB());
        matrices.pop();
    }
    
    private void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        Matrix4f mat = matrices.peek().getPositionMatrix();
        buffer.vertex(mat, x1, y2, 0).color(r,g,b,a).next();
        buffer.vertex(mat, x2, y2, 0).color(r,g,b,a).next();
        buffer.vertex(mat, x2, y1, 0).color(r,g,b,a).next();
        buffer.vertex(mat, x1, y1, 0).color(r,g,b,a).next();
        tess.draw();
    }
    
    private void drawOutline(MatrixStack matrices, int x1, int y1, int x2, int y2, int thickness, int color) {
        fill(matrices, x1, y1, x2, y1 + thickness, color);
        fill(matrices, x1, y2 - thickness, x2, y2, color);
        fill(matrices, x1, y1, x1 + thickness, y2, color);
        fill(matrices, x2 - thickness, y1, x2, y2, color);
    }
    
    private void drawString(MatrixStack matrices, String text, int x, int y, int color) {
        mc.textRenderer.draw(matrices, text, x, y, color);
    }
}
