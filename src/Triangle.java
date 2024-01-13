import java.awt.*;

public class Triangle {
    
    private
        Node p1, p2, p3;
        Color color;

    public
        Triangle() {}

        Triangle(Node p1, Node p2, Node p3, Color color) {
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.color = color;
        }

        void setP1(Node p1) {
            this.p1 = p1;
        }
        Node getP1() {
            return p1;
        }

        void setP2(Node p2) {
            this.p2 = p2;
        }
        Node getP2() {
            return p2;
        }


        void setP3(Node p3) {
            this.p3 = p3;
        }
        Node getP3() {
            return p3;
        }

        void setColor(Color color) {
            this.color = color;
        }
        Color getColor() {
            return color;
        }

}