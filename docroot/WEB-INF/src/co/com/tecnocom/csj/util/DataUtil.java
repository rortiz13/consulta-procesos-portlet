package co.com.tecnocom.csj.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletPreferences;

import org.apache.log4j.Logger;

import co.com.tecnocom.csj.controller.Controller;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portlet.dynamicdatalists.model.DDLRecord;
import com.liferay.portlet.dynamicdatalists.service.DDLRecordLocalServiceUtil;

public class DataUtil {
	
	private static DataUtil instance;
	private Logger _log = Logger.getLogger(getClass());
	
	private static Map<String, String> cities;
	private static List<SpecialtyConnection> allSpecialties;
	private static Map<String, List<SpecialtyConnection>> specialtiesByCity;

	private DataUtil(PortletPreferences prefs, Locale locale) {
		initLists(prefs, locale);
	}
	
	public static DataUtil getInstance(PortletPreferences prefs, Locale locale){
		if(instance == null) {
			instance = new DataUtil(prefs, locale);
		}
		
		return instance;
	}
	
	/**
	 * Can return a null instance if DataUtil has not been initialized first by calling getInstance(PortletPreferences prefs, Locale locale)
	 */
	public static DataUtil getInstance() {
		return instance;
	}
	
	private void initLists(PortletPreferences prefs, Locale locale) {
		System.out.println("inicializar listas   ******");
		cities = new LinkedHashMap<String, String>();
		allSpecialties = new LinkedList<SpecialtyConnection>();
		specialtiesByCity = new LinkedHashMap<String, List<SpecialtyConnection>>();
		
		Long idProcessQueriesList = Long.parseLong(prefs.getValue("idProcessQueries", "1463818"));
		System.out.println("despues del querie *******");
		
		try {
			List<DDLRecord> records = DDLRecordLocalServiceUtil.getRecords(idProcessQueriesList);
			for (DDLRecord ddlRecord : records) {
				System.out.println("probar cuenta");
//				_log.info(ddlRecord);
				String rawCityCode = ddlRecord.getField("city").getValue().toString(); 
				String cityCode = rawCityCode.substring(rawCityCode.indexOf("\"")+1, rawCityCode.lastIndexOf("\""));
				if(!cities.containsKey(cityCode)) {
					cities.put(cityCode, ddlRecord.getField("city").getRenderedValue(locale));
				}
				
				if(!specialtiesByCity.containsKey(cityCode)) {
					specialtiesByCity.put(cityCode, new LinkedList<SpecialtyConnection>());
				}
				
				SpecialtyConnection specialtyConnection = new SpecialtyConnection(cityCode);
				String rawSpecialtyCode = ddlRecord.getField("specialty").getValue().toString();
				String specialtyCode = rawSpecialtyCode.substring(rawSpecialtyCode.indexOf("\"")+1, rawSpecialtyCode.lastIndexOf("\""));
				
				String rawCorporationCode = ddlRecord.getField("corporation").getValue().toString();
				String corporationCode = rawCorporationCode.substring(rawCorporationCode.indexOf("\"")+1, rawCorporationCode.lastIndexOf("\""));
				
				specialtyConnection.setCorporationSpecialtyCode(corporationCode + "" + specialtyCode);
				specialtyConnection.setSpecialtyCustomName(ddlRecord.getField("custom_name").getValue().toString());
				specialtyConnection.setConnectionDatabase(ddlRecord.getField("conn_db").getValue().toString());
				specialtyConnection.setConnectionDatasource(ddlRecord.getField("conn_ds").getValue().toString());
				
				// Si el datasource contiene 4 partes, es un repetible, se reasigna el specialtycode
				if(null != specialtyConnection.getConnectionDatasource() && !specialtyConnection.getConnectionDatasource().isEmpty()) {
					String[] datasourceParts = specialtyConnection.getConnectionDatasource().split("_");
					if(datasourceParts.length == 4) {
						specialtyConnection.setCorporationSpecialtyCode(corporationCode + "" + specialtyCode + "_" + datasourceParts[3]);
					}
				}
				
				specialtiesByCity.get(cityCode).add(specialtyConnection);
				allSpecialties.add(specialtyConnection);
			}
			
		} catch (SystemException se) {
			_log.error("SystemException en initLists", se);
		} catch (PortalException pe) {
			_log.error("PortalException en initLists", pe);
		}
	}

	public Map<String, String> getAllCities() {
		
		ResultSet result =Controller.selecCiudad();
		try {
			if (result != null) {
				while (result.next()) {
					cities.put(result.getString(1), result.getString(2));
				}				
			} else {
					System.out.println("no ahy coincidencias de busqueda");
				}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return cities;
	}
	
	public List<SpecialtyConnection> getAllSpecialties() {
		return allSpecialties;
	}
	
	public SpecialtyConnection getSpecialtyByCode(String corporationSpecialtyCode, String cityCode) {
		for(SpecialtyConnection specialty : getAllSpecialties()) {
			if(specialty.getCityCode().equals(cityCode) && specialty.getCorporationSpecialtyCode().equals(corporationSpecialtyCode)) {
				return specialty;
			}
		}
		
		return null;
	}
	
	public List<SpecialtyConnection> getSpecialtiesByCity(String cityCode) {
		System.out.println("consultar especialidades**************  " +cityCode);
		if(specialtiesByCity != null && specialtiesByCity.get(cityCode) != null) {
			return specialtiesByCity.get(cityCode);
		}
		
		return new LinkedList<SpecialtyConnection>();
	}

	public Map<String, String> generateDatasourcesMap(String cityCode, String corporationSpecialtyCode) {
		Map<String, String> datasourcesDatabasesMap = new LinkedHashMap<String, String>();
		
		if(corporationSpecialtyCode != null && !corporationSpecialtyCode.isEmpty()) {
			SpecialtyConnection selected = getSpecialtyByCode(corporationSpecialtyCode, cityCode);
			if(selected != null) {
				datasourcesDatabasesMap.put(selected.getConnectionDatasource(), selected.getConnectionDatabase());
			}
		} else {
			// Si no hay una corporacion/especialidad seleccionada, obtener todos los datasources de la ciudad
			List<SpecialtyConnection> citySpecialties = getSpecialtiesByCity(cityCode);
			for (SpecialtyConnection specialtyConnection : citySpecialties) {
				datasourcesDatabasesMap.put(specialtyConnection.getConnectionDatasource(), specialtyConnection.getConnectionDatabase());
			}
		}
		
		return datasourcesDatabasesMap;
	}
	
	public String getCityByCityCode(String cityCode) {
		if(cities.containsKey(cityCode)) {
			return cities.get(cityCode);
		}
		
		return StringPool.BLANK;
	}
	
	public String getSpecialtyBySpecialtyCode(String specialtyCode, String cityCode) {
		SpecialtyConnection sp = new SpecialtyConnection(cityCode);
		sp.setCorporationSpecialtyCode(specialtyCode);
		if(allSpecialties.contains(sp)) {
			return allSpecialties.get(allSpecialties.indexOf(sp)).getSpecialtyCustomName();
		}
		
		return StringPool.BLANK;
	}
}
