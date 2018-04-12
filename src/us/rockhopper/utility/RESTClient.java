package us.rockhopper.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class RESTClient {

	public static GameAction getDecision(String roleName, GameState state) throws ClientProtocolException, IOException {

		List<String> contents = new ArrayList<>(state.getData().length);
		for (int n : state.getData()) {
			contents.add(Integer.toString(n));
		}

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet(
				"http://127.0.0.1:5000/get_decision_behavior/" + roleName.toLowerCase() + "/" + String.join(",", contents));

		HttpResponse response = httpClient.execute(getRequest);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = null;
		while ((line = br.readLine()) != null) {
			return GameAction.values()[Integer.parseInt(line.replaceAll("\\s", ""))];
		}
		return GameAction.INVALID;
	}
}
