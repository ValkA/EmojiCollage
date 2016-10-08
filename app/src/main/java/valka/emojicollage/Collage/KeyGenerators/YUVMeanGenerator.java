package valka.emojicollage.Collage.KeyGenerators;

import android.graphics.Bitmap;

import valka.emojicollage.Utils.ColorConverter;

/**
 * Created by ValkA on 10-Sep-16.
 */
public class YUVMeanGenerator extends RGBMeanGenerator {


    @Override
    public int getKeyDimension() {
        return 3;
    }

    @Override
    public double[] calculateKey(Bitmap bitmap, int x, int y, int width, int height) {
        double[] rgb = super.calculateKey(bitmap, x, y, width, height);
        double[] yuv = new double[3];
        ColorConverter.RGBToYUV(rgb, yuv);
        return yuv;
    }

}
