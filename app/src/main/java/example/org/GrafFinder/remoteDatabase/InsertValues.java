package example.org.GrafFinder.remoteDatabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayInputStream;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import example.org.GrafFinder.ImageProcessing;
import example.org.GrafFinder.LocalDatabase;
import example.org.GrafFinder.Point_Info;
import example.org.GrafFinder.WelcomeActivity;
import example.org.GrafFinder.uploadsManager.SingleUpload;

public class InsertValues extends AppCompatActivity {

    Connection connection;
    Context context;
   // int location_id = -1;
    public ArrayList<String> allIDsAtLocation;
    public ArrayList<Bitmap> allIconsAtLocation;
    public boolean isConnected=false;
    public static final int UNKNOWN_USER=-1;
    public static final int NOT_INSERTED =0;
    public static final int INSERTED = 1;


    public InsertValues(Context context) {
        this.context = context;
        Connect connect = new Connect();
        try {
            connect.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            isConnected=false;
        }
    }

    /**
     * Connect to the database.
     */
    class Connect extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection("jdbc:postgresql://193.219.91.103:1835/pblDatabase", "postgres", "postgres");
                isConnected=true;
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                isConnected=false;
            }
            return null;
        }
    }

    /**
     * Add new user to the database.
     */
    public void addUser(final String name, final String email, final String id){
        final String[] returnedName = {name};
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO UserAcc VALUES('"+id+"','"+email+"','"+
                            name+"') ON CONFLICT ON CONSTRAINT useracc_pkey DO UPDATE SET token=EXCLUDED.token RETURNING username");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while(resultSet.next()){
                        returnedName[0] =resultSet.getString(1);
                        Log.i("USERNAME",returnedName[0]);
                        Log.i("USERNAME",resultSet.getString(1));
                    }
                    resultSet.close();
                    preparedStatement.close();

                    SharedPreferences preferences = context.getSharedPreferences(WelcomeActivity.ACCOUNT_INFO, 0);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putString(WelcomeActivity.USER_TOKEN, id);
                    editor.putString(WelcomeActivity.USER_NAME, returnedName[0]);
                    editor.putString(WelcomeActivity.EMAIL, email);
                    editor.apply();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void changeUsername(){
        String query;
        SharedPreferences accountname = context.getSharedPreferences(WelcomeActivity.ACCOUNT_INFO,0);

        query = "UPDATE UserAcc SET username='";
        query += accountname.getString(WelcomeActivity.USER_NAME,"Unknown") + "'";
        query += " WHERE token='"+accountname.getString(WelcomeActivity.USER_TOKEN, "Unknown")+"';";
        executeQuery(query);
    }

    /**
     * Check if graffiti at specified position already exist. if yes, return true, otherwise - false(and
     * add new graffiti to the database).
     * @param address address to add
     * @param latitude latitude to add
     * @param longitude longitude to add
     * @param icon icon of the graffiti
     * @param description description of graffiti
     * @param tags tags of graffiti
     */
    public int addGraffiti(String address, String latitude, String longitude, Bitmap icon, String description, String[] tags){
        if(idsAndIconsAtLocation(address)){    // if this graffiti is the first one
            int location = insertLocation(address,latitude,longitude);
            addNewGraffiti(location, ImageProcessing.convertToByteArray(icon),description,tags);
            return 0;
        }
        else{   // if other graffities at this location already exist
            return 1;
        }
    }

    /**
     * Add new graffiti to the database.
     * @param location_id location id of the graffiti
     * @param icon icon of the graffiti
     * @param description description of the graffiti
     * @param tags tags of graffiti
     */
    public void addNewGraffiti(final int location_id, final byte[] icon, final String description, final String[] tags){
        final String gid = "Graffiti" + UUID.randomUUID().toString();
        final boolean status = getStatus();
        final String token = getAuthor();
        final String date = getDate();
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    PreparedStatement pst = connection.prepareStatement("INSERT INTO Graffiti VALUES(?,null,?,?,?,?,?,'"+date+"')");
                    pst.setString(1,gid);
                    pst.setInt(2,location_id);
                    pst.setString(3,token);
                    pst.setBinaryStream(4, new ByteArrayInputStream(icon));
                    pst.setString(5,description);
                    pst.setBoolean(6,status);
                    pst.executeUpdate();
                    pst.close();
                    addTags(tags,gid);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addTags(String[] tags, String gid){
        String query ="INSERT INTO Tags(tag) VALUES ";
        for(String tag: tags) {
            query += "('"+tag+"'),";
        }

        query=query.substring(0,query.length()-1)+" ON CONFLICT ON CONSTRAINT tags_tag_key DO UPDATE SET tag=EXCLUDED.tag RETURNING tagid";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet resultSet =ps.executeQuery();
            query = "INSERT INTO GTags VALUES ";
            while(resultSet.next()){
                query+= "("+resultSet.getInt(1)+",'"+gid+"'),";
            }
            resultSet.close();
            ps.close();
            query=query.substring(0,query.length()-1);
            PreparedStatement pst = connection.prepareStatement(query);
            pst.executeQuery();
            pst.close();
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public int addLike(final String gid){
        final int[] res = new int[1];
        final String token = getAuthor();
        if(token.equals("Unknown"))
            return UNKNOWN_USER;
        else{
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO Rates VALUES ('"+token+"','"+gid+"') " +
                    "ON CONFLICT DO NOTHING");
                    res[0] = ps.executeUpdate();   //jei idejo +1 grazinama 1, jei jau buvo ideta - 0.
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res[0];
        }
    }

    public int addReport(final String gid){
        final String token =getAuthor();
        final int[] res = new int[1];
        if(token.equals("Unknown"))
            return UNKNOWN_USER;
        else{
            Thread t = new Thread(){
                @Override
                public void run() {
                    try {
                        PreparedStatement ps = connection.prepareStatement("INSERT INTO Reports VALUES('"+token+"','"+gid+"')" +
                                "ON CONFLICT DO NOTHING");
                        res[0] = ps.executeUpdate();    //jei idejo, reiksme bus 1, jei jau egzistavo, tai 0.
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return res[0];
        }
    }

    /**
     * Update graffiti that is already existing in the database.
     * @param oldID id of graffiti that should be updated
     * @param icon new graffiti icon
     * @param description new graffiti description
     * @param tags new graffiti tags
     */
    public void addModified(final int location_id, final String oldID, final byte[] icon, final String description, final String[] tags){
        final String newID = "Graffiti" + UUID.randomUUID().toString();
        final String author = getAuthor();
        final boolean status = getStatus();
        final String date = getDate();
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    PreparedStatement pst = connection.prepareStatement("INSERT INTO Graffiti VALUES(?,?,?,?,?,?,?,'"+date+"')");
                    pst.setString(1,newID);
                    pst.setString(2,oldID);
                    pst.setInt(3,location_id);
                    pst.setString(4,author);
                    pst.setBinaryStream(5, new ByteArrayInputStream(icon));
                    pst.setString(6,description);
                    pst.setBoolean(7,status);
                    pst.executeUpdate();
                    pst.close();
                    addTags(tags,newID);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize class variables allIDsAtLocation and allIconsAtLocation. Find all IDs and Icons
     * at specified location id and add them to class variables.
     */
    public boolean idsAndIconsAtLocation(final String address){
        allIDsAtLocation = new ArrayList<String>();
        allIconsAtLocation = new ArrayList<Bitmap>();
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT  * FROM getNewestPhotosAtLocation('"+address+"')");
                    ResultSet rs = preparedStatement.executeQuery();
                    while (rs.next()){
                            allIDsAtLocation.add(rs.getString(1));
                            allIconsAtLocation.add(ImageProcessing.convertToBitmap(rs.getBytes(2)));
                    }
                    rs.close();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return allIDsAtLocation.isEmpty();
     }

    /**
     * Based on latitude and longitude, check if such location does not already exist in database.
     * If location already exist, return its' ID, otherwise return -1.
     * @return location_id or -1
     */
    int location_id;
    public int getLocationIDs(String address, Double lat, Double lng){
        location_id =-1;
        final String query= "SELECT location_id FROM Location WHERE address LIKE '"+address +
                "' AND latitude LIKE '"+lat+"' AND longitude LIKE '"+lng+"'";
        Thread t = new Thread(){
            public void run(){
                try {
                    PreparedStatement st = connection.prepareStatement(query);
                    ResultSet rs = st.executeQuery();
                    while(rs.next()){
                    location_id =(rs.getInt(1));
                    }
                    rs.close();
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();   // wait until thread finishes, and only then return the result.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return location_id;
    }

    /**
     * Get authors' token from sharedPreferences.
     * @return authors' token.
     */
    public String getAuthor(){
        SharedPreferences sharedPreferences= context.getSharedPreferences(WelcomeActivity.ACCOUNT_INFO,0);
        return sharedPreferences.getString(WelcomeActivity.USER_TOKEN,"Unknown");
    }

    /**
     * get photos' status (confirmed(true) or not(false))
     * @return status
     */
    public boolean getStatus(){
        boolean status = true;
        if(getAuthor().equalsIgnoreCase("unknown"))  //if unknown user is uploading new graffiti, photo status will be false.
            status = false;
        return status;
    }

    /**
     * If location does not exist in database, add new entity, returning the location id.
     * @param address address to add
     * @param latitude latitude to add
     * @param longitude longitude to add
     * @return location_id of new entity
     */
    int location;
    public int insertLocation(final String address, final String latitude, final String longitude){
        location = -1;
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    PreparedStatement st = connection.prepareStatement("INSERT INTO location(address, latitude, longitude) VALUES ('"+address+"','"+latitude+"','"+longitude+"') RETURNING location_id");
                    ResultSet rs = st.executeQuery();
                    while (rs.next()){
                        location = rs.getInt(1);
                    }
                    rs.close();
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * Execute query that does not require to return anything.
     * @param query query that needs to be executed.
     */
    private void executeQuery(final String query){
        Thread t = new Thread(){
            public void run(){
                try {
                    PreparedStatement st = connection.prepareStatement(query);
                    ResultSet rs = st.executeQuery();
                    rs.close();
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();   //wait until thread finishes, and only then return the result.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get current date in yyyy/mm/dd HH:mm format
     * @return current date
     */
    private String getDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date dd = new Date();
        return dateFormat.format(dd);
    }


    ArrayList<String> foundIds;

    /**
     * Insert new graffiti from remote database to local database, by returning the list of graffiti
     * ids that need to be updated or inserted.
     * @return graffiti ids that needs to be updated or inserted to localDB
     */
    public ArrayList<String> sync(final int newestCount, final String token, final int topX){
        final LocalDatabase localDatabase = new LocalDatabase(context,null,null,1);
        final String lastSync = localDatabase.getLastSyncDate();
        foundIds = new ArrayList<>();
        Thread t = new Thread(){
            @Override
            public void run() {
                    try {
                        PreparedStatement st = connection.prepareStatement("SELECT * FROM getSyncItems(" + newestCount + ",'" + token + "'," + topX + ")");
                        ResultSet rs = st.executeQuery();
                        localDatabase.deleteGraffitiPoints();
                        while (rs.next()) {
                            String[] tags = getTags(rs.getString(1));
                            Point_Info info = new Point_Info(rs.getString(3), rs.getString(1), ImageProcessing.convertToBitmap(rs.getBytes(2)), new LatLng(Double.parseDouble(rs.getString(5)), Double.parseDouble(rs.getString(6))), rs.getString(4));
                            localDatabase.addNewGraffitiPoint(info, tags, rs.getString(7));
                            //   localDatabase.syncGraffiti(info,tags,rs.getString(7),Double.parseDouble(rs.getString(5)),Double.parseDouble(rs.getString(6)));
                            foundIds.add(rs.getString(1));
                        }
                        rs.close();
                        st.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return foundIds;
    }

    private String[] getTags(String gid){
        String[] arr = null;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM getTags('"+gid+"')");
            ResultSet rs = ps.executeQuery();
            Array a;
            while (rs.next()){
                a=rs.getArray(1);
                arr =(String[])a.getArray();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return arr;
    }

    public ArrayList<SingleUpload> getUserUploads(final String token){
        final String author = token;
        final ArrayList<SingleUpload> uploads = new ArrayList<SingleUpload>();
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    PreparedStatement st = connection.prepareStatement("SELECT g.icon, g.description, l.latitude, l.longitude, l.address " +
                            "FROM graffiti as g, location as l WHERE l.location_id = g.location_id AND g.author='"+author+"'");
                    ResultSet rs = st.executeQuery();
                    while(rs.next()){
                        String tags = "qwerty";
                        LatLng lng = new LatLng(Double.valueOf(rs.getString(3)),Double.valueOf(rs.getString(4)));
                        SingleUpload upload = new SingleUpload(null, ImageProcessing.convertToBitmap(rs.getBytes(1)), rs.getString(2), true, tags, rs.getString(5), lng);

                        uploads.add(upload);

                        //   localDatabase.syncGraffiti(info,tags,rs.getString(7),Double.parseDouble(rs.getString(5)),Double.parseDouble(rs.getString(6)));
                    }
                    rs.close();
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return uploads;
    }

    public ArrayList<String> authors;
    public ArrayList<String> desciptions;
    public ArrayList<Bitmap> icons;
    public ArrayList<String> dates;
    public ArrayList<String> ids;
    public ArrayList<String> tags;

    /**
     * Get the history of specified graffiti id. Information that is read from the remote database
     * needs to be stored in class variables (authors, descriptions,icons and dates). The information
     * about specified graffiti id is also needed, and it is added to the last position of all
     * class variables.
     * @param id graffiti id
     */
    public void getHistory(final String id){
        Log.d("INFORMACIJA","id: "+id);
        authors = new ArrayList<>();
        desciptions = new ArrayList<>();
        icons = new ArrayList<>();
        dates = new ArrayList<>();
        ids = new ArrayList<>();
        tags = new ArrayList<>();
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    PreparedStatement pst = connection.prepareStatement(
                            "SELECT * FROM getHistory('"+id+"')");
                    ResultSet rs = pst.executeQuery();
                    while (rs.next()){
                        authors.add(rs.getString(1));
                        icons.add(ImageProcessing.convertToBitmap(rs.getBytes(2)));
                        desciptions.add(rs.getString(3));
                        dates.add(rs.getString(4));
                        ids.add(rs.getString(5));
                        String tagString="Tags: ";
                        for(String tag: getTags(rs.getString(5)))
                        tagString+=tag+" ";
                        tags.add(tagString);
                    }
                    pst.close();
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}