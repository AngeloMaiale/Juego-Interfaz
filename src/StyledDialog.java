import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class StyledDialog {
    static final Color ACCENT = new Color(98, 165, 255);
    static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    static final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    static Integer showTimeDialog(Component parent, int defaultSeconds){
        java.awt.Window owner = (parent == null) ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = new JDialog(owner, "Tiempo de partida", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        JPanel content = new RoundedPanel(14, new Color(36,39,46));
        content.setLayout(new BorderLayout(12,12));
        content.setBorder(new EmptyBorder(14,14,14,14));

        JLabel title = new JLabel("Tiempo de la partida (segundos)");
        title.setFont(TITLE_FONT);
        title.setForeground(Color.WHITE);

        JTextField input = new JTextField(String.valueOf(defaultSeconds));
        input.setFont(TEXT_FONT);
        input.setBackground(new Color(50,54,61));
        input.setForeground(Color.WHITE);
        input.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JPanel center = new JPanel(new BorderLayout(6,6));
        center.setOpaque(false);
        center.add(title, BorderLayout.NORTH);
        center.add(input, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        buttons.setOpaque(false);
        JButton ok = styledButton("Aceptar");
        JButton cancel = styledButton("Cancelar");
        buttons.add(cancel); buttons.add(ok);

        content.add(center, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        dlg.getContentPane().add(content);
        dlg.pack();
        dlg.setLocationRelativeTo(owner);

        final Integer[] result = {null};
        ok.addActionListener(e -> {
            String txt = input.getText().trim();
            try {
                int v = Integer.parseInt(txt);
                if (v <= 0) throw new NumberFormatException();
                result[0] = v;
                dlg.dispose();
            } catch (NumberFormatException ex){
                input.setBackground(new Color(120,40,40));
                javax.swing.Timer t = new javax.swing.Timer(600, ev -> input.setBackground(new Color(50,54,61)));
                t.setRepeats(false); t.start();
            }
        });
        cancel.addActionListener(e -> { result[0] = null; dlg.dispose(); });

        dlg.getRootPane().setDefaultButton(ok);
        dlg.getRootPane().registerKeyboardAction(e -> { result[0]=null; dlg.dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dlg.setVisible(true);
        return result[0];
    }

    static int showEndDialog(Component parent, int finalScore){
        java.awt.Window owner = (parent == null) ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = new JDialog(owner, "Fin de la partida", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        JPanel content = new RoundedPanel(14, new Color(36,39,46));
        content.setLayout(new BorderLayout(12,12));
        content.setBorder(new EmptyBorder(14,14,14,14));

        JLabel title = new JLabel("Tiempo terminado");
        title.setFont(TITLE_FONT);
        title.setForeground(Color.WHITE);

        JLabel msg = new JLabel("<html><div style='text-align:center;'>Puntuación final: <b>" + finalScore + "</b></div></html>");
        msg.setFont(TEXT_FONT);
        msg.setForeground(Color.LIGHT_GRAY);
        msg.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(title, BorderLayout.NORTH);
        center.add(msg, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER,12,0));
        buttons.setOpaque(false);
        JButton replay = styledButton("Jugar otra partida");
        JButton exit = styledButton("Salir");
        buttons.add(replay); buttons.add(exit);

        content.add(center, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        dlg.getContentPane().add(content);
        dlg.pack();
        dlg.setLocationRelativeTo(owner);

        final int[] choice = {1};
        replay.addActionListener(e -> { choice[0] = 0; dlg.dispose(); });
        exit.addActionListener(e -> { choice[0] = 1; dlg.dispose(); });

        dlg.getRootPane().registerKeyboardAction(e -> { choice[0]=1; dlg.dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dlg.setVisible(true);
        return choice[0];
    }

    static JButton styledButton(String text){
        JButton b = new JButton(text);
        b.setFont(TEXT_FONT);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(java.awt.event.MouseEvent evt){ b.setBackground(ACCENT.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt){ b.setBackground(ACCENT); }
        });
        return b;
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        RoundedPanel(int radius, Color bg){ this.radius = radius; this.bg = bg; setOpaque(false); }
        @Override protected void paintComponent(Graphics g0){
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(bg);
            g.fillRoundRect(0,0,getWidth(),getHeight(),radius,radius);
            g.dispose();
            super.paintComponent(g0);
        }
    }
}