////////////////////////////////////////////////////////////////////////////////
//
//  TDR - An Android Time Domain Reflectometer written in Java.
//
//  Copyright (C) 2014	Bill Farmer
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Bill Farmer	 william j farmer [at] yahoo [dot] co [dot] uk.
//
///////////////////////////////////////////////////////////////////////////////

package org.billthefarmer.tdr;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

// MainActivity
public class Main extends Activity
{
    private static final String PREF_SCREEN = "pref_screen";
    private static final String PREF_DARK = "pref_dark";

    private static final String TAG = "TDR";

    private static final int VERSION_M = 23;

    protected static final int SIZE = 20;
    protected static final int DEFAULT_RANGE = 3;
    protected static final float SCALE = 20;

    private static final float values[] =
    {
        0.1f, 0.2f, 0.5f, 1.0f,
        2.0f, 5.0f, 10.0f
    };

    private static final int counts[] =
    {
        256, 512, 1024, 2048,
        4096, 8192, 16384
    };

    private Scope scope;
    private XScale xscale;
    private YScale yscale;
    private Unit unit;

    private Audio audio;
    private Toast toast;
    private SubMenu submenu;

    private boolean dark;
    private boolean screen;

    private int range;

    // On create
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get preferences
        getPreferences();

        if (dark)
            setTheme(R.style.AppDarkTheme);

        setContentView(R.layout.main);

        scope = findViewById(R.id.scope);
        xscale = findViewById(R.id.xscale);
        yscale = findViewById(R.id.yscale);
        unit = findViewById(R.id.unit);

        // Get action bar
        ActionBar actionBar = getActionBar();

        // Create audio
        audio = new Audio();

