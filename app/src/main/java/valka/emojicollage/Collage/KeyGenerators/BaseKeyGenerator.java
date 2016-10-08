package valka.emojicollage.Collage.KeyGenerators;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * Created by ValkA on 10-Sep-16.
 */
public abstract class BaseKeyGenerator {
    public static double sqrDistance(double[] k1, double[] k2){
        double sqrDist = 0;
        for (int i = 0; i< k1.length; ++i){
            double d = (k2[i]-k1[i]);
            sqrDist += d*d;
        }
        return sqrDist;
    }

    public abstract int getKeyDimension();
    public double[] calculateKey(Bitmap bitmap){
        return calculateKey(bitmap,0,0,bitmap.getWidth(), bitmap.getHeight());
    }
    public double[] calculateKey(Bitmap bitmap, Rect rect){
        return calculateKey(bitmap, rect.left, rect.top, rect.width(), rect.height());
    }
    public abstract double[] calculateKey(Bitmap bitmap, int x, int y, int width, int height);
}
