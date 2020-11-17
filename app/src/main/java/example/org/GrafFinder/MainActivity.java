package example.org.GrafFinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;

import example.org.GrafFinder.SharedPrefs.PrefUtils;
import example.org.GrafFinder.remoteDatabase.InsertValues;
import example.org.GrafFinder.uploadsManager.MyUploadsActivity;

import static com.google.android.gms.auth.api.Auth.GoogleSignInApi;
import static example.org.GrafFinder.MapsActivity.TABLE_G;
import static example.org.GrafFinder.R.id.sub_menu_logout;
import static example.org.GrafFinder.WelcomeActivity.isGuest;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TabHost tabhost;
    private SearchView searchViewer;
    private ListView listGraffiti;
    private TextView txtName;
    private TextView txtemail;
    HistoryActivity history;
    MapsActivity maps;
    Button synchronize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        history = new HistoryActivity();
        maps = new MapsActivity();
        SharedPreferences sharedPreferences = getSharedPreferences(WelcomeActivity.ACCOUNT_INFO, 0);
        String token = sharedPreferences.getString(WelcomeActivity.USER_TOKEN, "Unknown");

        if (token.equalsIgnoreCase("unknown") && !WelcomeActivity.isGuest)
            goLoginScreen();

        else {
            setContentView(R.layout.activity_main);
            tabhost = (TabHost) findViewById(R.id.tabHost);
            tabhost.setup();

            TabHost.TabSpec specs = tabhost.newTabSpec("recentTag");
            specs.setContent(R.id.relativeLayout1);
            specs.setIndicator("RECENT");
            tabhost.addTab(specs);

            specs = tabhost.newTabSpec("accountTag");
            specs.setContent(R.id.linearLayout2);
            specs.setIndicator("Account");
            tabhost.addTab(specs);

            specs = tabhost.newTabSpec("mapTag");
            specs.setContent(R.id.linearLayout3);
            specs.setIndicator("MAP");
            tabhost.addTab(specs);

            synchronize = (Button) findViewById(R.id.button);
            synchronize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkForInternet()) {
                        Intent intent = new Intent(MainActivity.this, SyncFilters.class);
                        startActivityForResult(intent, 1);
                    }
                    else {
                        Toast toast = Toast.makeText(MainActivity.this, "You are not connected to the internet", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });

            repopulateListView();

            tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                Intent intent = null;

                @Override
                public void onTabChanged(String tabId) {
                    switch (tabId) {
                        case "recentTag":
                            repopulateListView();
                            RECENTS = true;
                            ACCOUNT = false;
                           /* LocalDatabase localDatabase = new LocalDatabase(MainActivity.this, null, null, 1);
                            final ArrayList<String> IDs = localDatabase.getObjectIDSet(MapsActivity.TABLE_G);
                            ArrayList<LatLng> latLngs = localDatabase.getMapPointSet(MapsActivity.TABLE_G);
                            Bitmap[] images = new Bitmap[IDs.size()];
                            String[] locations = new String[IDs.size()];
                            String[] authors = new String[IDs.size()];

                            for (int i = 0; i<IDs.size();i++) {
                                Point_Info point_info = localDatabase.getGraffitiInfo(IDs.get(i));
                               if(point_info == null){
                                    i--;
                                    continue;
                                }
                                images[i] = point_info.getImg();   //get all images from local database
                                locations[i] = point_info.getLocation();    //get all locations (addresses) from localDB
                                authors[i] = point_info.getAuthor(); // get all authors from local database
                            }
                            ListViewActivity adapter = new ListViewActivity(MainActivity.this, new ArrayList<String>(Arrays.asList(locations)),new ArrayList<Bitmap>(Arrays.asList(images)), new ArrayList<String>(Arrays.asList(authors)), latLngs, IDs,ListViewActivity.RECENTS);
                            listGraffiti = (ListView) findViewById(R.id.graffitiList);
                            listGraffiti.setAdapter(adapter);    //set all graffities to RECENT window
                            listGraffiti.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent startItem = new Intent(MainActivity.this, DescriptionActivity.class);
                                    startItem.putExtra("chosenID", IDs.get(position));
                                    startActivity(startItem);
                                }
                            });*/
                            break;
                        case "accountTag":
                            ACCOUNT = true;
                            RECENTS = false;

                            ImageButton addimage = (ImageButton) findViewById(R.id.buttonAdd);
                            addimage.setOnClickListener(MainActivity.this);
                            Button planRoute = (Button) findViewById(R.id.btnMyRoute);
                            planRoute.setOnClickListener(MainActivity.this);

                            txtName = (TextView) findViewById(R.id.fullName);
                            txtemail = (TextView) findViewById(R.id.userEmail);

                            SharedPreferences preferences = getSharedPreferences(WelcomeActivity.ACCOUNT_INFO, 0);
                            // Strings for google shared prefs
                            String name = preferences.getString(WelcomeActivity.USER_NAME, "Guest");
                            String email = preferences.getString(WelcomeActivity.EMAIL, "You are a guest!");

                            txtName.setText(name);
                            txtemail.setText(email);

                            Button startSettings = (Button) findViewById(R.id.settings);
                            Button startUploads = (Button) findViewById(R.id.btnUploads);
                            startSettings.setOnClickListener(MainActivity.this);


                            if(WelcomeActivity.isGuest) {
                                startUploads.setBackgroundColor(Color.rgb(0, 0, 0));
                                startUploads.setTextColor(Color.rgb(255, 255, 255));
                                startUploads.setText("Uploads are unavailable");
                                startSettings.setText("Login");

                                startSettings.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                            else {
                                startUploads.setOnClickListener(MainActivity.this);
                                startSettings.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(MainActivity.this, UserSettings.class);
                                        startActivity(intent);
                                    }
                                });
                            }

