package co.com.tecnocom.csj.util;

public class SpecialtyConnection {

	private String cityCode;
	private String corporationSpecialtyCode;
	private String specialtyCustomName;

	private String connectionDatabase;
	private String connectionDatasource;
	
	public SpecialtyConnection(String cityCode) {
		super();
		this.cityCode = cityCode;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getCorporationSpecialtyCode() {
		return corporationSpecialtyCode;
	}

	public void setCorporationSpecialtyCode(String corporationSpecialtyCode) {
		this.corporationSpecialtyCode = corporationSpecialtyCode;
	}

	public String getSpecialtyCustomName() {
		return specialtyCustomName;
	}

	public void setSpecialtyCustomName(String specialtyCustomName) {
		this.specialtyCustomName = specialtyCustomName;
	}

	public String getConnectionDatabase() {
		return connectionDatabase;
	}

	public void setConnectionDatabase(String connectionDatabase) {
		this.connectionDatabase = connectionDatabase;
	}

	public String getConnectionDatasource() {
		return connectionDatasource;
	}

	public void setConnectionDatasource(String connectionDatasource) {
		this.connectionDatasource = connectionDatasource;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cityCode == null) ? 0 : cityCode.hashCode());
		result = prime * result + ((corporationSpecialtyCode == null) ? 0 : corporationSpecialtyCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpecialtyConnection other = (SpecialtyConnection) obj;
		if (cityCode == null) {
			if (other.cityCode != null)
				return false;
		} else if (!cityCode.equals(other.cityCode))
			return false;
		if (corporationSpecialtyCode == null) {
			if (other.corporationSpecialtyCode != null)
				return false;
		} else if (!corporationSpecialtyCode.equals(other.corporationSpecialtyCode))
			return false;
		return true;
	}

}
