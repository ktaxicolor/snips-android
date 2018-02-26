package ai.snips.snipsdemo;

/**
 * Created by Taxicolor on 26/02/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.KeyEvent;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import ai.snips.hermes.IntentMessage;
import ai.snips.queries.ontology.Slot;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class MusicManager {
    public static final String NO_MUSIC_MSG = "No music to play  with :'(";
    public static final String NO_INTERNET_MSG = "Please connect yoself to the internet";
    public static final String NO_ARTIST_MSG = "Artist was not found";

    public static final String NULL_ID = "0";

    private static final OkHttpClient client = new OkHttpClient();

    public static void pauseMusic(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (mAudioManager.isMusicActive()) {
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);
            mAudioManager.dispatchMediaKeyEvent(event);
        } else {
            Toast.makeText(context, NO_MUSIC_MSG, Toast.LENGTH_SHORT);
        }

    }

    public static void playMusic(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (!mAudioManager.isMusicActive()) {
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
            mAudioManager.dispatchMediaKeyEvent(event);
        } else {
            Toast.makeText(context, NO_MUSIC_MSG, Toast.LENGTH_SHORT);
        }
    }

    public static void volumeUp(Context context){
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (mAudioManager.isMusicActive()) {
            mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        } else {
            Toast.makeText(context, NO_MUSIC_MSG, Toast.LENGTH_SHORT);
        }
    }


    public static void volumeDown(Context context){
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (mAudioManager.isMusicActive()) {
            mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        } else {
            Toast.makeText(context, NO_MUSIC_MSG, Toast.LENGTH_SHORT);
        }
    }

    public static void playNextSong(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (mAudioManager.isMusicActive()) {
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
            mAudioManager.dispatchMediaKeyEvent(event);
        } else {
            Toast.makeText(context, NO_MUSIC_MSG, Toast.LENGTH_SHORT);
        }
    }

    public static void playArtist(Context context, IntentMessage intent) {
        if (intent != null) {
            List<Slot> slots = intent.getSlots();
            for (Slot slot : slots) {
                if (slot.getSlotName().equals("artist") && slot.getValue() != null) {
                    String artist = slot.getRawValue();

                    String artistDeezerId = null;
                    try {
                        artistDeezerId = getArtistId(context, artist);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(!artistDeezerId.equals(NULL_ID)) {
                        Uri uri = Uri.parse("https://www.deezer.com/artist/" + artistDeezerId + "?autoplay=true");
                        Intent deezerIntent = new Intent(Intent.ACTION_VIEW, uri);

                        // Verify there is an app to take care of this intent
                        PackageManager packageManager = context.getPackageManager();
                        List<ResolveInfo> activities = packageManager.queryIntentActivities(deezerIntent, 0);
                        boolean isIntentSafe = activities.size() > 0;
                        // Start an activity if it's safe
                        if (isIntentSafe) {
                            context.startActivity(deezerIntent);
                        }
                    }
                    else {
                        Toast.makeText(context, NO_ARTIST_MSG, Toast.LENGTH_SHORT);
                    }
                }
            }
        }
    }

    private static String getArtistId(Context context, String artistName) throws IOException {
        String artistId = "";
        if (!isConnected(context)) {
            Toast.makeText(context, NO_INTERNET_MSG, Toast.LENGTH_LONG).show();
            return NULL_ID;
        }
        artistName = artistName.toLowerCase().replace(' ', '-');
        Request request = new Request.Builder().url("https://api.deezer.com/search?q=" + artistName + "&limit=1&output=json").build();

        try(Response response = client.newCall(request).execute()){
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            /*
              Headers responseHeaders = response.headers();
              for (int i = 0; i < responseHeaders.size(); i++) {
            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
              }
            */
            // Parse JSON to get ID
            artistId = parseId(response.body().string());
        }

        return artistId;
    }

    private static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private static String parseId(String jsonAsString)
    {
        String id = NULL_ID;
        try {
            JSONObject obj = new JSONObject(jsonAsString);

            id = obj.getJSONArray("data").getJSONObject(0).getJSONObject("artist").getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

}