        if (scope != null)
            scope.audio = audio;
    }

    // onCreateOptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuItem item;

        // Inflate the menu; this adds items to the action bar if it
        // is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    // Restore state
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    // Save state
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    // On options item
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Get id
        int id = item.getItemId();
        switch (id)
        {
        case R.id.r10m:
            range = 0;
            item.setChecked(true);
            setRange(range);
            break;

        case R.id.r20m:
            range = 1;
            item.setChecked(true);
            setRange(range);
            break;

        case R.id.r50m:
            range = 2;
            item.setChecked(true);
            setRange(range);
            break;

        case R.id.r100m:
            range = 3;
            item.setChecked(true);
            setRange(range);
            break;

        case R.id.r200m:
            range = 4;
            item.setChecked(true);
            setRange(range);
            break;

        case R.id.r500m:
            range = 5;
            item.setChecked(true);
            setRange(range);
            break;

        case R.id.r1000m:
            range = 6;
            item.setChecked(true);
            setRange(range);
            break;

        default:
        }

        return super.onOptionsItemSelected(item);
    }

    // On Resume
    @Override
    protected void onResume()
    {
        super.onResume();

        boolean theme = dark;

        // Get preferences
        getPreferences();

        if (theme != dark && Build.VERSION.SDK_INT != VERSION_M)
            recreate();

        // Start the audio thread
        audio.start();
    }

    // On pause
    @Override
    protected void onPause()
    {
        super.onPause();

        // Save preferences
        savePreferences();

        // Stop audio thread
        audio.stop();
    }

    // Set range
    void setRange(int range)
    {
        if (scope != null && xscale != null && unit != null)
        {
            // Set up scale
            scope.scale = values[range];
            xscale.scale = scope.scale;
            xscale.step = 1000 * xscale.scale;
            unit.scale = scope.scale;

            // Set up scope points
            scope.points = (range == 0);

            // Reset start
            scope.start = 0;
            xscale.start = 0;

            // Update display
            xscale.postInvalidate();
            unit.postInvalidate();
        }
    }

    // Get preferences
    void getPreferences()
    {
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);

        boolean screen = preferences.getBoolean(PREF_SCREEN, false);

        // Check screen
        Window window = getWindow();
        if (screen)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        dark = preferences.getBoolean(PREF_DARK, false);
    }

    // Save preferences
    void savePreferences()
    {
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);

        // TODO
    }

    // Show alert
    void showAlert(int appName, int errorBuffer)
    {
        // Create an alert dialog builder
        AlertDialog.Builder builder =
            new AlertDialog.Builder(this);

        // Set the title, message and button
        builder.setTitle(appName);
        builder.setMessage(errorBuffer);
        builder.setNeutralButton(android.R.string.ok,
                                 (dialog, which) ->
        {
            // Dismiss dialog
            dialog.dismiss();
        });

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Show it
        dialog.show();
    }

    // Show toast
    void showToast(int key)
    {
        Resources resources = getResources();
        String text = resources.getString(key);

        showToast(text);
    }

    // Show toast
    void showToast(String text)
    {
        // Cancel the last one
        if (toast != null)
            toast.cancel();

        // Make a new one
        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // Audio
    protected class Audio implements Runnable
    {
        // Preferences
        protected int input;
        protected int sample;

        // Data
        protected Thread thread;
        protected short data[];
        protected long length;

        // Private data
        private static final int SAMPLES = 524288;
        private static final int FRAMES = 4096;

        private static final int INIT = 0;
        private static final int FIRST = 1;
        private static final int NEXT = 2;
        private static final int LAST = 3;

        private AudioRecord audioRecord;
        private short buffer[];

        // Constructor
        protected Audio()
        {
            buffer = new short[FRAMES];
            data = new short[SAMPLES];
        }

        // Start audio
        protected void start()
        {
            // Start the thread
            thread = new Thread(this, "Audio");
            thread.start();
        }

        // Run
        @Override
        public void run()
        {
            processAudio();
        }

        // Stop
        protected void stop()
        {
            // Stop and release the audio recorder
            cleanUpAudioRecord();

            Thread t = thread;
            thread = null;

            // Wait for the thread to exit
            while (t != null && t.isAlive())
                Thread.yield();
        }

        // Stop and release the audio recorder
        private void cleanUpAudioRecord()
        {
            if (audioRecord != null &&
                    audioRecord.getState() == AudioRecord.STATE_INITIALIZED)
            {
                try
                {
                    if (audioRecord.getRecordingState() ==
                            AudioRecord.RECORDSTATE_RECORDING)
                        audioRecord.stop();

                    audioRecord.release();
                }
                catch (Exception e)
                {
                }
            }
        }

        // Process Audio
        protected void processAudio()
        {
            // Assume the output sample rate will work on the input as
            // there isn't an AudioRecord.getNativeInputSampleRate()
            sample =
                AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);

            // Get buffer size
            int size =
                AudioRecord.getMinBufferSize(sample,
                                             AudioFormat.CHANNEL_IN_MONO,
                                             AudioFormat.ENCODING_PCM_16BIT);
            // Give up if it doesn't work
            if (size == AudioRecord.ERROR_BAD_VALUE ||
                    size == AudioRecord.ERROR ||
                    size <= 0)
            {
                runOnUiThread(() -> showAlert(R.string.app_name,
                                              R.string.error_buffer));
                thread = null;
                return;
            }

            // Create the AudioRecord object
            try
            {
                audioRecord =
                    new AudioRecord(input, sample,
                                    AudioFormat.CHANNEL_IN_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT,
                                    size);
            }

            // Exception
            catch (Exception e)
            {
                runOnUiThread(() -> showAlert(R.string.app_name,
                                              R.string.error_init));
                thread = null;
                return;
            }

            // Check audiorecord
            // Check state
            int state = audioRecord.getState();

            if (state != AudioRecord.STATE_INITIALIZED)
            {
                runOnUiThread(() -> showAlert(R.string.app_name,
                                              R.string.error_init));

                audioRecord.release();
                thread = null;
                return;
            }

            // Start recording
            audioRecord.startRecording();

            int index = 0;
            int count = 0;

            state = INIT;
            short last = 0;

            // Continue until he thread is stopped
            while (thread != null)
            {
                // Read a buffer of data
                size = audioRecord.read(buffer, 0, FRAMES);

                // Stop the thread if no data or error state
                if (size <= 0)
                {
                    thread = null;
                    break;
                }

                // Update display
                scope.postInvalidate();
            }

            // Stop and release the audio recorder
            cleanUpAudioRecord();
        }
    }
}
