package example.org.GrafFinder.routePackage;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadXML extends AsyncTask<String, Void, String> {

    public static String storedData;

    @Override
    protected String doInBackground(String... params) {
        storedData = downloadXMLFile(params[0]);
        return null;
    }

    private String downloadXMLFile(String path) {
        StringBuilder res = new StringBuilder();
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();    //open url connection
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream()); //start reading data

            int charRead;
            char[] inputBuffer = new char[500]; //read file, 500 bites at the time
            while (true) {
                charRead = inputStreamReader.read(inputBuffer); //read data and (charRead) return the number of the characters that was read
                if (charRead <= 0)    // when 0 chars are read, means there is an end of the file
                    break;
                else
                    res.append(String.copyValueOf(inputBuffer, 0, charRead)); //append read info to res, starting at pos 0 to charRead
            }
            return res.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
