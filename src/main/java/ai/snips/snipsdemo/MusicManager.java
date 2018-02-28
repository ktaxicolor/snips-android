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

    private static final String SLOT_ARTIST = "artist";
    private static final String SLOT_ALBUM = "album";

    private static final String DEEZER_JSON_ARTIST = "artist";
    private static final String DEEZER_JSON_ALBUM = "album";

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

        mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);

    }

    public static void volumeDown(Context context){
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
    }

    public static void setVolume(Context context, IntentMessage intent){
        if (intent != null) {

            // 1) Get the volume level
            List<Slot> slots = intent.getSlots();
            for (Slot slot : slots) {
                if (slot.getSlotName().equals("volume_set_fr") && slot.getValue() != null) {
                    try {
                        int volume = Integer.parseInt(slot.getRawValue());

                        if (volume != 0) {
                            // 2) Change the volume
                            AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                            if (volume <= mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
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

    private static String getSlotRawValue(IntentMessage intent, String slotName){
        List<Slot> slots = intent.getSlots();
        for (Slot slot : slots) {
            if (slot.getSlotName().equals(slotName) && slot.getValue() != null) {
                return slot.getRawValue();
            }
        }
        return "";
    }

    public static void playArtist(Context context, IntentMessage intent) {
        if (intent != null) {
            String artist = getSlotRawValue(intent, SLOT_ARTIST);

            String artistDeezerId = NULL_ID;
            try {
                artistDeezerId = getArtistId(context, artist);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(!artistDeezerId.equals(NULL_ID)) {
                // Top tracks
                //startDeezerActivity(context, "https://www.deezer.com/artist/" + artistDeezerId + "/top_track?autoplay=true");
                // or  mix based on artist
                startDeezerActivity(context, "https://www.deezer.com/artist/" + artistDeezerId + "autoplay=true");
            }
        }
    }

    public static void playAlbum(Context context, IntentMessage intent) {
        if (intent != null) {
            String album = getSlotRawValue(intent, SLOT_ALBUM);
            String artist = getSlotRawValue(intent, SLOT_ARTIST);

            String albumDeezerId = NULL_ID;

            try{
                albumDeezerId = getAlbumId(context, album, artist);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(!albumDeezerId.equals(NULL_ID)) {
                startDeezerActivity(context, "https://www.deezer.com/album/" + albumDeezerId + "?autoplay=true");
            }


        }
    }


    public static void playSong(Context context, IntentMessage intent) {
    }

    public static void playPlaylist(Context context, IntentMessage intent) {
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

            // Parse JSON to get ID
            artistId = parseId(response.body().string(), DEEZER_JSON_ARTIST);
        }

        return artistId;
    }

    private static String getAlbumId(Context context, String albumName, String artistName) throws IOException {
        String albumId = "";
        if (!isConnected(context)) {
            Toast.makeText(context, NO_INTERNET_MSG, Toast.LENGTH_LONG).show();
            return NULL_ID;
        }
        artistName = artistName.toLowerCase().replace(' ', '-');
        Request request = new Request.Builder().url("https://api.deezer.com/search?q=artist:'"+artistName+"'album:'"+albumName+"'&limit=1&output=json").build(); // + &order=RANKING ??

        try(Response response = client.newCall(request).execute()){
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Parse JSON to get ID
            albumId = parseId(response.body().string(), DEEZER_JSON_ALBUM);
        }

        return albumId;
    }

    private static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private static String parseId(String jsonAsString, String jsonParam)
    {
        String id = NULL_ID;
        try {
            JSONObject obj = new JSONObject(jsonAsString);

            id = obj.getJSONArray("data").getJSONObject(0).getJSONObject(jsonParam).getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    private static void startDeezerActivity (Context context, String deezerUrl){
        Uri uri = Uri.parse(deezerUrl);

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
}