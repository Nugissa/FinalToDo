import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            TodoFrame window = new TodoFrame();
            window.setVisible(true);
        });
    }
}