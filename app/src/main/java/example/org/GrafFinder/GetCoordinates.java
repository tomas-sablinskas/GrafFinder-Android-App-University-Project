package example.org.GrafFinder;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GetCoordinates {

    private LatLng latlng;

    /**
     * Constructor
     * @param path path to the file name from which coordinates should be read
     */
    public GetCoordinates(String path) {
        getExif(path);
    }

    /**
     * Takes the attributes (latitude and longitude) from image if they are known, and stores them in class variable.
     * @param filePath path to the file name from which coordinates should be read
     */
    private void getExif(String filePath) {
        double lat = 0, lon = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);

            String dms = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE); //latitude in "DMS" format
            if (dms != null) {
                String[] degArr = dms.split("/");
                lat = getDegrees(degArr[0], degArr[1], degArr[2], degArr[3]);   //DMS converted to Decimal

                dms = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                degArr = dms.split("/");
                lon = getDegrees(degArr[0], degArr[1], degArr[2], degArr[3]);

                setLatlng(new LatLng(lat, lon));    // stores coordinates in class variable
            } else {
                Log.d("GetCoordinates", "Unable to read coordinates from image");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts seconds to degrees
     * @param seconds seconds to convert
     * @param divisor divisor
     * @return converted seconds
     */
    private double getSeconds(String seconds, String divisor) {
        return (Integer.parseInt(seconds.substring(2)) / Double.parseDouble(divisor)) / 3600;
    }

    /**
     * Converts minutes to degrees
     * @param minutes minutes to convert
     * @return converted minutes
     */
    private double getMinutes(String minutes) {
        return Integer.parseInt(minutes.substring(2)) / 60.0;
    }

    /**
     * Converts DMS format coordinates to Decimal format coordinates
     * @param degress degrees
     * @param minutes minutes to convert
     * @param seconds seconds to convert
     * @param divisor divisor
     * @return coodrinates in Decimal format
     */
    private double getDegrees(String degress, String minutes, String seconds, String divisor) {
        return getSeconds(seconds, divisor) + getMinutes(minutes) + Double.parseDouble(degress);
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    /**
     * Method takes lat and lng and transforms them to real address (Street,Unit number, City and Country)
     * @param context context
     * @param latlng coordinates to get address from
     * @return real address from coordinates
     */
    public static String getAddress(Context context, LatLng latlng) {
        String res = null;

        double longitude = latlng.longitude;
        double latitude = latlng.latitude;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (null != listAddresses && listAddresses.size() > 0) {
                res = listAddresses.get(0).getAddressLine(0)+", "+listAddresses.get(0).getLocality()+
                        ", "+listAddresses.get(0).getCountryName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public String updateAddress(Context c, LatLng latLng) throws IOException {
        return getAddress(c, latLng);
    }
}
