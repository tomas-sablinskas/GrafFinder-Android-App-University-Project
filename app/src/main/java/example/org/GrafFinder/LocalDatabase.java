package example.org.GrafFinder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;

import example.org.GrafFinder.uploadsManager.SingleUpload;


/**
 * Class for localDB
 */

public class
LocalDatabase extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "localDB";

    public static final String TABLE_G_POINTS = "table_graffiti";
    public static final String COLUMN_ICON = "thumbnail";
    public static final String COLUMN_GID = "GID";
    public static final String COLUMN_LID = "LID";
    public static final String COLUMN_UPLOADER = "uploader";
    public static final String COLUMN_LASTMOD = "last_modified";

    public static final String TABLE_TAGS = "table_tags";
    public static final String COLUMN_TAG = "tag";

    public static final String TABLE_GTAGS = "table_g_tags";

    public static final String TABLE_LOCATIONS = "table_locations";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_MARKED = "marked";

    public static final String TABLE_MY_UPLOADS = "my_uploads";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_ABOUT = "description";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_TIME = "time";

    public static final String TABLE_ROUTE="route_item";
    //latitude, longitude and location text format is taken out of locations table.

    private Context context;



    public LocalDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        this.context = context;
        getWritableDatabase();
    }

    /**
     * Creates all the tables in the local database
     * @param db the database
     * Is invoked automatically when the database is created
     */

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_G_POINTS + "("
        +COLUMN_GID + " TEXT PRIMARY KEY,"
        +COLUMN_ICON + " BLOB,"
        +COLUMN_LID + " INTEGER REFERENCES "+TABLE_LOCATIONS+"("+COLUMN_LID+"),"//TODO: uzdeti UNIQUE constraint (nuimtas testavimo tikslais)
        +COLUMN_UPLOADER + " TEXT,"
        +COLUMN_LASTMOD + " TEXT,"
        +COLUMN_MARKED + " INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_LOCATIONS + "("
        +COLUMN_LID + " INTEGER PRIMARY KEY,"
        +COLUMN_LATITUDE + " TEXT,"
        +COLUMN_LONGITUDE + " TEXT,"
        +COLUMN_LOCATION + " TEXT,"
        +"UNIQUE ("+COLUMN_LATITUDE+","+COLUMN_LONGITUDE+","+COLUMN_LOCATION+"))");
        db.execSQL("CREATE TABLE "+TABLE_TAGS+"("
        +COLUMN_ID+" INTEGER PRIMARY KEY,"
        +COLUMN_TAG+" TEXT UNIQUE)");
        db.execSQL("CREATE TABLE "+TABLE_GTAGS+"("
        +COLUMN_GID+" TEXT REFERENCES "+ TABLE_G_POINTS + "("+COLUMN_GID+"),"
        +COLUMN_ID +" INTEGER REFERENCES "+TABLE_TAGS + "("+COLUMN_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_ROUTE + "("
        +COLUMN_GID + " TEXT PRIMARY KEY)");
        db.execSQL("CREATE TABLE " + TABLE_MY_UPLOADS + "("
        +COLUMN_ID + " TEXT,"
        +COLUMN_LID + " INTEGER,"
        +COLUMN_IMAGE + " BLOB UNIQUE,"
        +COLUMN_ABOUT + " TEXT,"
        +COLUMN_STATUS + " INTEGER,"
        +COLUMN_TAG + " TEXT)"
        );
        String[] tags = {"art","human","massive","writing"};
        addTags(tags, db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    /**
     * Creates a new graffiti point.
     * @param info all general info about graffiti.
     * @param tags an array of tags that is associated with that graffiti.
     */

    public String addNewGraffitiPoint(Point_Info info, String[] tags, String time) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();


        String date = time;

        int LID = addNewLocation(info.getLatLng().latitude, info.getLatLng().longitude, info.getLocation());

        values.put(COLUMN_ICON, ImageProcessing.convertToByteArray(info.getImg()));
        values.put(COLUMN_GID, info.getID());
        values.put(COLUMN_LID, LID);
        values.put(COLUMN_UPLOADER, info.getAuthor());
        values.put(COLUMN_LASTMOD, date);
        values.put(COLUMN_MARKED, 0);
        try {
            db.insert(TABLE_G_POINTS, null, values);
        }catch(SQLiteConstraintException e){
            values.remove(COLUMN_GID);
            values.remove(COLUMN_LID);
            db.update(TABLE_G_POINTS, values, COLUMN_GID + "='"+ info.getID()+"'", null );
        }

        addTags(info.getID(), tags);


        db.close();
        return info.getID();
    }


    /**
     * Creates a ContentValues object for inserting into a location table.
     * @param x latitude.
     * @param y longitude.
     * @param location location in text.
     * @return ContentValues object that is used to insert into locations table.
     */
    private int addNewLocation(double x, double y, String location){
        int LID = getLocationID(x,y,location);
        if(LID!=0) return LID;

        int ID = getLastLocationID()+1;
        ContentValues res = new ContentValues();
        res.put(COLUMN_LID, ID);
        res.put(COLUMN_LATITUDE, String.valueOf(x));
        res.put(COLUMN_LONGITUDE, String.valueOf(y));
        res.put(COLUMN_LOCATION, location);

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_LOCATIONS, null, res);

        return ID;
    }

    private int getLocationID(double x, double y, String location){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT "+ COLUMN_LID + " FROM " + TABLE_LOCATIONS +
                " WHERE " + COLUMN_LATITUDE + "='" + String.valueOf(x) + "' AND "
                + COLUMN_LONGITUDE + "='" + String.valueOf(y) + "' AND "
                + COLUMN_LOCATION + " LIKE '" + location + "'" , null);

        c.moveToFirst();

        try{
            return c.getInt(c.getColumnIndex(COLUMN_LID));
        }catch(CursorIndexOutOfBoundsException e){
            return 0;
        }
    }

    private int getLastLocationID(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" + COLUMN_LID + ") AS "+COLUMN_LID+" FROM "+TABLE_LOCATIONS, null);

        try{
            c.moveToFirst();
            return c.getInt(c.getColumnIndex(COLUMN_LID));
        }catch(IllegalStateException e){
            return 0;
        }
    }

    /**
     * Creates a ContentValues array for inserting into a specific tag table.
     * @param ID ID of a map point.
     * @param tags An array of tags.
     */

    private void addTags(String ID, String[] tags){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        for (int i = 0; i<tags.length; i++){
            values.clear();
            int TID = getTagID(tags[i].toLowerCase());
            values.put(COLUMN_ID, TID);
            values.put(COLUMN_TAG, tags[i].toLowerCase());

            db.insert(TABLE_TAGS, null, values);

            values.clear();

            values.put(COLUMN_ID, TID);
            values.put(COLUMN_GID, ID);

            db.insert(TABLE_GTAGS, null , values);
        }
        db.close();
    }

    private int getTagID(String tag){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT "+COLUMN_ID+" FROM "+TABLE_TAGS + " WHERE "+COLUMN_TAG +" LIKE '"+tag+"'", null);
        c.moveToFirst();

        if(c.getCount()==0){
            return getLastTagID()+1;
        }

        return c.getInt(c.getColumnIndex(COLUMN_ID));
    }

    private int getLastTagID(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT MAX("+COLUMN_ID+") AS "+COLUMN_ID+" FROM "+TABLE_TAGS, null);
        c.moveToFirst();

        return c.getInt(c.getColumnIndex(COLUMN_ID));
    }

    public void addToMyUploads(SingleUpload upload, double latitude, double longitude, String location){

        int LID = addNewLocation(latitude, longitude, location);

        int UID = getLastUploadID()+1;

        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();


        contentValues.put(COLUMN_ID, UID);
        contentValues.put(COLUMN_LID, LID);
        contentValues.put(COLUMN_IMAGE, ImageProcessing.convertToByteArray(upload.getImg()));
        contentValues.put(COLUMN_ABOUT, upload.getDescription());
        contentValues.put(COLUMN_STATUS, (upload.isStatus())? 1:0);
        contentValues.put(COLUMN_TAG, upload.getTags());

        db.insert(TABLE_MY_UPLOADS, null, contentValues);

        db.close();
    }

    private int getLastUploadID(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" + COLUMN_ID +") AS "+COLUMN_ID+ " FROM "+TABLE_MY_UPLOADS, null);

        try{
            c.moveToFirst();
            return c.getInt(c.getColumnIndex(COLUMN_ID));
        }catch(IllegalStateException e){
            return 0;
        }

    }
    /**
     * Return a map point set.
     * @param i specifies a type of point. 0 = Graffiti point.
     * @return Returns an ArrayList of LatLng values.
     */

    public ArrayList<LatLng> getMapPointSet(int i){

        SQLiteDatabase db = getWritableDatabase();
        ArrayList<LatLng> resultSet = new ArrayList<LatLng>();
        Cursor c = db.rawQuery("SELECT L.* FROM "+ TABLE_LOCATIONS + " AS L,"+TABLE_G_POINTS+" AS GP WHERE GP."+COLUMN_LID + "=L."+COLUMN_LID , null);

        c.moveToFirst();

        while(!c.isAfterLast()){
            String x = c.getString(c.getColumnIndex(COLUMN_LATITUDE));
            String y = c.getString(c.getColumnIndex(COLUMN_LONGITUDE));

            resultSet.add(new LatLng(Double.parseDouble(x), Double.parseDouble(y)));

            c.moveToNext();
        }
        c.close();
        db.close();

        return resultSet;
    }


    public Point_Info getGraffitiInfo(String gid){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db. rawQuery("SELECT GP." + COLUMN_ICON + ",GP."+COLUMN_UPLOADER + ",L." + COLUMN_LOCATION
                + " FROM " + TABLE_G_POINTS + " AS GP,"+TABLE_LOCATIONS +  " AS L WHERE GP."
                + COLUMN_GID + "='" + gid+"' AND GP." + COLUMN_LID + "=L."+COLUMN_LID, null );
        c.moveToFirst();

        Bitmap img = ImageProcessing.convertToBitmap(c.getBlob(c.getColumnIndex(COLUMN_ICON)));

        Point_Info point = new Point_Info(img, c.getString(c.getColumnIndex(COLUMN_UPLOADER)),
                c.getString(c.getColumnIndex(COLUMN_LOCATION)), gid
        );

        c.close();
        db.close();

        return point;
    }

    public ArrayList<String> getObjectIDSet(int i){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT " + COLUMN_GID + " FROM " + TABLE_G_POINTS /*+ " ORDER BY " + COLUMN_LASTMOD + " DESC"*/,null);
        c.moveToFirst();

        ArrayList<String> res = new ArrayList<String>();

        while(!c.isAfterLast()){
            res.add(c.getString(c.getColumnIndex(COLUMN_GID)));
            c.moveToNext();
        }
        c.close();
        db.close();

        return res;
    }

    public ArrayList<String> getTagSet(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT " + COLUMN_TAG + " FROM " + TABLE_TAGS, null);

        c.moveToFirst();

        ArrayList<String> resTags = new ArrayList<String>();

        while(!c.isAfterLast()){
            resTags.add(c.getString(c.getColumnIndex(COLUMN_TAG)));
            c.moveToNext();
        }

        c.close();
        db.close();

        return resTags;
    }

    public ArrayList<SingleUpload> getUploadsSet(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_MY_UPLOADS, null);
        c.moveToFirst();

        ArrayList<SingleUpload> uploads = new ArrayList<SingleUpload>();

        while(!c.isAfterLast()){
            String ID = c.getString(c.getColumnIndex(COLUMN_ID));
            Bitmap img = ImageProcessing.convertToBitmap(c.getBlob(c.getColumnIndex(COLUMN_IMAGE)));
            String description = c.getString(c.getColumnIndex(COLUMN_ABOUT));
            boolean status = (c.getInt(c.getColumnIndex(COLUMN_STATUS))==0)? false:true;
            String tags = c.getString(c.getColumnIndex(COLUMN_TAG));

            uploads.add(new SingleUpload(ID, img, description, status, tags, getUploadAddress(ID), getUploadLocation(ID)));

            c.moveToNext();
        }
        c.close();
        db.close();
        return uploads;
    }

    public SingleUpload getUpload(String ID){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_MY_UPLOADS + " WHERE "+ COLUMN_ID + " LIKE '" + ID +"'", null);

        c.moveToFirst();
        String UID = c.getString(c.getColumnIndex(COLUMN_ID));
        Bitmap img = ImageProcessing.convertToBitmap(c.getBlob(c.getColumnIndex(COLUMN_IMAGE)));
        String description = c.getString(c.getColumnIndex(COLUMN_ABOUT));
        boolean status = (c.getInt(c.getColumnIndex(COLUMN_STATUS))==0)? false:true;
        String tags = c.getString(c.getColumnIndex(COLUMN_TAG));

        SingleUpload upload = new SingleUpload(UID, img, description, status, tags, getUploadAddress(ID), getUploadLocation(ID));

        c.close();
        db.close();

        return upload;
    }

    public void removeUpload(String ID){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_MY_UPLOADS, COLUMN_ID + " LIKE '"+ID+"'",null);

        db.close();
    }

    private LatLng getUploadLocation(String ID){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT "+ COLUMN_LATITUDE+","+COLUMN_LONGITUDE+" FROM "+TABLE_LOCATIONS+" AS L,"+TABLE_MY_UPLOADS+" AS U"
                +" WHERE U."+COLUMN_ID + "='"+ID+"' AND L."+ COLUMN_LID + "= U."+COLUMN_LID, null);
        c.moveToFirst();

        LatLng latLng = new LatLng(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_LATITUDE)))
                , Double.valueOf(c.getString(c.getColumnIndex(COLUMN_LONGITUDE))));

        c.close();

        return latLng;
    }

    private String getUploadAddress(String ID){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT "+ COLUMN_LOCATION +" FROM "+TABLE_LOCATIONS+" AS L,"+TABLE_MY_UPLOADS+" AS U"
                +" WHERE U."+COLUMN_ID + "='"+ID+"' AND L."+ COLUMN_LID + "= U."+COLUMN_LID, null);
        c.moveToFirst();

        String location = c.getString(c.getColumnIndex(COLUMN_LOCATION));

        c.close();

        return location;
    }

    public void changeUploadStatus(String ID){
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, 1);

        SQLiteDatabase db = getWritableDatabase();

        db.update(TABLE_MY_UPLOADS, values, COLUMN_ID+" LIKE '"+ID+"'", null);

        db.close();
    }



    public ArrayList<String> getFilteredIDSet(ArrayList<String> tags, LatLng currentLocation, int seenCond, int kmcond) {
        SQLiteDatabase db = getReadableDatabase();

        Log.i("Q", "SELECT * FROM ("+
                "("+ getTagsQuery(tags)+") AS R NATURAL JOIN  ("+getSeenUnseenQuery(seenCond)+") AS A)"+" AS RES");

        Cursor c = db.rawQuery("SELECT * FROM ((SELECT "+COLUMN_GID +","+COLUMN_LATITUDE+","+COLUMN_LONGITUDE + " FROM "+TABLE_LOCATIONS +" AS L,"+TABLE_G_POINTS
                +" AS GP WHERE L."+COLUMN_LID+"=GP."+COLUMN_LID+") AS GP NATURAL JOIN ("+getSeenUnseenQuery(seenCond)+") AS S NATURAL JOIN ("
                +getTagsQuery(tags)+") AS T))) AS RES", null);

        ArrayList<String> res = new ArrayList<String>();

        c.moveToFirst();
        while(!c.isAfterLast()){
            LatLng pointLocation = new LatLng(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_LATITUDE))),Double.valueOf(c.getString(c.getColumnIndex(COLUMN_LONGITUDE))));
            double dLat = Math.toRadians(pointLocation.latitude - currentLocation.latitude);
            double dLon = Math.toRadians(pointLocation.longitude - currentLocation.longitude);
            int Radius = 6371;

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(currentLocation.latitude))
                    * Math.cos(Math.toRadians(pointLocation.latitude)) * Math.sin(dLon / 2)
                    * Math.sin(dLon / 2);

            double cc = 2 * Math.asin(Math.sqrt(a));
            double valueResult = Radius * cc;
            double km = valueResult / 1;

            DecimalFormat newFormat = new DecimalFormat("####");
            int kmInDec = Integer.valueOf(newFormat.format(km));
            if(kmcond == -1) res.add(c.getString(c.getColumnIndex(COLUMN_GID)));
            else if (kmInDec <= kmcond)res.add(c.getString(c.getColumnIndex(COLUMN_GID)));

            c.moveToNext();
        }
        return res;
    }


    private String getTagsQuery(ArrayList<String> tags){
        String query = "SELECT "+COLUMN_GID + " FROM ((SELECT * FROM "+TABLE_GTAGS+") AS G NATURAL JOIN (SELECT "+ COLUMN_ID + " FROM "+TABLE_TAGS;
        if(tags==null) return query;
        if(tags.isEmpty()) return query;
        query += " WHERE";
        for(int i = 0; i < tags.size(); i++){
            String or = (i==tags.size() - 1)? "":" OR";
            query += " " + COLUMN_TAG + " LIKE '"+ tags.get(i)+"'"+ or;
        }

        return query;
    }

    private String getSeenUnseenQuery(int i){
        String query = "SELECT " + COLUMN_GID + " FROM "+ TABLE_G_POINTS;

        query += (i==0)? " WHERE "+COLUMN_MARKED + "="+0:((i==1)? " WHERE "+ COLUMN_MARKED + "="+1:"");

        return query;
    }

    public LatLng getLocationByID(String ID){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT "+ COLUMN_LATITUDE+","+COLUMN_LONGITUDE + " FROM " + TABLE_LOCATIONS + " AS L,"+TABLE_G_POINTS
                +" AS GP WHERE GP."+COLUMN_GID + " LIKE '"+ID+"' AND GP."+ COLUMN_LID + "=L."+COLUMN_LID, null);

        c.moveToFirst();
       return (new LatLng(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_LATITUDE))), Double.valueOf(c.getString(c.getColumnIndex(COLUMN_LONGITUDE)))));
    }

    /**
     * Add new element to TABLE_ROUTE table
     * @param ID unique graffiti id
     */
    public boolean addToRoute(String ID){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GID, ID);
        try {
            db.insertOrThrow(TABLE_ROUTE, null, values);
            return true;
        }
        catch(SQLiteConstraintException e){
            Toast.makeText(context, "This item has been added already!",Toast.LENGTH_SHORT).show();
            db.close();
            return false;
            }
    }
    public void removeFromRoute(String id){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ROUTE,COLUMN_GID+"='"+id+"'",null);
        db.close();
    }

    public Point_Info getRouteItemInfo(String id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT GP."+COLUMN_ICON+",GP."+COLUMN_UPLOADER+", L."+COLUMN_LOCATION+",L."+COLUMN_LATITUDE+
                ",L."+COLUMN_LONGITUDE+" FROM "+TABLE_ROUTE+" AS R,"+TABLE_G_POINTS+" AS GP,"+TABLE_LOCATIONS+" AS L WHERE GP."+COLUMN_GID+
                " ='"+id+"' AND R."+COLUMN_GID +"='"+id+"' AND L."+COLUMN_LID+"=GP."+COLUMN_LID,null);
        c.moveToFirst();
        Bitmap img = ImageProcessing.convertToBitmap(c.getBlob(c.getColumnIndex(COLUMN_ICON)));
        LatLng latlng = new LatLng(Double.parseDouble(c.getString(c.getColumnIndex(COLUMN_LATITUDE))), Double.parseDouble(c.getString(c.getColumnIndex(COLUMN_LONGITUDE))));
        Point_Info point = new Point_Info(c.getString(c.getColumnIndex(COLUMN_UPLOADER)),id,img,latlng,c.getString(c.getColumnIndex(COLUMN_LOCATION)));
        c.close();
        db.close();
        return  point;
    }

    public ArrayList<String> getAllIDsFromRoute(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT "+COLUMN_GID+" FROM " + TABLE_ROUTE, null);
        ArrayList<String> allIDs = new ArrayList<String>();

        c.moveToFirst();

        while(!c.isAfterLast()){
            allIDs.add(c.getString(c.getColumnIndex(COLUMN_GID)));
            c.moveToNext();
        }
        c.close();
        db.close();
        return allIDs;
    }

    /**
     * Get the last synchronization date (go to graffiti points table and take maximum value from
     * last_modified attribute).
     * @return last synchronization date
     */
    public String getLastSyncDate(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX("+COLUMN_LASTMOD+") AS max FROM "+TABLE_G_POINTS,null);
        c.moveToFirst();
        String res =c.getString(c.getColumnIndex("max"));
        c.close();
        if(res == null)
            return "2016/01/01 00:00";
        return res;
    }
    //TODO: update existing graffities

    /**
     * Store new graffiti from remote database to local database.
     * @param info graffiti id, author, location and icon of graffiti as Point_Info object
     * @param tags tags of the photo
     * @param date upload date of the photo
     * @param x latitude of the photo
     * @param y longitude of the photo
     */
    public void syncGraffiti(Point_Info info, String[] tags, String date,double x,double y){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ICON, ImageProcessing.convertToByteArray(info.getImg()));
        values.put(COLUMN_GID, info.getID());
        values.put(COLUMN_UPLOADER, info.getAuthor());
        values.put(COLUMN_LASTMOD, date);
        try{
            db.insertOrThrow(TABLE_G_POINTS,null,values);
            values.clear();
            //values = addNewLocation(x,y,info.getLocation());
            db.insert(TABLE_LOCATIONS, null, values);
        }catch (SQLiteConstraintException e){
            //TODO: update the table
            db.update(TABLE_G_POINTS, values,COLUMN_GID+"='"+info.getID()+"'", null );
        }
        Log.d("INFORMACIJA","added: "+info.getImg()+" id: "+info.getID()+" author: "+info.getAuthor()+" date" +date+" x: "+x+" y: "+y+" address: "+info.getLocation());

        //TODO: INSERT TAGS
    }

    public void deleteAllUploads(){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_MY_UPLOADS, "", null);

        db.close();
    }

    public void deleteUploads(){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_MY_UPLOADS, COLUMN_STATUS+"="+1, null);

        db.close();
    }

    public void deleteGraffitiPoints(){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_GTAGS, "1", null);
        db.delete(TABLE_TAGS, "1", null);

        String[] tags = {"art", "massive", "human", "writing"};
        addTags(tags, db);

        Cursor c = db.rawQuery("SELECT " + COLUMN_LID+" FROM "+TABLE_G_POINTS, null);
        db.delete(TABLE_G_POINTS, "1", null);
        c.moveToFirst();

        while(!c.isAfterLast()){
            db.delete(TABLE_LOCATIONS, "="+c.getInt(c.getColumnIndex(COLUMN_LID)), null);
            c.moveToNext();
        }
        c.close();
        db.close();
    }

    private void addTags(String [] tags, SQLiteDatabase db){
        ContentValues values = new ContentValues();


        for(int i = 0; i < tags.length; i++){
            values.clear();
            values.put(COLUMN_ID, i+1);
            values.put(COLUMN_TAG, tags[i].toLowerCase());
            db.insert(TABLE_TAGS, null, values);
        }
    }

    public void changeSeen(String id){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MARKED, 1);
        db.update(TABLE_G_POINTS, values, COLUMN_GID+"='"+id+"'", null);
        db.close();
    }



}
