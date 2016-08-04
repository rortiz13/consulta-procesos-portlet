<%@ include file="../init.jsp" %>

<%@page import="co.com.tecnocom.csj.util.DataUtil"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Calendar"%>
<%@page import="co.com.tecnocom.csj.core.util.dto.locationFinder.Specialty"%>
<%@page import="co.com.tecnocom.csj.core.util.ProcessQueriesUtil"%>
<%@page import="co.com.tecnocom.csj.core.util.dto.locationFinder.City"%>
<%@page import="java.util.List"%>

<style>
	.buttonLink { color: #FFF !important; text-shadow: none !important; }
</style>

<%
	// Criterios
	Map<String, String> cities = DataUtil.getInstance(renderRequest.getPreferences(), renderRequest.getLocale()).getAllCities();
	Calendar calendar = Calendar.getInstance();
	
	Integer startingYearRange = Integer.valueOf(renderRequest.getPreferences().getValue("startingYearRange", "2000"));
	Integer maxTimeLapseInDays = Integer.valueOf(renderRequest.getPreferences().getValue("maxTimeLapseInDays", "365"));
%>

<portlet:actionURL name="personQuery" var="personQuery"/>
<portlet:actionURL name="companyQuery" var="companyQuery" />

<portlet:actionURL name="testExporter" var="testExporter" />
<div>
<form action="<%= testExporter %>" id="testExporter" name="testExporter" method="POST">
	<button type="submit">Probar Export</button>
</form>
</div>

<div id="formBox">
	<liferay-ui:tabs names="person,company" refresh="false">
		<liferay-ui:section>
			<aui:form id="process-query-person-form" name="process-query-person-form" action="<%= personQuery %>" inlineLabels="true" method="POST">
				<aui:fieldset cssClass="fieldSet">
					<aui:input name="hiddenField" type="hidden">
						<aui:validator name="required"/>
					</aui:input>
					
					<div class="portlet-msg-info">
						Recuerde que debe ingresar los datos para consultar de la misma forma en la que quedaron registrados en el Proceso.
					</div>
					
					<aui:layout>
					 	<aui:column first="true" columnWidth="50">
							<aui:input name="name" type="text" showRequiredLabel="">
<%-- 								<aui:validator name="required"/> --%>
								<aui:validator name="custom" errorMessage="name-error-message">
								    function(value, fieldNode, ruleValue) {
								    	var RegExpression = /^[a-zA-Z\s]*$/;
								    	if (RegExpression.test(value)) { 
								    		return true;
								    	} else {
								          	return false;
								      	}
								    }
								</aui:validator>
							</aui:input>
							
							<aui:input name="document" type="text" showRequiredLabel="" label="cc">
<%-- 								<aui:validator name="required"/> --%>
								<aui:validator name="digits"/>
							</aui:input>
							
							<aui:input name="email" type="text" showRequiredLabel="">
								<aui:validator name="required"/>
								<aui:validator name="email"/>
							</aui:input>
						</aui:column>
						
						<aui:column last="true" columnWidth="50">
							<aui:select name="city" showEmptyOption="true" inputCssClass="param">
								<%-- 
								<% for(City city : cities) { %>
								<aui:option label="<%= city.getName() %>" value="<%= city.getCode() %>" />
								<% } %>
								--%>
								<% for(String cityCode : cities.keySet()) { %>
								<aui:option label="<%= cities.get(cityCode) %>" value="<%= cityCode %>" />
								<% } %>
							</aui:select>
							
							<aui:select name="specialty" showEmptyOption="true" inputCssClass="param">
								<%-- 
								<% for(Specialty specialty : specialties) { %>
								<aui:option label="<%= specialty.getName() %>" value="<%= specialty.getCode() %>" />
								<% } %>
								--%>
							</aui:select>
						</aui:column>
					</aui:layout>
					
					
					<aui:field-wrapper name="dateOption" inlineLabel="true">
						<aui:layout>
							<aui:column first="true" columnWidth="25">
								<aui:input name="dateOption" type="radio" value="processStartDate" label="processStartDate" checked="<%= true %>" />
								<aui:input name="dateOption" type="radio" value="actDate" label="actDate" />
							</aui:column>
							
							<aui:column columnWidth="35" cssClass="dateField">
								<aui:field-wrapper label="date-from">
									<liferay-ui:input-date 
										yearRangeEnd="<%= calendar.get(Calendar.YEAR) %>"
										yearRangeStart="<%= startingYearRange %>"
										yearNullable="false"
										yearParam="init-year"
										yearValue="<%= startingYearRange %>"
										
										monthNullable="false"
										monthValue="<%= Calendar.JANUARY %>"
										monthParam="init-month"
										
										dayNullable="false"
										dayValue="1"
										dayParam="init-day"
									/>
								</aui:field-wrapper>
							</aui:column>
								
							<aui:column last="true" columnWidth="35" cssClass="dateField">
								<aui:field-wrapper label="date-to">
									<liferay-ui:input-date 
										yearRangeEnd="<%= calendar.get(Calendar.YEAR) %>" 
										yearRangeStart="<%= startingYearRange %>"
										yearNullable="false"
										yearParam="end-year"
										yearValue="<%= calendar.get(Calendar.YEAR) %>"
										
										monthNullable="false"
										monthValue="<%= calendar.get(Calendar.MONTH) %>"
										monthParam="end-month"
										
										dayNullable="false"
										dayValue="<%= calendar.get(Calendar.DAY_OF_MONTH) %>"
										dayParam='end-day'
									/>
								</aui:field-wrapper>
							</aui:column>
							
							<aui:column columnWidth="75" last="true">
								<div class="portlet-msg-info" style="margin-right: 30px;">
									El rango de fechas para la consulta es de m&aacute;ximo <%= maxTimeLapseInDays %> d&iacute;as.
								</div>
							</aui:column>
						</aui:layout>
					</aui:field-wrapper>
				</aui:fieldset>
				
				<aui:button type="submit" onClick="verifyPersonData()" value="generateReport" inputCssClass="buttonLink"/>
				
				<script type="text/javascript">
					var A = AUI();
					
					function verifyPersonData() {
						var err = false;
						var alerted = false;
						
						console.log("Verify - err: ", err);
						
						if(A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />city").val() == "") {
							if(!A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />city").ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
								A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />city").addClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").addClass("aui-form-validator-error-container").one(".aui-field-element").append("<label class='aui-form-validator-stack-error'><div class='aui-form-validator-message city' role='alert'>Por favor, seleccione una ciudad.</div></label>");
							}
							
							err = true;
						}
						
						var dayDiffBetweenDates = daydiff(A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />init-day").val(), A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />init-month").val(), A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />init-year").val(), A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />end-day").val(), A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />end-month").val(), A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />end-year").val());
						console.log("Diff: " + dayDiffBetweenDates, "Max: <%= maxTimeLapseInDays%>");
						
						if(Number(dayDiffBetweenDates) < 0) {
							alert("La fecha inicial no puede ser mayor a la fecha final");
							err = true;
							alerted = true;
						} else if(Number(dayDiffBetweenDates) > <%= maxTimeLapseInDays %>) {
							alert("El No. m�ximo de d�as entre la fecha inicial y la fecha final es de <%= maxTimeLapseInDays %> ");
							err = true;
							alerted = true;
						}
						
