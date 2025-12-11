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
    int selR=-1, selC=-1;
    boolean busy=false, timeUp=false;

    javax.swing.Timer swapBackTimer=null;
    javax.swing.Timer matchLoopTimer=null;
    javax.swing.Timer countdownTimer=null;
    javax.swing.Timer animTimer=null;

    int timeLeft=0;
    final int ANIM_MS = 160; // duración animación swap
    final int ANIM_STEP = 16; // ms por frame

    void start(){
        Integer chosen = askTimeFromPlayer();
        if (chosen==null) System.exit(0);
        timeLeft = chosen;

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(view, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        view.updateHUD(model.score, timeLeft);
        startCountdown();

        // mousePressed + mouseReleased para click/drag natural
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
                    // fuera: deseleccionar
                    selR = selC = -1; view.selR = view.selC = -1; view.boardHolder.repaint(); return;
                }
                // si sueltas en la misma celda -> deseleccionar
                if (r==selR && c==selC){
                    selR = selC = -1; view.selR = view.selC = -1; view.boardHolder.repaint(); return;
                }
                // si adyacente -> intentar swap
                if (Math.abs(selR - r) + Math.abs(selC - c) == 1){
                    int r1 = selR, c1 = selC, r2 = r, c2 = c;
                    selR = selC = -1; view.selR = view.selC = -1; view.boardHolder.repaint();
                    attemptSwapAnimated(r1,c1,r2,c2);
                } else {
                    // soltar en no adyacente -> seleccionar nueva
                    selR = r; selC = c; view.selR = selR; view.selC = selC; view.boardHolder.repaint();
                }
            }
        });
    }

    Integer askTimeFromPlayer(){
        while(true){
            String input = JOptionPane.showInputDialog(null,"Introduce el tiempo de la partida en segundos:","Tiempo de juego",JOptionPane.QUESTION_MESSAGE);
            if (input==null) return null;
            input = input.trim();
            if (input.isEmpty()) continue;
            try {
                int val = Integer.parseInt(input);
                if (val<=0){ JOptionPane.showMessageDialog(null,"Introduce un número mayor que 0.","Error",JOptionPane.ERROR_MESSAGE); continue; }
                return val;
            } catch(NumberFormatException ex){ JOptionPane.showMessageDialog(null,"Introduce un número válido.","Error",JOptionPane.ERROR_MESSAGE); }
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
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,"Tiempo terminado.\nPuntuación final: " + model.score,"Fin de la partida",JOptionPane.INFORMATION_MESSAGE));
    }

    // Animación de swap: mueve visualmente dos fichas, luego aplica swap en modelo y procesa matches.
    void attemptSwapAnimated(int r1,int c1,int r2,int c2){
        if (busy || timeUp) return;
        busy = true;
        // calcular desplazamiento en píxeles
        int dx = (c2 - c1) * view.TILE;
        int dy = (r2 - r1) * view.TILE;
        // inicializar offsets
        zeroOffsets();
        // frames
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
                // limpiar offsets
                view.offsetX[r1][c1]=view.offsetY[r1][c1]=0;
                view.offsetX[r2][c2]=view.offsetY[r2][c2]=0;
                view.boardHolder.repaint();
                // aplicar swap en modelo
                model.swap(r1,c1,r2,c2);
                // comprobar runs
                List<List<Point>> runs = model.findRuns();
                if (runs.isEmpty()){
                    // no match -> animar swap de vuelta (visual) y revertir modelo después
                    animateSwapBack(r1,c1,r2,c2);
                } else {
                    // hay matches -> procesar bucle
                    processMatchesLoop();
                }
            }
        });
        animTimer.setRepeats(true);
        animTimer.start();
    }

    void animateSwapBack(int r1,int c1,int r2,int c2){
        // animación de vuelta: similar a attemptSwapAnimated pero invierte
        int dx = (c2 - c1) * view.TILE;
        int dy = (r2 - r1) * view.TILE;
        int frames = Math.max(1, ANIM_MS / ANIM_STEP);
        final int[] step = {0};
        if (animTimer!=null && animTimer.isRunning()) animTimer.stop();
        animTimer = new javax.swing.Timer(ANIM_STEP, ev -> {
            step[0]++;
            double t = (double)step[0]/frames;
            // animar desde 0 hacia -dx (volver)
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
                // revertir modelo (ya estaba swap aplicado), así queda como antes
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

    void processMatchesLoop(){
        if (matchLoopTimer!=null && matchLoopTimer.isRunning()) matchLoopTimer.stop();
        matchLoopTimer = new javax.swing.Timer(220, new ActionListener(){
            @Override public void actionPerformed(ActionEvent e){
                List<List<Point>> runs = model.findRuns();
                if (runs.isEmpty()){
                    busy = false;
                    matchLoopTimer.stop();
                    view.updateHUD(model.score, timeLeft);
                    return;
                }
                model.removeRunsAndCollapse(runs);
                view.updateHUD(model.score, timeLeft);
            }
        });
        matchLoopTimer.setRepeats(true);
        matchLoopTimer.start();
    }
}