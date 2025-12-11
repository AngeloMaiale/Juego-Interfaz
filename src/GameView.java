import javax.swing.*;
import java.awt.*;

class GameView extends JPanel {
    final int TILE = 48;
    GameModel model;
    JLabel scoreLabel = new JLabel("Score: 0");
    JLabel timeLabel = new JLabel("Time: 0");
    JPanel boardHolder;
    int selR=-1, selC=-1;

    // offsets para animación (en píxeles) dibujados por boardHolder
    int[][] offsetX, offsetY;

    GameView(GameModel model){
        this.model = model;
        offsetX = new int[model.ROWS][model.COLS];
        offsetY = new int[model.ROWS][model.COLS];

        setLayout(new BorderLayout());
        setBackground(new Color(30,30,30));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER,20,6));
        top.setBackground(new Color(40,40,40));
        scoreLabel.setForeground(Color.WHITE);
        timeLabel.setForeground(Color.WHITE);
        top.add(scoreLabel); top.add(timeLabel);
        add(top, BorderLayout.NORTH);

        boardHolder = new JPanel(){
            @Override public Dimension getPreferredSize(){ return new Dimension(model.COLS*TILE, model.ROWS*TILE); }
            @Override protected void paintComponent(Graphics g0){
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(50,50,50));
                g.fillRoundRect(0,0,getWidth(),getHeight(),12,12);

                for (int r=0;r<model.ROWS;r++){
                    for (int c=0;c<model.COLS;c++){
                        int t = model.board[r][c];
                        int x = c*TILE + offsetX[r][c];
                        int y = r*TILE + offsetY[r][c];
                        g.setColor(colorFor(t));
                        g.fillRoundRect(x+4,y+4,TILE-8,TILE-8,10,10);
                    }
                }
                g.setColor(new Color(0,0,0,80));
                for (int r=0;r<=model.ROWS;r++) g.drawLine(0,r*TILE,model.COLS*TILE,r*TILE);
                for (int c=0;c<=model.COLS;c++) g.drawLine(c*TILE,0,c*TILE,model.ROWS*TILE);

                if (selR>=0 && selC>=0){
                    int sx = selC*TILE, sy = selR*TILE;
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(3));
                    g.drawRoundRect(sx+3,sy+3,TILE-6,TILE-6,10,10);
                }
                g.dispose();
            }
        };
        boardHolder.setBackground(getBackground());
        boardHolder.setOpaque(true);
        add(boardHolder, BorderLayout.CENTER);
    }

    private Color colorFor(int t){
        if (t<0) return getBackground();
        switch(t){
            case 0: return new Color(100,149,237);
            case 1: return new Color(220,20,60);
            case 2: return new Color(255,215,0);
            case 3: return new Color(60,179,113);
            default: return new Color(186,85,211);
        }
    }

    void updateHUD(int score, int timeLeft){
        scoreLabel.setText("Score: " + score);
        timeLabel.setText("Time: " + timeLeft + "s");
        scoreLabel.repaint(); timeLabel.repaint(); boardHolder.repaint();
    }
}