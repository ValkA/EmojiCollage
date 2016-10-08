package valka.emojicollage.Collage.PatchLoaders;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import valka.emojicollage.R;

/**
 * Created by ValkA on 09-Oct-16.
 */
public class RawResourcesLoader extends BasePatchLoader{
    final Context context;

    public RawResourcesLoader(Context context){
        this.context = context;
    }

    @Override
    public List<Bitmap> getPatchesList(PatchLoaderListener listener) {
        ArrayList<Bitmap> patchesList = new ArrayList<>(R.raw.class.getFields().length);
        Field[] fields = R.raw.class.getFields();
        for(int i = 0; i<fields.length; i++){
            Bitmap bitmap = null;
            try {
                Integer resourceID = fields[i].getInt(fields[i]);
                bitmap = loadBitmap(context.getResources(), resourceID);
                patchesList.add(bitmap);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if(listener != null){
                listener.onPatchLoaderProgress(bitmap, (float)(i+1)/(float)fields.length);
            }
        }
        return patchesList;
    }

    private Bitmap loadBitmap(Resources resources, int resourceID){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inScaled = false;
        return BitmapFactory.decodeResource(resources, resourceID, opt);
    }
}
