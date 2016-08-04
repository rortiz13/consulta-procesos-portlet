package co.com.tecnocom.csj.util.export;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletContext;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;

import org.apache.log4j.Logger;

import co.com.tecnocom.csj.core.util.dto.ProcessData;

import com.liferay.mail.service.MailServiceUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.mail.Account;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;

public enum ExportUtil {
	INSTANCE;

	private static final String ATTACHED_EMAIL_LOCATION = "/html/static/AttachedMail.html";
	private static final String EMPTY_EMAIL_LOCATION = "/html/static/EmptyMail.html";
//	private static final String BANNER_LOCATION = "/html/static/banner.png";

	private SimpleDateFormat sdf = new SimpleDateFormat("EEEEE d 'de' MMMMM 'de' yyyy");
	private Logger _log = Logger.getLogger(getClass());

	public void exportProcesses(Set<ProcessData> processes, QueryData queryData, String email, String hostname, ServletContext servletContext) {
		System.out.println("exportprocesss  ***** ");
		if (processes != null && processes.size() > 0) {
			List<JasperPrint> jasperPrintList = new LinkedList<JasperPrint>();

			try {
				JasperReport report = JasperCompileManager.compileReport(ExportUtil.class.getResourceAsStream("/co/com/tecnocom/csj/util/export/csj_process.jrxml"));

				for (ProcessData processData : processes) {
					Map<String, Object> parameters = new LinkedHashMap<String, Object>();
					parameters.put("CSJ_QueryDate", sdf.format(new Date()));
					parameters.put("CSJ_ProcessCode", processData.getProcessNumber());
					parameters.put("CSJ_ProcessCity", processData.getProcessCity());
					parameters.put("CSJ_ProcessSpecialty", processData.getProcessSpecialty());
					
					parameters.put("CSJ_ProcessCorporation", processData.getProcessCorporation());
					parameters.put("CSJ_ProcessPerson", processData.getProcessPerson());
					
					parameters.put("CSJ_ProcessClass", processData.getProcessClass());
					
					parameters.put("CSJ_ProcessSubjects0001", processData.getSubjectsCode0001());
					parameters.put("CSJ_ProcessSubjects0002", processData.getSubjectsCode0002());

					parameters.put("CSJ_ActuacionDespacho", processData.getActuacionDespacho());
					parameters.put("CSJ_AnotacionDespacho", processData.getAnotacionDespacho());
					parameters.put("CSJ_FechaDespacho", processData.getFechaActuacionDespacho());

					parameters.put("CSJ_ActuacionSecretaria", processData.getActuacionSecretaria());
					parameters.put("CSJ_AnotacionSecretaria", processData.getAnotacionSecretaria());
					parameters.put("CSJ_FechaSecretaria", processData.getFechaActuacionSecretaria());
					parameters.put("CSJ_FechaInicialSecretaria", processData.getFechaInicialSecretaria());
					parameters.put("CSJ_FechaFinalSecretaria", processData.getFechaFinalSecretaria());

					JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
					jasperPrintList.add(print);
				}

				// JasperExportManager.exportReportToPdfFile(print,
				// "C:\\Software\\Jaspersoft Studio-5.5.0.final\\JaspersoftWorkspace\\CSJ\\JasperPDF.pdf");

				JRPdfExporter PdfExporter = new JRPdfExporter();
				PdfExporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrintList);

				String filePath = PropsUtil.get(PropsKeys.LIFERAY_HOME) + "/data/process_files/";
				// Si el directorio no existe, se intenta crear
				try {
					File directory = new File(filePath);
					if (directory.mkdirs()) {
						_log.info("Directory created");
					} else {
						_log.info("Directory exists");
					}
				} catch (Exception ex) {
					_log.error("Exception with directory", ex);
				}

				String fileName = "Process_" + System.currentTimeMillis() + ".pdf";
				String exportedFile = filePath + fileName;
				_log.info("Exporting PDF in: " + exportedFile);

				PdfExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, exportedFile);
				PdfExporter.exportReport();

				// Enviar correo con el reporte generado
				try {
					generateMailMessage(email, queryData, hostname, servletContext, filePath, fileName);
				} catch (MessagingException me) {
					me.printStackTrace();
				} catch (SystemException se) {
					se.printStackTrace();
				}
			} catch (JRException e) {
				_log.error("Exception in PDF generation", e);
				e.printStackTrace();
			}
		} else {
			//	Enviar correo con mensaje genérico
			try {
				generateMailMessage(email, queryData, hostname, servletContext, null, null);
			} catch (MessagingException me) {
				me.printStackTrace();
			} catch (SystemException se) {
				se.printStackTrace();
			}
		}
	}

	private void generateMailMessage(String email, QueryData queryData, String hostname, ServletContext servletContext, String filePath, String fileName) throws MessagingException, SystemException {
		_log.info("generateMailMessage");
		Session session = MailServiceUtil.getSession();
		String protocol = GetterUtil.getString(session.getProperty("mail.transport.protocol"), Account.PROTOCOL_SMTP);
		String smtpHost = session.getProperty("mail." + protocol + ".host");
		int smtpPort = GetterUtil.getInteger(session.getProperty("mail." + protocol + ".port"), Account.PORT_SMTP);
		String user = session.getProperty("mail." + protocol + ".user");
		String password = session.getProperty("mail." + protocol + ".password");
		
		_log.info("Construir mailMessage");
		Message mailMessage = new MimeMessage(session);
		mailMessage.setFrom(new InternetAddress(user));
//		Message mailMessage = new Message(session);
//		mailMessage.setHTMLFormat(true);
		mailMessage.setRecipient(RecipientType.TO, new InternetAddress(email));
//		mailMessage.setRecipient(RecipientType.BCC, new InternetAddress("andread.soul2@gmail.com"));
//		mailMessage.setBCC(new InternetAddress("andread.soul2@gmail.com"));
//		mailMessage.setFrom(new InternetAddress(session.getProperty("mail.smtps.")));
		mailMessage.setSubject("Consulta de Procesos - Portal Rama Judicial");
		
		//	Email Date
		SimpleDateFormat sdf = new SimpleDateFormat("EEEEE dd 'de' MMMMM 'de' yyyy");
		String currentDate = sdf.format(new Date());
		
//		String[] htmlParameters = new String[] { currentDate };
		String htmlData = getHtmlFile(servletContext, EMPTY_EMAIL_LOCATION);
		
		//	Si el archivo adjunto fue generado correctamente
		if (fileName != null && !fileName.isEmpty()) {
			htmlData = getHtmlFile(servletContext, ATTACHED_EMAIL_LOCATION);
			
//			File attachedFile = new File(filePath + fileName);
//	        mailMessage.addFileAttachment(attachedFile, fileName);
		}
			
//		System.out.println("HTMLDATA: ");
//		System.out.println(htmlData);
//		
//		System.out.println("PARAMETERS: ");
//		for (int i = 0; i < htmlParameters.length; i++) {
//			System.out.println(htmlParameters[i]);	
//		}
		
		_log.info("Generar HtmlData");
		Multipart multipart = new MimeMultipart("mixed");
//		htmlData = setParameters(htmlData, htmlParameters);
		htmlData = htmlData.replace("##hostname##", hostname).replace("##report_date##", currentDate);
		
		// Datos de la Consulta
		htmlData = replaceQueryData(htmlData, queryData);
		
//		_log.info("Resultado:\n" + htmlData);
		
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setHeader("Content-Type", "text/html; charset=\"utf-8\"");
		htmlPart.setContent(htmlData, "text/html; charset=utf-8");
		multipart.addBodyPart(htmlPart);
		
		if (fileName != null && !fileName.isEmpty()) {
			BodyPart messageBodyPart = new MimeBodyPart();
	        DataSource source = new FileDataSource(filePath + fileName);
	        messageBodyPart.setDataHandler(new DataHandler(source));
	        messageBodyPart.setFileName(fileName);
	        multipart.addBodyPart(messageBodyPart);
		}
		
//		BodyPart imgPart = new MimeBodyPart();
//        DataSource ds = new  FileDataSource("c:/tmp/img.gif");
		
//		DataSource ds = new URLDataSource(servletContext.getResource(BANNER_LOCATION));
//		DataSource ds = new InputStreamDataSource(servletContext.getResourceAsStream(BANNER_LOCATION));
		
//		DataSource ds = new FileDataSource(filePath + "banner.png");
//		imgPart.setDataHandler(new DataHandler(ds));
//        imgPart.setHeader("Content-ID","<banner-img>");
//        imgPart.setDisposition(MimeBodyPart.INLINE);
//        multipart.addBodyPart(imgPart);

        mailMessage.setContent(multipart);
		
        //	TODO: Sólo para pruebas locales
        //	FIXME: Sólo para pruebas locales
//		System.setProperty("http.proxyHost", "10.25.0.210");
//		System.setProperty("http.proxyPort", "8080");
		

//		_log.info(session.getProperty("mail.smtps.user"));
//		_log.info(session.getProperty("mail.smtps.password"));
		
		_log.info("Send Message "/* + protocol + ", " + smtpHost + ", " + smtpPort + ", " + user + ", " + password*/);
		
		Transport transport = session.getTransport(protocol);
		transport.connect(smtpHost, smtpPort, user, password);
        transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
        transport.close();
        
        _log.info("sent");
		
//        MailServiceUtil.sendEmail(mailMessage);
        
        
        if (fileName != null && !fileName.isEmpty()) {
        	//	Borrar el archivo
        	_log.info("Mensaje enviado, borrando archivo - " + fileName);
        	File attachedFile = new File(filePath + fileName);
            if(attachedFile.delete()) {
            	_log.info("File deleted");
            } else {
            	_log.info("Error deleting file");
            }
        } else {
        	_log.info("Mensaje Enviado");
        }

//		EmailUtil.INSTANCE.sendEmail("info@cendoj.ramajudicial.gov.co", email, "Consulta de Procesos - Portal Rama Judicial", servletContext, ATTACHED_EMAIL_LOCATION, new String[] { currentDate });

	}