// 						if(A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />name").val() == "" && A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />document").val()) {
// 							alert("Debe ingresar el nombre o el documento para buscar");
// 							err = true;
// 							alerted = true;
// 						}
						
// 						console.log("End verify - err: ", err);
						
						if(!err) {
							A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />hiddenField").val("pass");
						} else {
							if(!alerted) {
								alert("Debe corregir los errores del formulario antes de realizar la consulta");								
							}
						}
					}
					
					A.ready("aui-io-request", "aui-loading-mask", function(){
						/* loadignmask para el ajax que trae las especialidades filtradas por ciudad */
						A.one("#<portlet:namespace/>process-query-person-form").plug(A.LoadingMask, { background: '#000' });
						
						A.on("change", function(){
							var cityCode = this.val();
							if(cityCode) {
								if(this.ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
									this.removeClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").removeClass("aui-form-validator-error-container").one(".aui-form-validator-stack-error").remove();
								}
								
								//	Query specialties by city
								A.io.request('<portlet:resourceURL/>', {
									dataType: 'json',
									cache: true,
									autoLoad: true,
									data: {
										cityCode: cityCode
									},
									on: {
										success: function(){
											var data = this.get('responseData');
											console.log("response", data);
											var specialties = data.specialties;
											var content = "<option value=''></option>";
											for(var i = 0; i < specialties.length; i++ ){
												content = content.concat("<option value='", specialties[i].code, "'>", specialties[i].name, "</option>");
											}
											A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />specialty").html(content);
										},
										start: function(){
											A.one("#<portlet:namespace/>process-query-person-form").loadingmask.toggle();
										},
										complete: function(){
											A.one("#<portlet:namespace/>process-query-person-form").loadingmask.toggle();
										}
									}
								});
							} else {
								//	Empty specialties list
								A.one("#<portlet:namespace />process-query-person-form #<portlet:namespace />specialty").html("<option value=''></option>");
							}
						}, "#<portlet:namespace />process-query-person-form #<portlet:namespace />city");
						
						A.on("blur", function(){
							if(this.val() == "" && !this.ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
								A.one("#<portlet:namespace />city").addClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").addClass("aui-form-validator-error-container").one(".aui-field-element").append("<label class='aui-form-validator-stack-error'><div class='aui-form-validator-message city' role='alert'>Por favor, seleccione una ciudad.</div></label>");
							}
						}, "#<portlet:namespace />process-query-person-form #<portlet:namespace />city");
						
						A.on("change", function(){
							if(this.val() != "" && this.ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
								this.removeClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").removeClass("aui-form-validator-error-container").one(".aui-form-validator-stack-error").remove();
							}
						}, "#<portlet:namespace />process-query-person-form #<portlet:namespace />specialty");
						
// 						A.on("blur", function(){
// 							if(this.val() == "" && !this.ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
// 								A.one("#<portlet:namespace />specialty").addClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").addClass("aui-form-validator-error-container").one(".aui-field-element").append("<label class='aui-form-validator-stack-error'><div class='aui-form-validator-message specialty' role='alert'>Por favor, seleccione una especialidad.</div></label>");
// 							}
// 						}, "#<portlet:namespace />process-query-person-form #<portlet:namespace />specialty");
					});
				</script>
			</aui:form>
		</liferay-ui:section>
		
		<liferay-ui:section>
			<aui:form id="process-query-company-form" name="process-query-company-form" action="<%= companyQuery %>" inlineLabels="true" method="POST">
				<aui:fieldset cssClass="fieldSet">
					<aui:input name="hiddenField" type="hidden">
						<aui:validator name="required"/>
					</aui:input>
					
					<div class="portlet-msg-info">
						Recuerde que debe ingresar los datos para consultar de la misma forma en la que quedaron registrados en el Proceso.
					</div>
					
					<aui:layout>
					 	<aui:column first="true" columnWidth="50">
							<aui:input name="name" type="text" showRequiredLabel="">
