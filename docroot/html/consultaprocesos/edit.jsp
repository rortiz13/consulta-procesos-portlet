<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://alloy.liferay.com/tld/aui" prefix="aui" %>

<portlet:defineObjects />

<%
	Integer startingYearRange = Integer.valueOf(renderRequest.getPreferences().getValue("startingYearRange", "1980"));
	Integer maxTimeLapseInDays = Integer.valueOf(renderRequest.getPreferences().getValue("maxTimeLapseInDays", "365"));
	Integer idProcessQueries = Integer.valueOf(renderRequest.getPreferences().getValue("idProcessQueries", "1463818"));
%>

<portlet:actionURL var="savePreferences" name="savePreferences" />
<div id="newsPortletConfig">
	<form id="savePreferencesForm" action="<%= savePreferences %>" method="POST">
		<aui:layout>
			<aui:column first="true" columnWidth="50">
				<aui:fieldset label="Preferencias del Buscador">
					<aui:input name="startingYearRange" label="Año Inicial" value="<%= startingYearRange %>" />
					<aui:input name="maxTimeLapseInDays" label="Lapso máximo entre fechas (en días)" value="<%= maxTimeLapseInDays %>" />
				</aui:fieldset>
			</aui:column>
			
			<aui:column last="true" columnWidth="50">
				<aui:fieldset label="Configuraciones de Datos">
					<aui:input name="idProcessQueries" label="ID de la lista dinámica de consulta de procesos" value="<%= idProcessQueries %>" />
				</aui:fieldset>
			</aui:column>
		</aui:layout>
		
		<aui:button-row>
			<button type="submit">Guardar</button>
		</aui:button-row>
	</form>
</div>