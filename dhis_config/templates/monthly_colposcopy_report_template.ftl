{
  "dataSet": "Ty54bZ7blXk",
  "period": "${paramReportingPeriod}",
  "orgUnit": "${paramOrgUnitId}",
  "dataValues": [
        <#list Colposcopy_CIN_I as row>
                {   "dataElement": "Kax6MfVu7rl", "categoryOptionCombo": "dCWAvZ8hcrs",
                        "value": "${row.v1}"
                },
        </#list>
        <#list Colposcopy_CIN_II as row>
                {   "dataElement": "enVPUaievIA", "categoryOptionCombo": "dCWAvZ8hcrs",
                        "value": "${row.v1}"
                },
        </#list>
        <#list Colposcopy_CIN_III as row>
                {   "dataElement": "PiWgphio89E", "categoryOptionCombo": "dCWAvZ8hcrs",
                        "value": "${row.v1}"
                },
        </#list>
        <#list Colposcopy_CA_CERVIX as row>
                {   "dataElement": "s4JdBEPnMbp", "categoryOptionCombo": "dCWAvZ8hcrs",
                        "value": "${row.v1}"
                },
        </#list>
        <#list Management_LEEP as row>
                {   "dataElement": "gUfcmU9fkHC", "categoryOptionCombo": "dCWAvZ8hcrs",
                      "value": "${row.v1}"
                },
        </#list>
        <#list Management_Cold_Coagulation as row>
                {   "dataElement": "RTgviJlf3oM", "categoryOptionCombo": "dCWAvZ8hcrs",
                        "value": "${row.v1}"
                },
        </#list>
        <#list Management_Cryotherapy as row>
                {   "dataElement": "rxDgC7Cihpt", "categoryOptionCombo": "dCWAvZ8hcrs",
                        "value": "${row.v1}"
                },
        </#list>
        <#list Histopathology_CIN_I as row>
                {   "dataElement": "qN08hfOyKNJ", "categoryOptionCombo": "dCWAvZ8hcrs",
                       "value": "${row.v1}"
                },
        </#list>
        <#list Histopathology_CIN_II as row>
                {   "dataElement": "ekWTDX0oimC", "categoryOptionCombo": "dCWAvZ8hcrs",
                        "value": "${row.v1}"
                },
        </#list>
        <#list Histopathology_CIN_III as row>
                {   "dataElement": "p8M3dKkyYAN", "categoryOptionCombo": "dCWAvZ8hcrs",
                        "value": "${row.v1}"
                },
        </#list>
        <#list Histopathology_CA_CERVIX as row>
                {   "dataElement": "azGDZdzn3BT", "categoryOptionCombo": "dCWAvZ8hcrs",
                        "value": "${row.v1}"
                }
        </#list>
  ]
}