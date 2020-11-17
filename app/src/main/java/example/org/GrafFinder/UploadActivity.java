package example.org.GrafFinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;

import example.org.GrafFinder.remoteDatabase.InsertValues;
import example.org.GrafFinder.uploadsManager.SingleUpload;

public class UploadActivity extends AppCompatActivity {
    float currentAngle;
    private String description;
    private Button btnDescription;
    private ImageView imageView;
    private Button btnSaveLocally;
    private TextView coordinates;
    private Button sendToRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_picture);
        //inicializing buttons
        Uri uri = Uri.parse(getIntent().getStringExtra("URI"));
        description = "";
        imageView = (ImageView) findViewById(R.id.imageView3);
        btnSaveLocally = (Button) findViewById(R.id.btnLocally);
        coordinates = (TextView) findViewById(R.id.textLocation);
        sendToRemote = (Button) findViewById(R.id.btnToRemote);
        btnDescription = (Button) findViewById(R.id.add_description);

        btnDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
                final EditText text = new EditText(UploadActivity.this);
                builder.setView(text);
                text.setText(description);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        description = text.getText().toString();
                    }
                });
                builder.show();
            }
        });

        final ImageButton rotate = (ImageButton) findViewById(R.id.btnRotate);
        try {
            final Bitmap bitmap = Bitmap.createScaledBitmap(MediaStore.Images.
                    Media.getBitmap(getContentResolver(), uri), 500, 500, true); //creates scaled bitmap(to fit to the screen)
            imageView.setImageBitmap(bitmap);
            final Bitmap[] rotated = new Bitmap[1];
            currentAngle=0;

            rotate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(currentAngle+=90);
                    rotated[0] = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                    imageView.setImageBitmap(rotated[0]);
                }
            });
            File file = new File(uri.getPath());
            final GetCoordinates getCoordinates;
            if (file.isFile()) {
                getCoordinates = new GetCoordinates(uri.getPath());  //if file already exists (uploaded from camera)
            } else {
                getCoordinates = new GetCoordinates(getRealPathFromURI(UploadActivity.this, uri));  //if file was uploaded from gallery
            }
            final LatLng latLng = getCoordinates.getLatlng();
            String res = "Unable to get coordinates from the image";
            if (latLng != null)
                res = "Coordinates were saved";
            try {
                String address = getCoordinates.updateAddress(UploadActivity.this, latLng);
                if (address != null) {    // if coordinates were saved AND converted to read address it is added to local database
                    res = address;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                coordinates.setText(res);
            }
            final String[] finalRes = {res};

            final EditText tagInput = (EditText) findViewById(R.id.inputTags) ;

            tagInput.setFocusable(true);
            tagInput.setFocusable(false);
            tagInput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(UploadActivity.this, TagChoosingActivity.class);
                    String selected = tagInput.getText().toString();
                    i.putExtra("TAGS", selected);
                    startActivityForResult(i, 12);
                }
            });
            btnSaveLocally.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocalDatabase db = new LocalDatabase(UploadActivity.this, null, null, 1);
                    String author = getSharedPreferences(WelcomeActivity.ACCOUNT_INFO, 0).getString(WelcomeActivity.USER_NAME, "Unknown");
                    Point_Info info;
                    if(rotated[0] != null)
                        info = new Point_Info(rotated[0],author, finalRes[0],null);
                    else
                        info = new Point_Info(bitmap, author, finalRes[0], null);
                    String tags = ((EditText) findViewById(R.id.inputTags)).getText().toString();
                    SingleUpload upload = new SingleUpload(null, info.getImg(), description, false, tags, finalRes[0],new LatLng(latLng.latitude, latLng.longitude));
                    db.addToMyUploads(upload, latLng.latitude, latLng.longitude, finalRes[0]);
                    Toast.makeText(UploadActivity.this, "Graffiti was uploaded!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            sendToRemote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(finalRes[0].equals("Coordinates were saved")){
                        final View view = v;
                        Toast.makeText(UploadActivity.this, "Save locally or check your internet connection",Toast.LENGTH_LONG);
                        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
                        builder.setTitle("Check your internet connection!");
                        if(!WelcomeActivity.isGuest){
                        builder.setPositiveButton("Save locally", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LocalDatabase db = new LocalDatabase(UploadActivity.this, null, null,1);
                                String utags = ((EditText) findViewById(R.id.inputTags)).getText().toString();
                                String author = getSharedPreferences(WelcomeActivity.ACCOUNT_INFO, 0).getString(WelcomeActivity.USER_NAME, "Unknown");
                                Point_Info info;
                                if(rotated[0] != null)
                                    info = new Point_Info(rotated[0],author, finalRes[0],null);
                                else
                                    info = new Point_Info(bitmap, author, finalRes[0], null);
                                SingleUpload upload = new SingleUpload(null, info.getImg(), description, false, utags, finalRes[0], new LatLng(latLng.latitude, latLng.longitude));
                                db.addToMyUploads(upload, latLng.latitude, latLng.longitude, finalRes[0]);
                                finish();
                            }
                        });}

                        builder.setNegativeButton("Try again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String addr = getCoordinates.updateAddress(UploadActivity.this, latLng);
                                    finalRes[0] = (addr==null)? "Coordinates were saved":addr;
                                    sendToRemote.callOnClick();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        builder.show();
                    }else{
                        InsertValues uploadNew = new InsertValues(UploadActivity.this);
                        String[] tags = ((EditText) findViewById(R.id.inputTags)).getText().toString().split(" "); //tag'us kol kas atskiriam space'u
                        int exist = uploadNew.addGraffiti(finalRes[0], String.valueOf(latLng.latitude), String.valueOf(latLng.longitude), bitmap, description, tags);
                        if (exist==1) {
                            Intent intent = new Intent(UploadActivity.this, ChooseExistingActivity.class);
                            intent.putExtra("address", finalRes[0]);
                            intent.putExtra("lat",latLng.latitude);
                            intent.putExtra("lng",latLng.longitude);
                            //    intent.putExtra("location_id", uploadNew.getLocationID(finalRes));
                            Uri imageUri;
                            if(rotated[0] != null)
                                imageUri=ImageProcessing.convertToUri(UploadActivity.this,rotated[0]);
                            else
                                imageUri=ImageProcessing.convertToUri(UploadActivity.this,bitmap);
                            intent.putExtra("image", imageUri.toString());
                            intent.putExtra("description", description);
                            intent.putExtra("tags", tags);
                            startActivity(intent);
                        }
                        if(!WelcomeActivity.isGuest){
                        LocalDatabase db = new LocalDatabase(UploadActivity.this, null, null, 1);
                        String author = getSharedPreferences(WelcomeActivity.ACCOUNT_INFO, 0).getString(WelcomeActivity.USER_NAME, "Unknown");
                        Point_Info info;
                        if(rotated[0] != null)
                            info = new Point_Info(rotated[0],author, finalRes[0],null);
                        else
                            info = new Point_Info(bitmap, author, finalRes[0], null);
                        String utags = ((EditText) findViewById(R.id.inputTags)).getText().toString();
                        SingleUpload upload = new SingleUpload(null, info.getImg(), description, true, utags, finalRes[0], new LatLng(latLng.latitude, latLng.longitude));
                        db.addToMyUploads(upload, latLng.latitude, latLng.longitude, finalRes[0]);
                        }
                        Toast.makeText(UploadActivity.this, "Graffiti was uploaded!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 12) {
            if (resultCode == RESULT_OK) {
                String selected = "";
                for (String tag : TagChoosingActivity.allTags) {
                    selected += tag + " ";
                }
                EditText text = (EditText) findViewById(R.id.inputTags);
                text.setText(selected);
                if(text.hasFocus()==true) text.setFocusable(false);
            }
        }
    }

    /**
     * Converts URI to real path (shoud be invoked if image was uploaded from gallery)
     *
     * @param context    context
     * @param contentUri URI to convert
     * @return the real path as String from URI
     */
    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}



