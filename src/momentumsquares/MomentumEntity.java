package momentumsquares;

import j2dgl.entity.DrawableEntity;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class MomentumEntity extends DrawableEntity {

    final double mass;
    private Color overrideColor = null;

    public MomentumEntity(double x, double y, int width, int height, double mass) {
        super(x, y, width, height);
        this.mass = mass;
    }

    public MomentumEntity(double x, double y, int width, int height, double mass, Color color) {
        this(x, y, width, height, mass);
        this.overrideColor = color;
    }

    @Override
    protected void draw(Graphics2D g2) {
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

    @Override
    public void update() {
        x += xIncrement;
        y += yIncrement;
    }

    public void processCollision(MomentumEntity otherEntity) {
        double m1 = this.mass;
        double m2 = otherEntity.mass;

        // Adjust speeds based on momentum exchange
        double thisNewXSpeed = (((m1 - m2) / (m1 + m2)) * this.xIncrement)
                + (((m2 * 2) / (m1 + m2)) * otherEntity.xIncrement);
        double thisNewYSpeed = (((m1 - m2) / (m1 + m2)) * this.yIncrement)
                + (((m2 * 2) / (m1 + m2)) * otherEntity.yIncrement);

        double otherNewXSpeed = (((m2 - m1) / (m1 + m2)) * otherEntity.xIncrement)
                + (((m1 * 2) / (m1 + m2)) * this.xIncrement);
        double otherNewYSpeed = (((m2 - m1) / (m1 + m2)) * otherEntity.yIncrement)
                + (((m1 * 2) / (m1 + m2)) * this.yIncrement);

        this.xIncrement = thisNewXSpeed;
        this.yIncrement = thisNewYSpeed;

        otherEntity.xIncrement = otherNewXSpeed;
        otherEntity.yIncrement = otherNewYSpeed;
    }

    private double getNewSpeedAfterCollision(double mass1, double mass2, double speed1, double speed2) {
        // Returns the new speed1 value
        return (((mass1 - mass2) / (mass1 + mass2)) * speed1)
                + (((mass2 * 2) / (mass1 + mass2)) * speed2);
    }

    private Color getFractionColor(Color startColor, Color endColor, double fraction) {
        int red = (int) (fraction * endColor.getRed() + (1 - fraction) * startColor.getRed());
        int green = (int) (fraction * endColor.getGreen() + (1 - fraction) * startColor.getGreen());
        int blue = (int) (fraction * endColor.getBlue() + (1 - fraction) * startColor.getBlue());
        return new Color(red, green, blue);
    }

    public boolean aboutToIntersect(MomentumEntity otherEntity) {
        Rectangle thisFutureRect = new Rectangle(this.getBounds());
        thisFutureRect.x += this.xIncrement;
        thisFutureRect.y += this.yIncrement;

        Rectangle otherFutureRect = new Rectangle(otherEntity.getBounds());
        otherFutureRect.x += otherEntity.xIncrement;
        otherFutureRect.y += otherEntity.yIncrement;

        return thisFutureRect.intersects(otherFutureRect);
    }
}
