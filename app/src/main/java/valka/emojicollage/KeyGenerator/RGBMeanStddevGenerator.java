package valka.emojicollage.KeyGenerator;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by ValkA on 10-Sep-16.
 */
public class RGBMeanStddevGenerator extends BaseKeyGenerator {
    private final double stddevWeight;
    public RGBMeanStddevGenerator(double stddevWeight){
        this.stddevWeight = stddevWeight;
    }

    @Override
    public int getKeyDimension() {
        return 6;
    }

    @Override
    public double[] calculateKey(Bitmap bitmap, int x, int y, int width, int height) {
        double[] key = {0d, 0d, 0d, 0d, 0d, 0d};
        int[] pixels = new int[width*height];
        double alphasum = 0d;
        bitmap.getPixels(pixels, 0, width, x, y, width, height);
        for (int pixel:pixels) {
            double alpha = ((double)Color.alpha(pixel)/255d);
            alphasum += alpha;
            key[0] += ((double)Color.red(pixel)*alpha);
            key[1] += ((double)Color.green(pixel)*alpha);
            key[2] += ((double)Color.blue(pixel)*alpha);
        }
        key[0] /= alphasum;//mean red
        key[1] /= alphasum;//mean green
        key[2] /= alphasum;//mean blue

        for (int pixel:pixels) {
            double alpha = ((double)Color.alpha(pixel)/255d);
            double r = ((double)Color.red(pixel) - key[0]);
            key[3] += r*r*alpha;
            double g = ((double)Color.green(pixel) - key[1]);
            key[4] += g*g*alpha;
            double b =((double)Color.blue(pixel) - key[2]);
            key[5] += b*b*alpha;
        }
        key[3] = Math.sqrt(key[3]/alphasum)*stddevWeight;//deviance red
        key[4] = Math.sqrt(key[4]/alphasum)*stddevWeight;//deviance green
        key[5] = Math.sqrt(key[5]/alphasum)*stddevWeight;//deviance blue
        return key;
    }
}
