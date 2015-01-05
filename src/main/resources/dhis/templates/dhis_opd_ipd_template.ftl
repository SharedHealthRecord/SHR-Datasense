{
  "dataSet": "iUz0yoVeeiZ",
  "period": "${period}",
  "orgUnit": "${orgUnit}",
  "dataValues": [
    <#list ip_male_5_to_15 as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "UBdaznQ8DlT",
      "value": "${row.v1}"
    },
</#list>
<#list ip_female_5_to_15 as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "tSwmrlTW11V",
      "value": "${row.v2}"
    },
</#list>
<#list op_male_5_to_15 as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "UBdaznQ8DlT",
      "value": "${row.v3}"
    },
</#list>
<#list op_female_5_to_15 as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "tSwmrlTW11V",
      "value": "${row.v4}"
    }
</#list>
  ]
}