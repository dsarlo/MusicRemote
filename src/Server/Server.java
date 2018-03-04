package Server;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;


public class Server implements Runnable
{
	
	private ServerSocket serverSocket;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream _out;
	
	private PlaybackListener _shuffleListener;
	private AdvancedPlayer player;
	
	private ArrayList<File> _songsInLibrary;
	private ExecutorService _service;
	
	private String operatingSystem = System.getProperty("os.name").toLowerCase();
	
	private Iterator<String> _shuffleQueue;
	
	private boolean _shuffle = false;
	private boolean _musicPlaying = false;
	
	private ServerStartClickHandler _serv;
	
	public Server(ServerStartClickHandler ServerRef)
	{
		_serv = ServerRef;
		_songsInLibrary = new ArrayList<File>();
		File selectedMusicLibrary = new File(_serv.getPath());
		getListOfSongsInLibrary(selectedMusicLibrary);
	}
	
	@Override
	public void run()
	{
		try 
		{
			serverSocket = new ServerSocket(7777);
			_serv.serverGui.appendText("SERVER LOG:\n\n\n");
			_serv.serverGui.appendText("Server Started!\nServer Address = " + InetAddress.getLocalHost().getHostAddress());
			socket = serverSocket.accept();
			_serv.serverGui.appendText("Client: " + socket.getInetAddress().toString() + " has connected\n\n\n");
			in = new DataInputStream(socket.getInputStream());
			_out = new DataOutputStream(socket.getOutputStream());
			
			while(true)
			{
				_service = Executors.newFixedThreadPool(4);
				
				if(in.readInt() == 1)
				{
					String readText = in.readUTF();
					System.out.println(readText);
					switch(readText)
					{
						case "!!":
							_serv.serverGui.appendText("Server Closed!");
							_out.writeUTF("Server Closed!");
							System.exit(0);
							break;
						case "||":
							String clientMessage = "No Music Currently Playing!";
							if(_musicPlaying)
							{
								stopMP3();
								clientMessage = "Music Stopped!";
								
								if(_shuffle) _shuffleQueue = null;
							}
							_serv.serverGui.appendText(clientMessage);
							_out.writeUTF(clientMessage);
							break;
						case "/list":
							showSongs();
							break;
						case "?":
							_serv.serverGui.appendText("Displayed Help Info");
							String help = "!! - Closes the server\n|| - Stops the current song\n/list - Lists all songs in library\n/s <SONGNAME> - Searches and displays songs with the typed phrases\n/shuffle creates a randomly shuffled playlist for you and begins playing it";
							_out.writeUTF(help);
							break;
						case "/shuffle":
							if(_musicPlaying)
							{
								stopMP3();
							}
							shufflePlaylist(createPlaylist());
							_serv.serverGui.appendText("Shuffled Songs");
					    	_out.writeUTF("Shuffled Songs");
							break;
						case ">>":
							if(_musicPlaying && _shuffle)
							{
								stopMP3();
								if(_shuffleQueue.hasNext())
								{
									String nextSong = _shuffleQueue.next();
									String nextSongFilename = formatFilenameForOS(nextSong);
									_service.submit(new Runnable()
									{
										public void run()
										{
											playMP3(nextSongFilename, true, _shuffleListener);
										}
									});
									_musicPlaying = true;
							    	_shuffle = true;
									_serv.serverGui.appendText("Current song: " + nextSongFilename);
									_out.writeUTF("Current song: " + nextSongFilename);
								}
								else
								{
									_serv.serverGui.appendText("No songs left in queue (Shuffle again?)");
									_out.writeUTF("No songs left in queue (Shuffle again?)");
								}
							}
							else
							{
								_serv.serverGui.appendText("No songs left in queue (Shuffle again?)");
								_out.writeUTF("No songs left in queue (Shuffle again?)");
							}
							break;
						default:
							if(readText.contains("/s "))//Search logic
							{
								_serv.serverGui.appendText("Searched Songs For: " + readText.substring(3));
								_out.writeUTF(searchSongs(readText.substring(3)));
								break;
							}
							
							String nowPlaying = checkFilesForFilename(readText);
						    if(nowPlaying == null)
						    {
						    	_serv.serverGui.appendText("File/Command Not Found!");
						    	_out.writeUTF("File/Command Not Found. Try Again");
						    }
						    else
						    {
						    	if(_musicPlaying)
								{
									stopMP3();
								}
						    	_service.submit(new Runnable()
						    	{
							        public void run() 
							        {
							        	playMP3(nowPlaying);
							        }
							    });
						    	
						    	_musicPlaying = true;
						    	_shuffle = false;
						    	int lastIndexOfNowPlaying = nowPlaying.lastIndexOf("\\");
						    	String filename = nowPlaying.substring(lastIndexOfNowPlaying + 1);
						    	_serv.serverGui.appendText("Played: " + filename);
								_out.writeUTF("Now Playing: " + filename);
						    }
							break;
					}
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void stopMP3()
	{
		player.close();
		_musicPlaying = false;
		_shuffle = false;
		_service.shutdown();
		_service = Executors.newFixedThreadPool(4);
	}
	
	public void playMP3(String songName)
	{
		try 
		{
			FileInputStream fis = new FileInputStream(songName);
			BufferedInputStream bis = new BufferedInputStream(fis);
			player = new AdvancedPlayer(bis);
			player.play();
		} 
		catch (FileNotFoundException | JavaLayerException e) 
		{
			e.printStackTrace();
			_serv.serverGui.appendText("Failed to play the file.");
		}
	}
	
	public void playMP3(String songName, Boolean shuffle, PlaybackListener shuffleListener)
	{
		try 
		{
			FileInputStream fis = new FileInputStream(songName);
			BufferedInputStream bis = new BufferedInputStream(fis);
			player = new AdvancedPlayer(bis);
			if(shuffle)
			{
				player.setPlayBackListener(shuffleListener);
			}
			player.play();
		} 
		catch (FileNotFoundException | JavaLayerException e) 
		{
			e.printStackTrace();
			_serv.serverGui.appendText("Failed to play the file.");
		}
	}
	
	public void getListOfSongsInLibrary(File selectedMusicLibrary)
	{
        File directory = selectedMusicLibrary;
        File[] fList = directory.listFiles();
        
        for (File file : fList)
        {
            if (file.isFile() && file.toString().endsWith(".mp3"))
            {
            	_songsInLibrary.add(new File(file.getAbsolutePath()));
            } 
            else if (file.isDirectory())
            {
            	getListOfSongsInLibrary(file);
            }
        }
    }
	
	private LinkedHashSet<String> createPlaylist()
	{
		LinkedHashSet<String> playlist = new LinkedHashSet<String>();
		Random rand = new Random();
		
		while(playlist.size() != _songsInLibrary.size())
		{
			int randomNumber = rand.nextInt(_songsInLibrary.size());
			playlist.add(_songsInLibrary.get(randomNumber).toString());
		}
		return playlist;
	}
	
	public void shufflePlaylist(LinkedHashSet<String> playlist)
	{
		_shuffleQueue = playlist.iterator();
		if(_shuffleQueue.hasNext())
		{
			String currentSong = _shuffleQueue.next();
			
			String currentSongFilename = formatFilenameForOS(currentSong);
			
			if(_musicPlaying)
			{
				stopMP3();
			}
			
			_shuffleListener = new PlaybackListener()
			{
				@Override
				public void playbackFinished(PlaybackEvent event) 
				{
					stopMP3();
					
					if(_shuffleQueue.hasNext())
					{
						String nextSong = _shuffleQueue.next();
						String nextSongFilename = formatFilenameForOS(nextSong);
						
						_service.submit(new Runnable()
						{
							public void run()
							{
								playMP3(nextSongFilename, true, _shuffleListener);
							}
						});
						
						_musicPlaying = true;
				    	_shuffle = true;
					}
				}
			};
			
			_service.submit(new Runnable()
			{
				public void run()
				{
					playMP3(currentSongFilename, true, _shuffleListener);
				}
			});
			
			_musicPlaying = true;
	    	_shuffle = true;
		}
	}

	private String formatFilenameForOS(String currentSong)
	{
		int substringValue = 0;
		
		if(operatingSystem.indexOf("win") >= 0)
		{
			substringValue = 2;
		}
		
		return currentSong.substring(substringValue);
	}

	public String searchSongs(String searchPhrase)
	{
		String foundSongs = "";
		
		for(int currentSong = 0; currentSong < _songsInLibrary.size(); currentSong++)
		{
			String currentSearchedSong = _songsInLibrary.get(currentSong).toString();
			int lastIndex = currentSearchedSong.lastIndexOf("\\");
			String currentSearchedSongName = currentSearchedSong.substring(lastIndex + 1);
			
			if(currentSearchedSongName.toLowerCase().contains(searchPhrase.toLowerCase()))
			{
				foundSongs += "\n" + currentSong + ": " + currentSearchedSongName;
			}
		}
		
		if(foundSongs.isEmpty())
		{
			foundSongs = "No Items Match Your Search. Try Again";
		}
		
		return foundSongs;
	}
	
	public String checkFilesForFilename(String songName)
	{
		String foundFilename = null;
		
		for(int currentSong = 0; currentSong < _songsInLibrary.size(); currentSong++)
		{
			String currentSearchedSong = _songsInLibrary.get(currentSong).toString().toLowerCase();
			int lastIndex = currentSearchedSong.lastIndexOf("\\");
			
			if(currentSearchedSong.substring(lastIndex + 1).contains(songName))
			{
				foundFilename = formatFilenameForOS(_songsInLibrary.get(currentSong).toString());
				return foundFilename;
			}
		}
		return foundFilename;
	}
	
	public void showSongs()
	{
		String fileNames = "";
		
		for(int currentSong = 0; currentSong < _songsInLibrary.size(); currentSong++)
		{
			String currentSearchedSong = _songsInLibrary.get(currentSong).toString();
			int lastIndex = currentSearchedSong.lastIndexOf("\\");
			fileNames += "\n" + currentSong + ": " + currentSearchedSong.substring(lastIndex + 1);
		}
		
		_serv.serverGui.appendText("Listed Songs");
		String output = fileNames.isEmpty() ? "No Songs Found In Current Directory!" : fileNames;
		
		try
		{
			_out.writeUTF(output);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
