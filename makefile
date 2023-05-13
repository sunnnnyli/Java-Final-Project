all: MusicPlayer.java MusicPlayerGUI.java
	javac MusicPlayer.java
	javac MusicPlayerGUI.java
run:
	java MusicPlayer
clean:
	rm *.class
