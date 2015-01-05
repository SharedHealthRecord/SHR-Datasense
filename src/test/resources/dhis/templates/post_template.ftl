{
  "dataSet": "${dataset}",
  "period": "${period}",
  "orgUnit": "${orgUnit}",
  "dataValues": [
    <#list Query_1 as row1>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "UBdaznQ8DlT",
      "value": "${row1.v1}"
    },
    </#list>
<#list Query_2 as row2>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "tSwmrlTW11V",
      "value": "${row2.v2}"
    }
</#list>
  ]
}