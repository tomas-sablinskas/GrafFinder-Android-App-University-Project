package example.org.GrafFinder.uploadsManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import example.org.GrafFinder.LocalDatabase;
import example.org.GrafFinder.MainActivity;
import example.org.GrafFinder.R;
import example.org.GrafFinder.WelcomeActivity;
import example.org.GrafFinder.remoteDatabase.InsertValues;

public class MyUploadsActivity extends MainActivity {
    public static SingleUpload selectedUpload;
    private MyUploadsAdapter adapter;
    private ArrayList<SingleUpload> Uploads;
    private LocalDatabase db;
    GridView myUploads;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_uploads);
        myUploads = (GridView) findViewById(R.id.my_uploads);

        db = new LocalDatabase(this, null, null, 1);

        adapter = new MyUploadsAdapter(this, R.layout.uploads_item);


        repopulateGridView();

        myUploads.setAdapter(adapter);

        myUploads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUpload = adapter.getItem(position);

                Intent i = new Intent(MyUploadsActivity.this, DetailedUploadActivity.class);
                i.putExtra("UID", selectedUpload.getID());
                startActivity(i);

                repopulateGridView();
            }
        });

        myUploads.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final String uid = adapter.getItem(position).getID();
                AlertDialog.Builder delete = new AlertDialog.Builder(MyUploadsActivity.this);

                delete.setTitle("Delete image?");

                delete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalDatabase db = new LocalDatabase(MyUploadsActivity.this, null, null, 1);
                        db.removeUpload(uid);
                        Toast.makeText(MyUploadsActivity.this, "Deleted!",Toast.LENGTH_SHORT).show();
                        repopulateGridView();
                    }
                });

                delete.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MyUploadsActivity.this, "Canceled!",Toast.LENGTH_SHORT).show();
                    }
                });

                delete.show();

                return true;
            }
        });



        Button getUploads = (Button) findViewById(R.id.getUploads);

        getUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkForInternet()) {
                    db.deleteUploads();

                    InsertValues values = new InsertValues(MyUploadsActivity.this);

                    ArrayList<SingleUpload> uploads = values.getUserUploads(getSharedPreferences(WelcomeActivity.ACCOUNT_INFO, 0).getString(WelcomeActivity.USER_TOKEN, "Unknown"));

                    db = new LocalDatabase(MyUploadsActivity.this, null, null, 1);
                    for (SingleUpload upload : uploads) {
                        db.addToMyUploads(upload, upload.getLng().latitude, upload.getLng().longitude, upload.getAddress());
                    }
                    repopulateGridView();
                }
                else {
                    Toast toast = Toast.makeText(MyUploadsActivity.this, "You are not connected to the internet", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    public boolean checkForInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        //we are connected to a network
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;
        else
            return false;
    }

    private void repopulateGridView(){
        Uploads = db.getUploadsSet();
        adapter.clear();
        adapter.addAll(Uploads);
    }
}
