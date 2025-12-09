import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

class GameController {
    GameModel model = new GameModel();
    GameView view = new GameView(model);
    JFrame frame = new JFrame("Match-3 MVC (Swing)");
    int selR = -1, selC = -1;
    boolean busy = false;
    javax.swing.Timer swapBackTimer = null;
    javax.swing.Timer matchLoopTimer = null;

    void start() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(view, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (busy) return;
                int r = e.getY() / view.TILE;
                int c = e.getX() / view.TILE;
                if (r < 0 || r >= model.ROWS || c < 0 || c >= model.COLS) return;

                if (selR == -1) {
                    selR = r; selC = c;
                    view.selR = selR; view.selC = selC;
                    view.repaint();
                    return;
                }

                if (Math.abs(selR - r) + Math.abs(selC - c) == 1) {
                    int r1 = selR, c1 = selC, r2 = r, c2 = c;
                    selR = selC = -1;
                    view.selR = view.selC = -1;
                    attemptSwap(r1, c1, r2, c2);
                } else {
                    selR = r; selC = c;
                    view.selR = selR; view.selC = selC;
                    view.repaint();
                }
            }
        });
    }

    void attemptSwap(int r1, int c1, int r2, int c2) {
        model.swap(r1, c1, r2, c2);
        view.repaint();

        java.util.List<Point> matches = model.findMatches();
        if (matches.isEmpty()) {
            busy = true;
            if (swapBackTimer != null && swapBackTimer.isRunning()) swapBackTimer.stop();
            swapBackTimer = new javax.swing.Timer(200, ae -> {
                model.swap(r1, c1, r2, c2);
                view.repaint();
                busy = false;
                swapBackTimer.stop();
            });
            swapBackTimer.setRepeats(false);
            swapBackTimer.start();
        } else {
            processMatchesLoop();
        }
    }

    void processMatchesLoop() {
        busy = true;
        if (matchLoopTimer != null && matchLoopTimer.isRunning()) matchLoopTimer.stop();

        matchLoopTimer = new javax.swing.Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Point> matches = model.findMatches();
                if (matches.isEmpty()) {
                    busy = false;
                    matchLoopTimer.stop();
                    return;
                }
                model.removeAndCollapse(matches);
                view.repaint();
            }
        });
        matchLoopTimer.setRepeats(true);
        matchLoopTimer.start();
    }
}