package example.org.GrafFinder.uploadsManager;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Tomas on 2016-12-02.
 */

public class SingleUpload {

    private String ID;
    private Bitmap img;
    private String description;
    private boolean status;
    private String tags;
    private String address;
    private LatLng lng;

    public SingleUpload(String ID, Bitmap img, String description, boolean status, String tags, String address, LatLng lng){
        this.ID = ID;
        this.img = img;
        this.description = description;
        this.status = status;
        this.tags = tags;
        this.address =address;
        this.lng = lng;
    }

    public String getDescription() {
        return description;
    }

    public String getID() {
        return ID;
    }

    public Bitmap getImg() {
        return img;
    }

    public boolean isStatus() {
        return status;
    }

    public String getTags() {
        return tags;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getLng() {
        return lng;
    }
}
