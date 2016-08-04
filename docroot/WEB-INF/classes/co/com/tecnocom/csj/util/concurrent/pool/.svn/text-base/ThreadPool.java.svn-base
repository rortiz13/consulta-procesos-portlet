package co.com.tecnocom.csj.util.concurrent.pool;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletContext;

import co.com.tecnocom.csj.util.concurrent.ProcessQueryThread;
import co.com.tecnocom.csj.util.concurrent.ProcessQueryWorker;

public enum ThreadPool {
	INSTANCE;

	private ExecutorService executorService;

	private ThreadPool() {
		executorService = Executors.newCachedThreadPool();
	}
	
	public void query(Map<String, String> datasources_databases_map, String name, String document, String dateOption, String initDate, String endDate, String cityCode, String corporationSpecialtyCode, String mail, String hostname, ServletContext servletContext) {
		executorService.execute(new ProcessQueryThread(datasources_databases_map, name, document, dateOption, initDate, endDate, cityCode, corporationSpecialtyCode, mail, hostname, servletContext));
	}
	//	new ProcessQueryThread(datasources_databases_map, name, cc.equals(0L) ? null : cc.toString(), dateOption, sdf.format(initDate.getTime()), sdf.format(endDate.getTime()));

	@SuppressWarnings("rawtypes")
	public Future submit(String dataSource, String processDatabase, Integer resultsLimit, String name, String document, String dateType, String fromDate, String toDate, String cityCode, String corporationSpecialtyCode) {
		return executorService.submit(new ProcessQueryWorker(dataSource, processDatabase, resultsLimit, name, document, dateType, fromDate, toDate, cityCode, corporationSpecialtyCode));
	}
}
