package com.ex.realcv.Func;


// OpenCV
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LibCv {

    // 파란색(H≈100~130) 옷을 마스크로 잡고, 외곽 윤곽선만 그려주는 함수
    public static Bitmap outlineBlueCloth(Bitmap srcBmp) {
        // 1) Bitmap -> Mat(BGR)
        Mat bgr = new Mat();
        org.opencv.android.Utils.bitmapToMat(srcBmp, bgr);          // RGBA일 수 있어 안전하게 변환
        Imgproc.cvtColor(bgr, bgr, Imgproc.COLOR_RGBA2BGR);

        // 2) HSV로 변환
        Mat hsv = new Mat();
        Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV);

        // 3) 파란색 범위 마스크 (조명 따라 S/V 하한은 필요시 조절)
        Mat mask = new Mat();
        Scalar lower = new Scalar(100, 60, 60);   // H,S,V
        Scalar upper = new Scalar(130, 255, 255);
        Core.inRange(hsv, lower, upper, mask);

        // 4) 노이즈 제거 & 구멍 메우기
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7,7));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);               // 작은 점 제거
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel, new Point(-1,-1), 2); // 구멍 메우기

        // 5) 윤곽선 추출(외곽만)
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 6) 너무 작은 조각 제거 + 부드럽게 근사 후 그리기
        double minArea = 0.005 * mask.rows() * mask.cols(); // 이미지의 0.5% 미만은 무시
        for (MatOfPoint c : contours) {
            if (Imgproc.contourArea(c) < minArea) continue;

            // 다각형 근사(선 깔끔)
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 2.0, true);
            MatOfPoint approxPts = new MatOfPoint(approx.toArray());

            // 윤곽선만 그리기 (보라색, 두께 5)
            List<MatOfPoint> one = Collections.singletonList(approxPts);
            Imgproc.drawContours(bgr, one, -1, new Scalar(255, 0, 255), 5);

            c2f.release(); approx.release(); approxPts.release();
        }

        // 7) Mat -> Bitmap
        Imgproc.cvtColor(bgr, bgr, Imgproc.COLOR_BGR2RGB);
        Bitmap out = Bitmap.createBitmap(bgr.cols(), bgr.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(bgr, out);

        // 8) 정리
        hsv.release(); mask.release(); kernel.release(); bgr.release();

        return out;
    }



}
