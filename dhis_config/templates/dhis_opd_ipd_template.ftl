{
  "dataSet": "${paramDatasetId}",
  "period": "${paramReportingPeriod}",
  "orgUnit": "${paramOrgUnitId}",
  "dataValues": [
<#list ip_male_0_to_4 as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "u2QXNMacZLt",
      "value": "${row.v1}"
    },
</#list>
<#list ip_female_0_to_4 as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "DA2N93v7s0O",
      "value": "${row.v1}"
    },
</#list>
<#list ip_male_5_to_14 as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "UBdaznQ8DlT",
      "value": "${row.v1}"
    },
</#list>
<#list ip_female_5_to_14 as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "tSwmrlTW11V",
      "value": "${row.v1}"
    },
</#list>
<#list ip_male_15_to_24 as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "GYRYyntlK7n",
      "value": "${row.v1}"
    },
</#list>
<#list ip_female_15_to_24 as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "KahybAysMCQ",
      "value": "${row.v1}"
    },
</#list>
<#list ip_male_25_to_49 as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "cs9cqLXiS5X",
      "value": "${row.v1}"
    },
</#list>
<#list ip_female_25_to_49 as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "GQAGIsvkK4Q",
      "value": "${row.v1}"
    },
</#list>
<#list ip_male_50_above as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "xSzlrnhqYNT",
      "value": "${row.v1}"
    },
</#list>
<#list ip_female_50_above as row>
    { "dataElement": "AiPqHCbJQJ1", "categoryOptionCombo": "MacxOPmyosZ",
      "value": "${row.v1}"
    },
</#list>
<#list emergency_male_0_to_4 as row>
    { "dataElement": "L07RnkDuRaA", "categoryOptionCombo": "u2QXNMacZLt",
      "value": "${row.v1}"
    },
</#list>
<#list emergency_female_0_to_4 as row>
    { "dataElement": "L07RnkDuRaA", "categoryOptionCombo": "DA2N93v7s0O",
      "value": "${row.v1}"
    },
</#list>
<#list emergency_male_5_to_14 as row>
    { "dataElement": "L07RnkDuRaA", "categoryOptionCombo": "UBdaznQ8DlT",
      "value": "${row.v1}"
    },
</#list>
<#list emergency_female_5_to_14 as row>
    { "dataElement": "L07RnkDuRaA", "categoryOptionCombo": "tSwmrlTW11V",
      "value": "${row.v1}"
    },
</#list>
<#list emergency_male_15_to_24 as row>
    { "dataElement": "L07RnkDuRaA", "categoryOptionCombo": "GYRYyntlK7n",
      "value": "${row.v1}"
    },
</#list>
<#list emergency_female_15_to_24 as row>
    { "dataElement": "L07RnkDuRaA", "categoryOptionCombo": "KahybAysMCQ",
      "value": "${row.v1}"
    },
</#list>
<#list emergency_male_25_to_49 as row>
    { "dataElement": "L07RnkDuRaA", "categoryOptionCombo": "cs9cqLXiS5X",
      "value": "${row.v1}"
    },
</#list>
<#list emergency_female_25_to_49 as row>
    { "dataElement": "L07RnkDuRaA", "categoryOptionCombo": "GQAGIsvkK4Q",
      "value": "${row.v1}"
    },
</#list>
<#list emergency_male_50_above as row>
    { "dataElement": "L07RnkDuRaA", "categoryOptionCombo": "xSzlrnhqYNT",
      "value": "${row.v1}"
    },
</#list>
<#list emergency_female_50_above as row>
    { "dataElement": "L07RnkDuRaA", "categoryOptionCombo": "MacxOPmyosZ",
      "value": "${row.v1}"
    },
</#list>
<#list op_male_0_to_4 as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "u2QXNMacZLt",
      "value": "${row.v1}"
    },
</#list>
<#list op_female_0_to_4 as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "DA2N93v7s0O",
    "value": "${row.v1}"
    },
</#list>
<#list op_male_5_to_14 as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "UBdaznQ8DlT",
      "value": "${row.v1}"
    },
</#list>
<#list op_female_5_to_14 as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "tSwmrlTW11V",
      "value": "${row.v1}"
    },
</#list>
<#list op_male_15_to_24 as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "GYRYyntlK7n",
      "value": "${row.v1}"
    },
</#list>
<#list op_female_15_to_24 as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "KahybAysMCQ",
      "value": "${row.v1}"
    },
</#list>
<#list op_male_25_to_49 as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "cs9cqLXiS5X",
      "value": "${row.v1}"
    },
</#list>
<#list op_female_25_to_49 as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "GQAGIsvkK4Q",
      "value": "${row.v1}"
    },
</#list>
<#list op_male_50_above as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "xSzlrnhqYNT",
      "value": "${row.v1}"
    },
</#list>
<#list op_female_50_above as row>
    { "dataElement": "Ft1woRONbkY", "categoryOptionCombo": "MacxOPmyosZ",
      "value": "${row.v1}"
    },
</#list>
<#list death_male_0_to_4 as row>
    { "dataElement": "I4ovrH4LgWb", "categoryOptionCombo": "u2QXNMacZLt",
      "value": "${row.v1}"
    },
</#list>
<#list death_female_0_to_4 as row>
    { "dataElement": "I4ovrH4LgWb", "categoryOptionCombo": "DA2N93v7s0O",
    "value": "${row.v1}"
    },
</#list>
<#list death_male_5_to_14 as row>
    { "dataElement": "I4ovrH4LgWb", "categoryOptionCombo": "UBdaznQ8DlT",
      "value": "${row.v1}"
    },
</#list>
<#list death_female_5_to_14 as row>
    { "dataElement": "I4ovrH4LgWb", "categoryOptionCombo": "tSwmrlTW11V",
      "value": "${row.v1}"
    },
</#list>
<#list death_male_15_to_24 as row>
    { "dataElement": "I4ovrH4LgWb", "categoryOptionCombo": "GYRYyntlK7n",
      "value": "${row.v1}"
    },
</#list>
<#list death_female_15_to_24 as row>
    { "dataElement": "I4ovrH4LgWb", "categoryOptionCombo": "KahybAysMCQ",
      "value": "${row.v1}"
    },
</#list>
<#list death_male_25_to_49 as row>
    { "dataElement": "I4ovrH4LgWb", "categoryOptionCombo": "cs9cqLXiS5X",
      "value": "${row.v1}"
    },
</#list>
<#list death_female_25_to_49 as row>
    { "dataElement": "I4ovrH4LgWb", "categoryOptionCombo": "GQAGIsvkK4Q",
      "value": "${row.v1}"
    },
</#list>
<#list death_male_50_above as row>
    { "dataElement": "I4ovrH4LgWb", "categoryOptionCombo": "xSzlrnhqYNT",
      "value": "${row.v1}"
    },
</#list>
<#list death_female_50_above as row>
    { "dataElement": "I4ovrH4LgWb", "categoryOptionCombo": "MacxOPmyosZ",
      "value": "${row.v1}"
    }
</#list>
  ]
}