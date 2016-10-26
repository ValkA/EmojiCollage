package valka.emojicollage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Future;

import valka.emojicollage.Collage.CollageCreator;
import valka.emojicollage.Collage.CollageListener;
import valka.emojicollage.Collage.KeyGenerators.RGBMeanGenerator;
import valka.emojicollage.Collage.PatchLoaders.BasePatchLoader;
import valka.emojicollage.Collage.PatchLoaders.FacesLoader;
import valka.emojicollage.Collage.PatchLoaders.GalleryLoader;
import valka.emojicollage.Collage.PatchLoaders.PatchLoaderListener;
import valka.emojicollage.Collage.PatchLoaders.RawResourcesLoader;

public class MainActivity extends AppCompatActivity implements CollageListener, PatchLoaderListener{
    static private final int SELECT_PICTURE_RESULT = 1;
    private final MainActivity that = this;
    private final String TAG = "MainActivity";

    CollageCreator collageCreator;
    ImageView collageImageView;
    Button shareCollageButton;
    Button choosePhotoButton;
    Button saveCollageButton;
    Spinner typeSpinner;
    Spinner patchesSpinner;
    ProgressBar progressBar;

    Bitmap collage = null;
    Bitmap inputBitmap = null;
    Future currentFuture = null;
    BasePatchLoader currentLoader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermissions();

        collageImageView = (ImageView)findViewById(R.id.collageImageView);
        collageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentFuture != null && currentFuture.cancel(true)){
                    Toast.makeText(that, getResources().getString(R.string.on_interrupt), Toast.LENGTH_LONG).show();
                }
            }
        });

        shareCollageButton = (Button)findViewById(R.id.shareCollageButton);
        shareCollageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (collage != null) {
                    shareBitmap(collage, "collage");
                }
            }
        });

        saveCollageButton = (Button)findViewById(R.id.saveCollageButton);
        saveCollageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(that, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    that.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 2909);
                    Toast.makeText(that, "Please acquire permissions and try again", Toast.LENGTH_SHORT).show();
                } else {
                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "emojicollage" + System.currentTimeMillis() + ".png", "emoji collage by the cool app");
                    Toast.makeText(that, "Collage was saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        choosePhotoButton = (Button)findViewById(R.id.choosePhotoButton);
        choosePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_RESULT);
            }
        });
        typeSpinner = (Spinner)findViewById(R.id.typeSpinner);
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.list_item , CollageCreator.CreatorType.values());
        adapter.setDropDownViewResource(R.layout.list_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (inputBitmap != null) {
                    disable();
                    collageCreator.createCollage(inputBitmap, that, 5, 2d, (CollageCreator.CreatorType) typeSpinner.getSelectedItem(), 1920);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        patchesSpinner = (Spinner)findViewById(R.id.patchesSpinner);
        ArrayAdapter patchesAdapter = new ArrayAdapter<>(this, R.layout.list_item , BasePatchLoader.LoaderType.values());
        patchesAdapter.setDropDownViewResource(R.layout.list_dropdown_item);
        patchesSpinner.setAdapter(patchesAdapter);
        patchesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch ((BasePatchLoader.LoaderType)patchesSpinner.getSelectedItem()){
                    case Gallery:
                        currentLoader = new GalleryLoader(that, 32);
                        break;
                    case Emoji:
                        currentLoader = new RawResourcesLoader(that);
                        break;
                    case Faces:
                        currentLoader = new FacesLoader(that);
                        break;
                }
                currentFuture = collageCreator.loadPatches(currentLoader, new RGBMeanGenerator(), that);
                disable();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        progressBar = (ProgressBar)findViewById(R.id.progressbar);

        collageCreator = new CollageCreator();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE_RESULT) {
                Uri imageUri = data.getData();
                try {
                    if(inputBitmap != null){
                        inputBitmap.recycle();
                    }
                    inputBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    disable();
                    Future future = collageCreator.createCollage(inputBitmap, this, 5, 2d, (CollageCreator.CreatorType) typeSpinner.getSelectedItem(), 1920);
                } catch (IOException e) {
                    Toast.makeText(this, "File not found", Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }
            }
        }
    }

    private void shareBitmap(Bitmap bitmap,String fileName) {
        try {
            File file = new File(this.getCacheDir(), fileName + ".png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enable(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentFuture = null;
                typeSpinner.setEnabled(true);
                patchesSpinner.setEnabled(true);
                choosePhotoButton.setEnabled(true);
                shareCollageButton.setEnabled(true);
                saveCollageButton.setEnabled(true);
            }
        });
    }

    private void disable(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                typeSpinner.setEnabled(false);
                patchesSpinner.setEnabled(false);
                choosePhotoButton.setEnabled(false);
                shareCollageButton.setEnabled(false);
                saveCollageButton.setEnabled(false);

            }
        });
    }

    //Collage creator callbacks
    Bitmap bitmap;
    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            collageImageView.setImageBitmap(bitmap);

        }
    };
    @Override
    public void onProgress(final Bitmap bitmap, final float progress) {
        this.bitmap = bitmap;
        this.runOnUiThread(updateRunnable);
        progressBar.setProgress((int) (progress * 100));
    }

    @Override
    public void onFinnished(Bitmap bitmap) {
        if(collage != null){
            collage.recycle();
        }
        collage = bitmap;
        this.runOnUiThread(updateRunnable);
        progressBar.setProgress(0);
        enable();
    }

    @Override
    public void onBackPressed(){
        if(currentFuture != null && currentFuture.cancel(true)){
            Toast.makeText(that, getResources().getString(R.string.on_interrupt), Toast.LENGTH_LONG).show();
        } else {
            super.onBackPressed();
        }
    }

    //Patch loader callbacks
    @Override
    public void onPatchLoaderProgress(Bitmap bitmap, float progress) {
        if(bitmap != null){
            this.bitmap = bitmap;
            this.runOnUiThread(updateRunnable);
        }
        progressBar.setProgress((int) (progress * 100));
    }

    @Override
    public void onPatchLoaderFinnished() {
        this.bitmap = null;
        this.runOnUiThread(updateRunnable);
        progressBar.setProgress(0);
        enable();
    }

    private void askPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant

                return;
            }
        }
    }
}
