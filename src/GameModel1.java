/*import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class GameModel {
    final int ROWS = 8;
    final int COLS = 8;
    final int TYPES = 5;
    int[][] board = new int[ROWS][COLS];
    Random rnd = new Random();
    int score = 0;

    GameModel() { initBoard(); }

    void initBoard() {
        score = 0;
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

    /**
     * Encuentra runs (secuencias) horizontales y verticales.
     * Devuelve una lista de runs; cada run es una lista de Points (row,col).
     */
    List<List<Point>> findRuns() {
        List<List<Point>> runs = new ArrayList<>();

        // filas
        for (int r = 0; r < ROWS; r++) {
            int runLen = 1;
            for (int c = 1; c <= COLS; c++) {
                if (c < COLS && board[r][c] == board[r][c - 1]) {
                    runLen++;
                } else {
                    if (runLen >= 3) {
                        List<Point> run = new ArrayList<>();
                        for (int k = 0; k < runLen; k++) run.add(new Point(r, c - 1 - k));
                        runs.add(run);
                    }
                    runLen = 1;
                }
            }
        }

        // columnas
        for (int c = 0; c < COLS; c++) {
            int runLen = 1;
            for (int r = 1; r <= ROWS; r++) {
                if (r < ROWS && board[r][c] == board[r - 1][c]) {
                    runLen++;
                } else {
                    if (runLen >= 3) {
                        List<Point> run = new ArrayList<>();
                        for (int k = 0; k < runLen; k++) run.add(new Point(r - 1 - k, c));
                        runs.add(run);
                    }
                    runLen = 1;
                }
            }
        }

        return runs;
    }

    /**
     * Suma puntuación según la longitud de la run:
     * 3 -> 100, 4 -> 200, 5+ -> 500
     */
    int scoreForRunLength(int len) {
        if (len == 3) return 100;
        if (len == 4) return 200;
        if (len >= 5) return 500;
        return 0;
    }

    /**
     * Elimina todas las celdas que aparecen en las runs (sin duplicados),
     * aplica gravedad y rellena con nuevos tipos.
     * Además actualiza la puntuación sumando por cada run detectada.
     */
    void removeRunsAndCollapse(List<List<Point>> runs) {
        if (runs == null || runs.isEmpty()) return;

        // sumar puntuación por cada run (según su longitud)
        for (List<Point> run : runs) {
            score += scoreForRunLength(run.size());
        }

        // marcar celdas a eliminar (usar Set para evitar duplicados)
        boolean[][] mark = new boolean[ROWS][COLS];
        for (List<Point> run : runs) {
            for (Point p : run) mark[p.x][p.y] = true;
        }

        // marcar como vacío (-1)
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) if (mark[r][c]) board[r][c] = -1;

        // gravedad por columna
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

*/