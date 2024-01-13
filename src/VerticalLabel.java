import javax.swing.*;
import java.awt.*;

public class VerticalLabel extends JLabel {
    private int direction;

    public VerticalLabel(String text, int alignment, int direction) {
        super(text);
        setHorizontalAlignment(alignment);
        this.direction = direction;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(0, getHeight() / 2);
        g2d.rotate(direction * Math.PI / 2);
        
        // Adjust position for correct rendering
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(getText());
        int height = fm.getHeight();
        g2d.drawString(getText(), -width / 2, -direction * height);

        g2d.dispose();
    }
 
    @Override
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        return new Dimension(dim.height, dim.width);
    }

}
