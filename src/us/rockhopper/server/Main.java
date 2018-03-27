package us.rockhopper.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

import lowentry.ue4.classes.sockets.LatentResponse;
import lowentry.ue4.classes.sockets.SocketClient;
import lowentry.ue4.classes.sockets.SocketServer;
import lowentry.ue4.classes.sockets.SocketServerListener;
import lowentry.ue4.library.LowEntry;

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
		File credentialFile = new File("./config/credentials.txt");
		BufferedReader credentialReader = new BufferedReader(new FileReader(credentialFile));
		String accessKeyLine = credentialReader.readLine();
		String secretKeyLine = credentialReader.readLine();
		String accessKey = accessKeyLine.substring(accessKeyLine.indexOf("=") + 1);
		String secretKey = secretKeyLine.substring(secretKeyLine.indexOf("=") + 1);
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
		credentialReader.close();

		// Establish a connection to the AWS data storage.
		AmazonDynamoDB dynamoClient = AmazonDynamoDBClientBuilder.standard() //
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds)) //
				.withRegion(Regions.US_EAST_1) //
				.build();
		DynamoDB dynamoDB = new DynamoDB(dynamoClient);
		Table eventTable = dynamoDB.getTable("action_histories");

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
				System.out.println("[" + Thread.currentThread().getName() + "] ClientConnected: " + client);

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
				System.out.println("[" + Thread.currentThread().getName() + "] ClientDisconnected: " + client);

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

			@SuppressWarnings("unchecked")
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
				try {
					JSONObject jsonMessage = (JSONObject) parser.parse(bufferString);

					// Inject the current tick of receiving this message.
					jsonMessage.put("tick", tick);

					// For this demonstration, simply forward the incoming data
					// straight to AWS.
					System.out.println(jsonMessage.toJSONString());
					Item item = new Item() //
							.withPrimaryKey("entry", System.currentTimeMillis()) //
							.withString("data", jsonMessage.toJSONString());
					eventTable.putItem(item);
					packetOrder += 1;

				} catch (ParseException e) {
					e.printStackTrace();
				}
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
				System.out.println("[" + Thread.currentThread().getName() + "] Start Receiving Message");
				return (bytes <= (10 * 1024));
			}

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
				System.out.println("[" + Thread.currentThread().getName() + "] Received Message: \""
						+ LowEntry.bytesToStringUtf8(bytes) + "\"");
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
		SocketServer server = new SocketServer(false, 8401, 8401, listener);
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