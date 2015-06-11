package physsquares.entity;

import j2dgl.entity.Entity;
import java.awt.Color;
import java.awt.Graphics2D;

public class PhysEntity extends Entity {

    private Color overrideColor = null;

    public PhysEntity(double x, double y, int width, int height, double mass) {
        super(x, y, width, height);
        this.mass = mass;
    }

    public PhysEntity(double x, double y, int width, int height, double mass, Color color) {
        this(x, y, width, height, mass);
        this.overrideColor = color;
    }

    @Override
    protected void drawSelf(Graphics2D g2) {
        if (overrideColor == null) {
            g2.setColor(new Color(0x365E63));
            g2.drawRect(0, 0, width - 1, height - 1);
            double ratio = mass / 10000;
            if (ratio > 0 && ratio < 1) {
                g2.setColor(getFractionColor(Color.WHITE, Color.BLACK, ratio));
            }
            g2.fillRect(1, 1, width - 2, height - 2);
        } else {
            g2.setColor(overrideColor);
            g2.fillRect(0, 0, width - 1, height - 1);
        }
    }

    private Color getFractionColor(Color startColor, Color endColor, double fraction) {
        int red = (int) (fraction * endColor.getRed() + (1 - fraction) * startColor.getRed());
        int green = (int) (fraction * endColor.getGreen() + (1 - fraction) * startColor.getGreen());
        int blue = (int) (fraction * endColor.getBlue() + (1 - fraction) * startColor.getBlue());
        return new Color(red, green, blue);
    }
}
