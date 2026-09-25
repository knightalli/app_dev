import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class SunMoodPanel extends JPanel {

    private static final Color LOW_SUN = new Color(220, 85, 85);
    private static final Color MID_SUN = new Color(241, 185, 64);
    private static final Color HIGH_SUN = new Color(82, 172, 103);

    private int score = 5;

    public SunMoodPanel() {
        setOpaque(false);
    }

    public void setScore(int score) {
        this.score = Math.max(1, Math.min(10, score));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            g2.dispose();
            return;
        }

        double mood = (score - 1) / 9.0;
        int size = Math.min(width, height);
        int centerX = width / 2;
        int centerY = height / 2;

        int radius = Math.max(8, (int) Math.round(size * 0.22));
        int innerRay = radius + Math.max(5, (int) Math.round(size * 0.08));
        int outerRay = innerRay + Math.max(6, (int) Math.round(size * 0.10));

        Color sunColor = interpolateMoodColor(mood);

        g2.setColor(new Color(
                sunColor.getRed(),
                sunColor.getGreen(),
                sunColor.getBlue(),
                190
        ));

        g2.setStroke(new BasicStroke(
                1.5f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        int rayCount = 12;

        for (int i = 0; i < rayCount; i++) {
            double angle = Math.PI * 2.0 * i / rayCount;

            int x1 = centerX + (int) Math.round(Math.cos(angle) * innerRay);
            int y1 = centerY + (int) Math.round(Math.sin(angle) * innerRay);
            int x2 = centerX + (int) Math.round(Math.cos(angle) * outerRay);
            int y2 = centerY + (int) Math.round(Math.sin(angle) * outerRay);

            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setColor(sunColor);
        g2.fillOval(
                centerX - radius,
                centerY - radius,
                radius * 2,
                radius * 2
        );

        g2.setColor(new Color(255, 255, 255, 120));
        int ring = radius + 4;
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(
                centerX - ring,
                centerY - ring,
                ring * 2,
                ring * 2
        );

        g2.dispose();
    }

    private Color interpolateMoodColor(double mood) {
        if (mood <= 0.5) {
            return mix(LOW_SUN, MID_SUN, mood * 2.0);
        }

        return mix(MID_SUN, HIGH_SUN, (mood - 0.5) * 2.0);
    }

    private Color mix(Color a, Color b, double t) {
        t = Math.max(0.0, Math.min(1.0, t));

        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t)
        );
    }
}
