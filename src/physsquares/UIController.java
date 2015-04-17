package physsquares;

import j2dgl.render.Renderer;
import j2dgl.ui.Button;
import j2dgl.ui.Controller;
import j2dgl.ui.Label;
import j2dgl.ui.Panel;
import j2dgl.ui.Slider;
import j2dgl.update.Updater;
import java.awt.Color;
import java.awt.Point;
import utility.BooleanHolder;

public class UIController extends Controller {

    private final SquareCore core;
    private final MomentumUpdater momentumUpdater;

    private Label entityCountLabel;
    private Slider entityCountSlider;
    private Label minEntitySizeLabel;
    private Slider minEntitySizeSlider;
    private Label maxEntitySizeLabel;
    private Slider maxEntitySizeSlider;
    private Label minEntityMassLabel;
    private Slider minEntityMassSlider;
    private Label maxEntityMassLabel;
    private Slider maxEntityMassSlider;
    private Label minEntitySpeedLabel;
    private Slider minEntitySpeedSlider;
    private Label maxEntitySpeedLabel;
    private Slider maxEntitySpeedSlider;

    private Button applyButton;
    private Button toggleGPUButton;

    private Panel mainMenuPanel;

    private final Color MENU_BACKGROUND = new Color(0x151515);
    private final Color MENU_FOREGROUND = new Color(0x4AEBFF);

    private final int menuX;
    private final int menuY;
    private final int MENU_WIDTH = 500;
    private final int MENU_HEIGHT = 225;
    
    public boolean menuVisible = false;

    public UIController(Updater updater, Renderer renderer, Point mouse, BooleanHolder mouseDown, 
            SquareCore core, MomentumUpdater momentumUpdater) {
        super(updater, renderer, mouse, mouseDown);
        this.core = core;
        this.momentumUpdater = momentumUpdater;
        menuX = core.getResolution().width / 2 - MENU_WIDTH / 2;
        menuY = core.getResolution().height / 2 - MENU_HEIGHT / 2;
        initComponents();
    }

    private void initComponents() {
        // Lables and sliders:
        entityCountLabel = new Label(menuX + 5, menuY + 5, 100, 25, "Squares: ", mouse,
                mouseDown, MENU_FOREGROUND, MENU_BACKGROUND);
        entityCountSlider = new Slider(menuX + MENU_WIDTH - 390 - 10, menuY + 5, 390, 25, mouse, mouseDown, 0, 10000, 750);
        entityCountSlider.setBackgroundColorAll(MENU_BACKGROUND);
        minEntitySizeLabel = new Label(menuX + 5, menuY + 30, 100, 25, "Min Size: ", mouse,
                mouseDown, MENU_FOREGROUND, MENU_BACKGROUND);
        minEntitySizeSlider = new Slider(menuX + MENU_WIDTH - 390 - 10, menuY + 30, 390, 25, mouse, mouseDown, 1, 100, 1);
        minEntitySizeSlider.setBackgroundColorAll(MENU_BACKGROUND);
        maxEntitySizeLabel = new Label(menuX + 5, menuY + 55, 100, 25, "Max Size: ", mouse,
                mouseDown, MENU_FOREGROUND, MENU_BACKGROUND);
        maxEntitySizeSlider = new Slider(menuX + MENU_WIDTH - 390 - 10, menuY + 55, 390, 25, mouse, mouseDown, 1, 100, 3);
        maxEntitySizeSlider.setBackgroundColorAll(MENU_BACKGROUND);
        minEntityMassLabel = new Label(menuX + 5, menuY + 80, 100, 25, "Min Mass: ", mouse,
                mouseDown, MENU_FOREGROUND, MENU_BACKGROUND);
        minEntityMassSlider = new Slider(menuX + MENU_WIDTH - 390 - 10, menuY + 80, 390, 25, mouse, mouseDown, 1, 5000, 100);
        minEntityMassSlider.setBackgroundColorAll(MENU_BACKGROUND);
        maxEntityMassLabel = new Label(menuX + 5, menuY + 105, 100, 25, "Max Mass: ", mouse,
                mouseDown, MENU_FOREGROUND, MENU_BACKGROUND);
        maxEntityMassSlider = new Slider(menuX + MENU_WIDTH - 390 - 10, menuY + 105, 390, 25, mouse, mouseDown, 1, 5000, 2000);
        maxEntityMassSlider.setBackgroundColorAll(MENU_BACKGROUND);
        minEntitySpeedLabel = new Label(menuX + 5, menuY + 130, 100, 25, "Min Speed: ", mouse,
                mouseDown, MENU_FOREGROUND, MENU_BACKGROUND);
        minEntitySpeedSlider = new Slider(menuX + MENU_WIDTH - 390 - 10, menuY + 130, 390, 25, mouse, mouseDown, 1, 30, 1);
        minEntitySpeedSlider.setBackgroundColorAll(MENU_BACKGROUND);
        maxEntitySpeedLabel = new Label(menuX + 5, menuY + 155, 100, 25, "Max Speed: ", mouse,
                mouseDown, MENU_FOREGROUND, MENU_BACKGROUND);
        maxEntitySpeedSlider = new Slider(menuX + MENU_WIDTH - 390 - 10, menuY + 155, 390, 25, mouse, mouseDown, 1, 30, 3);
        maxEntitySpeedSlider.setBackgroundColorAll(MENU_BACKGROUND);
        // Buttons:
        applyButton = new Button(menuX + MENU_WIDTH - 70 - 10,
                menuY + MENU_HEIGHT - 25 - 10, 70, 25, mouse, mouseDown, "Apply", () -> {
                    applyButtonPressed();
                });
        applyButton.getLabel().foregroundColor = MENU_FOREGROUND;
        toggleGPUButton = new Button(menuX + 10, menuY + MENU_HEIGHT - 25 - 10,
                50, 25, mouse, mouseDown, "CPU", () -> {
                    toggleGPUButtonPressed();
                });
        toggleGPUButton.getLabel().foregroundColor = MENU_FOREGROUND;
        // Panel:
        mainMenuPanel = new Panel(menuX, menuY, MENU_WIDTH, MENU_HEIGHT, mouse,
                mouseDown, MENU_BACKGROUND, MENU_BACKGROUND);
        registerComponents(mainMenuPanel, entityCountLabel, entityCountSlider, minEntitySizeLabel,
                minEntitySizeSlider, maxEntitySizeLabel, maxEntitySizeSlider,
                minEntityMassLabel, minEntityMassSlider, maxEntityMassLabel,
                maxEntityMassSlider, minEntitySpeedLabel, minEntitySpeedSlider,
                maxEntitySpeedLabel, maxEntitySpeedSlider, applyButton, toggleGPUButton);
    }

    private void applyButtonPressed() {
        core.generateEntities(entityCountSlider.getValue(), minEntitySizeSlider.getValue(),
                maxEntitySizeSlider.getValue(), minEntityMassSlider.getValue(),
                maxEntityMassSlider.getValue(), minEntitySpeedSlider.getValue(),
                maxEntitySpeedSlider.getValue());
        menuVisible = false;
        momentumUpdater.resetStats();
        core.resetStats();
    }

    private void toggleGPUButtonPressed() {
        if (momentumUpdater.gpuMode) {
            momentumUpdater.gpuMode = false;
            toggleGPUButton.getLabel().text = "GPU";
        } else {
            momentumUpdater.gpuMode = true;
            toggleGPUButton.getLabel().text = "CPU";
        }
        momentumUpdater.resetStats();
        core.resetStats();
    }
}