//                            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
//                            startActivity(intent);

                            // TODO: Profile image download
                            //new DownloadImageTask((ImageView) findViewById(R.id.profileImage))
                            // .execute("https://graph.facebook.com/" + "100001064294679" + "/picture?type=large");

                            // opens new dialog with 2 options

                            break;
                        case "mapTag":
                            intent = new Intent(MainActivity.this, MapsActivity.class);
                            break;
                    }
                    if (intent != null) {
                        startActivity(intent);
                        intent = null;
                        tabhost.setCurrentTab(0);
                    }
                }
            });
            ((TextView)findViewById(R.id.swipem)).setText("Swipe <-- to account");
            ((TextView)findViewById(R.id.swipem2)).setText("Swipe --> to recents | <-- to map");
        }

    }

    public boolean checkForInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        else
            return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("All data will be deleted! \n This function uses internet connection! \n Proceed?");

            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    syncData(SyncFilters.newestCount, SyncFilters.usertoken, SyncFilters.topNum);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        }
    }

    private void repopulateListView() {
        LocalDatabase localDatabase = new LocalDatabase(MainActivity.this, null, null, 1);
        final ArrayList<String> IDs = localDatabase.getObjectIDSet(TABLE_G);
        ArrayList<LatLng> latLngs = localDatabase.getMapPointSet(TABLE_G);
        Bitmap[] images = new Bitmap[IDs.size()];
        String[] locations = new String[IDs.size()];
        String[] authors = new String[IDs.size()];

        for (int i = 0; i < IDs.size(); i++) {
            Point_Info point_info = localDatabase.getGraffitiInfo(IDs.get(i));
            if (point_info == null) {
                i--;
                continue;
            }
            images[i] = point_info.getImg();   //get all images from local database
            locations[i] = point_info.getLocation();    //get all locations (addresses) from localDB
            authors[i] = point_info.getAuthor(); // get all authors from local database
        }
        ListViewActivity adapter = new ListViewActivity(MainActivity.this, new ArrayList<String>(Arrays.asList(locations)), new ArrayList<Bitmap>(Arrays.asList(images)), new ArrayList<String>(Arrays.asList(authors)), latLngs, IDs, ListViewActivity.RECENTS);
        listGraffiti = (ListView) findViewById(R.id.graffitiList);
       /* listGraffiti.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case (MotionEvent.ACTION_DOWN):
                        x1 = event.getX();
                        y1 = event.getY();
                        return true;
                    case (MotionEvent.ACTION_UP):
                        Display display = getWindowManager().getDefaultDisplay();
                        Point point = new Point();
                        display.getSize(point);
                        int x = point.x;
                        int y = point.y;
                        x2 = event.getX();
                        y2 = event.getY();
                        double angle = Math.toDegrees(Math.atan(Math.abs((y2 - y1) / (x2 - x1))));

                        if(x1==x2 && y1==y2){
                            listGraffiti.callOnClick();
                        }

                        if (x2 > x1 && (angle) <= 10 && Math.abs(x2 - x1) >= x / 2) { //left to right
                            if(ACCOUNT){
                                tabhost.setCurrentTabByTag("recentTag");
                                RECENTS = true;
                                ACCOUNT = false;
                            }
                        } else if (x2 < x1 && (angle) <= 10 && Math.abs(x2 - x1) >= x / 2) { // right to left
                            if (RECENTS) {
                                tabhost.setCurrentTabByTag("accountTag");
                                RECENTS = false;
                                ACCOUNT = true;
                            }
                        }

                        return true;
                }
                return false;

        }});*/
        listGraffiti.setAdapter(adapter);    //set all graffities to RECENT window
        listGraffiti.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (checkForInternet()) {
                    Intent startItem = new Intent(MainActivity.this, DescriptionActivity.class);
                    startItem.putExtra("chosenID", IDs.get(position));
                    startActivity(startItem);
                }
                else{
                    Toast toast = Toast.makeText(MainActivity.this, "You are not connected to the internet", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        //initializing searchView
        MenuItem searchMenuItem = menu.findItem(R.id.search_menu);
        searchViewer = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchViewer.setMaxWidth(Integer.MAX_VALUE);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case sub_menu_logout:
                if (isLoggedInFacebook()) {
                    logout();
                } else if (WelcomeActivity.googleSignedIn) {
                    signOut();
                } else if (isGuest) {
                    Toast.makeText(this, R.string.user_no_login, Toast.LENGTH_LONG).show();
                } else break;

                break;
            case R.id.sub_menu_login:
                Intent Login = new Intent(this, WelcomeActivity.class);
                startActivity(Login);
                MainActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean isLoggedInFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    public void goLoginScreen() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        MainActivity.this.finish();
    }

    public void signOut() {
        WelcomeActivity.getmGoogleApiClient().connect();
        WelcomeActivity.getmGoogleApiClient().registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                GoogleSignInApi.signOut(WelcomeActivity.getmGoogleApiClient()).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()){
                            deleteSharedPrefs();
                            WelcomeActivity.googleSignedIn = false;
                            goLoginScreen();
                        }
                    }
                });
            }

            @Override
            public void onConnectionSuspended(int i) {
            }
    });
    }

    private void deleteSharedPrefs() {
        WelcomeActivity.isGuest = true;
        SharedPreferences preferenences = getSharedPreferences(WelcomeActivity.ACCOUNT_INFO, 0);
        preferenences.edit().clear().apply();
    }

    public void logout() {
        LoginManager.getInstance().logOut();
        PrefUtils.clearCurrentUser(this);
        deleteSharedPrefs();
        goLoginScreen();
    }

    /**
     * Metodas 'swipe motion' apdorojimui
     **/



   /* public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                x1 = event.getX();
                y1 = event.getY();
                return true;
            case (MotionEvent.ACTION_UP):
                Display display = getWindowManager().getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int x = point.x;
                int y = point.y;
                x2 = event.getX();
                y2 = event.getY();
                double angle = Math.toDegrees(Math.atan(Math.abs((y2 - y1) / (x2 - x1))));

                if (x2 > x1 && (angle) <= 10 && Math.abs(x2 - x1) >= x / 2) { //left to right
                    if(ACCOUNT){
                        tabhost.setCurrentTabByTag("recentTag");
                        RECENTS = true;
                        ACCOUNT = false;
                    }
                } else if (x2 < x1 && (angle) <= 10 && Math.abs(x2 - x1) >= x / 2) { // right to left
                    if (RECENTS) {
                        tabhost.setCurrentTabByTag("accountTag");
                        RECENTS = false;
                        ACCOUNT = true;
                    }
                }

                return true;
        }
        return super.onTouchEvent(event);
    }
*/
    /**
     * Synchronize local database data, by adding new graffities.
     */
    private void syncData(int newestQuantity, String token, int topX){
        InsertValues getIds = new InsertValues(this);
        if(getIds.sync(newestQuantity,token,topX).isEmpty()) {
            Toast.makeText(MainActivity.this, "All graffities are up to date", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(MainActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
        }
        repopulateListView();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()){
            case R.id.buttonAdd:
                intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.btnMyRoute:
                intent = new Intent(MainActivity.this, RouteActivity.class);
                startActivity(intent);
                break;
            case R.id.settings:
                intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            break;
            case R.id.btnUploads:
                Intent i = new Intent(MainActivity.this, MyUploadsActivity.class);
                startActivity(i);
            break;

        }
    }

    public static double x1, y1;
    public static double x2, y2;
    public static boolean RECENTS = true;
    public static boolean ACCOUNT = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                x1 = event.getX();
                y1 = event.getY();
                return true;
            case (MotionEvent.ACTION_UP):
                Display display = getWindowManager().getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int x = point.x;
                x2 = event.getX();
                y2 = event.getY();
                double angle = Math.toDegrees(Math.atan(Math.abs((y2 - y1) / (x2 - x1))));

                if (x1 == x2 && y1 == y2) {
                    listGraffiti.callOnClick();
                }

                if (x2 > x1 && (angle) <= 10 && Math.abs(x2 - x1) >= x / 2) { //left to right
                    if (ACCOUNT) {
                        tabhost.setCurrentTabByTag("recentTag");
                        RECENTS = true;
                        ACCOUNT = false;
                    }
                } else if (x2 < x1 && (angle) <= 10 && Math.abs(x2 - x1) >= x / 2) { // right to left
                    if (RECENTS) {
                        tabhost.setCurrentTabByTag("accountTag");
                        RECENTS = false;
                        ACCOUNT = true;
                    }else{
                        RECENTS = true;
                        ACCOUNT = false;
                        tabhost.setCurrentTabByTag("mapTag");
                    }

                }

                return true;
        }
        return false;
    }
}
