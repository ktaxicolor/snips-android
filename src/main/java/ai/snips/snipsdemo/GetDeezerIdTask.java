package ai.snips.snipsdemo;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Taxicolor on 25/02/2018.
 */

/*class GetDeezerIdTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
        HttpsURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL("https://api.deezer.com/search?q="+params[0].toLowerCase().replace(' ','-') + "&limit=1&output=json");
            connection = (HttpsURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getIdFromString(String s) throws JSONException {
        JSONArray jsonArray = new JSONArray(s);
        return jsonArray.getJSONObject(0).getString("artist");
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try {
            return getIdFromString(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
*/