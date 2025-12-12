import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

class GameController {
    GameModel model = new GameModel();
    GameView view = new GameView(model);
    JFrame frame = new JFrame("Match-3 — Styled");
    int selR=-1, selC=-1;
    boolean busy=false, timeUp=false;

    javax.swing.Timer swapBackTimer=null;
    javax.swing.Timer matchLoopTimer=null;
    javax.swing.Timer countdownTimer=null;
    javax.swing.Timer animTimer=null;

    int timeLeft=0;
    final int ANIM_MS = 160;
    final int ANIM_STEP = 16;
    final int DISSOLVE_MS = 220;
    final int DISSOLVE_STEP = 16;
    final int FALL_MS = 260;
    final int FALL_STEP = 16;

    void start(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(false);
        frame.setLayout(new BorderLayout());
        frame.setContentPane(new GradientPanel());
        frame.getContentPane().setLayout(new GridBagLayout());
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(true);
        container.setBackground(new Color(48, 54, 62));
        container.setBorder(new EmptyBorder(18,18,18,18));
        container.add(view, BorderLayout.CENTER);
        frame.getContentPane().add(container);
        frame.pack();
        frame.setLocationRelativeTo(null);
        Integer chosen = StyledDialog.showTimeDialog(frame, 60);
        if (chosen==null) System.exit(0);
        timeLeft = chosen;
        frame.setVisible(true);

        view.updateHUD(model.score, timeLeft);
        startCountdown();

        view.boardHolder.addMouseListener(new MouseAdapter(){
            @Override public void mousePressed(MouseEvent e){
                if (busy || timeUp) return;
                int c = e.getX() / view.TILE;
                int r = e.getY() / view.TILE;
                if (r<0||r>=model.ROWS||c<0||c>=model.COLS) return;
                selR = r; selC = c;
                view.selR = selR; view.selC = selC;
                view.boardHolder.repaint();
            }
            @Override public void mouseReleased(MouseEvent e){
                if (busy || timeUp) return;
                int c = e.getX() / view.TILE;
                int r = e.getY() / view.TILE;
                if (r<0||r>=model.ROWS||c<0||c>=model.COLS){
                    selR = selC = -1; view.selR = view.selC = -1; view.boardHolder.repaint(); return;
                }
                if (r==selR && c==selC){
                    selR = selC = -1; view.selR = view.selC = -1; view.boardHolder.repaint(); return;
                }
                if (Math.abs(selR - r) + Math.abs(selC - c) == 1){
                    int r1 = selR, c1 = selC, r2 = r, c2 = c;
                    selR = selC = -1; view.selR = view.selC = -1; view.boardHolder.repaint();
                    attemptSwapAnimated(r1,c1,r2,c2);
                } else {
                    selR = r; selC = c; view.selR = selR; view.selC = selC; view.boardHolder.repaint();
                }
            }
        });
    }

