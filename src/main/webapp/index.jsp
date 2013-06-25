    <%@ include file="WEB-INF/jsp/includes.jsp" %>
        <%@ include file="WEB-INF/jsp/header.jsp" %>

        <!--
        <%--<img src="<spring:url value="/static/images/pets.png" htmlEscape="true" />" align="right" style="position:relative;right:30px;">--%>
        -->

        <c:set var="method" value="GET" scope="page"/>

        <%--<fmt:setLocale value="en"/>--%>

        <%--<h2><fmt:message key="welcome"/></h2>--%>

        <div id="body">
        <div id="menubar">
        <form:form action="searchQuery" method="${method}">
        <input type="text" name="query" size="80"/>
        <%--<input type="text" name="query" size="80" />--%>
        <input type="submit" name="submit" value="search" />
        </form:form>


        <br/>
        <br/>

        <table border="0" width="564">
        <tr>
        <td width="400">
        <div id="crti" style="margin-left:-142px;width:400;">
        <h2><s>Create Index</s></h2>
        </div>
        </td>
        <td width="100">
        <div style="float:right;">
        <form:form action="createIndex" method="${method}">
        <%--<input type="text" name="query" size="80" />--%>
        <input type="submit" name="submit" value="create" size="80" />
        </form:form>
        </div>
        </td></tr>
        </table>
        </div>
        <%@ include file="WEB-INF/jsp/footer.jsp" %>
        </div>
        </div>