import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Path2D;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryChartPanel extends JPanel {

    private static final Color CARD = new Color(255, 255, 255);
    private static final Color BORDER = new Color(231, 231, 226);
    private static final Color GRID = new Color(237, 237, 232);
    private static final Color MUTED = new Color(145, 146, 141);
    private static final Color TEXT = new Color(43, 44, 41);
    private static final Color ACCENT = new Color(226, 164, 45);
    private static final Color ACCENT_SOFT = new Color(249, 236, 204);

    private List<MoodEntry> entries = new ArrayList<>();

    public HistoryChartPanel() {
        setOpaque(false);
    }

    public void setEntries(List<MoodEntry> entries) {
        this.entries = new ArrayList<>(entries);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int width = getWidth();
        int height = getHeight();

        g2.setColor(CARD);
        g2.fillRoundRect(0, 0, width, height, 24, 24);

        g2.setColor(BORDER);
        g2.drawRoundRect(
                0,
                0,
                Math.max(0, width - 1),
                Math.max(0, height - 1),
                24,
                24
        );

        int left = 58;
        int right = 30;
        int top = 42;
        int bottom = 44;

        int chartWidth = Math.max(1, width - left - right);
        int chartHeight = Math.max(1, height - top - bottom);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.setColor(TEXT);
        g2.drawString("Динамика настроения", 22, 28);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        int[] labels = {10, 8, 6, 4, 2};

        for (int score : labels) {
            int y =
                    top
                    + chartHeight
                    - (int) Math.round(
                            (score - 1)
                            / 9.0
                            * chartHeight
                    );

            g2.setColor(GRID);
            g2.drawLine(left, y, left + chartWidth, y);

            g2.setColor(MUTED);
            g2.drawString(String.valueOf(score), 24, y + 4);
        }

        if (entries.isEmpty()) {
            String empty = "Пока нет данных для графика";

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.setColor(MUTED);

            FontMetrics fm = g2.getFontMetrics();

            g2.drawString(
                    empty,
                    (width - fm.stringWidth(empty)) / 2,
                    height / 2
            );

            g2.dispose();
            return;
        }

        int n = entries.size();

        int[] xs = new int[n];
        int[] ys = new int[n];

        for (int i = 0; i < n; i++) {
            MoodEntry entry = entries.get(i);

            double xRatio =
                    n == 1
                            ? 0.5
                            : i / (double) (n - 1);

            xs[i] =
                    left
                    + (int) Math.round(xRatio * chartWidth);

            ys[i] =
                    top
                    + chartHeight
                    - (int) Math.round(
                            (entry.getScore() - 1)
                            / 9.0
                            * chartHeight
                    );
        }

        if (n >= 2) {
            Path2D area = new Path2D.Double();

            area.moveTo(xs[0], top + chartHeight);

            for (int i = 0; i < n; i++) {
                area.lineTo(xs[i], ys[i]);
            }

            area.lineTo(xs[n - 1], top + chartHeight);
            area.closePath();

            g2.setColor(new Color(
                    ACCENT_SOFT.getRed(),
                    ACCENT_SOFT.getGreen(),
                    ACCENT_SOFT.getBlue(),
                    100
            ));
            g2.fill(area);
        }

        g2.setStroke(new BasicStroke(
                2.2f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        g2.setColor(ACCENT);

        for (int i = 1; i < n; i++) {
            g2.drawLine(xs[i - 1], ys[i - 1], xs[i], ys[i]);
        }

        for (int i = 0; i < n; i++) {
            g2.setColor(CARD);
            g2.fillOval(xs[i] - 6, ys[i] - 6, 12, 12);

            g2.setColor(ACCENT);
            g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM");

        int labelsToShow = Math.min(5, n);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(MUTED);

        for (int j = 0; j < labelsToShow; j++) {
            int index =
                    labelsToShow == 1
                            ? 0
                            : (int) Math.round(
                                    j * (n - 1) / (double) (labelsToShow - 1)
                            );

            String label =
                    entries.get(index)
                            .getTimestamp()
                            .format(formatter);

            FontMetrics fm = g2.getFontMetrics();

            g2.drawString(
                    label,
                    xs[index] - fm.stringWidth(label) / 2,
                    height - 16
            );
        }

        g2.dispose();
    }
}
