package co.com.tecnocom.csj.util.export;

import com.liferay.portal.kernel.util.StringPool;

public class QueryData {

	private String name;
	private String document;
	private String city;
	private String specialty;
	private String dateOption;
	private String fromDate;
	private String toDate;
	
	public QueryData() {
		this.name = StringPool.BLANK;
		this.document = StringPool.BLANK;
		this.city = StringPool.BLANK;
		this.specialty = StringPool.BLANK;
		this.dateOption = StringPool.BLANK;
		this.fromDate = StringPool.BLANK;
		this.toDate = StringPool.BLANK;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDocument() {
		return document;
	}
	public void setDocument(String document) {
		this.document = document;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getSpecialty() {
		return specialty;
	}
	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}
	public String getDateOption() {
		return dateOption;
	}
	public void setDateOption(String dateOption) {
		this.dateOption = dateOption;
	}
	public String getFromDate() {
		if(fromDate == null){
			return StringPool.BLANK;
		}
			
		return fromDate;
	}
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	public String getToDate() {
		if(toDate == null) {
			return StringPool.BLANK;
		}
		
		return toDate;
	}
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
}
