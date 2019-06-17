package bg.sofia.uni.fmi.mjt.spotify;

public enum Commands {

	REGISTER("register"),
	LOGIN("login"),
	DISCONNECT("disconnect"),
	TOP("top"),
	SEARCH("search"),
	CREATE_PLAYLIST("create-playlist"),
	ADD_SONG_TO_PLAYLIST("add-song-to"),
	SHOW_PLAYLIST("show-playlist"),
	PLAY("play"),
	STOP("stop");

	private String commandName;
	
	private Commands(String commandName) {
		this.commandName = commandName;
	}
	
	public String getCommandName() {
		return commandName;
	}
}
