package valka.emojicollage.Collage.PatchLoaders;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;

import java.util.LinkedList;
import java.util.List;

import valka.emojicollage.Utils.BitmapHelper;

/**
 * Created by ValkA on 09-Oct-16.
 */
public class GalleryLoader extends BasePatchLoader{
    final private static String TAG = "GalleryLoader";
    final Context context;
    final int patchSize;

    public GalleryLoader(Context context, int patchSize){
        this.context = context;
        this.patchSize = patchSize;
    }

    @Override
    public List<Bitmap> getPatchesList(PatchLoaderListener listener) {
        String[] columns = new String[] {
                ImageColumns.DATA,
                ImageColumns.HEIGHT,
                ImageColumns.WIDTH};
        String where = ImageColumns.DATA + " LIKE '%DCIM%' OR " + ImageColumns.DATA + " LIKE '%WhatsApp%' OR " + ImageColumns.DATA + " LIKE '%Camera%'";//TODO: give user to choose album or something like that ...
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
                options.inSampleSize = BitmapHelper.calculateInSampleSize(width, height, patchSize, patchSize);
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                patchesList.add(bitmap);
                //Log.i(TAG, String.format("(%dx%d)=>(%dx%d)\t\t%s",width,height,bitmap.getWidth(),bitmap.getHeight(),path));
                if(listener != null){
                    listener.onPatchLoaderProgress(bitmap, (float)cursor.getPosition()/(float)cursor.getCount());
                }
            } while (cursor.moveToNext() && !Thread.interrupted());
        }
        return patchesList;
    }
}
