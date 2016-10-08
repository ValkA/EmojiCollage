package valka.emojicollage.Collage.PatchLoaders;

import android.graphics.Bitmap;

/**
 * Created by ValkA on 09-Oct-16.
 */
public interface PatchLoaderListener {
    void onPatchLoaderProgress(Bitmap bitmap, float progress);
    void onPatchLoaderFinnished();
}
