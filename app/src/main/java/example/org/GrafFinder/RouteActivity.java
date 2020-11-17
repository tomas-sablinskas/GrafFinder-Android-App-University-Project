package example.org.GrafFinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import example.org.GrafFinder.routePackage.ProcessRawXML;

public class RouteActivity extends HistoryActivity {

    private ListView route;
    private TextView isEmpty;
    private Button btnDirections;
    private boolean inStart;
  ArrayList<LatLng> coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        route = (ListView)findViewById(R.id.routeList);
        isEmpty=(TextView)findViewById(R.id.message);
        btnDirections = (Button)findViewById(R.id.showDirections);

        final LocalDatabase localDatabase = new LocalDatabase(RouteActivity.this,null,null,1);
        ArrayList<String> IDs = localDatabase.getAllIDsFromRoute(); //get all elements that were added to the route
        if(IDs.size() == 0){
            btnDirections.setVisibility(View.INVISIBLE);
            isEmpty.setText("Your list is empty!\nAdd graffities to the route by clicking ADD TO THE ROUTE button near graffiti");
            isEmpty.setTextSize(20);
        }
        else if(ListViewActivity.startPos != -1 && ListViewActivity.endPos == -1){
            inStart = false;
            isEmpty.setText("Choose end position and click SHOW DIRECTIONS");
            btnDirections.setVisibility(View.VISIBLE);
            btnDirections.setText("SHOW DIRECTIONS");
        }
        else {
            inStart = true;
        isEmpty.setText("Choose start position");
        btnDirections.setVisibility(View.VISIBLE);
        btnDirections.setText("NEXT");}

        ArrayList<Bitmap> images = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<String> locations = new ArrayList<>();
        coordinates = new ArrayList<>();

        //retrieving each element's information that is in allCordinates arraylist (all elements that were added to the route)
        for(int i=0; i<IDs.size();i++){
            Point_Info point_info = localDatabase.getRouteItemInfo(IDs.get(i));
            images.add(point_info.getImg());
            authors.add(point_info.getAuthor());
            locations.add(point_info.getLocation());
            coordinates.add(point_info.getLatLng());
        }
        //adding elements to listview
        final ListViewActivity adapter = new ListViewActivity(RouteActivity.this,locations,images,authors,coordinates, IDs,ListViewActivity.ROUTE, inStart);
        route.setAdapter(adapter);
        btnDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ListViewActivity.startPos == -1 && ListViewActivity.endPos == -1)
                    Toast.makeText(RouteActivity.this, "You must choose start position first!", Toast.LENGTH_SHORT).show();
               else if(ListViewActivity.startPos != -1 && ListViewActivity.endPos == -1){    // if start position is chosen, then open again this window to choose end postion
                    Intent intent = new Intent(RouteActivity.this,RouteActivity.class);
                    startActivity(intent);}
                else{
                    coordinates = adapter.latsLngs;
                   ProcessRawXML planRoute = new ProcessRawXML(new LatLng(coordinates.get(ListViewActivity.startPos).latitude, coordinates.get(ListViewActivity.startPos).longitude),
                           new LatLng(coordinates.get(ListViewActivity.endPos).latitude,coordinates.get(ListViewActivity.endPos).longitude),coordinates);
                   Intent intent = new Intent(RouteActivity.this,MapsActivity.class);
                   startActivity(intent);
               }}
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}