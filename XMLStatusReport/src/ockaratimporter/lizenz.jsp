<%-- 
    Document   : lizenz
    Created on : 14.10.2008, 12:43:33
    Author     : A. Sopicki
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="de.di.xml.Importer,de.di.xml.web.XMLimporterServlet" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<fmt:bundle basename="de.di.xml.web.resource.servlet">
<html>
<head>
    <title><fmt:message key="page.licence.title.text"></fmt:message></title>
    <style type="text/css">
body {
	background-color: #fff;
    font-family: Arial,sans-serif;
     }  
table.status {
	border-collapse: collapse;
    }
table.status td {
    background-color: #cbcdcf;
    border: 1px solid gray;
    padding-top: 0.3em; 
    padding-bottom: 0.3em; 
    padding-left: 0.7em;
    padding-right: 0.7em;
    }
table.status{
    width:600px;
    margin-left:auto;
    margin-right:auto;
	}
table.status-top{
    width:600px;
    margin-left:auto;
    margin-right:auto;
    }
table.status td.name {
    font-weight: bold;
    }
table.status td.value { 
    text-align: right;
    }

	table.status-top{
            width:600px;
            margin-left:auto;
            margin-right:auto;
        }

</style>
</head>
<body>

<%
            Importer importer = (Importer) application.getAttribute(XMLimporterServlet.importerAttribute);
            if (importer != null && importer.isAlive()) {
                java.util.Map<String, String> status = importer.getStatus();

                String productname = status.get("product_name");

%>

<table class="status-top">
<tr>    
        <td height="50" width="10"> 
            <img src="images/info.gif" height="25">
        </td>
        <td width="5"> 
        </td>
        <td> 
            <div class="name"><fmt:message key="page.licence.header.text"></fmt:message> <b>DOK</b>inform<sup>&#174;</sup> <b>OCKARAT</b><i>importer</i></div>
        </td>
    </tr>
	</table>
<hr>
<br>
    
<table class="status">
<tbody>
<tr>
    <td class="name"><fmt:message key="license.properties.licensee.text"></fmt:message></td><td class="value"><%= status.get("client") %></td>
</tr>
<tr>
    <td class="name"><fmt:message key="license.properties.validity.text"></fmt:message></td><td class="value"><%= status.get("expiration_date") %></td>
</tr>
<tr>
    <td class="name"><fmt:message key="license.properties.erp_system.text"></fmt:message></td><td class="value"><%= status.get("ERP_system") %></td>
</tr>
<tr>
    <td class="name"><fmt:message key="license.properties.license.type.text"></fmt:message></td><td class="value"><%= status.get("license_type") %></td>
</tr>
<tr>
  <td class="name"><fmt:message key="license.properties.dms.text"></fmt:message></td><td class="value"><%= status.get("ELO_version")%></td>
</tr>
</tbody>
</table>

<% 
} else {%>
<table>
    <tr>    
        <td width="10" height="50"> 
            <img src="images/error.gif" height = "25">
        </td>
        <td width="5"> 
        </td>
        <td valign="middle"> 
            <div class="warning" valign="middle"><fmt:message key="system.inactive.text"></fmt:message></div>
        </td>
    </tr>
</table>

<%
                application.removeAttribute(XMLimporterServlet.importerAttribute);
            }%>

        
<div id="footer">
    
    <table class="fullwidth">
        
        <tr>
            <td>
                
            </td>
        </tr>	            
    </table>
</div>
</body>
</html>
</fmt:bundle>