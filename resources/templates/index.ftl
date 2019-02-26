<#-- @ftlvariable name="data" type="com.spothero.lab.IndexData" -->
<html>
<body>
<ul>
    <#list data.items as item>
        <li>${item}</li>
    </#list>
</ul>
</body>
</html>
