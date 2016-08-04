package co.com.tecnocom.csj.persistence;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class PropertiesUtil extends Properties {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Log _log = LogFactoryUtil.getLog(PropertiesUtil.class);

	private PropertiesUtil() {
		InputStream is = getClass().getResourceAsStream("BD_AUTOR_CORE.properties");
		try {
			this.load(is);
		} catch (IOException ex) {
			_log.error("ConfigurationPropertiesInit", ex);
		}
	}

	public static PropertiesUtil getInstance() {
		return PropertiesUtilHolder.INSTANCE;
	}

	private static class PropertiesUtilHolder {

		private static final PropertiesUtil INSTANCE = new PropertiesUtil();
	}
}
