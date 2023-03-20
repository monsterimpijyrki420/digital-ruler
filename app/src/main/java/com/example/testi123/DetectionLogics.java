package com.example.testi123;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DetectionLogics {

    private double noteLength = 15.6;
    private double noteHeight = 6.63;
    private double threshold = 1.0;
    private double noteRatio1 = (noteLength-threshold) /noteHeight;
    private double noteRatio2 = (noteLength+threshold) /noteHeight;
    //private Mat matto;
    private double sideLength = 0.0;

    public DetectionLogics() {
    }

    public double doEverything(Bitmap bitmap, List<Float> pointList) {
        System.out.println(OpenCVLoader.initDebug());

        //reads the image to mat
        Mat og_mat = new Mat();
        Utils.bitmapToMat(bitmap, og_mat);

        //does the preprocessing
        Mat matto = new Mat(og_mat.rows(), og_mat.cols(), og_mat.type());
        Imgproc.GaussianBlur(og_mat, matto, new Size(3, 3), 0);
        Imgproc.cvtColor(matto, matto, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(matto, matto, 100, 200);

        //finds contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchey = new Mat();
        Imgproc.findContours(matto, contours, hierarchey, Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);

        //sorts the contours
        Collections.sort(contours, Collections.reverseOrder(contourSorter));

        //finds the matching contour
        //List<MatOfPoint> theContour = new ArrayList<>();
        //theContour.add(findTheCountour(contours));
        MatOfPoint noteContour = findTheNote(contours);

        //does the calculation
        double distance = distanceCalculator(pointList, sideLength);
        return distance;
        /**
        System.out.println(distance);
        System.out.println(distance);
        System.out.println(distance);
        System.out.println(distance);



        Scalar color = new Scalar(0, 0, 255);
        Imgproc.drawContours(og_mat, contours, 0, color, 1, Imgproc.LINE_8,
                hierarchey, 2, new Point() ) ;

        Bitmap drawnBitmap = Bitmap.createBitmap(og_mat.cols(), og_mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(og_mat, drawnBitmap);

        return drawnBitmap;
         **/
    }

    private MatOfPoint findTheNote(List<MatOfPoint> contours) {

        for (MatOfPoint contour : contours) {
            if (check4Match(contour)) {
                return contour;
            }
        }
        return null;
    }

    private boolean check4Match(MatOfPoint contour) {

        //gets the corner points
        Point rightPoint = findRightPoint(contour);
        Point leftPoint = findLeftPoint(contour);
        Point topPoint = findTopPoint(contour);
        Point bottomPoint = findBottomPoint(contour);

        /**
        System.out.println(leftPoint);
        System.out.println(rightPoint);
        System.out.println(topPoint);
        System.out.println(bottomPoint);
         **/

        if (noteDetector(leftPoint, rightPoint, topPoint, bottomPoint)) {
            System.out.println("ding ding ding");
            System.out.println("ding ding ding");
            System.out.println("ding ding ding");
            return true;
        }


        // gets corner points if the note is straight / perpindicular
        rightPoint = findTopRightPoint(contour);
        leftPoint = findBottomLeftPoint(contour);
        topPoint = findTopLeftPoint(contour);
        bottomPoint = findBottomRightPoint(contour);


        if (noteDetector(leftPoint, rightPoint, topPoint, bottomPoint)) {
            System.out.println("ding ding ding");
            System.out.println("ding ding ding");
            System.out.println("ding ding ding");
            return true;
        }

        return false;
    }

    private Point findRightPoint(MatOfPoint contour) {
        Point thePoint = null;
        for (Point point : contour.toList()) {
            if (thePoint == null || point.x > thePoint.x) {
                thePoint = point;
            }
        }
        return thePoint;
    }
    private Point findLeftPoint(MatOfPoint contour) {
        Point thePoint = null;
        for (Point point : contour.toList()) {
            if (thePoint == null || point.x < thePoint.x) {
                thePoint = point;
            }
        }
        return thePoint;
    }
    private Point findTopPoint(MatOfPoint contour) {
        Point thePoint = null;
        for (Point point : contour.toList()) {
            if (thePoint == null || point.y < thePoint.y) {
                thePoint = point;
            }
        }
        return thePoint;
    }
    private Point findBottomPoint(MatOfPoint contour) {
        Point thePoint = null;
        for (Point point : contour.toList()) {
            if (thePoint == null || point.y > thePoint.y) {
                thePoint = point;
            }
        }
        return thePoint;
    }
    private Point findTopLeftPoint(MatOfPoint contour) {
        Point thePoint = null;
        for (Point point : contour.toList()) {
            if (thePoint == null || point.x + point.y < thePoint.x + thePoint.y) {
                thePoint = point;
            }
        }
        return thePoint;
    }
    private Point findBottomRightPoint(MatOfPoint contour) {
        Point thePoint = null;
        for (Point point : contour.toList()) {
            if (thePoint == null || point.x + point.y > thePoint.x + thePoint.y) {
                thePoint = point;
            }
        }
        return thePoint;
    }
    private Point findTopRightPoint(MatOfPoint contour) {
        Point thePoint = null;
        for (Point point : contour.toList()) {
            if (thePoint == null || point.x - point.y > thePoint.x - thePoint.y) {
                thePoint = point;
            }
        }
        return thePoint;
    }
    private Point findBottomLeftPoint(MatOfPoint contour) {
        Point thePoint = null;
        for (Point point : contour.toList()) {
            if (thePoint == null || point.x - point.y < thePoint.x - thePoint.y) {
                thePoint = point;
            }
        }
        return thePoint;
    }

    private boolean noteDetector(Point leftPoint, Point rightPoint, Point topPoint, Point bottomPoint) {
        //calculates the distances of possible corners
        double side1 = Math.pow(Math.pow(leftPoint.x - bottomPoint.x, 2) +
                Math.pow(leftPoint.y - bottomPoint.y, 2), 0.5);
        double side2 = Math.pow(Math.pow(rightPoint.x - bottomPoint.x, 2) +
                Math.pow(rightPoint.y - bottomPoint.y, 2), 0.5);
        double side3 = Math.pow(Math.pow(leftPoint.x - topPoint.x, 2) +
                Math.pow(leftPoint.y - topPoint.y, 2), 0.5);
        double side4 = Math.pow(Math.pow(rightPoint.x - topPoint.x, 2) +
                Math.pow(rightPoint.y - topPoint.y, 2), 0.5);

        //calculates the ratios of sides
        double ratio1 = -100.0;
        double ratio2 = -100.0;
        if (side1 > side2) {
            ratio1 = side1 / side2;
        } else {
            ratio1 = side2 / side1;
        }
        if (side3 > side4) {
            ratio2 = side3 / side4;
        } else {
            ratio2 = side4 / side3;
        }

        /**
        System.out.println("ratiot");
        System.out.println(ratio1);
        System.out.println(ratio2);
        System.out.println(noteRatio1);
        System.out.println(noteRatio2);
         **/
        if (ratio1 > noteRatio1 && ratio1 < noteRatio2) {
            if (ratio2 > noteRatio1 && ratio2 < noteRatio2) {

                //updates the sideLength, i.e. note length in pixels
                if (side1 > side2) {
                    sideLength = side1;
                } else {
                    sideLength = side2;
                }

                return true;
            }
        }
        sideLength = 0.0;
        return false;
    }


    private double distanceCalculator( List<Float> point_list, double reference_pixels) {
            //int pixel1_row, int pixel1_column, int pixel2_row, int pixel2_column,

        float pixel1_row = point_list.get(0);
        float pixel1_column = point_list.get(1);
        float pixel2_row = point_list.get(2);
        float pixel2_column = point_list.get(3);
        double pixels_2_cm = noteLength / reference_pixels;
        double pixel_distance_sq = Math.pow((pixel1_row - pixel2_row), 2)
                + Math.pow((pixel1_column - pixel2_column), 2);
        double pixel_distance = Math.pow(pixel_distance_sq, 0.5);

        // there was some problem with the screen size
        // and image size being differet
        // so the 0.157 is the correction
        return pixel_distance*pixels_2_cm* 0.16;
    }

    Comparator<MatOfPoint> contourSorter = new Comparator<MatOfPoint>() {
        @Override
        public int compare(MatOfPoint o1, MatOfPoint o2) {
            double area1 = Imgproc.contourArea(o1);
            double area2 = Imgproc.contourArea(o2);
            return Double.compare(area1, area2);
        }
    };
}
