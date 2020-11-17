package example.org.GrafFinder.routePackage;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class ProcessRawXML {
    public static ArrayList<LatLng> coordinates;
    public static String optimizedWaypoints;
    private String xmlData;
    private ArrayList<LatLng> coordinatesToVisit;
    ArrayList<Integer> sequence;

    public ProcessRawXML(LatLng startPos, LatLng endPos, ArrayList allCoordinates) {
        this.coordinatesToVisit = allCoordinates;
        sequence = new ArrayList<>();
        LatLng origin = startPos;
        LatLng destination = endPos;

        String coord = "";
        for (LatLng i : coordinatesToVisit)
            coord += "|" + i.latitude + "," + i.longitude;

        String fullLink = "https://maps.googleapis.com/maps/api/directions/xml?origin=" + origin.latitude + "," +
                origin.longitude + "&destination=" + destination.latitude + "," + destination.longitude +
                "&waypoints=optimize:true" + coord + "&mode=walking&key=AIzaSyANasgQ8-ZV7LTADT3H4W4wGosMRj205aU";
        Log.d("INFORMACIJA","fulllink: "+fullLink);

        DownloadXML downloadXML = new DownloadXML();
        try {
            downloadXML.execute(fullLink).get();
            this.xmlData = DownloadXML.storedData;
            getInfo();
            optimizedWaypoints ="";
            for(int i=0;i<sequence.size();i++){
                LatLng latlng = coordinatesToVisit.get(sequence.get(i));
                String strLatLng = latlng.latitude+","+latlng.longitude;
                if(i!=0)
                strLatLng="'"+strLatLng+"'/";
                else
                strLatLng+="/";
                optimizedWaypoints+= strLatLng;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void getInfo() {
        boolean inStep = false;
        String value = "";
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); //extract data from raw XML file
            factory.setNamespaceAware(true);
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(this.xmlData));
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xmlPullParser.getName(); //getting current tag name
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equalsIgnoreCase("step"))
                            inStep = true;
                        break;
                    case XmlPullParser.TEXT:
                        value = xmlPullParser.getText(); // current value
                        break;
                    case XmlPullParser.END_TAG:
                        if (!inStep) {
                            if(tagName.equalsIgnoreCase("waypoint_index"))
                                sequence.add(Integer.parseInt(value));
                        } else {
                            if (tagName.equalsIgnoreCase("step"))
                                inStep = false;
                             else if (tagName.equalsIgnoreCase("points"))
                                decodePolyline(value);
                        }
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    private void decodePolyline(String encoded) {
        int shift = 0, res = 0;
        LinkedList<Double> lats = null;
        LinkedList<Double> longs = null;
        boolean isLat = true;
        double result;
        for (int i = 0; i < encoded.length(); i++) {
            int dec_63 = (int) encoded.charAt(i) - 63;
            int xor = dec_63;
            if (dec_63 >= 0x20) {
                xor = (dec_63 ^ 0x20) << shift;
                res = res | xor;
                shift += 5;
            } else {
                res = res | (xor << shift);
                if ((res & 1) == 0) {
                    res = res >> 1;
                    result = res / 1E5;
                } else {
                    res = ~res;
                    res = res >> 1;
                    res -= 1;
                    res = ~res;
                    result = (res / 1E5) * (-1);
                }
                res = 0;
                shift = 0;
                if (isLat) {
                    if (lats != null)
                        lats.add(lats.getLast() + result);
                    else {
                        lats = new LinkedList<>();
                        lats.add(result);
                    }
                    isLat = false;
                } else {
                    if (longs != null) {
                        longs.add(longs.getLast() + result);
                    } else {
                        longs = new LinkedList<>();
                        longs.add(result);
                    }
                    isLat = true;
                }
            }
        }
        if (longs != null && lats != null) {
            for (int i = 0; i < lats.size(); i++) {
                if (coordinates == null)
                    coordinates = new ArrayList<>();
                coordinates.add(new LatLng(lats.get(i), longs.get(i)));
            }
        }
    }


}