//	private String setParameters(String htmlData, String... parameters) {
//		System.out.println(htmlData);
//		System.out.println(String.format("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">	<title>Consulta de Procesos - Portal Rama Judicial</title></head><body>	<div id=\"mailContent\" style=\"font-family: Arial, Verdana; width: 530px; font-size: 14px; line-height: 14px;\">		<table cellspacing=\"0\" border=\"0\" cellpadding=\"0\" width=\"100%%\">			<tr>				<td>					<table cellspacing=\"0\" border=\"0\" align=\"center\" cellpadding=\"0\" width=\"532\">						<tr>							<td valign=\"top\">								<img src=\"<hostname>/documents/10228/2189662/Header_Consulta_de_Procesos.png/ec971866-34d2-4d8b-8bef-8d0acd6734f6\" width=\"532\" height=\"92\" alt=\"Rama Judicial del Poder Público\" />																<table cellspacing=\"0\" border=\"0\" cellpadding=\"0\" width=\"532\" bgcolor=\"#ffffff\" style=\"padding: 20px; margin: 10px 0px; border-radius: 6px; background: #ffffff; border: 1px solid #DDD;\">									<tr>										<td align=\"right\">											<span><report_date></span>										</td>									</tr>									<tr>										<td align=\"left\">											<p>Se&ntilde;or(a) usuario(a):</p>											<p>No se encontraron procesos con los datos proporcionados.</p>										</td>									</tr>								</table>																<table cellspacing=\"0\" border=\"0\" cellpadding=\"0\" width=\"532\" bgcolor=\"#ffffff\" style=\"background-color: #006DCA; border-radius: 6px; padding: 20px; color: #FFFFFF;\">									<tr>										<td align=\"center\">											<p style=\"margin: 2px;\"><span style=\"font-weight: bold\">Rama Judicial</span></p>											<p style=\"margin: 2px;\">Calle 12 No. 7-65 Bogot&aacute; Colombia</p>											<p style=\"margin: 2px;\">PBX: <a style=\"text-decoration: none; color: #FFFFFF;\" href=\"tel:%28571%29%20565%2085%2000\" value=\"+15715658500\" target=\"_blank\">(571) 565 85 00</a> - Email: <a href=\"mailto:info.cendoj@ramajudicial.gov.co\" style=\"text-decoration: none; color: #FFFFFF;\">info.cendoj@ramajudicial.gov.co</a></p>										</td>									</tr>								</table>							</td>						</tr>					</table>				</td>			</tr>		</table>	</div></body></html>", (Object[])parameters));
//		return String.format(htmlData, (Object[])parameters);
//	}

	private String replaceQueryData(String htmlData, QueryData queryData) {
		String replacedString = htmlData;
		
		if(queryData.getName() == null || queryData.getName().isEmpty()) { replacedString = replacedString.replace("##query_name##", ""); }
		else { replacedString = replacedString.replace("##query_name##", "<span>Nombre: " + queryData.getName() + "</span><br />"); }
		
		if(queryData.getDocument() == null || queryData.getDocument().isEmpty()) { replacedString = replacedString.replace("##query_document##", ""); }
		else { replacedString = replacedString.replace("##query_document##", "<span>Documento: " + queryData.getDocument() + "</span><br />"); }
		
		if(queryData.getCity() == null || queryData.getCity().isEmpty()) { replacedString = replacedString.replace("##query_city##", ""); }
		else { replacedString = replacedString.replace("##query_city##", "<span>Ciudad: " + queryData.getCity() + "</span><br />"); }
		
		if(queryData.getSpecialty() == null || queryData.getSpecialty().isEmpty()) { replacedString = replacedString.replace("##query_specialty##", ""); }
		else { replacedString = replacedString.replace("##query_specialty##", "<span>Especialidad: " + queryData.getSpecialty() + "</span><br />"); }
		
		if(queryData.getDateOption() == null || queryData.getDateOption().isEmpty()) { replacedString = replacedString.replace("##query_date##", ""); }
		else { replacedString = replacedString.replace("##query_date##", "<span>" + queryData.getDateOption() + " entre: " + queryData.getFromDate() + " - " + queryData.getToDate() + "</span>"); }
		
		return replacedString;
	}

	private final String getHtmlFile(ServletContext servletContext, String path) {
		InputStream is = null;
		try {
			is = servletContext.getResourceAsStream(path);
			Scanner s = new Scanner(is).useDelimiter("\\A");
			String data = s.hasNext() ? s.next() : "";
			s.close();

			return data;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