<%-- 								<aui:validator name="required"/> --%>
								<aui:validator name="custom" errorMessage="name-error-message">
								    function(value, fieldNode, ruleValue) {
								    	var RegExpression = /^[a-zA-Z\s]*$/;
								    	if (RegExpression.test(value)) { 
								    		return true;
								    	} else {
								          	return false;
								      	}
								    }
								</aui:validator>
							</aui:input>
							
							<aui:input name="document" type="text" showRequiredLabel="" label="nit">
<%-- 								<aui:validator name="required"/> --%>
								<aui:validator name="digits"/>
							</aui:input>
							
							<aui:input name="email" type="text" showRequiredLabel="">
								<aui:validator name="required"/>
								<aui:validator name="email"/>
							</aui:input>
						</aui:column>
						
						<aui:column last="true" columnWidth="50">
							<aui:select name="city" showEmptyOption="true" inputCssClass="param">
								<%-- 
								<% for(City city : cities) { %>
								<aui:option label="<%= city.getName() %>" value="<%= city.getCode() %>" />
								<% } %>
								--%>
								<% for(String cityCode : cities.keySet()) { %>
								<aui:option label="<%= cities.get(cityCode) %>" value="<%= cityCode %>" />
								<% } %>
							</aui:select>
							
							<aui:select name="specialty" showEmptyOption="true" inputCssClass="param">
								<%-- 
								<% for(Specialty specialty : specialties) { %>
								<aui:option label="<%= specialty.getName() %>" value="<%= specialty.getCode() %>" />
								<% } %>
								--%>
							</aui:select>
						</aui:column>
					</aui:layout>
					
					
					<aui:field-wrapper name="dateOption" inlineLabel="true">
						<aui:layout>
							<aui:column first="true" columnWidth="25">
								<aui:input name="dateOption" type="radio" value="processStartDate" label="processStartDate" checked="<%= true %>" />
								<aui:input name="dateOption" type="radio" value="actDate" label="actDate" />
							</aui:column>
							
							<aui:column columnWidth="35" cssClass="dateField">
								<aui:field-wrapper label="date-from">
									<liferay-ui:input-date 
										yearRangeEnd="<%= calendar.get(Calendar.YEAR) %>"
										yearRangeStart="<%= startingYearRange %>"
										yearNullable="false"
										yearParam="init-year"
										yearValue="<%= startingYearRange %>"
										
										monthNullable="false"
										monthValue="<%= Calendar.JANUARY %>"
										monthParam="init-month"
										
										dayNullable="false"
										dayValue="1"
										dayParam="init-day"
									/>
								</aui:field-wrapper>
							</aui:column>
								
							<aui:column last="true" columnWidth="35" cssClass="dateField">
								<aui:field-wrapper label="date-to">
									<liferay-ui:input-date 
										yearRangeEnd="<%= calendar.get(Calendar.YEAR) %>" 
										yearRangeStart="<%= startingYearRange %>"
										yearNullable="false"
										yearParam="end-year"
										yearValue="<%= calendar.get(Calendar.YEAR) %>"
										
										monthNullable="false"
										monthValue="<%= calendar.get(Calendar.MONTH) %>"
										monthParam="end-month"
										
										dayNullable="false"
										dayValue="<%= calendar.get(Calendar.DAY_OF_MONTH) %>"
										dayParam='end-day'
									/>
								</aui:field-wrapper>
							</aui:column>
							
							<aui:column columnWidth="75" last="true">
								<div class="portlet-msg-info" style="margin-right: 30px;">
									El rango de fechas para la consulta es de m&aacute;ximo <%= maxTimeLapseInDays %> d&iacute;as.
								</div>
							</aui:column>
						</aui:layout>
					</aui:field-wrapper>
				</aui:fieldset>
				
				<aui:button type="submit" onClick="verifyCpData()" value="generateReport" inputCssClass="buttonLink"/>
				
				<script type="text/javascript">
					var A = AUI();
					
					function verifyCpData() {
						var err = false;
						var alerted = false;
						
	//						console.log("Verify - err: ", err);
						
						if(A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />city").val() == "") {
							if(!A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />city").ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
								A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />city").addClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").addClass("aui-form-validator-error-container").one(".aui-field-element").append("<label class='aui-form-validator-stack-error'><div class='aui-form-validator-message city' role='alert'>Por favor, seleccione una ciudad.</div></label>");
							}
							
							err = true;
						}
						
	//						if(A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />specialty").val() == "" && !A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />specialty").ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
	//							A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />specialty").addClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").addClass("aui-form-validator-error-container").one(".aui-field-element").append("<label class='aui-form-validator-stack-error'><div class='aui-form-validator-message specialty' role='alert'>Por favor, seleccione una especialidad.</div></label>");
	//							err = true;
	//						}
						
						var dayDiffBetweenDates = daydiff(A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />init-day").val(), A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />init-month").val(), A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />init-year").val(), A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />end-day").val(), A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />end-month").val(), A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />end-year").val());
						//console.log("Diff: " + dayDiffBetweenDates);
						if(Number(dayDiffBetweenDates) < 0) {
							alert("La fecha inicial no puede ser mayor a la fecha final");
							err = true;
							alerted = true;
						} else if(Number(dayDiffBetweenDates) > <%= maxTimeLapseInDays %>) {
							alert("El No. m�ximo de d�as entre la fecha inicial y la fecha final es de <%= maxTimeLapseInDays %> ");
							err = true;
							alerted = true;
						}
						
