package gr.akapnos.app.map_classes;

import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import gr.akapnos.app.Helper;
import gr.akapnos.app.R;
import gr.akapnos.app.utilities.RunnableArg;
import trikita.log.Log;

public class DirectionsGetter {
    private static RunnableArg block;
    private boolean walking_distance = false;
//    public void get_directions_time(LatLng origin, LatLng dest, RunnableArg runnable) {
//        // Getting URL to the Google Directions API
//        walking_distance = isWalkingDistance(origin, dest);
//        get_directions_time(origin, dest, walking_distance, runnable);
//    }
    public void get_directions_time(LatLng origin, LatLng dest, boolean walking_distance, RunnableArg runnable) {
        // Getting URL to the Google Directions API
        this.walking_distance = walking_distance;

        String url = getDirectionsUrl(origin, dest);
        block = runnable;
        DownloadTask downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    public boolean isWalkingDistance(LatLng origin, LatLng dest) {
        Location loc1 = new Location("");
        loc1.setLatitude(origin.latitude);
        loc1.setLongitude(origin.longitude);
        Location loc2 = new Location("");
        loc2.setLatitude(dest.latitude);
        loc2.setLongitude(dest.longitude);

        float distance = loc1.distanceTo(loc2);
        return distance <= 500;
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=" + (walking_distance ? "walking" : "driving");

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+mode;

        parameters += "&key=" + Helper.appCtx().getResources().getString(R.string.google_play_server_api_key);

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/json?"+parameters;
        Log.v("GetDistanceURL = " + url);
        return url;
    }

    /** A method to download json data from url */
    private static String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb  = new StringBuilder();

            String line;
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception url", e.toString());
        }finally{
            if(iStream != null) { iStream.close(); }
            if(urlConnection != null) { urlConnection.disconnect(); }
        }
        return data;
    }



    // Fetches data from url passed
    private static class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
//                Log.e("Background Task data = ",data);
            }catch(Exception e){
                Log.e("Background Task Exception",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private static class ParserTask extends AsyncTask<String, Integer, String> {//List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected String doInBackground(String... jsonData) {//List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            String total_time = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                total_time = parser.parseTotalTime(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return total_time;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(String result) {
            if(block != null) {
                block.run(result);
            }
        }
    }
}
