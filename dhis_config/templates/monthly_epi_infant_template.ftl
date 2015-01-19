{
"dataSet": "yChWWiTkK6i",
"period": "${period}",
"orgUnit": "${orgUnit}",
"dataValues": [
        <#list BCG_0_to_11_months as row>
            {   "dataElement": "nbQTnNFs1I8", "categoryOptionCombo": "dCWAvZ8hcrs",
                "value": "${row.v1}"
            },
        </#list>
        <#list BCG_12_to_23_months as row>
            {   "dataElement": "o40c3EW6tuZ", "categoryOptionCombo": "dCWAvZ8hcrs",
                "value": "${row.v1}"
            },
        </#list>
        <#list OPV1_0_to_11_months as row>
            {   "dataElement": "eYJ3MgWzghH", "categoryOptionCombo": "dCWAvZ8hcrs",
                "value": "${row.v1}"
            },
        </#list>
        <#list OPV1_12_to_23_months as row>
            {   "dataElement": "AgfjXpHFl7j", "categoryOptionCombo": "dCWAvZ8hcrs",
                "value": "${row.v1}"
            },
        </#list>
        <#list OPV2_0_to_11_months as row>
            {   "dataElement": "YkajaYobus9", "categoryOptionCombo": "dCWAvZ8hcrs",
                "value": "${row.v1}"
            },
        </#list>
        <#list OPV2_12_to_23_months as row>
            {   "dataElement": "Skwid1Sa4Qm", "categoryOptionCombo": "dCWAvZ8hcrs",
                "value": "${row.v1}"
            },
        </#list>
        <#list OPV3_0_to_11_months as row>
            {   "dataElement": "AFIo5tpZjyr", "categoryOptionCombo": "dCWAvZ8hcrs",
                "value": "${row.v1}"
            },
        </#list>
        <#list OPV3_12_to_23_months as row>
            {   "dataElement": "Y5nylfVJpHj", "categoryOptionCombo": "dCWAvZ8hcrs",
                "value": "${row.v1}"
            }
        </#list>
    ]
}