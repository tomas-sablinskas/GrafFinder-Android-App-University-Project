package example.org.GrafFinder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
public class HistoryActivity extends MainActivity{
    public static final int CAMERA_IMAGE_REQUEST = 1;
    public static final int GALLERY_IMAGE_REQUEST = 2;
    public static final String FILE_NAME = "temp.jpeg";
    private Button chooseCamera;
    private Button chooseGallery;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);
        chooseCamera= (Button)findViewById(R.id.btnCamera);
        chooseGallery=(Button)findViewById(R.id.btnGallery);
        chooseGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGalleryChooser();
            }
        });
        chooseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera();
            }
        });
    }
    /**
     * Starts gallery
     */
    public void startGalleryChooser() {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        Log.i("MSS",i.toString());
        startActivityForResult(i, GALLERY_IMAGE_REQUEST);
    }
    /**
     * Starts camera
     */
    public void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCameraFile()));    // save file to specified directory
        startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
    }
    public File getCameraFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }
    /**
     * Handles received activity result - depending on received request code, invokes method uploadImage
     *
     * @param requestCode Camera or Gallery request
     * @param resultCode
     * @param data        received data as Intent(if request is Gallery), otherwise - null
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null)
            uploadImage(data.getData());
        else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK)
            uploadImage(Uri.fromFile(getCameraFile()));
    }
    public void uploadImage(final Uri uri) {
        if (uri != null) {
            Intent i = new Intent(HistoryActivity.this, UploadActivity.class);
            i.putExtra("URI",uri.toString());
            startActivity(i);
            finish();
        }
    }
}