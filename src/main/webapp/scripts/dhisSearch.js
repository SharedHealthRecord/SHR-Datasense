function searchDHISDataset(searchTxt) {
    var deferredRes = $.Deferred();
    $.get( "/dhis2/reports/search?name=" + searchTxt)
        .done(function(result) {
            $('#searchResultsContainer').hide();
            $(".configure-btn").unbind("click", configureDatasetForReport);
            var template = $('#template_search_results').html();
            Mustache.parse(template);
            var rendered = Mustache.render(template, result.dataSets);
            $('#searchResultsContainer').html(rendered);
            $('#searchResultsContainer').show();
            $(".configure-btn").bind("click", configureDatasetForReport);
            deferredRes.resolve();
        })
        .fail(function() {
            alert( "error" );
            deferredRes.reject('error');
        });
    return deferredRes;
}

function configureDatasetForReport(e) {
  var dhisDatasetId = $(e.target).attr("data-datasetId");
  var periodType = $(e.target).attr("data-periodType");
  var dhisDatasetName = $("div[data-datasetId="+dhisDatasetId+"]").text().trim();
  var dsReportName = $("#dsReportName").val().trim();
  var dsConfigFile = $("#dsConfigFile").val().trim();

  var postData = {"name": dsReportName, "configFile": dsConfigFile, "datasetName": dhisDatasetName, "datasetId": dhisDatasetId, "periodType": periodType };

  $.ajax({
    type: "POST",
    url: "/dhis2/reports/configure",
    data: JSON.stringify(postData),
    contentType: "application/json; charset=utf-8",
    success: function(data, status) { console.log(data); window.location.href="/dhis2/reports"; },
    dataType: "json"
  });

}



function searchDHISOrgUnit(searchTxt) {
    $.get( "/dhis2/orgUnits/search?name=" + searchTxt)
    .done(function(result) {
        $('#searchResultsContainer').hide();
        $(".configure-btn").unbind("click", configureOrgUnitForFacility);
        var template = $('#template_search_results').html();
        Mustache.parse(template);
        var rendered = Mustache.render(template, result.organisationUnits);
        $('#searchResultsContainer').html(rendered);
        $('#searchResultsContainer').show();
        $(".configure-btn").bind("click", configureOrgUnitForFacility);
    })
    .fail(function() {
        alert( "error" );
    });
}

function configureOrgUnitForFacility(e) {
  var dhisOrgUnitId = $(e.target).attr("data-orgUnitId");
  var dhisOrgUnitName = $("div[data-orgUnitId="+dhisOrgUnitId+"]").text().trim();
  var dsFacilityId = $("#dsFacilityId").val().trim();

  var postData = {"facilityId": dsFacilityId, "orgUnitId": dhisOrgUnitId, "orgUnitName": dhisOrgUnitName };

  $.ajax({
    type: "POST",
    url: "/dhis2/orgUnits/configure",
    data: JSON.stringify(postData),
    contentType: "application/json; charset=utf-8",
    success: function(data, status) { console.log(data); window.location.href="/dhis2/orgUnits"; },
    dataType: "json"
  });

}