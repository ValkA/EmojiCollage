package valka.emojicollage.Collage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import net.sf.javaml.core.kdtree.KDTree;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import valka.emojicollage.Collage.KeyGenerators.BaseKeyGenerator;
import valka.emojicollage.Collage.PatchLoaders.BasePatchLoader;
import valka.emojicollage.Collage.PatchLoaders.PatchLoaderListener;
import valka.emojicollage.Utils.CannyEdge.CannyEdgeWrapper;

/**
 * Created by ValkA on 10-Sep-16.
 */
public class CollageCreator {
    public enum CreatorType {RandomWithEdges, RandomBySize, Linear}
    private static String TAG = "CollageCreator";
    private final int subimageSize = 32;//width & height
    private KDTree subimagesTree;
    private BaseKeyGenerator keyGenerator;
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2));

    private final int cannyWidth = 400;
    private final int cannyHeight = 400;
    private final CannyEdgeWrapper cannyEdgeWrapper = new CannyEdgeWrapper(cannyWidth,cannyHeight);
    private final Paint paint = new Paint();

    public CollageCreator(){
        paint.setFilterBitmap(true);
    }

    public void loadPatches(final BasePatchLoader patchLoader, final BaseKeyGenerator keyGenerator, final PatchLoaderListener listener){
        this.keyGenerator = keyGenerator;
        subimagesTree = new KDTree(keyGenerator.getKeyDimension());
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<Bitmap> patchesList = patchLoader.getPatchesList(listener);
                for (Bitmap patch : patchesList) {
                    double[] patchKey = keyGenerator.calculateKey(patch, 0, 0, patch.getWidth(), patch.getHeight());
                    subimagesTree.insert(patchKey, patch);
                }
                if(listener != null) {
                    listener.onPatchLoaderFinnished();
                }
            }
        });
    }

    public void createCollage(final Bitmap input, final CollageListener listener, final int density, final double randomness, final CreatorType type, final int outputBoxSize){
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                float max = Math.max(input.getWidth(), input.getHeight());
                float min = Math.min(input.getWidth(), input.getHeight());
                float ratio = min/max;
                int scaledWidth, scaledHeight;
                if(input.getWidth()>input.getHeight()){
                    scaledWidth = outputBoxSize;
                    scaledHeight = (int)(outputBoxSize*ratio);
                } else {
                    scaledWidth = (int)(outputBoxSize*ratio);
                    scaledHeight = outputBoxSize;
                }
                final Bitmap scaledInput = Bitmap.createScaledBitmap(input, scaledWidth, scaledHeight, true);
                Bitmap output = Bitmap.createBitmap(scaledInput.getWidth(), scaledInput.getHeight(), Bitmap.Config.ARGB_8888);

                switch (type) {
                    case Linear:
                        linearCreator(scaledInput, output, listener, density, randomness);
                        break;
                    case RandomBySize:
                        randomBySizeCreator(scaledInput, output, listener, density, randomness);
                        break;
                    case RandomWithEdges:
                        randomBySizeCreator(scaledInput, output, listener, density, randomness);
                        cannyEdgedCreator(scaledInput, output, listener, density, randomness);
                        break;
                }

                listener.onFinnished(output);
            }
        });
    }

    private void cannyEdgedCreator(final Bitmap scaledInput, final Bitmap output, final CollageListener listener, final int density, final double randomness){
        Canvas outputCanvas = new Canvas(output);
        Rect dstRect = new Rect();
        int cannyDensity = 3;

        //canny edge add
        cannyEdgeWrapper.processCanny(scaledInput);
        int[] cannyPixels = cannyEdgeWrapper.getCannyPixels();
        for (int i=0; i<cannyPixels.length; ++i){
            int grayscale = Color.blue(cannyPixels[i]);
            if(grayscale > 128){
                for(int j=0; j<cannyDensity; ++j) {
                    double randomX = (sqr((Math.random() - 0.5d) * 2) * 32d);
                    double randomY = (sqr((Math.random() - 0.5d) * 2) * 32d);
                    int centerX = cannyEdgeWrapper.transformCannyIToOutputX(i) + (int) randomX;
                    int centerY = cannyEdgeWrapper.transformCannyIToOutputY(i) + (int) randomY;
                    double dist = Math.abs(randomX) + Math.abs(randomY);
                    int frameSubimageSize = (int) ((dist / 2d + 8) / 2);
                    dstRect.top = centerX - frameSubimageSize;
                    dstRect.left = centerY - frameSubimageSize;
                    dstRect.bottom = centerX + frameSubimageSize;
                    dstRect.right = centerY + frameSubimageSize;
                    drawSubimage(outputCanvas, output, scaledInput, dstRect);
                }


            }
            if(i % cannyWidth == 0){
                float progress = (float)i/cannyPixels.length;
                listener.onProgress(output, progress);
            }
        }
    }

    private double sqr(double x){
        return x*x*x;
    }

    private Rect srcRect = new Rect();
    private void drawSubimage(Canvas outputCanvas, Bitmap outputBitmap, Bitmap scaledInput, Rect dstRect){
        if (rectNotInCanvas(outputCanvas, dstRect)) return;
        double[] desiredKey = keyGenerator.calculateKey(scaledInput, dstRect.left, dstRect.top, dstRect.width(), dstRect.height());
        Bitmap nearestSubimage = (Bitmap) subimagesTree.nearest(desiredKey);

        /* //add some color randomness
        double[] nearestKey = keyGenerator.calculateKey(nearestSubimage, 0, 0, nearestSubimage.getWidth(), nearestSubimage.getHeight());
        for(int i=0; i<desiredKey.length; ++i) {
            desiredKey[i] += (desiredKey[i] - nearestKey[i]) * (Math.random() * 2d - 1d) * 2d;
        }
        nearestSubimage = (Bitmap) subimagesTree.nearest(desiredKey);*/

        srcRect.left = 0;
        srcRect.top = 0;
        srcRect.right = nearestSubimage.getWidth();
        srcRect.bottom = nearestSubimage.getHeight();
        outputCanvas.drawBitmap(nearestSubimage, srcRect, dstRect, paint);
    }

    private void randomBySizeCreator(final Bitmap scaledInput, final Bitmap output, final CollageListener listener, final int density, final double randomness){
        Canvas outputCanvas = new Canvas(output);
        Rect dstRect = new Rect();
        for(int i=0; i<density; ++i){
            int min = Math.min(scaledInput.getWidth(), scaledInput.getHeight());
            int max = Math.max(scaledInput.getWidth(), scaledInput.getHeight());
            int subimages = (i+2)*(i+2);//for min line
            int frameSubimageSize = min/subimages;
            if(frameSubimageSize < 4){
                Log.e(TAG, "too small subimages, breaking");
                break;
            }
            int subimagesCount = (max/frameSubimageSize+5)*(min/frameSubimageSize+5);
            for(int j=0; j < subimagesCount; ++j){
                int sizeRandomness = (int)((Math.random())*randomness*frameSubimageSize);
                dstRect.left = (int)(Math.random()*outputCanvas.getWidth());
                dstRect.top = (int)(Math.random()*outputCanvas.getHeight());
                dstRect.right = dstRect.left + frameSubimageSize;
                dstRect.bottom = dstRect.top + frameSubimageSize;
                //randomness in size
                dstRect.left -= (dstRect.left >= sizeRandomness/2 ? sizeRandomness/2 : 0);
                dstRect.top -= (dstRect.top >= sizeRandomness/2 ? sizeRandomness/2 : 0);
                dstRect.right += (scaledInput.getWidth() - dstRect.right <= (sizeRandomness+1)/2 ? (sizeRandomness+1)/2 : 0);
                dstRect.bottom += (scaledInput.getHeight() - dstRect.bottom <= (sizeRandomness+1)/2 ? (sizeRandomness+1)/2 : 0);
                drawSubimage(outputCanvas, output, scaledInput, dstRect);

                float progress = ((float) i + (float) j / subimagesCount) / ((float) density);
                listener.onProgress(output, progress);
            }
        }
    }

    private void linearCreator(final Bitmap scaledInput, final Bitmap output, final CollageListener listener, final int density, final double randomness){
        Canvas outputCanvas = new Canvas(output);
        Rect dstRect = new Rect();
        int widthSubimages = scaledInput.getWidth() / subimageSize;
        int heightSubimages = scaledInput.getHeight() / subimageSize;
        int dr = subimageSize/density;
        for(int i=0; i<density; ++i) {
            for (int y = 0; y < heightSubimages; ++y) {
                for (int x = 0; x < widthSubimages; ++x) {
                    int sizeRandomness = (int)((Math.random())*randomness*subimageSize);
                    dstRect.left = x * subimageSize + dr*i;
                    dstRect.top = y * subimageSize + dr*i;
                    dstRect.right = dstRect.left + subimageSize;
                    dstRect.bottom = dstRect.top + subimageSize;
                    //randomness in size
                    dstRect.left -= (dstRect.left >= sizeRandomness/2 ? sizeRandomness/2 : 0);
                    dstRect.top -= (dstRect.top >= sizeRandomness/2 ? sizeRandomness/2 : 0);
                    dstRect.right += (scaledInput.getWidth() - dstRect.right <= (sizeRandomness+1)/2 ? (sizeRandomness+1)/2 : 0);
                    dstRect.bottom += (scaledInput.getHeight() - dstRect.bottom <= (sizeRandomness+1)/2 ? (sizeRandomness+1)/2 : 0);
                    drawSubimage(outputCanvas, output, scaledInput, dstRect);
                }
                float progress = (((float)i) + (((float)y)/((float)heightSubimages)))/((float)density);
                listener.onProgress(output, progress);
            }
        }
    }

    private boolean rectNotInBitmap(Bitmap bitmap, Rect rect){
        return (rect.right > bitmap.getWidth() || rect.bottom > bitmap.getHeight() || rect.left < 0 || rect.top < 0);
    }

    private boolean rectNotInCanvas(Canvas canvas, Rect rect){
        return (rect.right > canvas.getWidth() || rect.bottom > canvas.getHeight() || rect.left < 0 || rect.top < 0);
    }
}
