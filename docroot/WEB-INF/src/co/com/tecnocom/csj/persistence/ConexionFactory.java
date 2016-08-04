package co.com.tecnocom.csj.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionFactory {

	private static String usuario;
	private static String password;
	private static String database;
	private static String server;
	private static int port;

	static {
		try {
			usuario = PropertiesUtil.getInstance().getProperty("usuario");
			password = PropertiesUtil.getInstance().getProperty("contrasena");
			database = PropertiesUtil.getInstance().getProperty("database");
			server = PropertiesUtil.getInstance().getProperty("server");
			port = Integer.parseInt(PropertiesUtil.getInstance().getProperty("port"));
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public Connection getConexion() throws SQLException {
		Connection conexion;

		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		conexion = DriverManager.getConnection("jdbc:sqlserver://"+server+":"+port+";databaseName="+database+";user="+usuario+";password="+password+";");

		return conexion;
	}
}
