import javax.swing.*;
import java.awt.*;

public class Match3MVC {
    public static void main(String[] args) {
        try {
            UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new GameController().start());
    }
}
