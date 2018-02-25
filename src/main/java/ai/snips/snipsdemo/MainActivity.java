package ai.snips.snipsdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import ai.snips.hermes.IntentMessage;
import ai.snips.hermes.SessionEndedMessage;
import ai.snips.hermes.SessionQueuedMessage;
import ai.snips.hermes.SessionStartedMessage;
import ai.snips.megazord.Megazord;
import ai.snips.queries.ontology.Slot;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    private static final int AUDIO_ECHO_REQUEST = 0;
    private static final String TAG = "MainActivity";

    private static final int FREQUENCY = 16_000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final String SPEAKER_INTERRUPT = "speakerInterrupt";
    public static final String NEXT_SONG = "nextSong";
    public static final String RESUME_MUSIC = "resumeMusic";
    public static final String PLAY_ARTIST = "playArtist";
    public static final String NOT_IMPLEMENTED_MSG = "Not implemented, sorry not sorry!";
    public static final String NO_MUSIC_MSG = "No music to play  with :'(";

    // Snips platform codename for android port is Megazord
    private static Megazord megazord;

    private AudioRecord recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ensurePermissions();

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ensurePermissions()) {
                    final Button button = (Button) findViewById(R.id.start);
                    button.setEnabled(false);
                    button.setText(R.string.loading);

                    final View scrollView = findViewById(R.id.scrollView);
                    scrollView.setVisibility(View.GONE);

                    final View loadingPanel = findViewById(R.id.loadingPanel);
                    loadingPanel.setVisibility(View.VISIBLE);

                    new Thread() {
                        public void run() {
                            startMegazordService();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingPanel.setVisibility(View.GONE);

                                    button.setEnabled(true);
                                    button.setText(R.string.start_dialog_session);
                                    button.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            // programmatically start a dialogue session
                                            megazord.startSession(null, null, false, null);
                                        }
                                    });
                                    scrollView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }.start();
                }
            }
        });
    }

    private boolean ensurePermissions() {
        int status = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO);
        if (status != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, AUDIO_ECHO_REQUEST);
            return false;
        }
        return true;
    }

    private void startMegazordService() {
        if (megazord == null) {
            // a dir where the assistant models was unziped. it should contain the folders asr dialogue hotword and nlu
            File assistantDir = new File(Environment.getExternalStorageDirectory().toString(), "snips_android_assistant");

            megazord = Megazord.builder(assistantDir)
                               .enableDialogue(true) // defaults to true
                               .enableHotword(true) // defaults to true
                               .enableSnipsWatchHtml(true) // defaults to false
                               .enableLogs(true) // defaults to false
                               .withHotwordSensitivity(0.5f) // defaults to 0.5
                               .enableStreaming(true) // defaults to false
                               .build();

            megazord.setOnHotwordDetectedListener(new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    Log.d(TAG, "an hotword was detected !");
                    // Do your magic here :D
                    return null;
                }
            });

            megazord.setOnIntentDetectedListener(new Function1<IntentMessage, Unit>() {
                @Override
                public Unit invoke(IntentMessage intentMessage) {
                    Log.d(TAG, "received an intent: " + intentMessage);
                    // Do your magic here :D
                    // Music related intents :
                    String intentName = intentMessage.getIntent().getIntentName();
                    switch (intentName) {
                        case SPEAKER_INTERRUPT: pauseMusic();
                        break;
                        case NEXT_SONG: playNextSong();
                        break;
                        case RESUME_MUSIC: playMusic();
                        break;
                        case PLAY_ARTIST: playArtist(intentMessage);
                        default: notImplementedToastDisplay();
                    }

                    megazord.endSession(intentMessage.getSessionId(), null);
                    return null;
                }


            });

            megazord.setOnListeningStateChangedListener(new Function1<Boolean, Unit>() {
                @Override
                public Unit invoke(Boolean isListening) {
                    Log.d(TAG, "asr listening state: " + isListening);
                    // Do you magic here :D
                    return null;
                }
            });

            megazord.setOnSessionStartedListener(new Function1<SessionStartedMessage, Unit>() {
                @Override
                public Unit invoke(SessionStartedMessage sessionStartedMessage) {
                    Log.d(TAG, "dialogue session started: " + sessionStartedMessage);
                    return null;
                }
            });

            megazord.setOnSessionQueuedListener(new Function1<SessionQueuedMessage, Unit>() {
                @Override
                public Unit invoke(SessionQueuedMessage sessionQueuedMessage) {
                    Log.d(TAG, "dialogue session queued: " + sessionQueuedMessage);
                    return null;
                }
            });

            megazord.setOnSessionEndedListener(new Function1<SessionEndedMessage, Unit>() {
                @Override
                public Unit invoke(SessionEndedMessage sessionEndedMessage) {
                    Log.d(TAG, "dialogue session ended: " + sessionEndedMessage);
                    return null;
                }
            });

            // This api is really for debugging purposes and you should not have features depending on its output
            // If you need us to expose more APIs please do ask !
            megazord.setOnSnipsWatchListener(new Function1<String, Unit>() {
                public Unit invoke(final String s) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // We enabled html logs in the builder, hence the fromHtml. If you only log to the console,
                            // or don't want colors to be displayed, do not enable the option
                            ((EditText) findViewById(R.id.text)).append(Html.fromHtml(s + "<br />"));
                            findViewById(R.id.scrollView).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ScrollView) findViewById(R.id.scrollView)).fullScroll(View.FOCUS_DOWN);
                                }
                            });
                        }
                    });
                    return null;
                }
            });

            // We enabled steaming in the builder, so we need to provide the platform an audio stream. If you don't want
            // to manage the audio stream do no enable the option, and the snips platform will grab the mic by itself
            new Thread() {
                public void run() {
                    runStreaming();
                }
            }.start();

            megazord.start(); // no way to stop it yet, coming soon
        }

}

    private void runStreaming() {
        final int minBufferSizeInBytes = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING);
        Log.d(TAG, "minBufferSizeInBytes: " + minBufferSizeInBytes);

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL, ENCODING, minBufferSizeInBytes);
        recorder.startRecording();

        // In a non demo app, you want to have a way to stop this :)
        while (true) {
            short[] buffer = new short[minBufferSizeInBytes / 2];
            recorder.read(buffer, 0, buffer.length);
            if (megazord != null) {
                megazord.sendAudioBuffer(buffer);
            }
        }
    }


    //CDispatch music commands
    private void playNextSong() {
        AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        if(mAudioManager.isMusicActive()) {
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
            mAudioManager.dispatchMediaKeyEvent(event);
        }else{
            Toast.makeText(this, NO_MUSIC_MSG, Toast.LENGTH_SHORT);
        }
    }

    private void pauseMusic() {
        AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        if(mAudioManager.isMusicActive()) {
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);
            mAudioManager.dispatchMediaKeyEvent(event);
        }
        else{
            Toast.makeText(this, NO_MUSIC_MSG, Toast.LENGTH_SHORT);
        }

    }

    private void playMusic() {
        AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        if(!mAudioManager.isMusicActive()) {
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
            mAudioManager.dispatchMediaKeyEvent(event);
        }else {
            Toast.makeText(this, NO_MUSIC_MSG, Toast.LENGTH_SHORT);
        }
    }

    private void playArtist(IntentMessage intent)
    {
        if(intent!= null)
        {
            List<Slot> slots = intent.getSlots();
            for(Slot slot : slots){
                if(slot.getSlotName().equals("artist") && slot.getValue()!=null)
                {
                    String artist = slot.getRawValue();

                    //new GetDeezerIdTask().execute(artist);
                    String artistDeezerId = DeezerApiConnector.getArtistDeezerID(artist);

                    Uri uri = Uri.parse("https://www.deezer.com/artist/"+artistDeezerId+"?autoplay=true");
                    Intent deezerIntent = new Intent(Intent.ACTION_VIEW, uri);

                    // Verify it resolves
                    PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> activities = packageManager.queryIntentActivities(deezerIntent, 0);
                    boolean isIntentSafe = activities.size() > 0;

                    // Start an activity if it's safe
                    if (isIntentSafe) {
                        startActivity(deezerIntent);
                    }

                }
            }
        }
    }

    private void notImplementedToastDisplay() {
        Toast.makeText(this, NOT_IMPLEMENTED_MSG, Toast.LENGTH_SHORT);
    }



}
