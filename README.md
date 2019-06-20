# Spotify

This is a client-server spotify-like system for streaming songs.

## Getting Started

Clone this repository to your computer:

`git clone https://github.com/nbratanov/spotify`

## Prerequisites

In order to run this project you need to have JRE 11+ . You can install it on: 
[https://www.oracle.com/technetwork/java/javase/downloads/index.html](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

## Running the program

1. Run bg.sofia.uni.fmi.mjt.spotify.SpotifyServer
2. Connect multiple clients from bg.sofia.uni.fmi.mjt.spotify.SpotifyClient

## Usage

1. **Register** and **login** through the system with:

	*register [name] [password]
	login [name] [password]*
	
2. **Search** different songs in your local file with path:

	*".../Spotify/resources/songs"*

	Command for search is:

	*search [keywords]*
	
3. **Play** song with its full name:

	*play [name of song]*
