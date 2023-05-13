/*
Sunny Li
Final Project

A music player program that utilizes the javax.sound.sampled library to play WAV audio files.
It includes a user interface, represented by the MusicPlayerGUI class, and a MusicPlayer class
that handles the playback and control of the audio. The program reads music and cover files from
specified folders and uses a Clip object to play the audio. The program runs on a background
thread and includes methods to respond to user input through the interface, such as playing,
pausing, and skipping songs. The current position of the audio is displayed on a progress bar,
which is updated every second. The song can also be fast-forwarded or rewinded by clicking on
the progress bar. The program also includes a shutdown hook to gracefully terminate
the thread when the program is closed.

To run:
	javac MusicPlayer.java
	javac MusicPlayerGUI.java
	java MusicPlayer
*/

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MusicPlayer implements Runnable {
	// Create a MusicPlayerGUI object for the user interface
	static MusicPlayerGUI player = new MusicPlayerGUI();

	// Initialize variables to store the current song information
	static Long currentFrame;
	static Clip clip;
	static String status;
	static int counter = 0;

	// Initialize variables for the audio and cover file paths and folders
	static AudioInputStream audioInputStream;
	static String musicFolderPath;
	static File musicFolder;
	static File[] musicFiles;
	static String musicFilePath;
	static String coverFolderPath;
	static File coverFolder;
	static File[] coverFiles;
	static String coverFilePath;

	// Constructor to initialize the audio input stream and clip
	public MusicPlayer() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// Create an AudioInputStream object from the music file
		audioInputStream = AudioSystem.getAudioInputStream(new File(musicFilePath).getAbsoluteFile());

		// Create a Clip object to play the audio
		clip = AudioSystem.getClip();

		// Open the audio input stream and attach it to the Clip object
		clip.open(audioInputStream);
	}

	@Override
	public void run() {
		// Thread that handles playing the music
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(10);
				// Play the next song if the current song is finished
				autoNextSong();
				Thread.sleep(10);
				// Update the progress bar approximately every second
				updateProgress();
				Thread.sleep(10);
				// Respond to button presses and seeking
				buttonResponder();
			} catch (UnsupportedAudioFileException e) {
			} catch (IOException e) {
			} catch (LineUnavailableException e) {
			} catch (InterruptedException e) {}
		}
	}

	// This method is called by the background thread to update the progress bar
	public synchronized void updateProgress() {
		player.updateProgressBar(clip.getMicrosecondPosition(), clip.getMicrosecondLength());
	}

	// This method is called by the background thread to automatically play the next song
	public synchronized void autoNextSong() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (!clip.isActive() && !status.equals("paused")) {
			next();
			player.updateImage(coverFilePath);
			player.updateTitle(coverFiles[counter].getName());
		}
	}

	// This method is called by the background thread to respond to user input
	public synchronized void buttonResponder() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (!player.jobFinished()) {
			if (player.isSeek()) {
				jump(player.getJump());
			} else if (player.isPlay() && !status.equals("playing")) {
				resumeAudio();
			} else if (player.isPause()) {
				pause();
			} else if (player.isBack()) {
				back();
				player.updateImage(coverFilePath);
				player.updateTitle(coverFiles[counter].getName());
			} else if (player.isNext()) {
				next();
				player.updateImage(coverFilePath);
				player.updateTitle(coverFiles[counter].getName());
			}
			player.finishedJob();
		}
	}

	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		try {
			// Set the path to the music and cover folders
			musicFolderPath = "Songs (WAV)";
			musicFolder = new File(musicFolderPath);
			musicFiles = musicFolder.listFiles();

			// Print out the list of songs in the music folder
			System.out.println("Songs in music folder:");
			for (File file : musicFiles) {
				if (file.isFile())
					System.out.println("\t" + file.getName());
			}

			// Set the path to the cover folder and select the first cover image
			coverFolderPath = "Covers";
			coverFolder = new File(coverFolderPath);
			coverFiles = coverFolder.listFiles();
			coverFilePath = "Covers\\" + coverFiles[counter].getName();

			// Set the path to the first song and update the GUI with the cover image and song title
			musicFilePath = "Songs (WAV)\\" + musicFiles[counter].getName();
			player.updateImage(coverFilePath);
			player.updateTitle(coverFiles[counter].getName());

			// Create a new MusicPlayer object and start the audio playback on separate threads
			MusicPlayer audioPlayer = new MusicPlayer();
			Thread thread0 = new Thread(audioPlayer);
			Thread thread1 = new Thread(audioPlayer);
			Thread thread2 = new Thread(audioPlayer);

			// Set up a shutdown hook to gracefully terminate the threads when the program is terminated
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				thread0.interrupt();
			}));
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				thread1.interrupt();
			}));
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				thread2.interrupt();
			}));

			// Start the background threads and audio playback
			thread0.start();
			thread1.start();
			thread2.start();
			audioPlayer.play();
		} catch (Exception e) {
			System.out.println("Error playing song.");
			e.printStackTrace();
		}
	}
	
	// Method to play the audio
	public void play() {
		//start the clip
		status = "playing";
		clip.start();
	}
	
	// Method to pause the audio
	public void pause() {
		status = "paused";
		this.currentFrame = this.clip.getMicrosecondPosition();
		clip.stop();
	}
	
	// Method to resume the audio
	public void resumeAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		status = "playing";
		clip.close();
		resetAudioStream();
		clip.setMicrosecondPosition(currentFrame);
		this.play();
	}
	
	// Method to stop the audio
	public void stop() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		currentFrame = 0L;
		clip.stop();
		clip.close();
	}
	
	// Method to jump over a specific part
	public void jump(int percentage) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		Long c = Long.valueOf((long) ((percentage / 100.0) * clip.getMicrosecondLength()));
		clip.stop();
		clip.close();
		resetAudioStream();
		currentFrame = c;
		clip.setMicrosecondPosition(c);
		this.play();
	}
	
	// Method to reset audio stream
	public void resetAudioStream() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		audioInputStream = AudioSystem.getAudioInputStream(new File(musicFilePath).getAbsoluteFile());
		clip.open(audioInputStream);
	}

	// Method to play next song
	public void next() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		clip.stop();
		clip.close();
		counter = (counter + 1) % musicFiles.length;
		musicFilePath = "Songs (WAV)\\" + musicFiles[counter].getName();
		coverFilePath = "Covers\\" + coverFiles[counter].getName();
		audioInputStream = AudioSystem.getAudioInputStream(new File(musicFilePath).getAbsoluteFile());
		clip = AudioSystem.getClip();
		clip.open(audioInputStream);
		play();
	}

	// Method to play previous song
	public void back() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		clip.stop();
		clip.close();
		counter = (counter+musicFiles.length-1) % musicFiles.length;
		musicFilePath = "Songs (WAV)\\" + musicFiles[counter].getName();
		coverFilePath = "Covers\\" + coverFiles[counter].getName();
		audioInputStream = AudioSystem.getAudioInputStream(new File(musicFilePath).getAbsoluteFile());
		clip = AudioSystem.getClip();
		clip.open(audioInputStream);
		play();
	}
}
