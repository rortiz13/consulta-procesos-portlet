package co.com.tecnocom.csj.util.concurrent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.ServletContext;

import co.com.tecnocom.csj.core.util.dto.ProcessData;
import co.com.tecnocom.csj.util.DataUtil;
import co.com.tecnocom.csj.util.concurrent.pool.ThreadPool;
import co.com.tecnocom.csj.util.export.ExportUtil;
import co.com.tecnocom.csj.util.export.QueryData;

public class ProcessQueryThread implements Runnable {
	private Map<String, String> datasources_databases_map;
	
	private String name;
	private String document;
	private String dateOption;
	private String initDate;
	private String endDate;
	private String cityCode;
	private String corporationSpecialtyCode;
	
	private String mail;
	private String hostname;
	private ServletContext servletContext;
	
	public ProcessQueryThread(Map<String, String> datasources_databases_map, String name, String document, String dateOption, String initDate, String endDate, String cityCode, String corporationSpecialtyCode, String mail, String hostname, ServletContext servletContext) {
		this.datasources_databases_map = datasources_databases_map;
		this.name = name;
		this.document = document;
		this.dateOption = dateOption;
		this.initDate = initDate;
		this.endDate = endDate;
		this.cityCode = cityCode;
		this.corporationSpecialtyCode = corporationSpecialtyCode;
		
		this.mail = mail;
		this.hostname = hostname;
		this.servletContext = servletContext;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run() {
		System.out.println("Running Thread -> cityCode(" + cityCode + "), corporationSpecialtyCode(" + corporationSpecialtyCode +")");
		
		List<Future> futures = new LinkedList<Future>();
		for(String datasource : datasources_databases_map.keySet()) {
			futures.add(ThreadPool.INSTANCE.submit(datasource, datasources_databases_map.get(datasource), 100, name, document, dateOption, initDate, endDate, cityCode, corporationSpecialtyCode));
		}
		
		System.out.println("Futures");
		Set<ProcessData> consolidatedSet = new LinkedHashSet<ProcessData>();
		
		for (Future future : futures) {
			try {
				List<ProcessData> futureResponse = (List<ProcessData>)future.get();
				if(futureResponse != null) {
					System.out.println("Response: " + futureResponse.size());
					consolidatedSet.addAll(futureResponse);
				}
			} catch (InterruptedException e) {
			    e.printStackTrace();
			} catch (ExecutionException e) {
			    e.printStackTrace();
			}
	    }
		
		System.out.println("Después de las consultas, consolidado: " + consolidatedSet.size());
		ExportUtil.INSTANCE.exportProcesses(consolidatedSet, generateQueryData(), mail, hostname, servletContext);
		System.out.println("Finishing Thread");
	}

	private QueryData generateQueryData() {
		QueryData queryData = new QueryData();
		queryData.setName(name);
		queryData.setDocument(document);
		queryData.setCity(DataUtil.getInstance().getCityByCityCode(cityCode));
		queryData.setSpecialty(DataUtil.getInstance().getSpecialtyBySpecialtyCode(corporationSpecialtyCode, cityCode));
		
		if(dateOption.equals("processStartDate")) {
			//	Inicio del Proceso
			queryData.setDateOption("Inicio del Proceso");
		} else if(dateOption.equals("actDate")) {
			//	Fecha de la actuación
			queryData.setDateOption("Fecha de la actuación");
		}
		
		//	Fechas
		SimpleDateFormat inputSdf = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat outputSdf = new SimpleDateFormat("dd-MM-yyyy");
		
		try {
			Date initialDate = inputSdf.parse(initDate);
			queryData.setFromDate(outputSdf.format(initialDate));
		} catch (Exception e) {}
		
		try {
			Date finalDate = inputSdf.parse(endDate);
			queryData.setToDate(outputSdf.format(finalDate));
		} catch (Exception e) {}
		
		return queryData;
	}

}
