package co.com.tecnocom.csj.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Conexion {

	
    private static Connection con;
    public static String DATA_SOURCE_PRINCIPAL="java:comp/env/jdbc/base_consulta_procesos";
    
    
    private Conexion(String nameDatasource) {
        setConection(nameDatasource);
    }

    private DataSource getConexion(String nameDatasource) throws NamingException {
        Context c = new InitialContext();
        return (DataSource) c.lookup(nameDatasource);
    }

    private void setConection(String nameDatasource) {
        try {
            DataSource ds = getConexion(nameDatasource);
            con = ds.getConnection();
        } catch (NamingException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    public boolean cerrarConexion() {
        try {
            con.close();
        } catch (SQLException e) {
            System.err.println("Error con la secuencia SQL al cerrar la cenexi�n");
        }
        return true;
    }

    public static Conexion obtenerConexion(String nameDatasource) {
        return new Conexion(nameDatasource);
    }

    public Connection obtenerConnection() {
        return con;
    }
	
    
    public static Connection open(String nameDatasource) throws ClassNotFoundException, java.sql.SQLException {
	       return Conexion.obtenerConexion(nameDatasource).obtenerConnection();
	}
    
    public static void close(Object... obj) {
	        for (int i = 0; i < obj.length; i++) {
	            try {
	                if(obj[i]!=null){
	                    if( obj[i] instanceof java.sql.PreparedStatement){
	                            ((java.sql.PreparedStatement)obj[i]).close();
	                    }else if( obj[i] instanceof java.sql.Statement){
	                            ((java.sql.Statement)obj[i]).close();
	                    }else if( obj[i] instanceof java.sql.Connection){
	                            ((java.sql.Connection)obj[i]).close();                            
	                    }else if( obj[i] instanceof java.sql.ResultSet){
	                            ((java.sql.ResultSet)obj[i]).close();
	                    }
	                }
	            } catch (Exception e) {
	            }
	        }
	  }
}
