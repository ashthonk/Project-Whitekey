package com.projectwhitekey;
/**
 * Adopted by Project Whitekey from publicly available code. 
 * This class handles the actual "playing" of the note. It receives a desired filename from the Keyboard class (which gets passed on a keytouch event)
 * We've used the AudioTrack implementation, instead of the MediaPlayer implementation as AudioTrack is less of a memory hog when it comes to playing multiple files 
 * quickly or simultaneously.
 * Originally by Martin Hoeller, adapted by Jace to work specifically for Project Whitekey
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioTrackSoundPlayer
{
	private HashMap<String, PlayThread> threadMap = null;
	private Context context;
	private int bytesRead;
	private long bytesWritten;
	private int headerOffset = 0x2C;
	public AudioTrackSoundPlayer(Context context)
	{
		this.context = context;
		threadMap = new HashMap<String, AudioTrackSoundPlayer.PlayThread>();
	}

	
	public void playNote(String note)
	{
		if (!isNotePlaying(note))
		{
			PlayThread thread = new PlayThread(note);
			thread.start();
			threadMap.put(note, thread);
		}
	}

	public void stopNote(String note)
	{
		PlayThread thread = threadMap.get(note);
		if (thread != null)
		{
			thread.requestStop();
			threadMap.remove(note);
		}
	}

	public boolean isNotePlaying(String note)
	{
		return threadMap.containsKey(note);
	}

	private class PlayThread extends Thread
	{
		String note;
		boolean stop = false;
		AudioTrack audioTrack = null;

		public PlayThread(String note)
		{
			super();
			this.note = note;
		}

		public void run()
		{
			try
			{
				//Construct the file path name 
				String path = note + ".wav"; 
				
				//Retrieve the file from the assets folder
				AssetManager assetManager = context.getAssets();
				AssetFileDescriptor ad = assetManager.openFd(path);
				long fileSize = ad.getLength();
				
				//Create a buffer to play the music. We get an adequate buffer by multiplying the minimum buffer size for the desired settings
				//which are, in our case 44100Hz Mono using PCM 16BIT Encoding (to save space) by 2
				int bufferSize = 2 * AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);//Create an audio buffer
				byte[] buffer = new byte[bufferSize];
				
				//This handles sound quality. Played at 44100 @ Mono PCM 16 bit to conserve memory resources. Will update the Encoding to 24BIT upon Android Support
				audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

				InputStream audioStream = null;

				
				bytesWritten = 0;
				bytesRead = 0;
				audioTrack.play();
					audioStream = assetManager.open(path);
					audioStream.read(buffer, 0, headerOffset);
					
					//Read until end of file, or until the user moves his/her finger off the screen. If we include effects later on, remove this one. 
					while (!stop && bytesWritten < fileSize - headerOffset)
					{
						bytesRead = audioStream.read(buffer, 0, bufferSize);
						bytesWritten += audioTrack.write(buffer, 0, bytesRead);
					}
				//}
				//Stop playing sound and reset the track
				audioTrack.stop();
				audioTrack.flush();
				audioTrack.release();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}
		
		//Called when the user moves his/her finger off the tablet
		public synchronized void requestStop()
		{
			stop = true;
		}
	}
}
