import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) {

        //set frame size
        int frameWidth = 800;
        int frameHeight = 900;


        //calculate frame center (the offsets are based on slider size)
        double centerX = (frameWidth - 225) / 2.0;
        double centerY = (frameHeight - 350) / 2.0;


        //generate shape
        List<Triangle> tris = new ArrayList<>();
        generateSphere(tris);


        //set up display window
        JFrame frame = new JFrame();
        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());


        //set up sliders
        JSlider sceneH = new JSlider(SwingConstants.HORIZONTAL, -90, 90, 0);
        JSlider sceneV = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        JSlider lightH = new JSlider(SwingConstants.HORIZONTAL, -90, 90, 45);
        JSlider lightV = new JSlider(SwingConstants.VERTICAL, -90, 90, 45);
        JSlider zoom = new JSlider(SwingConstants.HORIZONTAL, 0, 200, 150);

        setupSliders(frame, sceneH, sceneV, lightH, lightV, zoom);


        //panel for scene rendering
        JPanel renderPanel = new JPanel() {

            //paint panel on update
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;


                //retrieve heading (about y) from scene horizontal slider
                double sPitch = sceneH.getValue() * Math.PI / 180;

                //create heading transform matrix
                Matrix3x3 horizontalMatrix = new Matrix3x3( new double[][]
                    {
                        {Math.cos(sPitch), 0, -Math.sin(sPitch)},
                        {0, 1, 0},
                        {Math.sin(sPitch), 0, Math.cos(sPitch)}
                    }
                );


                //retrieve roll (about x) from scene vertical slider 
                double sRoll = sceneV.getValue() * Math.PI / 180;

                //create roll transform matrix
                Matrix3x3 verticalMatrix = new Matrix3x3( new double[][]
                    {
                        {1, 0, 0},
                        {0, Math.cos(sRoll), Math.sin(sRoll)},
                        {0, -Math.sin(sRoll), Math.cos(sRoll)}
                    }
                );


                //multiply pitch and roll transforms together to get transform matrix
                Matrix3x3 transformMatrix = horizontalMatrix.multiply3x3(verticalMatrix);


                //calculate direction of lighting relative
                double lRoll = lightH.getValue() * Math.PI / 180;
                double lPitch = lightV.getValue() * Math.PI / 180;
                double[] ligthSourceVec = new double[] {Math.sin(lRoll) * Math.cos(lPitch), -Math.sin(lPitch), Math.cos(lRoll) * Math.cos(lPitch)};

                //normalize light direction vector
                double len = Math.sqrt(ligthSourceVec[0] * ligthSourceVec[0] + ligthSourceVec[1] * ligthSourceVec[1] + ligthSourceVec[2] * ligthSourceVec[2]);
                ligthSourceVec[0] /= len;
                ligthSourceVec[1] /= len;
                ligthSourceVec[2] /= len;


                //create depthBuffer for all XY pixels holding pixel depth and initialize to max depth
                double[][] depthBuffer = new double[frameWidth][frameHeight];
                
                for (int x = 0; x < frameWidth; x++) {
                    for (int y = 0; y < frameHeight; y++) {
                        depthBuffer[x][y] = Double.MAX_VALUE;
                    }
                }


                //create frameBuffer for all XY pixels to hold pixel color and initialize to black
                Color[][] frameBuffer = new Color[frameWidth][frameHeight];

                for (int x = 0; x < frameWidth; x++) {
                    for (int y = 0; y < frameHeight; y++) {
                        frameBuffer[x][y] = Color.BLACK;
                    }
                }


                //process triangles
                for (Triangle t : tris) {


                    //update face brightness:
                    //get edge vectors then face normal vector
                    //get the dot product with the light vector

                    //calculate two edge vectors of traingle
                    double[] v1 = {t.getP2().getX() - t.getP1().getX(), t.getP2().getY() - t.getP1().getY(), t.getP2().getZ() - t.getP1().getZ()};
                    double[] v2 = {t.getP3().getX() - t.getP1().getX(), t.getP3().getY() - t.getP1().getY(), t.getP3().getZ() - t.getP1().getZ()};


                    //calculate cross product of edge vectors to get normal vector
                    double[] normal = {
                        v1[1] * v2[2] - v1[2] * v2[1],
                        v1[2] * v2[0] - v1[0] * v2[2],
                        v1[0] * v2[1] - v1[1] * v2[0]
                    };


                    //normalize normal vector
                    double length = Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
                    normal[0] /= length;
                    normal[1] /= length;
                    normal[2] /= length;


                    //back-face culling
                    double vRoll = sceneH.getValue() * Math.PI / 180;
                    double vPitch = sceneV.getValue() * Math.PI / 180;
                    double[] viewerSourceVec = new double[] {Math.sin(vRoll) * Math.cos(vPitch), -Math.sin(vPitch), Math.cos(vRoll) * Math.cos(vPitch)};
                    double viewFaceProduct = normal[0] * viewerSourceVec[0] + normal[1] * viewerSourceVec[1] + normal[2] * viewerSourceVec[2];
                    if (viewFaceProduct < 0) {
                        continue;
                    }


                    //calculate dotProduct of triangle normal vector and light direction vector
                    double dotProduct = normal[0] * ligthSourceVec[0] + normal[1] * ligthSourceVec[1] + normal[2] * ligthSourceVec[2];


                    //get shaded color (gamma-correct the linear dot product)
                    double correctedDotProduct = Math.pow(dotProduct, 1.0 / 2.2);
                    Color finalColor = new Color(Math.min(255, (int)  (t.getColor().getRed() * correctedDotProduct)), Math.min(255, (int) (t.getColor().getGreen() * correctedDotProduct)), Math.min(255, (int) (t.getColor().getBlue() * correctedDotProduct)));


                    //apply scene pitch and heading transform
                    //this is performed after lighting as it is scene rotation as opposed to object rotation
                    Node p1Transform = transformMatrix.multiplyPoint(t.getP1());
                    Node p2Transform = transformMatrix.multiplyPoint(t.getP2());
                    Node p3Transform = transformMatrix.multiplyPoint(t.getP3());


                    //zoom calculation
                    //translate linear input to exponential centering at 1 via function 0.5 * 2^(x/100)
                    double scale = 0.5 * Math.pow(2.0, (zoom.getValue()/100.0));


                    //adjust points based on zoom scaling factor and frame center
                    p1Transform.setX(p1Transform.getX() * scale + centerX);
                    p1Transform.setY(p1Transform.getY() * scale + centerY);
                    p1Transform.setZ(p1Transform.getZ() * scale);

                    p2Transform.setX(p2Transform.getX() * scale + centerX);
                    p2Transform.setY(p2Transform.getY() * scale + centerY);
                    p2Transform.setZ(p2Transform.getZ() * scale);

                    p3Transform.setX(p3Transform.getX() * scale + centerX);
                    p3Transform.setY(p3Transform.getY() * scale + centerY);
                    p3Transform.setZ(p3Transform.getZ() * scale);


                    //perform rasterization (display on pixel-by-pixel basis)

                    //create bounding box
                    double minX = Math.min( Math.min(p1Transform.getX(), p2Transform.getX()), p3Transform.getX());
                    double maxX = Math.max( Math.max(p1Transform.getX(), p2Transform.getX()), p3Transform.getX());

                    double minY = Math.min( Math.min(p1Transform.getY(), p2Transform.getY()), p3Transform.getY());
                    double maxY = Math.max( Math.max(p1Transform.getY(), p2Transform.getY()), p3Transform.getY());


                    //iterate through bounding box pixels checking if pixel in triangle, update depth buffer accordingly
                    for (int x = (int) minX; x <= maxX; x++) {
                        for (int y = (int) minY; y <= maxY; y++) {
                            if (pointInTriangle(x, y, p1Transform, p2Transform, p3Transform)) {
                                double depth = getDepth(x, y, p1Transform, p2Transform, p3Transform);

                                //if pixel closer to viewer than current depth buffer pixel XY, update depth and frame buffers
                                if (depth < depthBuffer[x][y]) {
                                    depthBuffer[x][y] = depth;
                                    frameBuffer[x][y] = finalColor;
                                }
                            }
                        }
                    }
                }


                //draw pixels
                for (int x = 0; x < frameWidth; x++) {
                    for (int y = 0; y < frameHeight; y++) {
                        g2.setColor(frameBuffer[x][y]);
                        g2.drawRect(x, y, 1, 1);
                    }
                }


            }

        };

        //create change listeners; update render on slider update
        sceneH.addChangeListener(e -> renderPanel.repaint());
        sceneV.addChangeListener(e -> renderPanel.repaint());
        lightH.addChangeListener(e -> renderPanel.repaint());
        lightV.addChangeListener(e -> renderPanel.repaint());
        zoom.addChangeListener(e -> renderPanel.repaint());


        frame.add(renderPanel, BorderLayout.CENTER);
        frame.setTitle("Java 3D Renderer");
        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
    }




    //
    static void setupSliders(Frame frame, JSlider sceneH, JSlider sceneV, JSlider lightH, JSlider lightV, JSlider zoom) {
        //South sliders below container
        JPanel southPanel = new JPanel(new GridLayout(10, 1));

        southPanel.add(new JLabel(""));
        southPanel.add(new JLabel("Scene Horizontal Rotation:", SwingConstants.CENTER));
        southPanel.add(sceneH);
        southPanel.add(new JLabel(""));
        southPanel.add(new JLabel("Light Horizontal rotation:", SwingConstants.CENTER));
        southPanel.add(lightH);
        southPanel.add(new JLabel(""));
        southPanel.add(new JLabel("Zoom:", SwingConstants.CENTER));
        southPanel.add(zoom);
        southPanel.add(new JLabel(""));

        frame.add(southPanel, BorderLayout.SOUTH);



        //West sliders left of container
        JPanel westPanel = new JPanel(new GridLayout (1, 4));

        westPanel.add(new JLabel(""));
        westPanel.add(new VerticalLabel("Light Vertical Rotation:", SwingConstants.CENTER, -1));
        westPanel.add(lightV);
        westPanel.add(new JLabel(""));

        frame.add(westPanel, BorderLayout.WEST);



        //East sliders right of container
        JPanel eastPanel = new JPanel(new GridLayout (1,4));

        eastPanel.add(new JLabel(""));
        eastPanel.add(sceneV);
        eastPanel.add(new VerticalLabel("Scene Vertical Rotation:", SwingConstants.CENTER, 1));
        eastPanel.add(new JLabel(""));

        frame.add(eastPanel, BorderLayout.EAST);


        //top margin
        frame.add(new JLabel(" "), BorderLayout.NORTH);
    }



    //generate sphere of triangles
    static void generateSphere(List<Triangle> tris) {
        int divisions = 65;

        //define list of colors for variety
        Color[] colors = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA
        };

        int colorIndex = 0;

        for (int i = 0; i < divisions; i++) {
            for (int j = 0; j < divisions; j++) {
                double theta1 = i * 2 * Math.PI / divisions;
                double theta2 = (i + 1) * 2 * Math.PI / divisions;
                double phi1 = j * Math.PI / divisions;
                double phi2 = (j + 1) * Math.PI / divisions;

                Node p1 = new Node(
                        Math.sin(theta1) * Math.sin(phi1) * 100,
                        Math.cos(phi1) * 100,
                        Math.cos(theta1) * Math.sin(phi1) * 100
                );

                Node p2 = new Node(
                        Math.sin(theta1) * Math.sin(phi2) * 100,
                        Math.cos(phi2) * 100,
                        Math.cos(theta1) * Math.sin(phi2) * 100
                );

                Node p3 = new Node(
                        Math.sin(theta2) * Math.sin(phi1) * 100,
                        Math.cos(phi1) * 100,
                        Math.cos(theta2) * Math.sin(phi1) * 100
                );

                Node p4 = new Node(
                        Math.sin(theta2) * Math.sin(phi2) * 100,
                        Math.cos(phi2) * 100,
                        Math.cos(theta2) * Math.sin(phi2) * 100
                );

                //assign colors sequentially from the predefined list
                tris.add(new Triangle(p1, p2, p3, colors[(colorIndex++) % colors.length]));
                tris.add(new Triangle(p2, p4, p3, colors[(colorIndex++) % colors.length]));
            }
        }
    }



    //check if a x,y point is inside a 2D triangle
    static boolean pointInTriangle(int x, int y, Node p1, Node p2, Node p3) {
        //calculate barycentric coordinates
        double area = (p2.getY() - p3.getY()) * (p1.getX() - p3.getX()) + (p3.getX() - p2.getX()) * (p1.getY() - p3.getY());
        double alpha = ((p2.getY() - p3.getY()) * (x - p3.getX()) + (p3.getX() - p2.getX()) * (y - p3.getY())) / area;
        double beta = ((p3.getY() - p1.getY()) * (x - p3.getX()) + (p1.getX() - p3.getX()) * (y - p3.getY())) / area;
        double gamma = 1 - alpha - beta;

        //check if the point is inside the triangle
        return alpha >= 0 && beta >= 0 && gamma >= 0;
    }



    //get x,y point depth in triangle
    static double getDepth(int x, int y, Node p1, Node p2, Node p3) {
        //calculate barycentric coordinates
        double area = (p2.getY() - p3.getY()) * (p1.getX() - p3.getX()) + (p3.getX() - p2.getX()) * (p1.getY() - p3.getY());
        double alpha = ((p2.getY() - p3.getY()) * (x - p3.getX()) + (p3.getX() - p2.getX()) * (y - p3.getY())) / area;
        double beta = ((p3.getY() - p1.getY()) * (x - p3.getX()) + (p1.getX() - p3.getX()) * (y - p3.getY())) / area;
        double gamma = 1 - alpha - beta;

        //calculate depth using barycentric coordinates
        return -(alpha * p1.getZ() + beta * p2.getZ() + gamma * p3.getZ());
    }
    
}