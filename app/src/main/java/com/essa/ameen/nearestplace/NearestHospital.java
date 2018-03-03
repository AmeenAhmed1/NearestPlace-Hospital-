package com.essa.ameen.nearestplace;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ameen on 23-Feb-18.
 */

public class NearestHospital extends AsyncTask<Void, Void, Void>{

    private static final String TAG = "NearestHospital";

    final String API = "AIzaSyCbn7c1kYWtK2AEoIE5zANSiblzPQznAW0";

    final String mURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
            "location=29.3274897,30.8351751&" +
            "radius=5000&" +
            "type=hospital&" +
            "key="+API;

    @Override
    protected Void doInBackground(Void... voids) {

        try{

            //To open the connection
            URL url = new URL(mURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");


            //Read from response
            InputStream in = httpURLConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            StringBuffer buffer = new StringBuffer();

            String line = "";
            while ((line = reader.readLine()) != null){
                buffer.append(line);
            }

            //To Convert the response
            String jsonDataFinal = buffer.toString();
            //convertStreamToString(jsonDataFinal);

            Log.i(TAG, "doInBackground: " + jsonDataFinal);

        }catch (Exception ex){
            Log.i(TAG, "doInBackground: " + ex.getMessage());
        }

        return null;
    }


    //To convert the data from json to strings
    /*private void convertStreamToString(String finalData) {

        list = new ArrayList<>();

        try {
            //To get the first element
            JSONObject mJsonObject = new JSONObject(finalData);

            JSONArray mJsonArray = mJsonObject.getJSONArray("results");

            //To get every node in the array  list
            for(int i = 0 ; i < mJsonArray.length() ; ++i){

                //to get the image url
                //String image_URL = "http://image.tmdb.org/t/p/w185/";

                //Json formating
                JSONObject mJsonResult = mJsonArray.getJSONObject(i);
                String title = mJsonResult.getString("title");
                String overView = mJsonResult.getString("overview");
                String date = mJsonResult.getString("release_date");
                String image = mJsonResult.getString("poster_path");
                String rate = mJsonResult.getString("vote_average");

                    /*Log.i(TAG, "convertStreamToString: Title : " + title);
                    Log.i(TAG, "convertStreamToString: View : " + overView);
                    Log.i(TAG, "convertStreamToString: Date : " + date);*/

                /*String finalImageUrl = "http://image.tmdb.org/t/p/w500/" + image;

                Log.i(TAG, "convertStreamToString: Image url = " + finalImageUrl);

                MovieListItem x = new MovieListItem(title, overView, date, finalImageUrl, rate);
                list.add(x);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/
}
