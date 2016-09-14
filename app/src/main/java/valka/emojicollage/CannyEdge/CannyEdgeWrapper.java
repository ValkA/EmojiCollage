package valka.emojicollage.CannyEdge;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by ValkA on 11-Sep-16.
 */
public class CannyEdgeWrapper {
    private final CannyEdgeDetector cannyEdgeDetector = new CannyEdgeDetector();

    private final Bitmap sourceBitmap;
    private final Canvas sourceCanvas;
    private final Paint paint;

    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    private final int cannyWidth;
    private final int cannyHeight;

    public CannyEdgeWrapper(int cannyWidth, int cannyHeight){
        this.cannyWidth = cannyWidth;
        this.cannyHeight = cannyHeight;

        dstRect.top = 0;
        dstRect.left = 0;
        dstRect.bottom = cannyHeight;
        dstRect.right = cannyWidth;

        sourceBitmap = Bitmap.createBitmap(cannyWidth, cannyHeight, Bitmap.Config.ARGB_8888);//TODO: grayscale ?
        sourceCanvas = new Canvas(sourceBitmap);
        paint = new Paint();
        paint.setFilterBitmap(true);
        cannyEdgeDetector.setSourceImage(sourceBitmap);
    }

    public void processCanny(Bitmap bitmap){
        srcRect.top = 0;
        srcRect.left = 0;
        srcRect.bottom = bitmap.getHeight();
        srcRect.right = bitmap.getWidth();
        sourceCanvas.drawBitmap(bitmap, srcRect, dstRect, null);
        cannyEdgeDetector.process();
    }


    public Bitmap getBit(){
        return cannyEdgeDetector.getEdgesBitmap();
    }

    public double getEdgeIntencity(int bitmapX, int bitmapY, int r){
        int localX = (int)((float)bitmapX*dstRect.right/srcRect.right);
        int localY = (int)((float)bitmapY*dstRect.bottom/srcRect.bottom);

        int fromX = Math.max(0, localX-r);
        int toX = Math.min(cannyWidth, localX + r);
        int fromY = Math.max(0, localY - r);
        int toY = Math.min(cannyHeight, localY + r);
        int count = (toX-fromX+1)*(toY-fromY+1);
        double intencity = 0d;
        for (int y = fromY; y < toY; ++y){
            for(int x = fromX; x < toX; ++x){
                intencity += Color.blue(cannyEdgeDetector.getEdgesPixels()[localY*cannyWidth + localX]);
            }
        }
        return intencity /= count*255d;
    }

    public int[] getCannyPixels(){
        return cannyEdgeDetector.getEdgesPixels();
    }

    public int transformOutputXYToI(int x, int y){
        int localX = (int)((float)x*dstRect.right/srcRect.right);
        int localY = (int)((float)y*dstRect.bottom/srcRect.bottom);
        return localY*cannyWidth + localX;
    }

    public int transformCannyIToOutputX(int i) {
        int x = i / cannyWidth;
        return (int)((double)x*(double)srcRect.bottom/(double)dstRect.bottom);
    }

    public int transformCannyIToOutputY(int i) {
        int y = i % cannyWidth;
        return (int)((double)y*(double)srcRect.right/(double)dstRect.right);
    }
}
