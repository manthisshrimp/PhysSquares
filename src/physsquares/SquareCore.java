package physsquares;

import physsquares.update.EntityUpdater;
import physsquares.entity.PhysEntity;
import j2dgl.Core;
import j2dgl.render.Renderer;
import j2dgl.update.Updater;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Random;
import javax.imageio.ImageIO;

public class SquareCore extends Core {

    private final Renderer entityRenderer = new Renderer();
    private final EntityUpdater entityUpdater = new EntityUpdater();
    private final Renderer uiRenderer = new Renderer();
    private final Updater uiUpdater = new Updater();
    private UIController uiController;

    private final DecimalFormat df = new DecimalFormat("##.###");
    private final Random random = new Random();

    private boolean showLoopDebug = false;
    private boolean lockRenderer = false;
    private boolean rendererLocked = true;

    private long runs = 0;
    private double totalEntityUpdateTime = 0;
    private long checkTime;

    public static void main(String[] args) {
        // (+ 2)'s are to compensate for an insets issue with the JFrame.
        new SquareCore(640, 360).startLoop();
    }

    public SquareCore(int width, int height) {
        super(width, height);
        df.setRoundingMode(RoundingMode.DOWN);
    }

    @Override
    protected void init() {
        uiController = new UIController(uiUpdater, uiRenderer, inputHandler, this, entityUpdater);
        frame.setTitle("Momentum Squares -<[Esc] Menu>- -<[9, 0] debug info>-");
        URL imageURL = SquareCore.class.getResource("res/ms.png");
        if (imageURL != null) {
            try {
                frame.setIconImage(ImageIO.read(imageURL));
            } catch (IOException ex) {
            }
        }
        generateEntities(100, 5, 5, 500, 500, 1, 1);
    }

    public void generateEntities(int entityCount, int minEntitySize, int maxEntitySize,
            int minEntityMass, int maxEntityMass, int minEntitySpeed, int maxEntitySpeed) {

        lockRenderer = true;
        while (!rendererLocked) {
            // Wait for renderer to lock.
            System.out.println("WAITING");
        }

        entityUpdater.clear();
        entityRenderer.clear();

        for (int i = 0; i < entityCount; i++) {
            int size = (maxEntitySize > minEntitySize)
                    ? random.nextInt(maxEntitySize - minEntitySize) + minEntitySize
                    : minEntitySize;
            PhysEntity newEntity = new PhysEntity(
                    random.nextInt(contentResolution.width - 10 - maxEntitySize) + 10,
                    random.nextInt(contentResolution.height - 10 - maxEntitySize) + 10,
                    size,
                    size,
                    (maxEntityMass > minEntityMass)
                            ? random.nextInt(maxEntityMass - minEntityMass) + minEntityMass
                            : minEntityMass);
            newEntity.applyMovement(
                    (random.nextInt(10) + 1) * (random.nextBoolean() ? 1 : -1),
                    (random.nextInt(10) + 1) * (random.nextBoolean() ? 1 : -1),
                    0.1);
                    //                    0,
                    //                    1,
//                    (maxEntitySpeed > minEntitySpeed)
//                            ? random.nextInt(maxEntitySpeed - minEntitySpeed) + minEntitySpeed
//                            : minEntitySpeed);
            entityRenderer.addDrawable(newEntity);
            entityUpdater.addUpdatable(newEntity);
        }

//        generateWalls();
        lockRenderer = false;
    }

    private void generateWalls() {
        PhysEntity topWall = new PhysEntity(0, 0,
                contentResolution.width - 10, 10, 2000000000, Color.DARK_GRAY);
        PhysEntity leftWall = new PhysEntity(0, 10,
                10, contentResolution.height - 10, 2000000000, Color.DARK_GRAY);
        PhysEntity bottomWall = new PhysEntity(10, contentResolution.height - 10,
                contentResolution.width - 10, 10, 2000000000, Color.DARK_GRAY);
        PhysEntity rightWall = new PhysEntity(contentResolution.width - 10, 0,
                10, contentResolution.width - 10, 2000000000, Color.DARK_GRAY);
        entityRenderer.addDrawables(topWall, leftWall, bottomWall, rightWall);
        entityUpdater.addUpdatables(topWall, leftWall, bottomWall, rightWall);
    }

    public void resetStats() {
        runs = 0;
        totalEntityUpdateTime = 0;
    }

    @Override
    protected void update() {
        // Handle key presses
        if (inputHandler.isKeyDownConsume(KeyEvent.VK_ESCAPE)) {
            uiController.menuVisible = !uiController.menuVisible;
        }
        if (inputHandler.isKeyDownConsume(KeyEvent.VK_9)) {
            showLoopDebug = !showLoopDebug;
        }
        if (uiController.menuVisible) {
            uiUpdater.updateAll();
        } else {
            checkTime = System.nanoTime();
            entityUpdater.updateAll();
            totalEntityUpdateTime += System.nanoTime() - checkTime;
            runs++;
        }
    }

    @Override
    public void draw(Graphics2D g2, int xOffset, int yOffset) {
        if (lockRenderer) {
            rendererLocked = true;
        } else {
            rendererLocked = false;
            entityRenderer.drawAll(g2);
        }
        if (uiController.menuVisible) {
            uiRenderer.drawAll(g2);
        }
        if (showLoopDebug) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, 165, 65);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            if (entityUpdater.compareRunsGPU > 0) {
                g2.drawString("Compare Avg : " + df.format(entityUpdater.totalCompareLoopTime / entityUpdater.compareRunsGPU / 1000000)
                        + "ms", 2, 12);
            }
            if (entityUpdater.updateRunsGPU > 0) {
                g2.drawString("Momentum Avg: " + df.format(entityUpdater.totalMomentumLoopTime / entityUpdater.updateRunsGPU / 1000000)
                        + "ms", 2, 24);
            }
            if (runs > 0) {
                g2.drawString("Movement Avg: " + df.format(entityUpdater.totalEntityUpdateTime / runs / 1000000)
                        + "ms", 2, 36);
                if (entityUpdater.compareRunsGPU > 0 && entityUpdater.updateRunsGPU > 0) {
                    g2.drawString("Combined Avg: " + df.format(
                            (entityUpdater.totalCompareLoopTime / entityUpdater.compareRunsGPU / 1000000)
                            + (entityUpdater.totalMomentumLoopTime / entityUpdater.updateRunsGPU / 1000000)
                            + (entityUpdater.totalEntityUpdateTime / runs / 1000000)) + "ms", 2, 48);
                }
                g2.drawString("Total Avg   : " + df.format(totalEntityUpdateTime / runs / 1000000)
                        + "ms", 2, 60);
            }
        }
    }
}
