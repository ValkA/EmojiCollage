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

import valka.emojicollage.KeyGenerator.YUVMeanGenerator;

public class MainActivity extends AppCompatActivity implements CollageListener{
    static private final int SELECT_PICTURE_RESULT = 1;
    private final MainActivity that = this;
    private final String TAG = "MainActivity";

    ImagesManager imagesManager;
    ImageView collageImageView;
    Button shareCollageButton;
    Button choosePhotoButton;
    Button saveCollageButton;
    Spinner typeSpinner;
    ProgressBar progressBar;

    Bitmap collage = null;
    Bitmap inputBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagesManager = new ImagesManager(this, new YUVMeanGenerator());

        collageImageView = (ImageView)findViewById(R.id.collageImageView);
        shareCollageButton = (Button)findViewById(R.id.shareCollageButton);
        shareCollageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(collage != null) {
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
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.list_item , ImagesManager.CreatorType.values());
        adapter.setDropDownViewResource(R.layout.list_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (inputBitmap != null) {
                    disable();
                    imagesManager.createCollage(inputBitmap, that, 5, 2d, (ImagesManager.CreatorType) typeSpinner.getSelectedItem(), 1920);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        progressBar = (ProgressBar)findViewById(R.id.progressbar);
    }

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
        progressBar.setProgress((int)(progress*100));
    }

    @Override
    public void onFinnished(Bitmap bitmap) {
        if(collage!=null){
            collage.recycle();
        }
        collage = bitmap;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                collageImageView.setImageBitmap(collage);
            }
        });
        progressBar.setProgress(0);
        enable();
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
                    imagesManager.createCollage(inputBitmap, this, 5, 2d, (ImagesManager.CreatorType)typeSpinner.getSelectedItem(), 1920);
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
                typeSpinner.setEnabled(true);
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
                choosePhotoButton.setEnabled(false);
                shareCollageButton.setEnabled(false);
                saveCollageButton.setEnabled(false);

            }
        });
    }
}
