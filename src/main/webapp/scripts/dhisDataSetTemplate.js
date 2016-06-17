function DhisDataSetTemplate() {
   var suggestionTemplate = [];
   var self = this;
   this.detach = function(e) {
      if (e) e.unbind("click", self.generateTemplate);
   };

   this.attach = function(e) {
      e.bind("click", self.generateTemplate);
   };

   this.generateTemplate = function(e) {
//      $("#templateHeading").attr("hidden",true);
      $("#suggestionContainer").hide();
      var dhisDatasetId = $(e.target).attr("data-datasetId");
      var dhisDatasetName = $(e.target).attr("data-datasetName");
      var targetUrl = "/dhis2/dataSets/" + dhisDatasetId + "/dataElements";
       $.ajax({
          type: "GET",
          url: targetUrl,
          success: function(result){
              $("#searchResultsContainer").hide();
              var dataSetConfiguration = {};
              dataSetConfiguration.name = result.name;
              dataSetConfiguration.dataSetId = dhisDatasetId;
              dataSetConfiguration.reportName = result.name.replace(/([~!@#$%^&*()_+=`{}\[\]\|\\:;'<>,.\/? ])+/g, '_').replace(/^(-)+|(_)+$/g,'');
              dataSetConfiguration.dataElementList =  [];
              if(result.dataElements.length==0){
                showErrors("Error occured");
              }
              else{
                  showHeading(dhisDatasetName,dhisDatasetId);
                  result.dataElements.forEach(function(dataElement){
                     var item = {};
                     item.name = dataElement.name;
                     item.dataElementId = dataElement.id;
                     item.categoryComboId = null;
                     item.categoryOptionCombos = [];
                     dataSetConfiguration.dataElementList.push(item);
                  });

                  var deferredDataElementCalls = [];
                  $.each(dataSetConfiguration.dataElementList, function(index, item) {
                      deferredDataElementCalls.push(
                          $.ajax({
                              url: "/dhis2/dataSets/dataElements/" + item.dataElementId,
                              success: function(rs) {
                                 item.categoryComboId = rs.categoryCombo.id;
                              }
                          })
                      );
                  });
                  // Can't pass a literal array, so use apply.
                  $.when.apply($, deferredDataElementCalls).then(function(data){
                      var deferredCategoryComboCalls = [];
                      $.each(dataSetConfiguration.dataElementList, function(index, item) {
                          deferredCategoryComboCalls.push(
                              $.ajax({
                                  url: "/dhis2/dataSets/categoryCombos/" + item.categoryComboId,
                                  success: function(rs) {
                                     item.categoryOptionCombos = rs.categoryOptionCombos;
                                     item.categoryOptionCombos.forEach(function(coc) {
                                          var query_name = item.name;
                                          if (coc.name !== '(default)') {
                                              query_name = query_name + ' ' + coc.name;
                                          }
                                          coc.queryName = query_name.replace(/([~!@#$%^&*()_+=`{}\[\]\|\\:;'<>,.\/? ])+/g, '_').replace(/^(-)+|(_)+$/g,'').replace(/-/g, '_');
                                          coc.dataElementId = item.dataElementId;
                                     });
                                  }
                              })
                          );
                      });

                      $.when.apply($, deferredCategoryComboCalls).then(function(data){
                          stringifyAndDisplaySuggestion(dataSetConfiguration)
                      });

                  }).fail(function(){
                      // Probably want to catch failure
                  }).always(function(){
                      // Or use always if you want to do the same thing
                      // whether the call succeeds or fails
                  });
              }
          },
          error: function(e) {
            showErrors(e);
           }
      });
   };
    var showHeading = function(dataElementName,dataElementId){
        var data = {"data-datasetName" : dataElementName,"data-datasetId" : dataElementId}
        var template = $('#template_heading').html();
        Mustache.parse(template);
        var rendered = Mustache.render(template, data);
        $('#templateHeading').html(rendered);
//        $("#templateHeading").attr("hidden",false);

    }

    var stringifyAndDisplaySuggestion = function(data) {
        var lastDataElement = data["dataElementList"][data["dataElementList"].length -1 ];
        lastDataElement["categoryOptionCombos"][lastDataElement["categoryOptionCombos"].length -1]["last"] = true;
        var jsonStr = JSON.stringify(data);

        var configTemplate = $('#mushTmpl_config_suggestion').html();
        Mustache.parse(configTemplate);
        var renderedConfigTemplate = Mustache.render(configTemplate, data);
        $('#aqsConfigSuggestion').html(renderedConfigTemplate);

        var queryTemplate = $('#mushTmpl_query_suggestion').html();
        Mustache.parse(queryTemplate);
        var renderedQueryTemplate = Mustache.render(queryTemplate, data);
        $('#aqsQuerySuggestion').html(renderedQueryTemplate);

        //$('#aqsTemplateSuggestion').html(jsonStr);

        var postTemplate = $('#mushTmpl_postTemplate_suggestion').html();
        postTemplate = postTemplate.replace(/&lt;/g,'<').replace(/&gt;/g,'>')
        Mustache.parse(postTemplate);
        var renderedQueryTemplate = Mustache.render(postTemplate, data);
        $('#aqsTemplateSuggestion').html(renderedQueryTemplate);
        $('#suggestionContainer').show();
        $("#templateHeading").attr("hidden",false);

    };

}