public class Node {
    
    private
        double x, y, z;

    public
        Node() {}

        Node(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    
        void setX(double x) {
            this.x = x;
        }
        double getX() {
            return x;
        }

        void setY(double y) {
            this.y = y;
        }
        double getY() {
            return y;
        }

        void setZ(double z) {
            this.z = z;
        }
        double getZ() {
            return z;
        }

}
