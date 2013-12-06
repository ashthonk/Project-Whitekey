package com.projectwhitekey;

/**
 * The "Core" of Project Whitekey. Here, we handle much of the core functionality and initialization. 
 * We use an "On Touch" event handling system to take care of the keyboard note presses and audio playback together with the AudioTrackSoundPlayer. 
 * On initialization, we create an array of Views to handle the button events, and we build the keyboard through user defined settings in the Prefs menu 
 * Jace: Initialization and handling of the soundPlayer. Button Events using OnTouch implementation. Calls to AudioTrackSoundPlayer Class. getPrefs method. 
 * Andrew: Initialization of View array, calls to Note and Scale classes, remapping of keyboard upon closure of Preferences menu
 */
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class Keyboard extends Activity {

	private AudioTrackSoundPlayer	soundPlayer			= null;
	private View[]					keys				= null;
	private boolean[]				buttonStates		= null;
	private Scale					keybScale;						// The scale the keyboard is currently in.
	private Note[]					everyNote;						// Stores every note availible.
	private Note[]					currentScale;					// "A" by default
	private int						rootNum				= 1;
	private int						octavenum			= 2;
	private int						notefile			= 1;
	private String					ScaleName;						// Taken from prefs
	private String					RootNote;						// Taken from prefs
	private String					Octave;						// Taken from prefs
	private MediaPlayer				mp;
	private Context					appContext;
	private Boolean					currentlyPlaying	= false;

	// Called when the Activity is First Created (Only happens once)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_keyboard);

		appContext = getApplicationContext();
		mp = MediaPlayer.create(appContext, R.raw.whitekey);

		// Set up each visual button in an array of their views from lowest to highest
		keys = new View[20];
		keys[0] = findViewById(R.id.keyLow0);
		keys[1] = findViewById(R.id.keyLow1);
		keys[2] = findViewById(R.id.keyLow2);
		keys[3] = findViewById(R.id.keyLow3);
		keys[4] = findViewById(R.id.keyLow4);
		keys[5] = findViewById(R.id.keyLow5);
		keys[6] = findViewById(R.id.keyLow6);
		keys[7] = findViewById(R.id.keyLow7);
		keys[8] = findViewById(R.id.keyLow8);
		keys[9] = findViewById(R.id.keyLow9);
		keys[10] = findViewById(R.id.keyMid0);
		keys[11] = findViewById(R.id.keyMid1);
		keys[12] = findViewById(R.id.keyMid2);
		keys[13] = findViewById(R.id.keyMid3);
		keys[14] = findViewById(R.id.keyMid4);
		keys[15] = findViewById(R.id.keyMid5);
		keys[16] = findViewById(R.id.keyMid6);
		keys[17] = findViewById(R.id.keyMid7);
		keys[18] = findViewById(R.id.keyMid8);
		keys[19] = findViewById(R.id.keyMid9);

		// Establish the full range of the keyboard and perform the initial mapping of the Keyboard
		Note rootNotes = new Note(notefile);
		everyNote = new Note[88];
		for (int i = 0; i < 88; i++)
			everyNote[i] = new Note(i);

		// Actually get the desired notes to map the keyboard. Mapping takes place further down
		keybScale = new Scale(rootNotes, Scale.scaleType.pentatonic_major, everyNote);
		currentScale = keybScale.getScaleElements(20);

		// All buttons take an active or inactive status, used when a key is targeted. Play is called using this array.This is also the "magic" behind
		// "multitouch"
		buttonStates = new boolean[keys.length];

		// Create the Soundplayer to handle note presses and sound playback
		soundPlayer = new AudioTrackSoundPlayer(this);

		// OnClick event for the settings button
		Button prefBtn = (Button) findViewById(R.id.SettingsButton);
		prefBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) { // OnClick, load the settings window
				Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
				startActivity(settingsActivity);
			}
		});

		// OnClick event for the Backtrack Play button
		Button startBtn = (Button) findViewById(R.id.PlayButton);
		startBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				// Create the Mediaplayer to handle the background track which is triggered by the play button
				appContext = getApplicationContext();
				mp.setLooping(true);

				Spinner backTrackSpinner = (Spinner) findViewById(R.id.songChooser);
				// Get the currently selected backtrack
				String currentBackTrack = backTrackSpinner.getSelectedItem().toString();
				int rawSongId;

				if (currentBackTrack.equals("Jazz drums"))
					{
						rawSongId = R.raw.whitekey;
					}

				else if (currentBackTrack.equals("Happiest Man"))
					{
						rawSongId = R.raw.happiestman;
					} else if (currentBackTrack.equals("Smooth Jam (short)"))
					{
						rawSongId = R.raw.smoothjamshort;
					} else if (currentBackTrack.equals("Smooth Jam (long)"))
					{
						rawSongId = R.raw.smoothjamlong;
					} else
					{
						System.out.println("none of these options were valid.");
						rawSongId = R.raw.whitekey;
					}
				// mp = MediaPlayer.create(appContext, rawSongId);

				try
					{
						mp.reset();
						AssetFileDescriptor afd = appContext.getResources().openRawResourceFd(rawSongId);
						mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
						afd.close();
						mp.prepare();

						System.out.println("Attempting to reset MP");
						// mp.reset();

					} catch (Exception e)
					{
						System.err.println("mp.SetdataSource Failure:");
						e.printStackTrace();
					}

				if (currentlyPlaying)
					{
						mp.pause();
						System.out.println("BACKTRACK pausing!");
						currentlyPlaying = false;
					} else
					{
						mp.start();
						System.out.println("BACKTRACK starting!");
						currentlyPlaying = true;
					}
			}
		});

	}

	// Called whenever there is a detected "tap" on one of the piano keys
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Change the value of the elements in the array based on how many keys are displayed in the layout.
		boolean[] newButtonStates = new boolean[20];

		int action = event.getAction();
		boolean isDownAction = (action & 0x5) == 0x5 || action == MotionEvent.ACTION_DOWN;
				//|| action == MotionEvent.ACTION_MOVE;
		// For loop runs for each detected point of contact on the device
		if (isDownAction)
			{
				for (int touchIndex = 0; touchIndex < event.getPointerCount(); touchIndex++)
					{ //
						// Find the key where there is detected movement.
						int x = (int) event.getX(touchIndex);
						int y = (int) event.getY(touchIndex);
						for (int buttonIndex = 0; buttonIndex < keys.length; buttonIndex++)
							{
								View button = keys[buttonIndex];
								int[] location = new int[2];
								button.getLocationOnScreen(location);
								int buttonX = location[0];
								int buttonY = location[1];
								// Draw a rectangle to determine if the touch took place inside of a button, and then pass the action
								Rect rect = new Rect(buttonX, buttonY, buttonX + button.getWidth(), buttonY
										+ button.getHeight());
								if (rect.contains(x, y))
									{
										newButtonStates[buttonIndex] = isDownAction;
										buttonIndex = keys.length + 1;
									}
							}
						// If touch was detected within a button, play its respective sound
						for (int index = 0; index < newButtonStates.length; index++)
							{
								if (buttonStates[index] != newButtonStates[index])
									{
										buttonStates[index] = newButtonStates[index];
										View button = keys[index];
										toggleButtonSound(button, newButtonStates[index]);
									}
							}
					}
			}
		else if(event.getAction() == MotionEvent.ACTION_UP)
			{
				for (int touchIndex = 0; touchIndex < event.getPointerCount(); touchIndex++)
					{ //
						// Find the key where there is detected movement.
						int x = (int) event.getX(touchIndex);
						int y = (int) event.getY(touchIndex);
						for (int buttonIndex = 0; buttonIndex < keys.length; buttonIndex++)
							{
								View button = keys[buttonIndex];
								int[] location = new int[2];
								button.getLocationOnScreen(location);
								int buttonX = location[0];
								int buttonY = location[1];
								// Draw a rectangle to determine if the touch took place inside of a button, and then pass the action
								Rect rect = new Rect(buttonX, buttonY, buttonX + button.getWidth(), buttonY
										+ button.getHeight());
								if (rect.contains(x, y))
									{
										newButtonStates[buttonIndex] = isDownAction;
										buttonIndex = keys.length + 1;
									}
							}
						// If touch was detected within a button, play its respective sound
						for (int index = 0; index < newButtonStates.length; index++)
							{
								System.out.println("Index is" + index);
								if (buttonStates[index] != newButtonStates[index])
									{
										buttonStates[index] = newButtonStates[index];
										View button = keys[index];
										toggleButtonSound(button, !newButtonStates[index]);
									}
							}
					}
			}
		return true;
	}

	// Call the proper file within the AudioTrackSoundPlayer class
	// This function fetches the filename of the sound to be played from the Note objects.
	private void toggleButtonSound(View button, boolean down) {
		String note = null;
		switch (button.getId()) {
			case R.id.keyLow0:
				note = Integer.toString(currentScale[10].getPitch());
				break;
			case R.id.keyLow1:
				note = Integer.toString(currentScale[11].getPitch());
				break;
			case R.id.keyLow2:
				note = Integer.toString(currentScale[12].getPitch());
				break;
			case R.id.keyLow3:
				note = Integer.toString(currentScale[13].getPitch());
				break;
			case R.id.keyLow4:
				note = Integer.toString(currentScale[14].getPitch());
				break;
			case R.id.keyLow5:
				note = Integer.toString(currentScale[15].getPitch());
				break;
			case R.id.keyLow6:
				note = Integer.toString(currentScale[16].getPitch());
				break;
			case R.id.keyLow7:
				note = Integer.toString(currentScale[17].getPitch());
				break;
			case R.id.keyLow8:
				note = Integer.toString(currentScale[18].getPitch());
				break;
			case R.id.keyLow9:
				note = Integer.toString(currentScale[19].getPitch());
				break;
			case R.id.keyMid0:
				note = Integer.toString(currentScale[0].getPitch());
				break;
			case R.id.keyMid1:
				note = Integer.toString(currentScale[1].getPitch());
				break;
			case R.id.keyMid2:
				note = Integer.toString(currentScale[2].getPitch());
				break;
			case R.id.keyMid3:
				note = Integer.toString(currentScale[3].getPitch());
				break;
			case R.id.keyMid4:
				note = Integer.toString(currentScale[4].getPitch());
				break;
			case R.id.keyMid5:
				note = Integer.toString(currentScale[5].getPitch());
				break;
			case R.id.keyMid6:
				note = Integer.toString(currentScale[6].getPitch());
				break;
			case R.id.keyMid7:
				note = Integer.toString(currentScale[7].getPitch());
				break;
			case R.id.keyMid8:
				note = Integer.toString(currentScale[8].getPitch());
				break;
			case R.id.keyMid9:
				note = Integer.toString(currentScale[9].getPitch());
				break;
		}

		// Only play a note if it is not already playing
		if (down && !soundPlayer.isNotePlaying(note))
			soundPlayer.playNote(note);
		else
			soundPlayer.stopNote(note);
	}

	// Called whenever the user closes the app, or loads the settings menu. As of right now, we only stop the backing track
	@Override
	public void onStop() {
		super.onStop();
		mp.pause();
	}

	// Called each time the user sees the keyboard layout (e.g. if the user goes to the settings and goes back)
	// Update the keyboard mappings with the user's desired Scale/Pitch/Root Note
	@Override
	public void onStart() {
		super.onStart();
		getPrefs();
		Note rootNotes = new Note(notefile);
		if (ScaleName.equals("Pentatonic Major"))
			keybScale = new Scale(rootNotes, Scale.scaleType.pentatonic_major, everyNote);
		else if (ScaleName.equals("Pentatonic Minor"))
			keybScale = new Scale(rootNotes, Scale.scaleType.pentatonic_minor, everyNote);
		else if (ScaleName.equals("Major"))
			keybScale = new Scale(rootNotes, Scale.scaleType.major, everyNote);
		else if (ScaleName.equals("Minor Natural"))
			keybScale = new Scale(rootNotes, Scale.scaleType.minor_natural, everyNote);
		else if (ScaleName.equals("Minor Harmonic"))
			keybScale = new Scale(rootNotes, Scale.scaleType.minor_harmonic, everyNote);
		else if (ScaleName.equals("Minor Melodic"))
			keybScale = new Scale(rootNotes, Scale.scaleType.minor_melodic, everyNote);
		else if (ScaleName.equals("Gypsy Minor"))
			keybScale = new Scale(rootNotes, Scale.scaleType.gypsy_minor, everyNote);
		else if (ScaleName.equals("Gypsy Major"))
			keybScale = new Scale(rootNotes, Scale.scaleType.gypsy_major, everyNote);
		else
			System.out.println("NO MATCH!");

		// Change the value of the param based on how many keys are displayed in the layout
		currentScale = keybScale.getScaleElements(20);
	}

	// Retrieve the settings from the Preferences menu
	private void getPrefs() {

		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		ScaleName = prefs.getString("listPref", "nr1");
		RootNote = prefs.getString("listPrefRN", "1");
		Octave = prefs.getString("listPrefO", "12");
		rootNum = Integer.parseInt(RootNote);
		octavenum = Integer.parseInt(Octave);

		// Based on the octave and root note, determine the position of the root note in the array of 88 piano notes
		notefile = (octavenum - 1) * 12 + rootNum;
	}
}
