package example.org.GrafFinder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import java.util.ArrayList;
import example.org.GrafFinder.markerclusterer.Marker;
import example.org.GrafFinder.routePackage.ProcessRawXML;
import static example.org.GrafFinder.WelcomeActivity.mGoogleApiClient;
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<Marker>, ClusterManager.OnClusterInfoWindowClickListener<Marker>,
        ClusterManager.OnClusterItemClickListener<Marker>, ClusterManager.OnClusterItemInfoWindowClickListener<Marker>,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    private ClusterManager<Marker> mClusterManager;
    LocationRequest mLocationRequest;
    private Marker clickedClusterItem;
    public GoogleMap mMap;
    private Context context;
    private Button openNavigation;
    Location mLastLocation;
    private ImageView imageView;
    com.google.android.gms.maps.model.Marker mCurrLocationMarker;
    public static LatLng currentlatLng;
    public static final int TABLE_G =0;
    public static final int TABLE_E = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
    private class GraffitiRenderer extends DefaultClusterRenderer<Marker> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;
        public GraffitiRenderer(MapsActivity mapsActivity, GoogleMap mMap, ClusterManager<Marker> mClusterManager) {
            super(getApplicationContext(), mMap, mClusterManager);
            View multiProfile = getLayoutInflater().inflate(R.layout.amu_info_window, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);
            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.cardview_default_radius);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.cardview_compat_inset_shadow);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }
        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 2;
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        context = this;
        Button filter = (Button)findViewById(R.id.btnFilter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, FilteringActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                mMap.clear();
                mClusterManager.clearItems();
                addFilteredItems();
                mClusterManager.cluster();
            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        LatLng lietuva = new LatLng(55.1735998, 23.8948016);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lietuva));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(5));
        mClusterManager = new ClusterManager<>(this, mMap);
        mClusterManager.setRenderer(new GraffitiRenderer(this, mMap, mClusterManager));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
        if(ProcessRawXML.coordinates !=null){
            addRouteItems();
            addPolyline();
            ProcessRawXML.coordinates = null;
            openNavigation = (Button)findViewById(R.id.btnOpenNavigation);
            openNavigation.setVisibility(View.VISIBLE);
            openNavigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("https://www.google.com/maps/dir/"+ProcessRawXML.optimizedWaypoints);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                }
            });
        }
        else addItems();
        mClusterManager
                .setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Marker>() {
                    @Override
                    public boolean onClusterItemClick(Marker item) {
                        clickedClusterItem = item;
                        return false;
                    }
                });
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(
                new CustomAdapterForItems());
        mClusterManager.cluster();
        //registerForContextMenu(findViewById(R.id.fab));
    }
    public class CustomAdapterForItems implements GoogleMap.InfoWindowAdapter {
        private final View myContentsView;
        CustomAdapterForItems() {
            myContentsView = getLayoutInflater().inflate(
                    R.layout.info_window, null);
        }
        @Override
        public View getInfoWindow(com.google.android.gms.maps.model.Marker marker) {
            TextView tvTitle = ((TextView) myContentsView
                    .findViewById(R.id.txtTitle));
            TextView tvSnippet = ((TextView) myContentsView
                    .findViewById(R.id.txtSnippet));
            tvTitle.setText(clickedClusterItem.getTitle());
            tvSnippet.setText(clickedClusterItem.getSnippet());
            LocalDatabase database = new LocalDatabase(MapsActivity.this, null, null, 1);
            Bitmap bitmap;
            bitmap = database.getGraffitiInfo(clickedClusterItem.getId()).getImg();
            ImageView ivIcon = ((ImageView)myContentsView.findViewById(R.id.object_image));
            ivIcon.setImageBitmap(bitmap);
            return myContentsView;
        }
        @Override
        public View getInfoContents(com.google.android.gms.maps.model.Marker marker) {
            return null;
        }
    }
    private void addFilteredItems(){
        LocalDatabase showFiltered = new LocalDatabase(this,null,null,1);
        for(String i: FilteringActivity.filteredIDs){
            LatLng filtered = showFiltered.getLocationByID(i);
            Point_Info info = showFiltered.getGraffitiInfo(i);
            Marker filteredItem = new Marker(filtered.latitude,filtered.longitude,info.getAuthor(), info.getLocation(), info.getID());
            mClusterManager.addItem(filteredItem);
        }
    }
    @Override
    public boolean onClusterClick(Cluster<Marker> cluster) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();
        // Animate camera to the bounds
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    @Override
    public void onClusterInfoWindowClick(Cluster<Marker> cluster) {
        // Does nothing, but you could go to a list of the users.
    }
    @Override
    public boolean onClusterItemClick(Marker item) {
        // on marker click
        return false;
    }
    @Override
    public void onClusterItemInfoWindowClick(Marker marker) {
        //Cluster item InfoWindow clicked, set title as action
        if(checkForInternet()) {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("chosenID", marker.getId());
            // i.setAction(marker.getTitle());
            startActivity(i);
        }
        else{
            Toast toast = Toast.makeText(MapsActivity.this, "You are not connected to the internet", Toast.LENGTH_SHORT);
            toast.show();
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.photo:
                Toast.makeText(this, "Camera pop up or something", Toast.LENGTH_LONG).show();
                return true;
            case R.id.nearby:
                Toast.makeText(this, "Nearby graffitis show",Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    private void addItems() {
        addGraffiti();
    }
    private void addRouteItems(){
        LocalDatabase localDatabase = new LocalDatabase(this,null,null,1);
        ArrayList IDs = localDatabase.getAllIDsFromRoute();
        for(Object i: IDs){
            Point_Info info = localDatabase.getRouteItemInfo(i.toString());
            Marker routeItem = new Marker(info.getLatLng().latitude,info.getLatLng().longitude, info.getAuthor(), info.getLocation(), info.getID());
            mClusterManager.addItem(routeItem);
        }
    }
    private void addGraffiti(){
        ArrayList<String> points = (new LocalDatabase(this, null, null, 1)).getObjectIDSet(1);
        for (String point : points){
            Point_Info info = new LocalDatabase(this,null,null,1).getGraffitiInfo(point);
            LatLng location = new LocalDatabase(this,null,null,1).getLocationByID(point);
            Marker graffiti = new Marker(location.latitude, location.longitude, info.getAuthor(), info.getLocation(), info.getID());
            mClusterManager.addItem(graffiti);
        }
    }
    private void addPolyline(){
        PolylineOptions polylineOptions = new PolylineOptions();
        for(LatLng i: ProcessRawXML.coordinates){
            polylineOptions.add(i);
        }
        Polyline polyline = mMap.addPolyline(polylineOptions);
    }
    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        currentlatLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
}