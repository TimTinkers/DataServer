package us.rockhopper.server;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import lowentry.ue4.classes.sockets.LatentResponse;
import lowentry.ue4.classes.sockets.SocketClient;
import lowentry.ue4.classes.sockets.SocketServer;
import lowentry.ue4.classes.sockets.SocketServerListener;
import lowentry.ue4.library.LowEntry;
import us.rockhopper.utility.DBConnect;
import us.rockhopper.utility.RDSInserter;

/**
 * The main class for the arrow-handling demo server.
 * 
 * @author Tim Clancy
 * @version 1.0.0
 * @date 11.21.2017
 */
public class Main {

	// Server-wide time.
	static int tick = 0;
	static int packetOrder = 0;

	/**
	 * The entry point for this Java server.
	 * 
	 * @param args
	 *            any input arguments.
	 * @throws Throwable
	 *             thrown if the server fails to initialize properly.
	 */
	public static void main(final String[] args) throws Throwable {

		// Create a JSON parser for use by the networking methods.
		JSONParser parser = new JSONParser();

		// Establish credentials for connecting to the AWS data storage.
		/*
		 * File credentialFile = new File("./config/credentials.txt");
		 * BufferedReader credentialReader = new BufferedReader(new
		 * FileReader(credentialFile)); String accessKeyLine =
		 * credentialReader.readLine(); String secretKeyLine =
		 * credentialReader.readLine(); String accessKey =
		 * accessKeyLine.substring(accessKeyLine.indexOf("=") + 1); String
		 * secretKey = secretKeyLine.substring(secretKeyLine.indexOf("=") + 1);
		 * BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey,
		 * secretKey); credentialReader.close();
		 * 
		 * // Establish a connection to the AWS data storage. AmazonDynamoDB
		 * dynamoClient = AmazonDynamoDBClientBuilder.standard() //
		 * .withCredentials(new AWSStaticCredentialsProvider(awsCreds)) //
		 * .withRegion(Regions.US_EAST_1) // .build(); DynamoDB dynamoDB = new
		 * DynamoDB(dynamoClient); Table eventTable =
		 * dynamoDB.getTable("action_histories");
		 */

		// Create a listener for the server's socket.
		SocketServerListener listener = new SocketServerListener() {

			@Override
			/**
			 * This method executes when a client connects to this SocketServer.
			 *
			 * @param server
			 *            the server listening to this socket.
			 * @param client
			 *            the client connection which connected.
			 */
			public void clientConnected(final SocketServer server, final SocketClient client) {
				System.out.println(System.currentTimeMillis() + " [" + Thread.currentThread().getName()
						+ "] ClientConnected: " + client);

				for (SocketClient c : server) {
					if (client != c) {
						c.sendMessage(LowEntry.stringToBytesUtf8("another client connected!"));
					}
				}
			}

			@Override
			/**
			 * This method executes when a client disconnects from this
			 * SocketServer.
			 *
			 * @param server
			 *            the server listening to this socket.
			 * @param client
			 *            the client connection which disconnected.
			 */
			public void clientDisconnected(final SocketServer server, final SocketClient client) {
				System.out.println(System.currentTimeMillis() + " [" + Thread.currentThread().getName()
						+ "] ClientDisconnected: " + client);

				for (SocketClient c : server) {
					c.sendMessage(LowEntry.stringToBytesUtf8("another client disconnected!"));
				}
			}

			@Override
			/**
			 * This method executes when a client is validating the connection.
			 *
			 * @param server
			 *            the server listening to this socket.
			 * @param client
			 *            the client connection which is validating.
			 */
			public void receivedConnectionValidation(SocketServer server, SocketClient client) {
			}

			@Override
			/**
			 * This method executes when the server starts receiving an
			 * unreliable (UDP) message packet.
			 *
			 * @param server
			 *            the server listening to this socket and receiving.
			 * @param client
			 *            the client connection which is sending.
			 * @param bytes
			 *            the size in bytes of the message.
			 * @return true to start receiving the packer, false to disconnect
			 *         the client.
			 */
			public boolean startReceivingUnreliableMessage(SocketServer server, SocketClient client, int bytes) {
				// System.out.println("[" + Thread.currentThread().getName()
				// + "] Start Receiving Unreliable Message");
				return (bytes <= (1 * 1024));
			}

			@Override
			/**
			 * This method executes when the server has received an unreliable
			 * (UDP) message packet.
			 *
			 * @param server
			 *            the server listening to this socket and receiving.
			 * @param client
			 *            the client connection which is sending.
			 * @param bytes
			 *            the buffer of bytes in the message.
			 */
			public void receivedUnreliableMessage(SocketServer server, SocketClient client, ByteBuffer bytes) {

				// Parse this message for JSON String and sender.
				// Normally you would read with LowEntry.readByteData(bytes)
				String bufferString = LowEntry.bytesToStringUtf8(LowEntry.getBytesFromByteBuffer(bytes));
				System.out.println(bufferString);

				// Parse the message into JSON format and take action.
				/*
				 * try { JSONObject jsonMessage = (JSONObject)
				 * parser.parse(bufferString);
				 * 
				 * // Inject the current tick of receiving this message.
				 * jsonMessage.put("tick", tick);
				 * 
				 * // For this demonstration, simply forward the incoming data
				 * // straight to AWS.
				 * System.out.println(jsonMessage.toJSONString()); Item item =
				 * new Item() // .withPrimaryKey("entry",
				 * System.currentTimeMillis()) // .withString("data",
				 * jsonMessage.toJSONString()); eventTable.putItem(item);
				 * packetOrder += 1;
				 * 
				 * } catch (ParseException e) { e.printStackTrace(); }
				 */
			}

			@Override
			/**
			 * This method executes when the server starts receiving a reliable
			 * message packet (TCP).
			 *
			 * @param server
			 *            the server listening to this socket and receiving.
			 * @param client
			 *            the client connection which is sending.
			 * @param bytes
			 *            the size in bytes of the message.
			 * @return true to start receiving the packer, false to disconnect
			 *         the client.
			 */
			public boolean startReceivingMessage(final SocketServer server, final SocketClient client,
					final int bytes) {
				System.out.println(System.currentTimeMillis() + " [" + Thread.currentThread().getName()
						+ "] Start Receiving Message");
				return (bytes <= (10 * 1024));
			}

			// A simple decision map for testing purposes.
			Map<String, Integer> priorDecision = new HashMap<String, Integer>();

			@Override
			/**
			 * This method executes when the server has received a reliable
			 * message packet (TCP).
			 *
			 * @param server
			 *            the server listening to this socket and receiving.
			 * @param client
			 *            the client connection which is sending.
			 * @param bytes
			 *            the array of bytes in the message.
			 */
			public void receivedMessage(final SocketServer server, final SocketClient client, final byte[] bytes) {

				// Parse this message for JSON String and sender.
				// Normally you would read with LowEntry.readByteData(bytes)
				String bufferString = LowEntry.bytesToStringUtf8(bytes);
				if (bufferString.equals("Bot")) {
					String botID = client.getRemoteAddress().toString();
					System.out.println("Bot connected! " + botID);

					// This is a state captured by some bot.
				} else if (bufferString.startsWith("bot:")) {
					String botID = client.getRemoteAddress().toString();
					System.out.println(botID + " - " + bufferString);

					// Generate a simple series of decisions.
					int decision = 0;
					if (!priorDecision.containsKey(botID)) {
						priorDecision.put(botID, 0);
					} else {
						int lastDecision = priorDecision.get(botID);
						switch (lastDecision) {
						case 0:
							decision = 5;
							break;
						case 5:
							decision = 7;
							break;
						case 7:
							decision = 0;
							break;
						}
						priorDecision.put(botID, decision);
					}
					client.sendMessage(LowEntry.stringToBytesUtf8(decision + ""));

					// This is state from a player. Parse the message into JSON
					// format and take action.
				} else {
					System.out.println(System.currentTimeMillis() + " Stowing to RDS!");
					DBConnect dbc;
					try {
						dbc = RDSInserter.getDatabaseConnection();
						try {
							JSONArray jsonArray = (JSONArray) parser.parse(bufferString);
							String actionEntry = (String) jsonArray.get(jsonArray.size() - 1);
							int action = Integer.parseInt(actionEntry.split(":")[1].trim());
							for (int i = 0; i < jsonArray.size() - 1; i++) {
								String row = tick + "," + (String) jsonArray.get(i) + "," + action;
								System.out.println(row);
								try {
									RDSInserter.insertRow(dbc, row);
								} catch (SQLException e) {
									System.out.println(System.currentTimeMillis() + " Failed to insert row.");
									e.printStackTrace();
								}
							}
						} catch (ParseException e) {
							System.out.println(System.currentTimeMillis() + " Failed to parse state.");
							e.printStackTrace();
						}
					} catch (SQLException e) {
						System.out.println(System.currentTimeMillis() + " Failed to connect to database.");
						e.printStackTrace();
					}
				}
			}

			@Override
			/**
			 * This method executes when the server starts receiving a packet
			 * for a function call.
			 *
			 * @param server
			 *            the server listening to this socket and receiving.
			 * @param client
			 *            the client connection which is sending.
			 * @param bytes
			 *            the size in bytes of the message.
			 * @return true to start receiving the packer, false to disconnect
			 *         the client.
			 */
			public boolean startReceivingFunctionCall(final SocketServer server, final SocketClient client,
					final int bytes) {
				System.out.println("[" + Thread.currentThread().getName() + "] Start Receiving Function Call");
				return (bytes <= (10 * 1024));
			}

			@Override
			/**
			 * This method executes when the server has received a packet for a
			 * function call.
			 *
			 * @param server
			 *            the server listening to this socket and receiving.
			 * @param client
			 *            the client connection which is sending.
			 * @param bytes
			 *            the array of bytes in the message.
			 */
			public byte[] receivedFunctionCall(final SocketServer server, final SocketClient client,
					final byte[] bytes) {

				// Parse and display the message as a String.
				String bufferString = LowEntry.bytesToStringUtf8(bytes);
				String playerIP = client.pyro().getRemoteAddress().toString();
				System.out.println("[" + Thread.currentThread().getName() + "] Received Function Call from " + playerIP
						+ ": " + bufferString);
				return bytes;
			}

			@Override
			/**
			 * This method executes when the server starts receiving a packet
			 * for a latent function call.
			 *
			 * @param server
			 *            the server listening to this socket and receiving.
			 * @param client
			 *            the client connection which is sending.
			 * @param bytes
			 *            the size in bytes of the message.
			 * @return true to start receiving the packer, false to disconnect
			 *         the client.
			 */
			public boolean startReceivingLatentFunctionCall(final SocketServer server, final SocketClient client,
					final int bytes) {
				System.out.println("[" + Thread.currentThread().getName() + "] Start Receiving Latent Function Call");
				return (bytes <= (10 * 1024));
			}

			@Override
			/**
			 * This method executes when the server has received a packet for a
			 * function call.
			 *
			 * @param server
			 *            the server listening to this socket and receiving.
			 * @param client
			 *            the client connection which is sending.
			 * @param bytes
			 *            the array of bytes in the message.
			 * @param response
			 *            the latent response data.
			 */
			public void receivedLatentFunctionCall(final SocketServer server, final SocketClient client,
					final byte[] bytes, final LatentResponse response) {
				System.out.println("[" + Thread.currentThread().getName() + "] Received Latent Function Call: \""
						+ LowEntry.bytesToStringUtf8(bytes) + "\"");
				response.done(null);
			}
		};

		// Starts the SocketServer based on our listener at the specified port.
		// Change the boolean to true if you want to accept external
		// connections, remove the second port to not listen to UDP.
		SocketServer server = new SocketServer(true, 18401, listener);
		System.out.println("Listening: " + server);
		long lastTickTime = System.currentTimeMillis();
		while (true) {
			server.listen();

			// Tick the server.
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastTickTime >= 33) {
				lastTickTime = currentTime;

				// Increment the tick counter.
				++tick;
			}
		}
	}
}