    static class GradientPanel extends JPanel {
        GradientPanel(){ setOpaque(true); }
        @Override protected void paintComponent(Graphics g0){
            Graphics2D g = (Graphics2D) g0.create();
            int w = getWidth(), h = getHeight();
            Color c1 = new Color(18,20,24);
            Color c2 = new Color(36,39,46);
            g.setPaint(new GradientPaint(0,0,c1, w,h,c2));
            g.fillRect(0,0,w,h);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    void startCountdown(){
        if (countdownTimer!=null && countdownTimer.isRunning()) countdownTimer.stop();
        view.updateHUD(model.score, timeLeft);
        countdownTimer = new javax.swing.Timer(1000, ae -> {
            timeLeft--;
            view.updateHUD(model.score, timeLeft);
            if (timeLeft<=0){ countdownTimer.stop(); onTimeUp(); }
        });
        countdownTimer.setRepeats(true); countdownTimer.start();
    }

    void onTimeUp(){
        timeUp = true; busy = true;
        if (swapBackTimer!=null && swapBackTimer.isRunning()) swapBackTimer.stop();
        if (matchLoopTimer!=null && matchLoopTimer.isRunning()) matchLoopTimer.stop();
        SwingUtilities.invokeLater(() -> {
            int choice = StyledDialog.showEndDialog(frame, model.score);
            if (choice == 0) restartGame(); else System.exit(0);
        });
    }

    void restartGame(){
        if (countdownTimer!=null && countdownTimer.isRunning()) countdownTimer.stop();
        if (matchLoopTimer!=null && matchLoopTimer.isRunning()) matchLoopTimer.stop();
        if (animTimer!=null && animTimer.isRunning()) animTimer.stop();
        if (swapBackTimer!=null && swapBackTimer.isRunning()) swapBackTimer.stop();

        model.initBoard();
        for (int r=0;r<model.ROWS;r++) for (int c=0;c<model.COLS;c++){
            view.offsetX[r][c]=0; view.offsetY[r][c]=0; view.alpha[r][c]=1f;
        }
        selR = selC = -1;
        view.selR = view.selC = -1;
        timeUp = false;
        busy = false;

        Integer chosen = StyledDialog.showTimeDialog(frame, timeLeft>0?timeLeft:60);
        if (chosen==null) System.exit(0);
        timeLeft = chosen;
        view.updateHUD(model.score, timeLeft);
        startCountdown();
        view.boardHolder.repaint();
    }

    void attemptSwapAnimated(int r1,int c1,int r2,int c2){
        if (busy || timeUp) return;
        busy = true;
        int dx = (c2 - c1) * view.TILE;
        int dy = (r2 - r1) * view.TILE;
        zeroOffsets();
        int frames = Math.max(1, ANIM_MS / ANIM_STEP);
        final int[] step = {0};
        if (animTimer!=null && animTimer.isRunning()) animTimer.stop();
        animTimer = new javax.swing.Timer(ANIM_STEP, ev -> {
            step[0]++;
            double t = (double)step[0]/frames;
            int ox1 = (int)Math.round(dx * t);
            int oy1 = (int)Math.round(dy * t);
            int ox2 = -ox1;
            int oy2 = -oy1;
            view.offsetX[r1][c1] = ox1; view.offsetY[r1][c1] = oy1;
            view.offsetX[r2][c2] = ox2; view.offsetY[r2][c2] = oy2;
            view.boardHolder.repaint();
            if (step[0] >= frames){
                animTimer.stop();
                view.offsetX[r1][c1]=view.offsetY[r1][c1]=0;
                view.offsetX[r2][c2]=view.offsetY[r2][c2]=0;
                view.boardHolder.repaint();
                model.swap(r1,c1,r2,c2);
                List<List<Point>> runs = model.findRuns();
                if (runs.isEmpty()){
                    animateSwapBack(r1,c1,r2,c2);
                } else {
                    animateDissolveThenFall(runs);
                }
            }
        });
        animTimer.setRepeats(true);
        animTimer.start();
    }

    void animateSwapBack(int r1,int c1,int r2,int c2){
        int dx = (c2 - c1) * view.TILE;
        int dy = (r2 - r1) * view.TILE;
        int frames = Math.max(1, ANIM_MS / ANIM_STEP);
        final int[] step = {0};
        if (animTimer!=null && animTimer.isRunning()) animTimer.stop();
        animTimer = new javax.swing.Timer(ANIM_STEP, ev -> {
            step[0]++;
            double t = (double)step[0]/frames;
            int ox1 = (int)Math.round(dx * (1 - t));
            int oy1 = (int)Math.round(dy * (1 - t));
            int ox2 = -ox1;
            int oy2 = -oy1;
            view.offsetX[r1][c1] = ox1; view.offsetY[r1][c1] = oy1;
            view.offsetX[r2][c2] = ox2; view.offsetY[r2][c2] = oy2;
            view.boardHolder.repaint();
            if (step[0] >= frames){
                animTimer.stop();
                view.offsetX[r1][c1]=view.offsetY[r1][c1]=0;
                view.offsetX[r2][c2]=view.offsetY[r2][c2]=0;
                view.boardHolder.repaint();
                model.swap(r1,c1,r2,c2);
                view.updateHUD(model.score, timeLeft);
                busy = false;
            }
        });
        animTimer.setRepeats(true);
        animTimer.start();
    }

    void zeroOffsets(){
        for (int r=0;r<model.ROWS;r++) for (int c=0;c<model.COLS;c++){ view.offsetX[r][c]=0; view.offsetY[r][c]=0; }
    }

    void animateDissolveThenFall(List<List<Point>> runs){
        boolean[][] toDissolve = new boolean[model.ROWS][model.COLS];
        for (List<Point> run: runs) for (Point p: run) toDissolve[p.x][p.y] = true;
        int frames = Math.max(1, DISSOLVE_MS / DISSOLVE_STEP);
        final int[] step = {0};
        for (int r=0;r<model.ROWS;r++) for (int c=0;c<model.COLS;c++) view.alpha[r][c]=1f;
        javax.swing.Timer dissolveTimer = new javax.swing.Timer(DISSOLVE_STEP, null);
        dissolveTimer.addActionListener(ae -> {
            step[0]++;
            double t = (double)step[0]/frames;
            float alphaVal = (float)Math.max(0.0, 1.0 - t);
            for (int r=0;r<model.ROWS;r++){
                for (int c=0;c<model.COLS;c++){
                    if (toDissolve[r][c]) view.alpha[r][c] = alphaVal;
                    else view.alpha[r][c] = 1f;
                }
            }
            view.boardHolder.repaint();
            if (step[0] >= frames){
                dissolveTimer.stop();

                List<List<Integer>> originalRowsPerCol = new ArrayList<>();
                for (int c=0;c<model.COLS;c++){
                    List<Integer> rows = new ArrayList<>();
                    for (int r=0;r<model.ROWS;r++){
                        if (!toDissolve[r][c]) rows.add(r);
                    }
                    originalRowsPerCol.add(rows);
                }

                model.removeRunsAndCollapse(runs);

                for (int r=0;r<model.ROWS;r++) for (int c=0;c<model.COLS;c++){ view.offsetX[r][c]=0; view.offsetY[r][c]=0; view.alpha[r][c]=1f; }

                for (int c=0;c<model.COLS;c++){
                    List<Integer> origRows = originalRowsPerCol.get(c);
                    List<Integer> stack = new ArrayList<>();
                    for (int i = origRows.size()-1; i>=0; i--) stack.add(origRows.get(i));
                    for (int dest = model.ROWS - 1; dest >= 0; dest--){
                        if (!stack.isEmpty()){
                            int sourceRow = stack.remove(0);
                            int deltaRows = sourceRow - dest;
                            if (deltaRows != 0){
                                view.offsetY[dest][c] = deltaRows * view.TILE;
                            } else {
                                view.offsetY[dest][c] = 0;
                            }
                        } else {
                            int aboveDistance = (model.ROWS - dest);
                            view.offsetY[dest][c] = - aboveDistance * view.TILE;
                        }
                    }
                }

                animateOffsetsToZeroThenContinue();
            }
        });
        dissolveTimer.setRepeats(true);
        dissolveTimer.start();
    }

    void animateOffsetsToZeroThenContinue(){
        int frames = Math.max(1, FALL_MS / FALL_STEP);
        final int[] step = {0};
        if (animTimer!=null && animTimer.isRunning()) animTimer.stop();
        animTimer = new javax.swing.Timer(FALL_STEP, ev -> {
            step[0]++;
            double t = (double)step[0]/frames;
            double ease = 1 - Math.pow(1 - t, 3);
            for (int r=0;r<model.ROWS;r++){
                for (int c=0;c<model.COLS;c++){
                    int start = view.offsetY[r][c];
                    int target = 0;
                    view.offsetY[r][c] = (int)Math.round(start + (target - start) * ease);
                }
            }
            view.boardHolder.repaint();
            if (step[0] >= frames){
                animTimer.stop();
                for (int r=0;r<model.ROWS;r++) for (int c=0;c<model.COLS;c++){ view.offsetX[r][c]=0; view.offsetY[r][c]=0; }
                view.boardHolder.repaint();
                continueMatchesAfterFall();
            }
        });
        animTimer.setRepeats(true);
        animTimer.start();
    }

    void continueMatchesAfterFall(){
        List<List<Point>> runs = model.findRuns();
        if (runs.isEmpty()){
            if (!model.hasPossibleMove()){
                busy = true;
                model.shuffleUntilSolvable(80);
                view.updateHUD(model.score, timeLeft);
                view.boardHolder.repaint();
                busy = false;
            } else {
                busy = false;
                view.updateHUD(model.score, timeLeft);
            }
        } else {
            animateDissolveThenFall(runs);
        }
    }
}