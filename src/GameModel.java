import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class GameModel {
    final int ROWS = 8, COLS = 8, TYPES = 5;
    int[][] board = new int[ROWS][COLS];
    Random rnd = new Random();
    int score = 0;

    GameModel() { initBoard(); }

    void initBoard() {
        score = 0;
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) {
            do { board[r][c] = rnd.nextInt(TYPES); } while (createsMatchAt(r,c));
        }
    }

    boolean createsMatchAt(int r, int c) {
        int t = board[r][c];
        if (c >= 2 && board[r][c-1]==t && board[r][c-2]==t) return true;
        if (r >= 2 && board[r-1][c]==t && board[r-2][c]==t) return true;
        return false;
    }

    void swap(int r1,int c1,int r2,int c2){
        int tmp = board[r1][c1]; board[r1][c1]=board[r2][c2]; board[r2][c2]=tmp;
    }

    java.util.List<java.util.List<Point>> findRuns(){
        java.util.List<java.util.List<Point>> runs = new ArrayList<>();
        // rows
        for (int r=0;r<ROWS;r++){
            int run=1;
            for (int c=1;c<=COLS;c++){
                if (c<COLS && board[r][c]==board[r][c-1]) run++; else {
                    if (run>=3){
                        java.util.List<Point> runList = new ArrayList<>();
                        for (int k=0;k<run;k++) runList.add(new Point(r,c-1-k));
                        runs.add(runList);
                    }
                    run=1;
                }
            }
        }
        // cols
        for (int c=0;c<COLS;c++){
            int run=1;
            for (int r=1;r<=ROWS;r++){
                if (r<ROWS && board[r][c]==board[r-1][c]) run++; else {
                    if (run>=3){
                        java.util.List<Point> runList = new ArrayList<>();
                        for (int k=0;k<run;k++) runList.add(new Point(r-1-k,c));
                        runs.add(runList);
                    }
                    run=1;
                }
            }
        }
        return runs;
    }

    int scoreForRunLength(int len){
        if (len==3) return 100;
        if (len==4) return 200;
        if (len>=5) return 500;
        return 0;
    }

    void removeRunsAndCollapse(java.util.List<java.util.List<Point>> runs){
        if (runs==null || runs.isEmpty()) return;
        for (java.util.List<Point> run: runs) score += scoreForRunLength(run.size());
        boolean[][] mark = new boolean[ROWS][COLS];
        for (List<Point> run: runs) for (Point p: run) mark[p.x][p.y]=true;
        for (int r=0;r<ROWS;r++) for (int c=0;c<COLS;c++) if (mark[r][c]) board[r][c] = -1;
        for (int c=0;c<COLS;c++){
            int write = ROWS-1;
            for (int r=ROWS-1;r>=0;r--) if (board[r][c]!=-1){ board[write][c]=board[r][c]; write--; }
            for (int r=write;r>=0;r--) board[r][c]=rnd.nextInt(TYPES);
        }
    }
}