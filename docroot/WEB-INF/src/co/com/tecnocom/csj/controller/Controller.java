package co.com.tecnocom.csj.controller;

import java.sql.ResultSet;
import java.sql.SQLException;

import co.com.tecnocom.csj.persistence.PersistenceUtil;

public class Controller {
	


	public static ResultSet selecCiudad(){
		
		String sql ="select distinct(T.A081codiciud), C.A065DESCCIUD from T081BRESPEENTI T, T065BACIUDGENE C where T.A081CODICIUD = C.A065CODICIUD order by C.A065DESCCIUD";
		try {
			
			ResultSet result;			
			result=PersistenceUtil.realizaConsulta(sql);				
			if(result!=null){			
			       return result;
			}else{				
				System.out.println("no ahy  registro cargadas en la base de datos");
			}			
			PersistenceUtil.terminaOperacion();			
		} catch(SQLException ex){
			ex.printStackTrace();
			System.out.println("Error de conexion a la bd  "+ex.getMessage());		
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.out.println("Error  excepcion  "+ex.getMessage());		
		}
		return null;		
	}
	public static ResultSet selecEspecialidad(String ciudad){
		String sql ="select distinct(T.A081codiciud), C.A065DESCCIUD from T081BRESPEENTI T, T065BACIUDGENE C where T.A081CODICIUD = C.A065CODICIUD order by C.A065DESCCIUD";
		try {
			
			ResultSet result;			
			result=PersistenceUtil.realizaConsulta(sql);				
			if(result!=null){			
			       return result;
			}else{				
				System.out.println("no ahy  registro cargadas en la base de datos");
			}			
			PersistenceUtil.terminaOperacion();			
		} catch(SQLException ex){
			ex.printStackTrace();
			System.out.println("Error de conexion a la bd  "+ex.getMessage());		
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.out.println("Error  excepcion  "+ex.getMessage());		
		}
		return null;	
	}
	
}
