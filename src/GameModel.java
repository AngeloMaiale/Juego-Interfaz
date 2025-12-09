import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class GameModel {
    final int ROWS = 8;
    final int COLS = 8;
    final int TYPES = 5;
    int[][] board = new int[ROWS][COLS];
    Random rnd = new Random();

    GameModel() { initBoard(); }

    void initBoard() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                do {
                    board[r][c] = rnd.nextInt(TYPES);
                } while (createsMatchAt(r, c));
            }
        }
    }

    boolean createsMatchAt(int r, int c) {
        int t = board[r][c];
        if (c >= 2 && board[r][c - 1] == t && board[r][c - 2] == t) return true;
        if (r >= 2 && board[r - 1][c] == t && board[r - 2][c] == t) return true;
        return false;
    }

    void swap(int r1, int c1, int r2, int c2) {
        int tmp = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = tmp;
    }

    java.util.List<Point> findMatches() {
        boolean[][] mark = new boolean[ROWS][COLS];

        // filas
        for (int r = 0; r < ROWS; r++) {
            int run = 1;
            for (int c = 1; c <= COLS; c++) {
                if (c < COLS && board[r][c] == board[r][c - 1]) run++;
                else {
                    if (run >= 3) for (int k = 0; k < run; k++) mark[r][c - 1 - k] = true;
                    run = 1;
                }
            }
        }

        // columnas
        for (int c = 0; c < COLS; c++) {
            int run = 1;
            for (int r = 1; r <= ROWS; r++) {
                if (r < ROWS && board[r][c] == board[r - 1][c]) run++;
                else {
                    if (run >= 3) for (int k = 0; k < run; k++) mark[r - 1 - k][c] = true;
                    run = 1;
                }
            }
        }

        java.util.List<Point> res = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) if (mark[r][c]) res.add(new Point(r, c));
        return res;
    }

    void removeAndCollapse(List<Point> matches) {
        for (Point p : matches) board[p.x][p.y] = -1;
        for (int c = 0; c < COLS; c++) {
            int write = ROWS - 1;
            for (int r = ROWS - 1; r >= 0; r--) {
                if (board[r][c] != -1) {
                    board[write][c] = board[r][c];
                    write--;
                }
            }
            // rellenar arriba
            for (int r = write; r >= 0; r--) board[r][c] = rnd.nextInt(TYPES);
        }
    }
}

