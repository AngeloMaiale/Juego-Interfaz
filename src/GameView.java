import javax.swing.*;
import java.awt.*;

class GameView extends JPanel {
    final int TILE = 48;
    GameModel model;
    int selR = -1, selC = -1;

    GameView(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(model.COLS * TILE, model.ROWS * TILE));
        setBackground(new Color(30, 30, 30));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int r = 0; r < model.ROWS; r++) {
            for (int c = 0; c < model.COLS; c++) {
                int t = model.board[r][c];
                int x = c * TILE;
                int y = r * TILE;
                g.setColor(colorFor(t));
                g.fillRoundRect(x + 4, y + 4, TILE - 8, TILE - 8, 10, 10);
            }
        }

        g.setColor(new Color(0, 0, 0, 80));
        for (int r = 0; r <= model.ROWS; r++) g.drawLine(0, r * TILE, model.COLS * TILE, r * TILE);
        for (int c = 0; c <= model.COLS; c++) g.drawLine(c * TILE, 0, c * TILE, model.ROWS * TILE);
        if (selR >= 0 && selC >= 0) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(selC * TILE + 3, selR * TILE + 3, TILE - 6, TILE - 6, 10, 10);
        }
        g.dispose();
    }

    private Color colorFor(int t) {
        if (t < 0) return getBackground();
        switch (t) {
            case 0: return new Color(100, 149, 237); // cornflower
            case 1: return new Color(220, 20, 60);   // crimson
            case 2: return new Color(255, 215, 0);   // gold-like
            case 3: return new Color(60, 179, 113);  // mediumseagreen
            default: return new Color(186, 85, 211); // plum-like
        }
    }
}