package valka.emojicollage.Utils;

/**
 * Created by ValkA on 06-Oct-16.
 */
public class ColorConverter {
    public static void RGBToYUV(double[] rgb, double[] yuv){
        yuv[0] = 0.299d * rgb[0] + 0.587d * rgb[1] + 0.114d * rgb[2];
        yuv[1] = 0.492d * ( rgb[2] - yuv[0] );
        yuv[2] = 0.877d * ( rgb[0] - yuv[0] );
    }
}
