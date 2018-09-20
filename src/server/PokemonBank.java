package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import shared.Request;
import shared.Pokemon;

/**
 * This class represents the server application, which is a Pokémon Bank.
 * It is a shared account: everyone's Pokémons are stored together.
 * @author strift
 *
 */
public class PokemonBank {
	
	public static final int SERVER_PORT = 3000;
	public static final String DB_FILE_NAME = "pokemons.db";
	

	private Database db; //The database instance

	private ServerSocket server; // the socketserver instance

	private ArrayList<Pokemon> pokemons; //The Pokémons stored in memory
	
	/**
	 * Constructor
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public PokemonBank() throws IOException, ClassNotFoundException {
		/*
		 * Here, you should initialize the Database and ServerSocket instances.
		 */
		server = new ServerSocket(SERVER_PORT);
		db = new Database(DB_FILE_NAME);

		System.out.println("Banque Pokémon (" + DB_FILE_NAME + ") démarrée sur le port " + SERVER_PORT);
		
		// Let's load all the Pokémons stored in database
		this.pokemons = this.db.loadPokemons();
		this.printState();
	}
	
	/**
	 * The main loop logic of your application goes there.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public void handleClient() throws IOException, ClassNotFoundException {
		System.out.println("En attente de connexion...");
		/*
		 * Here, you should wait for a client to connect.
		 */
		Socket client = server.accept();
		System.out.println("Client connecté :");

		/*
		 * TODO
		 * You will need one stream to read and one to write.
		 * Classes you can use:
		 * - ObjectInputStream
		 * - ObjectOutputStream
		 * - Request
		 */

		ObjectOutputStream  out = new ObjectOutputStream(client.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(client.getInputStream());


		
		
		// For as long as the client wants it
		boolean running = true;
		while (running) {
			/*
			 * TODO
			 * Here you should read the stream to retrieve a Request object
			 */
			Request request = (Request) in.readObject();
			
			/*
			 * Note: the server will only respond with String objects.
			 */
			switch(request) {
			case LIST:
				System.out.println("Request: LIST");
				if (this.pokemons.size() == 0) {
					// There is no Pokémons, so just send a message to the client using the output stream.
					 out.writeObject("There is no Pokemon in the list");
					 out.flush();
				} else {
					/*
					 *
					 * Here you need to build a String containing the list of Pokémons, then write this String
					 * in the output stream.
					 * Classes you can use:
					 * - StringBuilder
					 * - String
					 * - the output stream
					 */
					StringBuilder listPokemon = new StringBuilder();
					pokemons.forEach((pkmn)->{
						listPokemon.append(pkmn.toString());
						listPokemon.append("\n");
					});
					out.writeObject(listPokemon.toString());
					out.flush();
					
				}
				break;
				
			case STORE:
				System.out.println("Request: STORE");
				/*
				 *
				 * If the client sent a STORE request, the next object in the stream should be a Pokémon.
				 * You need to retrieve that Pokémon and add it to the ArrayList.
				 */
				Pokemon incomming = (Pokemon) in.readObject();
				pokemons.add(incomming);
				/*
				 *
				 * Then, send a message to the client so he knows his Pokémon is safe.
				 */
				out.writeObject(incomming.toString() + " is save in your personnal computer \n");
				out.flush();

				break;
				
			case CLOSE:
				System.out.println("Request: CLOSE");
				/*
				 * TODO
				 * Here, you should use the output stream to send a nice 'Au revoir !' message to the client. 
				 */
				out.writeObject("We care about your shared pokemon, please send 1,400 poké$ to Leo for using saving services \n\n");
				out.flush();
				// Closing the connection
				System.out.println("Fermeture de la connexion...");
				running = false;
				break;
			}
			this.printState();
		};
		
		/*
		 * Now you can close both I/O streams, and the client socket.
		 */
		in.close();
		out.close();
		client.close();
		/*
		 * Now that everything is done, let's update the database.
		 */
		System.out.println("saving...");
		db.savePokemons(pokemons);
		System.out.println("save done \n\n");
		
	}
	
	/**
	 * Print the current state of the bank
	 */
	private void printState() {
		System.out.print("printing state: [");
		for (int i = 0; i < this.pokemons.size(); i++) {
			if (i > 0) {
				System.out.print(", ");
			}
			System.out.print(this.pokemons.get(i));
		}
		System.out.println("]");
	}


	
	/**
	 * Stops the server.
	 * Note: This function will never be called in this project.
	 * @throws IOException 
	 */
	public void stop() throws IOException {
		this.server.close();
	}
}
