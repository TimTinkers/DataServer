package us.rockhopper.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

public class RDSInserter {

	private static String[] loadSettings() {
		String[] settings = new String[6];
		try {
			BufferedReader br = new BufferedReader(new FileReader("./config/settings.db"));
			String line = null;
			for (int i = 0; i < 6; i++) {
				line = br.readLine();
				if (line == null) {
					break;
				}
				settings[i] = line;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return settings;
	}

	public static DBConnect getDatabaseConnection() throws SQLException {
		String[] settings = loadSettings();
		return new DBConnect(settings[0], settings[1], Integer.parseInt(settings[2]), settings[3], settings[4],
				settings[5]);
	}

	public static void insertRow(DBConnect dbc, String row) throws SQLException {
		String[] values = row.split(",");
		String query = "INSERT INTO game_data VALUES (" + String.join(", ", values) + ");";
		dbc.executeUpdate(query);
	}
}