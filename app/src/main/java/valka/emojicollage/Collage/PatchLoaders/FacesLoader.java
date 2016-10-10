package valka.emojicollage.Collage.PatchLoaders;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ValkA on 09-Oct-16.
 */
public class FacesLoader extends BasePatchLoader{
    final private static String TAG = "FacesLoader";
    final Context context;
    final int imageSize = 128;
    //final FaceDetector faceDetector;
    final Paint paint;
    final PorterDuffXfermode porterduff;

    public FacesLoader(Context context){
        this.context = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        porterduff = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    @Override
    public List<Bitmap> getPatchesList(PatchLoaderListener listener) {
        String[] columns = new String[] {
                ImageColumns.DATA,
                ImageColumns.HEIGHT,
                ImageColumns.WIDTH};
        String where = ImageColumns.DATA + /*" LIKE '%DCIM%' OR " + ImageColumns.DATA +*/ " LIKE '%WhatsApp%'";//TODO: give user to choose album or something like that ...
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, where, null, null);
        LinkedList<Bitmap> patchesList = new LinkedList<>();
        
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            final int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH);
            final int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            do {
                final String path = cursor.getString(dataColumn);
                final int width = cursor.getInt(widthColumn);
                final int height = cursor.getInt(heightColumn);
                options.inSampleSize = calculateInSampleSize(width, height, imageSize, imageSize);
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                //Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                //Canvas canvas = new Canvas(mutableBitmap);

                FaceDetector faceDetector = new FaceDetector.Builder(context)
                        .setMode(FaceDetector.FAST_MODE)
                        .setLandmarkType(FaceDetector.NO_LANDMARKS)
                        .build();
                SparseArray<Face> faces = faceDetector.detect(frame);
                for(int i=0; i<faces.size(); ++i){
                    Face face = faces.valueAt(i);
                    Bitmap faceBitmap = getFaceBitmap(bitmap, face);
                    patchesList.add(faceBitmap);
                    //canvas.drawRect(x,y, x+w, y+h, paint);
                    if(listener != null){
                        listener.onPatchLoaderProgress(faceBitmap, (float)cursor.getPosition()/(float)cursor.getCount());
                    }
                }
                faceDetector.release();
            } while (cursor.moveToNext());
        }
        return patchesList;
    }

    public Bitmap getFaceBitmap(Bitmap bitmap, Face face) {
        int x = Math.max((int) face.getPosition().x, 0);
        int y = Math.max((int) face.getPosition().y, 0);
        int w = ((int)face.getWidth() + x > bitmap.getWidth() ? bitmap.getWidth() - x : (int)face.getWidth());
        int h = ((int)face.getHeight() + y > bitmap.getHeight() ? bitmap.getHeight() - y : (int)face.getHeight());

        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Rect src = new Rect(x, y, x+w, y+h);
        final Rect dst = new Rect(0, 0, w, h);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xffff0000);
        paint.setStyle(Paint.Style.FILL);
        paint.setXfermode(null);
        canvas.drawOval(0, 0, w, h, paint);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) 4);
        paint.setXfermode(porterduff);
        canvas.drawBitmap(bitmap, src, dst, paint);

        return output;
    }

    private int calculateInSampleSize(final int width, final int height, final int reqWidth, final int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
