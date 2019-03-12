<#-- @ftlvariable name="data" type="com.spothero.lab.spotnow.app.IndexData" -->
<html>
<body>
<ul>
    <#list data.items as item>
        <li>${item}</li>
    </#list>
</ul>
</body>
</html>
