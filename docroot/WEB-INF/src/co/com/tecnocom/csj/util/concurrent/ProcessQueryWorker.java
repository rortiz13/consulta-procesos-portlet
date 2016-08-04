package co.com.tecnocom.csj.util.concurrent;

import java.util.List;
import java.util.concurrent.Callable;

import co.com.tecnocom.csj.core.util.ProcessQueriesUtil;
import co.com.tecnocom.csj.core.util.dto.ProcessData;

public class ProcessQueryWorker implements Callable<List<ProcessData>> {
	private String dataSource;
	private String processDatabase;
	private Integer resultsLimit;
	private String name;
	private String document;
	private String dateType;
	private String fromDate;
	private String toDate;
	private String cityCode;
	private String corporationSpecialtyCode;
	
		
	public ProcessQueryWorker(String dataSource, String processDatabase, Integer resultsLimit, String name, String document, String dateType, String fromDate, String toDate, String cityCode, String corporationSpecialtyCode) {
		this.dataSource = dataSource;
		this.processDatabase = processDatabase;
		this.resultsLimit = resultsLimit;
		this.name = name;
		this.document = document;
		this.dateType = dateType;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.cityCode = cityCode;
		this.corporationSpecialtyCode = corporationSpecialtyCode;
	}

	@Override
	public List<ProcessData> call() throws Exception {
		//	Si el corporationSpecialtyCode es de 2 partes, procesarlo
		if(null != corporationSpecialtyCode && !corporationSpecialtyCode.isEmpty() && corporationSpecialtyCode.split("_").length == 2) {
			corporationSpecialtyCode = corporationSpecialtyCode.substring(0, corporationSpecialtyCode.indexOf("_"));
		}
		
		System.out.println("findProcessQueries call -> datasource(" + dataSource + "), processDatabase(" + processDatabase + "), cityCode(" + cityCode + "), corporationSpecialtyCode(" + corporationSpecialtyCode + ")");
		return ProcessQueriesUtil.INSTANCE.findProcessQueries(dataSource, processDatabase, resultsLimit, name, document, dateType, fromDate, toDate, cityCode, corporationSpecialtyCode);
	}

}
