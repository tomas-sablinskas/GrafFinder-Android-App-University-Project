package example.org.GrafFinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import example.org.GrafFinder.remoteDatabase.InsertValues;

public class ChooseExistingActivity extends AppCompatActivity {

    Button addGraffiti;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.existing_layout);

        ListView existing = (ListView)findViewById(R.id.list);
        addGraffiti = (Button)findViewById(R.id.btnAdd);

        final Intent intent = getIntent();
        final String address = intent.getStringExtra("address");
        final InsertValues getGraffities = new InsertValues(this);
        getGraffities.idsAndIconsAtLocation(address);

        ArrayList<String> allIDs = getGraffities.allIDsAtLocation; //get all ids at location
        ArrayList<Bitmap> allIcons = getGraffities.allIconsAtLocation;  //get all icons at location

        //show all icons in a listview
        final ListViewActivity adapter = new ListViewActivity(ChooseExistingActivity.this, allIDs, allIcons);
        existing.setAdapter(adapter);

        addGraffiti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChooseExistingActivity.this, "chosen: "+adapter.selected, Toast.LENGTH_SHORT).show();
                try {
                    Uri uri = Uri.parse(intent.getStringExtra("image"));
                    Bitmap image = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), uri),1000,750,true);
                    String description = intent.getStringExtra("description");
                    String[] tags = intent.getStringArrayExtra("tags");
                    InsertValues insertValues = new InsertValues(ChooseExistingActivity.this);

                    int location = getGraffities.getLocationIDs(address,intent.getDoubleExtra("lat",0),intent.getDoubleExtra("lng",0));
                    if(location==-1)    //if no location was found based on address, lat and lng
                        location = getGraffities.insertLocation(address,String.valueOf(intent.getDoubleExtra("lat",0)),String.valueOf(intent.getDoubleExtra("lng",0))); //insert new location returning location_id

                    if(adapter.selected.equalsIgnoreCase("")){
                        insertValues.addNewGraffiti(location, ImageProcessing.convertToByteArray(image), description,tags );
                    }
                    else{
                        insertValues.addModified(location, adapter.selected, ImageProcessing.convertToByteArray(image), description,tags);
                    }
                    Toast.makeText(ChooseExistingActivity.this, "Graffiti was uploaded!", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
