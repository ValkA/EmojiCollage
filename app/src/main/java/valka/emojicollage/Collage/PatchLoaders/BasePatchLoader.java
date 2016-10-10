package valka.emojicollage.Collage.PatchLoaders;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by ValkA on 09-Oct-16.
 */
public abstract class BasePatchLoader {
    public enum LoaderType {Emoji, Faces, Gallery}
    public abstract List<Bitmap> getPatchesList(PatchLoaderListener listener);
}
