package example.org.GrafFinder.uploadsManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

import example.org.GrafFinder.ChooseExistingActivity;
import example.org.GrafFinder.GetCoordinates;
import example.org.GrafFinder.HistoryActivity;
import example.org.GrafFinder.ImageProcessing;
import example.org.GrafFinder.LocalDatabase;
import example.org.GrafFinder.R;
import example.org.GrafFinder.remoteDatabase.InsertValues;

public class DetailedUploadActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_upload);

        final String id = getIntent().getStringExtra("UID");

        final LocalDatabase db = new LocalDatabase(this, null, null, 1);
        final SingleUpload upload = db.getUpload(id);

        ((ImageView) findViewById(R.id.uploadImg)).setImageBitmap(upload.getImg());
        //String tags = (upload.getTags() == "")? "No tags":upload.getTags();
        TextView statusText = (TextView) findViewById(R.id.status);
        TextView tagText = (TextView) findViewById(R.id.tags);
        tagText.setText(upload.getTags());
        if(!upload.isStatus()){
            CharSequence g = "Not uploaded";
            statusText.setText(g);
            //statusText.setTextColor(Color.rgb(213,0,0));
            Log.i("WTF","The part to set text");
            final String[] address = {upload.getAddress()};
                ((Button)findViewById(R.id.uploadbttn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(address[0].equals("Coordinates were saved")){
                        String addr = GetCoordinates.getAddress(DetailedUploadActivity.this, upload.getLng());
                        address[0] = (addr==null)? "Coordinates were saved":addr;
                    }
                    if(address[0].equals("Coordinates were saved")){
                        Toast.makeText(DetailedUploadActivity.this, "Check your internet connection!", Toast.LENGTH_LONG).show();
                    }else {
                        InsertValues uploadNew = new InsertValues(DetailedUploadActivity.this);
                        String[] tags = upload.getTags().split(" "); //tag'us kol kas atskiriam space'u
                        int exist = uploadNew.addGraffiti(upload.getAddress(), String.valueOf(upload.getLng().latitude), String.valueOf(upload.getLng().longitude), upload.getImg(), upload.getDescription(), tags);
                        if (exist == 1) {
                            Intent intent = new Intent(DetailedUploadActivity.this, ChooseExistingActivity.class);
                            intent.putExtra("address", address[0]);
                            intent.putExtra("lat", upload.getLng().latitude);
                            intent.putExtra("lng", upload.getLng().longitude);
                            //    intent.putExtra("location_id", uploadNew.getLocationID(finalRes));
                            Uri imageUri = ImageProcessing.convertToUri(DetailedUploadActivity.this, upload.getImg());
                            intent.putExtra("image", imageUri.toString());
                            intent.putExtra("description", upload.getDescription());
                            intent.putExtra("tags", tags);
                            startActivity(intent);
                        }

                        Toast.makeText(DetailedUploadActivity.this, "Graffiti was uploaded!", Toast.LENGTH_SHORT).show();

                        db.changeUploadStatus(upload.getID());
                        finish();
                    }
                }
            });

        }
        else{
            statusText.setText("Uploaded");
            statusText.setTextColor(Color.rgb(27,94,32));
        }

        ((Button) findViewById(R.id.exit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
}
