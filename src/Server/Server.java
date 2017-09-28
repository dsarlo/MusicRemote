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
	private DataOutputStream out;
	private AdvancedPlayer player;
	private int countPlayed;
	private ArrayList<File> searchedSongs = new ArrayList<File>();
	private ArrayList<Integer> randoms = new ArrayList<Integer>();
	private ExecutorService service;
	private String operatingSystem = System.getProperty("os.name").toLowerCase();
	private int counter = 0;
	private boolean shuffle = false;
	
	private ServerStartClickHandler serv;
	private File folder;
	
	public Server(ServerStartClickHandler ServerRef)
	{
		serv = ServerRef;
		folder = new File(serv.getPath());
	}
	
	@Override
	public void run()
	{
		try 
		{
			serverSocket = new ServerSocket(7777);
			serv.serverGui.appendText("SERVER LOG:\n\n\n");
			serv.serverGui.appendText("Server Started!\nServer Address = " + InetAddress.getLocalHost().getHostAddress());
			socket = serverSocket.accept();
			serv.serverGui.appendText("Client: " + socket.getInetAddress().toString() + " has connected\n\n\n");
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			
			
			while(true)
			{
				service = Executors.newFixedThreadPool(4);
				if(in.readInt() == 1)
				{
					String readText = in.readUTF();
					if(readText.equals("!!"))
					{
						serv.serverGui.appendText("Server Closed!");
						out.writeUTF("Server Closed!");
						serverSocket.close();
						System.exit(0);
					}
					else if(readText.equals("||"))
					{
						stopMP3();
						service.shutdown();
						serv.serverGui.appendText("Music Stopped!");
						out.writeUTF("Music Stopped!");
					}
					else if(readText.equals("/list"))
					{
						showSongs();
					}
					else if(readText.equals("?"))
					{
						serv.serverGui.appendText("Displayed Help Info");
						String help = "!! - Closes the server\n|| - Stops the current song\n/list - Lists all songs in library\n/s <SONGNAME> - Searches and displays songs with the typed phrases\n/shuffle creates a randomly shuffled playlist for you and begins playing it";
						out.writeUTF(help);
					}
					else if(readText.contains("/s "))
					{
						serv.serverGui.appendText("Searched Songs For: " + readText.substring(3));
						out.writeUTF(searchSongs(readText.substring(3)));
					}
					else if(readText.equals("/shuffle"))
					{
						randoms.removeAll(randoms);
						if(countPlayed >= 1)
						{
							stopMP3();
							service.shutdown();
							service = Executors.newFixedThreadPool(4);
						}
						shuffle();
						serv.serverGui.appendText("Shuffled Songs");
				    	out.writeUTF("Shuffled Songs");
					}
					else
					{
						shuffle = false;
						String nowPlaying = checkFilesForFilename(readText);
					    if(nowPlaying == null)
					    {
					    	serv.serverGui.appendText("File/Command Not Found!");
					    	out.writeUTF("File/Command Not Found. Try Again");
					    }
					    else
					    {
					    	if(countPlayed >= 1)
							{
								stopMP3();
								service.shutdown();
								service = Executors.newFixedThreadPool(4);
							}
					    	service.submit(new Runnable()
					    	{
						        public void run() 
						        {
						        	playMP3(nowPlaying);
						        }
						    });
					    	countPlayed++;
					    	serv.serverGui.appendText("Played: " + nowPlaying);
							out.writeUTF("Now Playing: " + nowPlaying);
					    }
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
			serv.serverGui.appendText("Failed to play the file.");
		}
		/*if(shuffle)
		{
			player.setPlayBackListener(new PlaybackListener() 
			{
		    @Override
		    public void playbackFinished(PlaybackEvent event) 
		    {
		        //pausedOnFrame = event.getFrame();
		    	counter++;
		    	int lastIndex = searchedSongs.get(randoms.get(counter)).toString().lastIndexOf("\\");
				String foundFilename = searchedSongs.get(randoms.get(counter)).toString().substring(lastIndex + 1);
				stopMP3();
				playMP3(foundFilename);
		    }
		});
		}
		else
		{
			
		}*/
	}
	
	public void listFilesAndFilesSubDirectories(String directoryName)
	{
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        for (File file : fList)
        {
            if (file.isFile())
            {
            	File fileola = new File(file.getAbsolutePath());
                searchedSongs.add(fileola);
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
			if(!(files.get(i).toString().toLowerCase().endsWith("")))
			{
				files.remove(i);
				i--;
			}
		}
	}
	
	public void shuffle()
	{
		counter = 0;
		listFilesAndFilter(searchedSongs);
		Random rand = new Random();
		for(int i = 0; i < searchedSongs.size(); i++)
		{
			int randomNumber = rand.nextInt(searchedSongs.size());
			while(randoms.contains(randomNumber))
			{
				randomNumber = rand.nextInt(searchedSongs.size());
			}
			randoms.add(randomNumber);
			System.out.println(randoms.get(i));
		}
		int lastIndex = searchedSongs.get(randoms.get(counter)).toString().lastIndexOf("\\");
		String foundFilename = searchedSongs.get(randoms.get(counter)).toString().substring(lastIndex + 1);
    	service.submit(new Runnable() 
    	{
	        public void run() 
	        {
	        	playMP3(foundFilename);
	        	shuffle = true;
	        	player.setPlayBackListener(new PlaybackListener() 
	        	{
				    @Override
				    public void playbackFinished(PlaybackEvent event) 
				    {
				        //pausedOnFrame = event.getFrame();
				    	counter++;
				    	int lastIndex = searchedSongs.get(randoms.get(counter)).toString().lastIndexOf("\\");
						String foundFilename = searchedSongs.get(randoms.get(counter)).toString().substring(lastIndex + 1);
						stopMP3();
						playMP3(foundFilename);
				    }
				});
	        }
	    });
    	searchedSongs.removeAll(searchedSongs);
    	countPlayed += 1;
	}

	public String searchSongs(String searchPhrase)
	{
		listFilesAndFilter(searchedSongs);
		String foundFilename = null;
		for(int i = 0; i < searchedSongs.size(); i++)
		{
			int lastIndex = searchedSongs.get(i).toString().lastIndexOf("\\");
			if(searchedSongs.get(i).toString().substring(lastIndex + 1).toLowerCase().contains(searchPhrase))
			{
				foundFilename += "\n" + i + ": " + searchedSongs.get(i).toString().substring(lastIndex + 1);
			}
		}
		if(foundFilename == null)
		{
			searchedSongs.removeAll(searchedSongs);
			return "No Items Match Your Search. Try Again";
		}
		else
		{
			searchedSongs.removeAll(searchedSongs);
			return foundFilename.substring(4);
		}
	}
	
	public String checkFilesForFilename(String songName)
	{
		listFilesAndFilter(searchedSongs);
		String foundFilename = null;
		for(int i = 0; i < searchedSongs.size(); i++)
		{
			if(searchedSongs.get(i).toString().toLowerCase().contains(songName))
			{
				int substringValue = 0;
				if(operatingSystem.indexOf("win") >= 0)
				{
					substringValue = 2;
				}
				foundFilename = searchedSongs.get(i).toString().substring(substringValue);//CHANGE TO 0 IF ON MAC      CHANGE TO 2 IF ON WINDOWS
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
		String fileNames = null;
		for(int i = 0; i < searchedSongs.size(); i++)
		{
			int lastIndex = searchedSongs.get(i).toString().lastIndexOf("\\");
			fileNames += "\n" + i + ": " + searchedSongs.get(i).toString().substring(lastIndex + 1);
		}
		searchedSongs.removeAll(searchedSongs);
		serv.serverGui.appendText("Listed Songs");
		if(!fileNames.isEmpty())
		{
			out.writeUTF(fileNames.substring(4));
		}
		else
		{
			out.writeUTF("No Songs Found In Current Directory!");
		}
	}

	public File getFolder() {
		return folder;
	}

	public void setFolder(File folder) {
		this.folder = folder;
	}
}
