package valka.emojicollage.KeyGenerator;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by ValkA on 10-Sep-16.
 */
public class RGBMeanGenerator extends BaseKeyGenerator {
    @Override
    public int getKeyDimension() {
        return 3;
    }

    @Override
    public double[] calculateKey(Bitmap bitmap, int x, int y, int width, int height) {
        double[] key = {0d, 0d, 0d};
        int[] pixels = new int[width*height];
        double alphasum = 0d;
        bitmap.getPixels(pixels, 0, width, x, y, width, height);
        for (int pixel:pixels) {
            double alpha = ((double)Color.alpha(pixel)/255d);
            alphasum += alpha;
            key[0] += ((double)Color.red(pixel))*alpha;
            key[1] += ((double)Color.green(pixel)*alpha);
            key[2] += ((double)Color.blue(pixel)*alpha);
        }
        if(alphasum != 0){
            key[0] /= alphasum;
            key[1] /= alphasum;
            key[2] /= alphasum;
        }
        return key;
    }
}
