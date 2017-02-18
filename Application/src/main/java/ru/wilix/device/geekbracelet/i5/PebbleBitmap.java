package ru.wilix.device.geekbracelet.i5;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.google.common.primitives.UnsignedInteger;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ru.wilix.device.geekbracelet.App;
import ru.wilix.device.geekbracelet.BuildConfig;

public class PebbleBitmap {
    public final byte[] data;
    public final UnsignedInteger flags;
    public final short height;
    public int index;
    public int offset;
    public final UnsignedInteger rowLengthBytes;
    public final short width;
    public final short f1286x;
    public final short f1287y;
    public static Typeface unifont = Typeface.createFromAsset(App.mContext.getAssets(), "ClearSans-Light.ttf");

    public static final String PIXEL_OFF = "0";
    public static final String PIXEL_ON = "1";
    public static final int SETTINGS_DEFAULT_MBR_SIZE = 4096;

    private PebbleBitmap(UnsignedInteger _rowLengthBytes, UnsignedInteger _flags, short _x, short _y, short _width, short _height, byte[] _data) {
        this.offset = 0;
        this.index = 0;
        this.rowLengthBytes = _rowLengthBytes;
        this.flags = _flags;
        this.f1286x = _x;
        this.f1287y = _y;
        this.width = _width;
        this.height = _height;
        this.data = _data;
    }

    public static PebbleBitmap fromString(String text, int w, int len) {
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(14.0f);
        if (w == 32)
            textPaint.setTextAlign(Align.CENTER);
        textPaint.setTypeface(unifont);
        float spacingmult = 1.0f;
        float spacingadd = 0.49f;
        boolean includepad = false;
        StaticLayout sl = new StaticLayout(text, textPaint, w, Alignment.ALIGN_CENTER, spacingmult, spacingadd, includepad);
        int h = sl.getHeight();
        if (h > len * 16) {
            h = len * 16;
        }
        Bitmap newBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        sl.draw(new Canvas(newBitmap));
        return fromAndroidBitmap(newBitmap);
    }

    public static PebbleBitmap fromAndroidBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int rowLengthBytes = width / 8;
        ByteBuffer data = ByteBuffer.allocate(rowLengthBytes * height);
        data.order(ByteOrder.LITTLE_ENDIAN);
        StringBuffer stringBuffer;
        StringBuilder stringBuffer1 = new StringBuilder("");
        for (int y = 0; y < height; y++) {
            int[] pixels = new int[width];
            bitmap.getPixels(pixels, 0, width * 2, 0, y, width, 1);
            stringBuffer = new StringBuffer("");

            for (int x = 0; x < width; x++) {
                if (pixels[x] == 0) {
                    stringBuffer.append(PIXEL_OFF);
                    if (BuildConfig.DEBUG)
                        stringBuffer1.append("-");
                } else {
                    stringBuffer.append(PIXEL_ON);
                    if (BuildConfig.DEBUG)
                        stringBuffer1.append("#");
                }
            }
            for (int k = 0; k < rowLengthBytes * 8; k += 8) {
                data.put((byte) new BigInteger(stringBuffer.substring(k, k + 8), 2).intValue());
            }
            if (BuildConfig.DEBUG) {
                stringBuffer1.append("\n");
                Log.i("info", stringBuffer.toString());
            }
        }
        if (BuildConfig.DEBUG)
            System.out.println(stringBuffer1.toString());

        if (!bitmap.isRecycled())
            bitmap.recycle();
        System.gc();
        return new PebbleBitmap(UnsignedInteger.fromIntBits(rowLengthBytes), UnsignedInteger.fromIntBits(SETTINGS_DEFAULT_MBR_SIZE), (short) 0, (short) 0, (short) width, (short) height, data.array());
    }

    public static PebbleBitmap fromPng(InputStream paramInputStream) throws IOException {
        return fromAndroidBitmap(BitmapFactory.decodeStream(paramInputStream));
    }
}
