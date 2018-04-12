package us.rockhopper.utility;

import java.util.Arrays;

public class GameState {
	private int[] data = null;

	public GameState(String data) {
		int[] intData = Arrays.stream(data.split(",")).mapToInt(Integer::parseInt).toArray();
		this.data = intData;
	}

	public GameState(int[] data) {
		this.data = data;
	}

	public int[] getData() {
		return data;
	}
}
