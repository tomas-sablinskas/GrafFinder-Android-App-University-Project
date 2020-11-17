package example.org.GrafFinder.markerclusterer;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Arnis on 2016-11-08.
 */

public class Marker implements ClusterItem {
    private final LatLng mPosition;
    private MarkerOptions marker;
    private final String mTitle;
    private final String mSnippet;
    private final String mId;

    public Marker(double lat, double lng, String t, String s, String id) {
        mPosition = new LatLng(lat, lng);
        mTitle = t;
        mSnippet = s;
        mId = id;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle(){
        return mTitle;
    }

    public String getSnippet(){
        return mSnippet;
    }


    public MarkerOptions getMarker() {
        return marker;
    }

    public void setMarker(MarkerOptions marker) {
        this.marker = marker;
    }

    public String getId() {
        return mId;
    }
}
