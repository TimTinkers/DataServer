package us.rockhopper.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnect {

	private String driver = null;
	private String dbname = null;
	private int port = 0;
	private String username = null;
	private String password = null;
	private String servername = null;

	private Connection conn = null;

	public DBConnect(String driver, String dbname, int port, String username, String password, String servername)
			throws SQLException {
		this.driver = driver;
		this.dbname = dbname;
		this.port = port;
		this.username = username;
		this.password = password;
		this.servername = servername;
		initConnection();
	}

	public ResultSet executeUpdate(String s) throws SQLException {
		if (conn == null) {
			throw new IllegalStateException("Connection is not ready, call initConnection()");
		}
		Statement statement = conn.createStatement();
		statement.executeUpdate(s);
		return null;
	}

	public ResultSet execute(String s) throws SQLException {
		if (conn == null) {
			throw new IllegalStateException("Connection is not ready, call initConnection()");
		}
		Statement statement = conn.createStatement();
		statement.execute(s);
		return null;
	}

	public ResultSet executeQuery(String s) throws SQLException {
		if (conn == null) {
			throw new IllegalStateException("Connection is not ready, call initConnection()");
		}
		Statement statement = conn.createStatement();
		statement.executeQuery(s);
		ResultSet result = statement.getResultSet();
		return result;
	}

	public void initConnection() throws SQLException {
		this.conn = getConnection();
	}

	private Connection getConnection() throws SQLException {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Properties connectionProps = new Properties();
			connectionProps.put("user", username);
			connectionProps.put("password", password);

			String url = "jdbc:" + driver + "://" + servername + ":" + port + "/" + dbname;

			System.out.println("Attempting to connect to: " + url);
			conn = DriverManager.getConnection(url, connectionProps);
			if (conn != null) {
				System.out.println("Connected to database");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return conn;
	}

}