// 						if(A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />name").val() == "" && A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />document").val()) {
// 							alert("Debe ingresar el nombre o el documento para buscar");
// 							err = true;
// 							alerted = true;
// 						}
						
	//						console.log("End verify - err: ", err);
						
						if(!err) {
							A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />hiddenField").val("pass");
						} else {
							if(!alerted) {
								alert("Debe corregir los errores del formulario antes de realizar la consulta");								
							}
						}
					}
					
					A.ready("aui-io-request", "aui-loading-mask", function(){
						/* loadignmask para el ajax que trae las especialidades filtradas por ciudad */
						A.one("#<portlet:namespace/>process-query-company-form").plug(A.LoadingMask, { background: '#000' });
						
						A.on("change", function(){
							var cityCode = this.val();
							if(cityCode) {
								if(this.ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
									this.removeClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").removeClass("aui-form-validator-error-container").one(".aui-form-validator-stack-error").remove();
								}
								
								//	Query specialties by city
								A.io.request('<portlet:resourceURL/>', {
									dataType: 'json',
									cache: true,
									autoLoad: true,
									data: {
										cityCode: cityCode
									},
									on: {
										success: function(){
											var data = this.get('responseData');
											console.log("response", data);
											var specialties = data.specialties;
											var content = "<option value=''></option>";
											for(var i = 0; i < specialties.length; i++ ){
												content = content.concat("<option value='", specialties[i].code, "'>", specialties[i].name, "</option>");
											}
											A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />specialty").html(content);
										},
										start: function(){
											A.one("#<portlet:namespace/>process-query-company-form").loadingmask.toggle();
										},
										complete: function(){
											A.one("#<portlet:namespace/>process-query-company-form").loadingmask.toggle();
										}
									}
								});
							} else {
								//	Empty specialties list
								A.one("#<portlet:namespace />process-query-company-form #<portlet:namespace />specialty").html("<option value=''></option>");
							}
						}, "#<portlet:namespace />process-query-company-form #<portlet:namespace />city");
						
						A.on("blur", function(){
							if(this.val() == "" && !this.ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
								A.one("#<portlet:namespace />city").addClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").addClass("aui-form-validator-error-container").one(".aui-field-element").append("<label class='aui-form-validator-stack-error'><div class='aui-form-validator-message city' role='alert'>Por favor, seleccione una ciudad.</div></label>");
							}
						}, "#<portlet:namespace />process-query-company-form #<portlet:namespace />city");
						
						A.on("change", function(){
							if(this.val() != "" && this.ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
								this.removeClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").removeClass("aui-form-validator-error-container").one(".aui-form-validator-stack-error").remove();
							}
						}, "#<portlet:namespace />process-query-company-form #<portlet:namespace />specialty");
						
	//						A.on("blur", function(){
	//							if(this.val() == "" && !this.ancestor(".aui-field.aui-field-select").hasClass("aui-form-validator-error-container")) {
	//								A.one("#<portlet:namespace />specialty").addClass("aui-form-validator-error").ancestor(".aui-field.aui-field-select").addClass("aui-form-validator-error-container").one(".aui-field-element").append("<label class='aui-form-validator-stack-error'><div class='aui-form-validator-message specialty' role='alert'>Por favor, seleccione una especialidad.</div></label>");
	//							}
	//						}, "#<portlet:namespace />process-query-company-form #<portlet:namespace />specialty");
					});
				</script>
			</aui:form>
		</liferay-ui:section>
	</liferay-ui:tabs>
	
	<script type="text/javascript">
		function daydiff(firstDay, firstMonth, firstYear, secondDay, secondMonth, secondYear) {
			var first = new Date(firstYear, firstMonth, firstDay);
			var second = new Date(secondYear, secondMonth, secondDay);
		    return (second-first)/(1000*60*60*24);
		}
	</script>
</div>