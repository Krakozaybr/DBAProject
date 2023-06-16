package com.example.dbaproject.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dbaproject.api.models.processed_data.ProcessedDataCreate;

import java.io.IOException;
import java.util.List;

public class ImageUtils {

    public static float getImageScale(Bitmap bmp, float maxWidth, float maxHeight){
        return Math.min(
                maxWidth / (float)bmp.getWidth(),
                maxHeight / (float)bmp.getHeight()
        );
    }

    public static Bitmap getScaledBitmap(Bitmap bm, float scale){
        return getResizedBitmap(bm, (int)(bm.getWidth() * scale), (int)(bm.getHeight() * scale));
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        final Bitmap copy = bm.copy(bm.getConfig(), true);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(
                copy, 0, 0, width, height, matrix, false);
        copy.recycle();
        return resizedBitmap;
    }

    public static Bitmap getProcessedImage(Bitmap img, List<ProcessedDataCreate.Shape> shapes, float scale) {
        if (img == null) {
            return Bitmap.createBitmap(0, 0, Bitmap.Config.ARGB_8888);
        }
        Bitmap dest = img.copy(img.getConfig(), true);
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        for (ProcessedDataCreate.Shape shape : shapes) {
            paint.setColor(Color.GREEN);
            // draw bbox
            List<Integer> bbox = shape.bbox;
            Rect rect = new Rect(
                    (int)(bbox.get(0) * scale),
                    (int)(bbox.get(1) * scale),
                    (int)(bbox.get(2) * scale),
                    (int)(bbox.get(3) * scale)
            );
            canvas.drawRect(rect, paint);

            // draw polygon
            paint.setColor(Color.RED);
            List<List<Integer>> polygon = shape.polygon;
            int n = polygon.size();

            float[] points = new float[4 * n];
            for (int i = 0; i < n; i++) {
                points[i * 4] = polygon.get(i).get(0) * scale;
                points[i * 4 + 1] = polygon.get(i).get(1) * scale;
                points[i * 4 + 2] = polygon.get((i + 1) % n).get(0) * scale;
                points[i * 4 + 3] = polygon.get((i + 1) % n).get(1) * scale;
            }
            canvas.drawLines(points, paint);
        }

        return dest;
    }

    public static Bitmap loadBitmap(Uri uri, AppCompatActivity activity, int defaultBitmapId){
        try {
            return MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
        } catch (IOException e){
            return BitmapFactory.decodeResource(activity.getResources(), defaultBitmapId);
        }
    }

    public static Bitmap rotate90(Bitmap bmp){
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(90);
        Bitmap res = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
        bmp.recycle();
        return res;
    }
}
