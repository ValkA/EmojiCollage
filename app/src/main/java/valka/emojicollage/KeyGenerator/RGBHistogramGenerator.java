package valka.emojicollage.KeyGenerator;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by ValkA on 10-Sep-16.
 */
public class RGBHistogramGenerator extends BaseKeyGenerator {
    private static final int COLORS = 3;
    private static final int RED_HISTOGRAM = 0;
    private static final int GREEN_HISTOGRAM = 1;
    private static final int BLUE_HISTOGRAM = 2;

    private final int buckets;
    private final int keyDimension;
    private final int bucketRange;

    public RGBHistogramGenerator(int buckets){
        this.buckets = buckets;
        this.keyDimension = COLORS * buckets;//COLORS histograms - R,G and B
        this.bucketRange = (255/buckets);
    }

    @Override
    public int getKeyDimension() {
        return keyDimension;
    }

    @Override
    public double[] calculateKey(Bitmap bitmap, int x, int y, int width, int height) {
        double[] key = new double[keyDimension];
        int[] pixels = new int[width*height];
        double alphasum = 0d;
        bitmap.getPixels(pixels, 0, width, x, y, width, height);
        for (int pixel:pixels) {
            double alpha = ((double)Color.alpha(pixel)/255d);
            alphasum += alpha;
            key[RED_HISTOGRAM * buckets + getBucketIndex(Color.red(pixel))] += ((double)Color.red(pixel))*alpha;
            key[GREEN_HISTOGRAM * buckets + getBucketIndex(Color.green(pixel))] += ((double)Color.green(pixel))*alpha;
            key[BLUE_HISTOGRAM * buckets +  getBucketIndex(Color.blue(pixel))] += ((double)Color.blue(pixel))*alpha;
        }
        //normalize
        for(int i=0; i<keyDimension; ++i){
            key[i] /= alphasum;
        }
        return key;
    }

    private int getBucketIndex(int grayscale){
        return grayscale/(bucketRange+1);
    }
}
