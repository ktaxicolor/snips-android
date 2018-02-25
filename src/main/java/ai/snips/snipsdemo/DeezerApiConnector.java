package ai.snips.snipsdemo;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Taxicolor on 25/02/2018.
 */

final public class  DeezerApiConnector {
    private DeezerApiConnector(){}

    //API Deezer : asking for IDs
    public static String getArtistDeezerID()
//    public static String getArtistDeezerID(String artistName)

    {

        //String urlAPIRequest = "https://api.deezer.com/search?q="+artistName.toLowerCase().replace(' ','-') + "&limit=1&output=json";
        String artistId = "";
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {/*
            URL url = new URL(urlAPIRequest);
            HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
            request.connect();*/
            File fileOutputJson = createFileAndDirectory();

//            URL url = new URL("http://api.deezer.com/search?q="+artistName.toLowerCase().replace(' ','-') + "&limit=1&output=json");
            URL url = new URL("http://api.deezer.com/search?q=david-bowie&limit=1&output=json");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
/*
            FileOutputStream fileOutput = new FileOutputStream(fileOutputJson);
            //urlConnection.setInstanceFollowRedirects(false);
            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[1024];
            int bufferLength;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            fileOutput.close();*/
            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            String sResponse = buffer.toString();
            JSONDeezerParser parser = new JSONDeezerParser();
            /*InputStreamReader reader = new InputStreamReader((InputStream) request.getContent());*/
           // artistId = parser.parseDeezerAPIResponse(sResponse);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return artistId;
    }

    private static File createFileAndDirectory() throws FileNotFoundException {
        final String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        final String temp_path = extStorageDirectory + "/jarvis/temp";
        File jsonOutputFile = new File(temp_path, "/");
        if (!jsonOutputFile.exists())
            jsonOutputFile.mkdirs();
        return new File(jsonOutputFile, "artist.json");
    }
}
