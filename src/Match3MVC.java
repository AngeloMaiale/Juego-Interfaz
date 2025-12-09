import javax.swing.*;

public class Match3MVC {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameController().start());
    }
}
