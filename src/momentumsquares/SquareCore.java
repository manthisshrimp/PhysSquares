package momentumsquares;

import j2dgl.Core;
import j2dgl.render.Renderer;
import j2dgl.update.Updater;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

public class SquareCore extends Core {

    private final Renderer entityRenderer = new Renderer();
    private final MomentumUpdater entityUpdater = new MomentumUpdater();
    private final Renderer uiRenderer = new Renderer();
    private final Updater uiUpdater = new Updater();
    private final UIController uiController = new UIController(uiUpdater, 
            uiRenderer, mouse, mouseDown, this, entityUpdater);
    
    private final Color MENU_OUTLINE = new Color(0x365E63);
    private final Color MENU_BACKGROUND = new Color(0x151515);
    private final DecimalFormat df = new DecimalFormat("##.###");
    private final Random random = new Random();

    private boolean showLoopDebug = true;
    private boolean lockRenderer = false;
    private boolean rendererLocked = true;

    private long runs = 0;
    private double totalEntityUpdateTime = 0;
    private long checkTime;

    public static void main(String[] args) {
        // (+ 2)'s are to compensate for an insets issue with the JFrame.
        new SquareCore(1280 + 2, 720 + 2).startLoop();
    }

    public SquareCore(int width, int height) {
        super(width, height);
        df.setRoundingMode(RoundingMode.DOWN);
    }

    @Override
    protected void init() {
        frame.setTitle("Momentum Squares -<[Esc] Menu>- -<[9, 0] debug info>-");
        URL imageURL = SquareCore.class.getResource("res/ms.png");
        if (imageURL != null) {
            try {
                frame.setIconImage(ImageIO.read(imageURL));
            } catch (IOException ex) {
            }
        }
        generateEntities(750, 1, 3, 100, 2000, 1, 3);
    }

    public void generateEntities(int entityCount, int minEntitySize, int maxEntitySize,
            int minEntityMass, int maxEntityMass, int minEntitySpeed, int maxEntitySpeed) {

        lockRenderer = true;
        while (!rendererLocked) {
            // Wait for renderer to lock.
            System.out.println("WAITING");
        }

        entityUpdater.clear();
        entityRenderer.getRenderables().clear();

        for (int i = 0; i < entityCount; i++) {
            int size = (maxEntitySize != minEntitySize)
                    ? random.nextInt(maxEntitySize - minEntitySize) + minEntitySize
                    : minEntitySize;
            MomentumEntity newEntity = new MomentumEntity(
                    random.nextInt(getResolution().width - 10 - maxEntitySize) + 10,
                    random.nextInt(getResolution().height - 10 - maxEntitySize) + 10,
                    size,
                    size,
                    (maxEntityMass != minEntityMass)
                            ? random.nextInt(maxEntityMass - minEntityMass) + minEntityMass
                            : minEntityMass);
            newEntity.setTarget(
                    getResolution().width / 2,
                    getResolution().height / 2,
                    (maxEntitySpeed != minEntitySpeed)
                            ? random.nextInt(maxEntitySpeed - minEntitySpeed) + minEntitySpeed
                            : minEntitySpeed);
            entityRenderer.addRenderable(newEntity);
            entityUpdater.addUpdatable(newEntity);
        }

        generateWalls();

        lockRenderer = false;
    }

    private void generateWalls() {
        MomentumEntity topWall = new MomentumEntity(0, 0,
                getResolution().width - 10, 10, 2000000000, Color.DARK_GRAY);
        MomentumEntity leftWall = new MomentumEntity(0, 10,
                10, getResolution().height - 10, 2000000000, Color.DARK_GRAY);
        MomentumEntity bottomWall = new MomentumEntity(10, getResolution().height - 10,
                getResolution().width - 10, 10, 2000000000, Color.DARK_GRAY);
        MomentumEntity rightWall = new MomentumEntity(getResolution().width - 10, 0,
                10, getResolution().width - 10, 2000000000, Color.DARK_GRAY);
        entityRenderer.addRenderables(topWall, leftWall, bottomWall, rightWall);
        entityUpdater.addUpdatables(topWall, leftWall, bottomWall, rightWall);
    }

    public void resetStats() {
        runs = 0;
        totalEntityUpdateTime = 0;
    }

    @Override
    protected void update() {
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
    protected void draw(Graphics2D g2) {
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
            g2.fillRect(0, 0, 120, 64);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            if (entityUpdater.runs > 0) {
                g2.drawString("CL Avg: " + df.format(entityUpdater.totalCompareLoopTime / entityUpdater.runs / 1000000)
                        + "ms", 2, 12);
                g2.drawString("GL Avg: " + df.format(entityUpdater.totalGetLoopTime / entityUpdater.runs / 1000000)
                        + "ms", 2, 24);
                g2.drawString("ML Avg: " + df.format(entityUpdater.totalMomentumLoopTime / entityUpdater.runs / 1000000)
                        + "ms", 2, 36);
                g2.drawString("SL Avg: " + df.format(entityUpdater.totalSetLoopTime / entityUpdater.runs / 1000000)
                        + "ms", 2, 48);
            }
            if (runs > 0) {
                g2.drawString("TO Avg: " + df.format(totalEntityUpdateTime / runs / 1000000)
                        + "ms", 2, 60);
            }
        }
    }

    @Override
    protected void keysPressed(ArrayList<Integer> keyQueue) {
        if (keyQueue.contains(KeyEvent.VK_ESCAPE)) {
            uiController.menuVisible = !uiController.menuVisible;
            keyQueue.remove((Integer) KeyEvent.VK_ESCAPE);
        }
        if (keyQueue.contains(KeyEvent.VK_9)) {
            showLoopDebug = !showLoopDebug;
            keyQueue.remove((Integer) KeyEvent.VK_9);
        }
    }

    @Override
    protected void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    protected void processMouse(MouseEvent mouseEvent) {

    }

    @Override
    protected void beforeClose() {

    }

}
