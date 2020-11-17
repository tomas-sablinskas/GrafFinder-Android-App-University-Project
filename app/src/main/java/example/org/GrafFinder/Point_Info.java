package example.org.GrafFinder;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Tomas on 2016-11-06.
 */

public class Point_Info {
    private Bitmap img;
    private String author;
    private String location;
    private String ID;
    private LatLng latLng;


    public Point_Info(Bitmap img, String author, String location, String ID){
        this.img = img;
        this.author = author;
        this.location = location;
        this.ID = ID;
    }

    public Point_Info(String author, String ID, Bitmap img, LatLng latLng, String location) {
        this.author = author;
        this.ID = ID;
        this.img = img;
        this.latLng = latLng;
        this.location = location;
    }

    public String getAuthor() {
        return author;
    }

    public Bitmap getImg() {
        return img;
    }

    public String getLocation() {
        return location;
    }

    public String getID(){
        return ID;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
