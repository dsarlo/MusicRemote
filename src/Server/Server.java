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
import java.util.HashSet;
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
	private ArrayList<File> searchedSongs = new ArrayList<File>();
	private ExecutorService _service;
	private String operatingSystem = System.getProperty("os.name").toLowerCase();
	Iterator<String> _queue;
	private boolean _shuffle = false;
	private boolean _musicPlaying = false;
	
	private ServerStartClickHandler _serv;
	private File folder;
	
	public Server(ServerStartClickHandler ServerRef)
	{
		_serv = ServerRef;
		folder = new File(_serv.getPath());
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
								if(_shuffle)
								{
									_queue = null;
									_shuffle = false;
								}
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
						case "/s ":
							_serv.serverGui.appendText("Searched Songs For: " + readText.substring(3));
							_out.writeUTF(searchSongs(readText.substring(3)));
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
								if(_queue.hasNext())
								{
									String nextSong = _queue.next();
									String nextSongFilename = formatFilenameForOS(nextSong);
									_service.submit(new Runnable()
									{
										public void run()
										{
											playMP3(nextSongFilename, true, _shuffleListener);
										}
									});
									_musicPlaying = true;
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
	
	public void listFilesAndFilesSubDirectories(String directoryName)
	{
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        
        for (File file : fList)
        {
            if (file.isFile())
            {
            	File copyOfFile = new File(file.getAbsolutePath());
                searchedSongs.add(copyOfFile);
            } 
            else if (file.isDirectory())
            {
                listFilesAndFilesSubDirectories(file.getAbsolutePath());
            }
        }
    }
	
	public void listFilesAndFilter(ArrayList<File> files)
	{
		listFilesAndFilesSubDirectories(folder.toString());
		for(int i = 0; i < searchedSongs.size(); i++)
		{
			if(!(files.get(i).toString().toLowerCase().endsWith(".mp3")))
			{
				files.remove(i);
				i--;
			}
		}
	}
	
	private LinkedHashSet<String> createPlaylist()
	{
		LinkedHashSet<String> playlist = new LinkedHashSet<String>();
		
		listFilesAndFilter(searchedSongs);
		Random rand = new Random();
		
		while(playlist.size() != searchedSongs.size())
		{
			int randomNumber = rand.nextInt(searchedSongs.size());
			playlist.add(searchedSongs.get(randomNumber).toString());
		}
		return playlist;
	}
	
	public void shufflePlaylist(LinkedHashSet<String> playlist)
	{
		_queue = playlist.iterator();
		if(_queue.hasNext())
		{
			String currentSong = _queue.next();
			
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
					
					if(_queue.hasNext())
					{
						String nextSong = _queue.next();
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
		
		searchedSongs.removeAll(searchedSongs);
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
		listFilesAndFilter(searchedSongs);
		String foundSongs = "";
		
		for(int i = 0; i < searchedSongs.size(); i++)
		{
			String currentSearchedSong = searchedSongs.get(i).toString();
			int lastIndex = currentSearchedSong.lastIndexOf("\\");
			String currentSearchedSongName = currentSearchedSong.substring(lastIndex + 1);
			
			if(currentSearchedSongName.toLowerCase().contains(searchPhrase))
			{
				foundSongs += "\n" + i + ": " + currentSearchedSongName;
			}
		}
		
		if(foundSongs.isEmpty())
		{
			foundSongs = "No Items Match Your Search. Try Again";
		}
		
		searchedSongs.removeAll(searchedSongs);
		
		return foundSongs;
	}
	
	public String checkFilesForFilename(String songName)
	{
		listFilesAndFilter(searchedSongs);
		String foundFilename = null;
		
		for(int i = 0; i < searchedSongs.size(); i++)
		{
			String currentSearchedSong = searchedSongs.get(i).toString().toLowerCase();
			int lastIndex = currentSearchedSong.lastIndexOf("\\");
			
			if(currentSearchedSong.substring(lastIndex + 1).contains(songName))
			{
				foundFilename = formatFilenameForOS(searchedSongs.get(i).toString());
				searchedSongs.removeAll(searchedSongs);
				return foundFilename;
			}
		}
		searchedSongs.removeAll(searchedSongs);
		return foundFilename;
	}
	
	public void showSongs() throws IOException
	{
		listFilesAndFilter(searchedSongs);
		String fileNames = "";
		
		for(int i = 0; i < searchedSongs.size(); i++)
		{
			String currentSearchedSong = searchedSongs.get(i).toString();
			int lastIndex = currentSearchedSong.lastIndexOf("\\");
			fileNames += "\n" + i + ": " + currentSearchedSong.substring(lastIndex + 1);
		}
		searchedSongs.removeAll(searchedSongs);
		_serv.serverGui.appendText("Listed Songs");
		if(fileNames.isEmpty())
		{
			_out.writeUTF("No Songs Found In Current Directory!");
		}
		else
		{
			_out.writeUTF(fileNames);
		}
	}

	public File getFolder() 
	{
		return folder;
	}

	public void setFolder(File folder) 
	{
		this.folder = folder;
	}
}
