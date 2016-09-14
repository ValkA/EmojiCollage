package valka.emojicollage;

import android.graphics.Bitmap;

/**
 * Created by ValkA on 11-Sep-16.
 */
public interface CollageListener {
    void onProgress(Bitmap bitmap, float progress);
    void onFinnished(Bitmap bitmap);
}
