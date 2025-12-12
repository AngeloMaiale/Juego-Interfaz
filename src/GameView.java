import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class GameView extends JPanel {
    final int TILE = 48;
    GameModel model;
    JLabel scoreLabel = new JLabel("Score: 0");
    JLabel timeLabel = new JLabel("Time: 0");
    JPanel boardHolder;
    int selR = -1, selC = -1;
    int[][] offsetX, offsetY;
    float[][] alpha;
    GameView(GameModel model) {
        this.model = model;
        offsetX = new int[model.ROWS][model.COLS];
        offsetY = new int[model.ROWS][model.COLS];
        alpha = new float[model.ROWS][model.COLS];
        for (int r = 0; r < model.ROWS; r++)
            for (int c = 0; c < model.COLS; c++)
                alpha[r][c] = 1f;
        setLayout(new BorderLayout());
        setOpaque(false);
        JPanel hud = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 10));
        hud.setOpaque(false);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        scoreLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timeLabel.setForeground(Color.WHITE);

        JPanel hudCard = new StyledCard();
        hudCard.setLayout(new FlowLayout(FlowLayout.CENTER, 24, 8));
        hudCard.add(scoreLabel);
        hudCard.add(timeLabel);
        hud.add(hudCard);
        add(hud, BorderLayout.NORTH);

        boardHolder = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(model.COLS * TILE, model.ROWS * TILE);
            }

            @Override
            protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int outerPadding = 8;
                int boardRadius = 14;
                Color boardBg = new Color(36, 40, 48);
                Color boardShadow = new Color(0, 0, 0, 80);
                int tileGap = 4;
                int tileInnerPad = 6;
                int w = getWidth();
                int h = getHeight();
                int bx = outerPadding;
                int by = outerPadding;
                int bw = Math.max(0, w - outerPadding * 2);
                int bh = Math.max(0, h - outerPadding * 2);
                g.setColor(boardShadow);
                g.fillRoundRect(bx + 3, by + 3, bw, bh, boardRadius, boardRadius);
                g.setColor(boardBg);
                g.fillRoundRect(bx, by, bw, bh, boardRadius, boardRadius);
                int gridX = bx + 6;
                int gridY = by + 6;
                int gridW = bw - 12;
                int gridH = bh - 12;
                int cols = model.COLS;
                int rows = model.ROWS;
                int cellW = gridW / cols;
                int cellH = gridH / rows;
                int cellSize = Math.min(cellW, cellH);
                int offsetXGrid = gridX + (gridW - cellSize * cols) / 2;
                int offsetYGrid = gridY + (gridH - cellSize * rows) / 2;
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        int t = model.board[r][c];
                        int x = offsetXGrid + c * cellSize + offsetX[r][c];
                        int y = offsetYGrid + r * cellSize + offsetY[r][c];
                        int tileSize = cellSize - tileGap;
                        int tilePad = tileInnerPad;
                        if (tileSize < 12) tilePad = Math.max(2, tileSize / 6);
                        float a = alpha[r][c];
                        Composite old = g.getComposite();
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, a))));
                        g.setColor(colorFor(t));
                        g.fillRoundRect(x + (tileGap / 2) + tilePad / 2,
                                y + (tileGap / 2) + tilePad / 2,
                                Math.max(1, tileSize - tilePad),
                                Math.max(1, tileSize - tilePad),
                                10, 10);
                        g.setColor(new Color(0, 0, 0, 60));
                        g.setStroke(new BasicStroke(1f));
                        g.drawRoundRect(x + (tileGap / 2) + tilePad / 2,
                                y + (tileGap / 2) + tilePad / 2,
                                Math.max(1, tileSize - tilePad),
                                Math.max(1, tileSize - tilePad),
                                10, 10);
                        g.setComposite(old);
                    }
                }
                g.setColor(new Color(0, 0, 0, 40));
                for (int r = 0; r <= rows; r++)
                    g.drawLine(offsetXGrid, offsetYGrid + r * cellSize, offsetXGrid + cols * cellSize, offsetYGrid + r * cellSize);
                for (int c = 0; c <= cols; c++)
                    g.drawLine(offsetXGrid + c * cellSize, offsetYGrid, offsetXGrid + c * cellSize, offsetYGrid + rows * cellSize);
                if (selR >= 0 && selC >= 0) {
                    int sx = offsetXGrid + selC * cellSize;
                    int sy = offsetYGrid + selR * cellSize;
                    g.setColor(new Color(255, 255, 255, 180));
                    g.setStroke(new BasicStroke(3));
                    g.drawRoundRect(sx + 6, sy + 6, cellSize - 12, cellSize - 12, 10, 10);
                }

                g.dispose();
            }
        };
        boardHolder.setOpaque(true);
        add(boardHolder, BorderLayout.CENTER);
    }

    private Color colorFor(int t) {
        if (t < 0) return new Color(30, 30, 30);
        switch (t) {
            case 0:
                return new Color(100, 149, 237);
            case 1:
                return new Color(220, 20, 60);
            case 2:
                return new Color(255, 215, 0);
            case 3:
                return new Color(60, 179, 113);
            default:
                return new Color(186, 85, 211);
        }
    }

    void updateHUD(int score, int timeLeft) {
        scoreLabel.setText("Score: " + score);
        timeLabel.setText("Time: " + timeLeft + "s");
        scoreLabel.repaint();
        timeLabel.repaint();
        boardHolder.repaint();
    }
    static class StyledCard extends JPanel {
        StyledCard() {
            setOpaque(false);
            setBorder(new EmptyBorder(8, 12, 8, 12));
        }
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setColor(new Color(0, 0, 0, 60));
            g.fillRoundRect(4, 4, Math.max(0, w - 8), Math.max(0, h - 8), 12, 12);
            g.setColor(new Color(36, 39, 46));
            g.fillRoundRect(0, 0, Math.max(0, w - 8), Math.max(0, h - 8), 12, 12);
            g.dispose();
            super.paintComponent(g0);
        }
    }
}
