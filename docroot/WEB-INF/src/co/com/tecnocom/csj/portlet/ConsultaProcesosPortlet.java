package co.com.tecnocom.csj.portlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import co.com.tecnocom.csj.core.util.dto.ProcessData;
import co.com.tecnocom.csj.util.DataUtil;
import co.com.tecnocom.csj.util.SpecialtyConnection;
import co.com.tecnocom.csj.util.concurrent.pool.ThreadPool;
import co.com.tecnocom.csj.util.export.ExportUtil;
import co.com.tecnocom.csj.util.export.QueryData;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class ConsultaProcesosPortlet
 */
public class ConsultaProcesosPortlet extends MVCPortlet {
	
	private Logger _log = Logger.getLogger(getClass());
	private boolean queryDone = false;
			
	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
		if(!queryDone) {
			super.doView(renderRequest, renderResponse);
		} else {
			queryDone = false;
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/html/consultaprocesos/view_land.jsp");
			prd.include(renderRequest, renderResponse);
		}
		
//		DataUtil.getInstance(renderRequest.getPreferences(), renderRequest.getLocale());
	}

	public void companyQuery(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
		personQuery(actionRequest, actionResponse);
	}
	
	public void personQuery(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
		String name = ParamUtil.getString(actionRequest, "name", "");
		Long document = ParamUtil.getLong(actionRequest, "document" , 0L);
		String mail = ParamUtil.getString(actionRequest, "email", "");
		String cityCode = ParamUtil.getString(actionRequest, "city", "");
		String corporationSpecialtyCode = ParamUtil.getString(actionRequest, "specialty", "");
		String dateOption = ParamUtil.getString(actionRequest, "dateOption", "processStartDate");
		
		Calendar current = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		/* Fecha Inicio */
		Integer initYear = ParamUtil.getInteger(actionRequest, "init-year", current.get(Calendar.YEAR));
		Integer initMonth = ParamUtil.getInteger(actionRequest, "init-month", current.get(Calendar.MONTH));
		Integer initDay = ParamUtil.getInteger(actionRequest, "init-day", current.get(Calendar.DATE));
		Calendar initDate = Calendar.getInstance();
		initDate.set(Calendar.YEAR, initYear);
		initDate.set(Calendar.MONTH, initMonth);
		initDate.set(Calendar.DATE, initDay);
		
		/* Fecha Fin */
		Integer endYear = ParamUtil.getInteger(actionRequest, "end-year", current.get(Calendar.YEAR));
		Integer endMonth = ParamUtil.getInteger(actionRequest, "end-month", current.get(Calendar.MONTH));
		Integer endDay = ParamUtil.getInteger(actionRequest, "end-day", current.get(Calendar.DATE));
		Calendar endDate = Calendar.getInstance();
		endDate.set(Calendar.YEAR, endYear);
		endDate.set(Calendar.MONTH, endMonth);
		endDate.set(Calendar.DATE, endDay);
		
		/* Hostname */
		HttpServletRequest request = PortalUtil.getHttpServletRequest(actionRequest);
		String hostname = request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getLocalPort();
//		_log.info("Hostname: " + hostname);
		
		
		//	Basado en la ciudad, corporacion/especialidad seleccionada, generar el mapa de datasources
		Map<String, String> datasources_databases_map = DataUtil.getInstance(actionRequest.getPreferences(), actionRequest.getLocale()).generateDatasourcesMap(cityCode, corporationSpecialtyCode); 
//				new LinkedHashMap<String, String>();
//		datasources_databases_map.put("ds1", "consejo");
//		datasources_databases_map.put("ds2", "consejo_prueba");
//		datasources_databases_map.put("ds3", "consejo");
//		datasources_databases_map.put("ds4", "consejo_prueba");
//		datasources_databases_map.put("ds5", "consejo");
//		datasources_databases_map.put("ds6", "consejo_prueba");
		
		_log.info("Portlet starting thread");
//		for(String datasource : datasources_databases_map.keySet()) {
//			_log.info("Datasource: " + datasource);
//			_log.info("Database: " + datasources_databases_map.get(datasource));
//		}
		ServletContext servletContext = request.getSession().getServletContext();
		ThreadPool.INSTANCE.query(datasources_databases_map, name, document.equals(0L) ? null : document.toString(), dateOption, sdf.format(initDate.getTime()), sdf.format(endDate.getTime()), cityCode, corporationSpecialtyCode, mail, hostname, servletContext);
		
		queryDone = true;
		_log.info("Portlet finishing view");
	}
	
	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException, PortletException {
		String cityCode = ParamUtil.getString(resourceRequest, "cityCode");
//		_log.info("city: " + cityCode);
		JSONArray jArray = JSONFactoryUtil.createJSONArray();
		List<SpecialtyConnection> specialties = DataUtil.getInstance(resourceRequest.getPreferences(), resourceRequest.getLocale()).getSpecialtiesByCity(cityCode);
		for (SpecialtyConnection specialty : specialties) {
			jArray.put(JSONFactoryUtil.createJSONObject().put("code", specialty.getCorporationSpecialtyCode()).put("name", specialty.getSpecialtyCustomName()));
		}
		
		JSONObject jResponse = JSONFactoryUtil.createJSONObject();
		jResponse.put("specialties", jArray);
		
//		_log.info(jResponse);
		resourceResponse.getWriter().write(jResponse.toString());
		super.serveResource(resourceRequest, resourceResponse);
	}
	
	
	public void savePreferences(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
		PortletPreferences prefs = actionRequest.getPreferences();
		Integer startingYearRange = ParamUtil.getInteger(actionRequest, "startingYearRange", 1980);
		Integer maxTimeLapseInDays = ParamUtil.getInteger(actionRequest, "maxTimeLapseInDays", 365);
		
		Integer idProcessQueries = ParamUtil.getInteger(actionRequest, "idProcessQueries", 1463818);
		
		prefs.setValue("startingYearRange", startingYearRange.toString());
		prefs.setValue("maxTimeLapseInDays", maxTimeLapseInDays.toString());
		
		prefs.setValue("idProcessQueries", idProcessQueries.toString());
		
		prefs.store();
	}
	
	public void testExporter(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
		QueryData q = new QueryData();
		q.setCity("City");
		q.setDateOption("DateOption");
		q.setFromDate("From");
		q.setToDate("To");
		
		HttpServletRequest request = PortalUtil.getHttpServletRequest(actionRequest);
		ServletContext servletContext = request.getSession().getServletContext();
		
		// set process data to test jasper
		ProcessData processData = new ProcessData();
		processData.setActuacionDespacho("Actuacion del Despacho Actuacion del Despacho Actuacion del Despacho Actuacion del Despacho ");
		processData.setActuacionSecretaria("Actuacion de la secretaria Actuacion de la secretaria Actuacion de la secretaria Actuacion de la secretaria ");
		processData.setAnotacionDespacho("Anotacion Despacho Anotacion Despacho Anotacion Despacho Anotacion Despacho Anotacion Despacho ");
		processData.setAnotacionSecretaria("Anotacion Secretaria Anotacion Secretaria Anotacion Secretaria Anotacion Secretaria ");
		processData.setFechaActuacionDespacho("10/10/2010");
		processData.setFechaActuacionSecretaria("10/10/2010");
		processData.setFechaFinalSecretaria("10/10/2010");
		processData.setFechaInicialSecretaria("10/10/2010");
		processData.setProcessCity("Ciudad del proceso");
		processData.setProcessCorporation("Corporacion");
		processData.setProcessDate("Fecha del Proceso");
		processData.setProcessNumber("Numero del Proceso 02020321351350");
		processData.setProcessPerson("Persona Ponente");
		processData.setProcessSpecialty("Especialidad del proceso");
		processData.setProcessClass("Clase del Proceso");
		
		List<String> demandantes = new LinkedList<String>();
		demandantes.add("Demandante Demandante Demandante 1");
		processData.setSubjectsCode0001(demandantes);
		
		List<String> demandados = new LinkedList<String>();
		demandados.add("Demandado Demandado Demandado 1");
		processData.setSubjectsCode0002(demandados);
		
		// 1
		Set<ProcessData> consolidatedSet = new LinkedHashSet<ProcessData>();
		consolidatedSet.add(processData);
		ExportUtil.INSTANCE.exportProcesses(consolidatedSet, q, "jromero@ability.com.co", "http://csjportalpruebas.ability.com.co", servletContext);
		
		// 2
		consolidatedSet = new LinkedHashSet<ProcessData>();
		demandantes.add("Demandante Demandante Demandante 2");
		demandados.add("Demandado Demandado Demandado 2");
		processData.setProcessNumber("Numero del Proceso 02020321351350 2");
		consolidatedSet.add(processData);
		ExportUtil.INSTANCE.exportProcesses(consolidatedSet, q, "jromero@ability.com.co", "http://csjportalpruebas.ability.com.co", servletContext);
		
		// 3
		consolidatedSet = new LinkedHashSet<ProcessData>();
		demandantes.add("Demandante Demandante Demandante 3");
		demandados.add("Demandado Demandado Demandado 3");
		processData.setProcessNumber("Numero del Proceso 02020321351350 3");
		consolidatedSet.add(processData);
		ExportUtil.INSTANCE.exportProcesses(consolidatedSet, q, "jromero@ability.com.co", "http://csjportalpruebas.ability.com.co", servletContext);
		
		// 4
		consolidatedSet = new LinkedHashSet<ProcessData>();
		demandantes.add("Demandante Demandante Demandante 4");
		demandados.add("Demandado Demandado Demandado 4");
		processData.setProcessNumber("Numero del Proceso 02020321351350 4");
		consolidatedSet.add(processData);
		ExportUtil.INSTANCE.exportProcesses(consolidatedSet, q, "jromero@ability.com.co", "http://csjportalpruebas.ability.com.co", servletContext);
	}
}
