package Internship;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImageRead extends JPanel {

    private static int[][] points;
    private static int range = 4;
    private static ArrayList<Shape> shape = new ArrayList<>();
    private static ImageRead irt = new ImageRead();
    private static int[] dimensionsOfImage = new int[2];
    private static Color color;

    public void addShape(Shape s) {
        shape.add(s);
    }

    public void changeColor(Color c){
        color = c;
    }

    public Color getColor(){
        return color;
    }

    public void rePaint(){
        repaint();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        int radiusOfPoint = 1;
        int counter = 0;

        this.setBackground(Color.BLACK);
        g2.setColor(Color.GREEN);

        for(int a = 0; a<points.length; a++){
            for (int b = 0; b<points[0].length; b++){
                if(points[a][b] != 0){
                    g2.drawOval(a,b,radiusOfPoint,radiusOfPoint);
                };
            }
        }

        g2.setColor(getColor());
        for (Shape s : shape) {
            g2.draw(s);
        }

    }



    public double[] findSlope(int x, int y){

        double accuracy;
        double slope;
        double intercept;
        double lastAccuracy = 0;
        double lastSlope = 0;
        double lastIntercept = 0;
        int boxAreaX = dimensionsOfImage[0]/20;
        int boxAreaY = dimensionsOfImage[1]/20;
        ArrayList<Double> xArea = new ArrayList<>();
        ArrayList<Double> yArea = new ArrayList<>();
        LinearRegression lr = null;
        int[] lastTolerantPoint;
        double[] xAreaArray;
        double[] yAreaArray;
        double[] line = new double[6];

        while(true){

            if(x+boxAreaX>points.length || y+boxAreaY>points[0].length){
                break;
            }

            for(int a = x; a<=x+boxAreaX; a++){
                for(int b = y; b<=y+boxAreaY; b++){
                    if(points[a][b] == 1){
                        xArea.add((double)a);
                        yArea.add((double)b);
                    }
                }
            }

            System.out.println("reachedMe!");

            xAreaArray = convertArrayListtoArrayDouble(xArea);
            yAreaArray = convertArrayListtoArrayDouble(yArea);

            System.out.println(xArea.toString());
            System.out.println(yArea.toString());

            if(xAreaArray.length == 0 || yAreaArray.length == 0){
                break;
            }

            lr = new LinearRegression(xAreaArray,yAreaArray);

            accuracy = 1-lr.R2();
            slope = lr.slope();
            intercept = lr.intercept();

            System.out.println(xAreaArray.length);
            System.out.println(yAreaArray.length);
            System.out.printf("Accuracy: %f\n",accuracy);
            System.out.println(lr.toString());

            System.out.printf("%f >= %f\n", accuracy,lastAccuracy);
            if(!(accuracy>=lastAccuracy)){
                break;
            }
            lastAccuracy = accuracy;
            lastSlope = slope;
            lastIntercept = intercept;

            System.out.printf("Box Area X: %d, Box Area Y: %d\n",boxAreaX,boxAreaY);
            boxAreaX = (int)(boxAreaX * 1.2);
            boxAreaY = (int)(boxAreaY * 1.2);

        }

        System.out.printf("final Accuracy: %f\n",lastAccuracy);
        System.out.printf("final Slope: %f\n",lastSlope);
        System.out.printf("final Intercept: %f\n",lastIntercept);


        line[0] = lastSlope;
        line[1] = lastIntercept;
        line[2] = x;
        line[3] = y;

        lastTolerantPoint = findLastTolerantPointFromLine(line,x,y,4,4);

        line[4] = lastTolerantPoint[0];
        line[5] = (int)(line[0]*line[4]+line[1]);

        if(lastAccuracy != 0){
            addShape(new Line2D.Double(line[2],line[3],line[4],line[5]));
        }

        rePaint();
        return line;

    }

    public static void drawFullLine(int x, int y){
        double[] line = irt.findSlope(x,y);

        while((int)line[4] != x && (int)line[1] != 0){
            System.out.println("-!-");
            System.out.println(Arrays.toString(line));
            line = irt.findSlope((int)line[4],(int)line[5]);
        }
    }

    public static int[] findLastTolerantPointFromLine(double[] line, int x1, int y1, int range, int tolerance){

        int[] similarPoints;
        int[] lastPoints = new int[2];
        int findX = x1;
        int findY = y1;
        double lowest = 999;
        double estimateYValue;

        while(true){
            similarPoints = findSimilarPointsF(findX,findY,range);

            System.out.println(similarPoints.length);
            if(similarPoints.length != 0){
                for(int c = 0; c<(similarPoints.length); c = c + 2){

                    System.out.println(Arrays.toString(similarPoints));
                    points[similarPoints[c]][similarPoints[c+1]] = 2;
                    irt.addShape(new Ellipse2D.Double(similarPoints[c],similarPoints[c+1],5,5));
                    estimateYValue = (line[0]*similarPoints[c]+line[1]);

                    System.out.printf("similar X Value: %d\n similar Y Value: %d\n", similarPoints[c],similarPoints[c+1]);

                    if(Math.abs(estimateYValue - similarPoints[c+1]) < lowest){
                        lowest = Math.abs(estimateYValue - similarPoints[c+1]);
                        findX = similarPoints[c];
                        findY = similarPoints[c+1];
                        System.out.printf("LOWEST: (%d,%d)\n", findX,findY);
                    }

                    if(!(estimateYValue+tolerance > similarPoints[c+1] && similarPoints[c+1] < estimateYValue+tolerance)){
                        irt.changeColor(Color.CYAN);
                        irt.rePaint();
                        return lastPoints;
                    }

                    lastPoints[0] = similarPoints[c];
                    lastPoints[1] = similarPoints[c+1];
                }
            }else{
                irt.changeColor(Color.CYAN);
                irt.rePaint();
                System.out.println("----");
                System.out.println(Arrays.toString(lastPoints));
                return lastPoints;
            }
            lowest = 999;
        }
    }

    public static int[] convertArrayListtoArrayInt(ArrayList<Integer> value){
        int[] ret = new int[value.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = value.get(i);
        }
        return ret;
    }

    public static double[] convertArrayListtoArrayDouble(ArrayList<Double> value){
        double[] ret = new double[value.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = value.get(i);
        }
        return ret;
    }

    public static int[] findSimilarPointsF(int x, int y, int range) {

        ArrayList<Integer> value;
        value = new ArrayList<>();

        System.out.printf("%d > %d || %d > %d\n", x+range,dimensionsOfImage[0],y+range,dimensionsOfImage[1]);
        if(x+range>dimensionsOfImage[0] || y+range>dimensionsOfImage[1] || x<0 || y-range<0){
            return convertArrayListtoArrayInt(value);
        }

        for(int a = x+range; a>x; a--){
            for(int b = y+range; b>y-range; b--){
                if (points[a][b] == 1) {
                    value.add(a);
                    value.add(b);
                }
            }
        }

        irt.rePaint();
        return convertArrayListtoArrayInt(value);
    }

    public static double findDistance(int x1, int y1, int x2, int y2){
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

    public static double[] findLine(int x1, int y1, int x2, int y2){
        double[] line = {0,0};

        line[0] = (double)(y2 - y1)/(x2 - x1);
        line[1] = (double)y1 - line[0]*(double)x1;

        return line;
    }

    public static boolean similarTest(Color a, Color b, int intensity) {
        return (a.getRed() < b.getRed() + intensity && a.getRed() > b.getRed() - intensity) ||
                (a.getBlue() < b.getBlue() + intensity && a.getBlue() > b.getBlue() - intensity) ||
                (a.getGreen() < b.getGreen() + intensity && a.getGreen() > b.getGreen() - intensity);
    }

    public static void deepString(int[][] array){
        for(int[] a:array){
            System.out.println(Arrays.toString(a));
        }
    };

    public static void display(JFrame f){
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageRead ir = new ImageRead();
        f.add(ir);
        f.setSize(2000,1200);
        f.setVisible(true);
    }

    public static void main(String[] args){
        BufferedImage originalImg;
        BufferedImage outputImg;

        File input = new File("D:\\OneDrive\\11th Grade\\Internship\\coolCar.jpg");
        File output =  new File("D:\\OneDrive\\11th Grade\\Internship\\outputCar.jpg");

        Color[][] originalImgArray;

        JFrame jFrame = new JFrame("test");
        ImageRead irt = new ImageRead();

        int intensity = 50;


        try {
            originalImg = ImageIO.read(input);

            dimensionsOfImage[0] = originalImg.getWidth();
            dimensionsOfImage[1] = originalImg.getHeight();

            outputImg = new BufferedImage(originalImg.getWidth(),originalImg.getHeight(),BufferedImage.TYPE_INT_RGB);
            originalImgArray = new Color[originalImg.getWidth()][originalImg.getHeight()];
            points = new int[originalImg.getWidth()][originalImg.getHeight()];

            for(int a = 0; a<originalImgArray.length; a++){
                for (int b = 0; b<originalImgArray[0].length; b++){
                    originalImgArray[a][b] = new Color(originalImg.getRGB(a,b));
                    outputImg.setRGB(a,b,originalImg.getRGB(a,b));
                }
            }

            for(int a = range; a<originalImgArray.length - range; a = a + range){
                for (int b = range; b<originalImgArray[0].length - range; b = b + range){
                    if(similarTest(originalImgArray[a][b], originalImgArray[a][b-range], intensity)){
                        if (similarTest(originalImgArray[a][b], originalImgArray[a][b+range], intensity)){
                            if (similarTest(originalImgArray[a][b], originalImgArray[a-range][b], intensity)){
                                if (similarTest(originalImgArray[a][b], originalImgArray[a+range][b], intensity)){
                                    continue;
                                }
                            }
                        }
                    }
                    outputImg.setRGB(a, b, new Color(0, 200, 0, 200).getRGB());
                    points[a][b] = 1;
                    System.out.printf("%d %d\n", a,b);
                }
            }
            drawFullLine(1576,340);
            display(jFrame);
            ImageIO.write(outputImg, "jpg", output);


        } catch (IOException e) {
            System.out.println("read errors");
        }
    }
}
