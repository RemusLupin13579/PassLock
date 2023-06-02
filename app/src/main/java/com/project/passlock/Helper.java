package com.project.passlock;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class Helper {

    public static byte[] bitmapToByteArray(Bitmap in){
        if (in != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            in.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            return bytes.toByteArray();
        }
        return null;
    }
    public static Bitmap byteArrayToBitmap(byte[] bytes) {
        if (bytes != null) {
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return decodedBitmap;
        }
        //return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }
}
