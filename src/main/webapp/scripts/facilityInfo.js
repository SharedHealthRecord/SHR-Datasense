function FacilityInformations() {
    $("#searchTxt").keyup(function(event){
        if(event.keyCode == 13){
            $("#searchBtn").click();
        }
    });

    $("#searchBtn").click(function() {
        var searchText = $("#searchTxt").val();
        searchFacility(searchText);
        $('.collapse').collapse("hide");
    });

    $(document).on('click','input[name=selectedFacilities]',function(e){
         var selectedFacilityId = getSelectedFacilityId();
         var selectedFacilityName = getSelectedFacilityName();
         var result = {"facilityName":selectedFacilityName,"facilityId":selectedFacilityId}
         $("#facilityInformations").attr("hidden",false);
         disableAllResult();
         clearAllDatePickers();
         disableAllButtons();
         clearErrors("#errorBlock1");
         renderDataToTheGivenTemplate('#template_facility_name',result,'#selectedFacilityName')
         getLastEncounterDate();

    });

    $("input[name=searchBy]").bind("click", function(e) {
       clearSearchBox();
       clearErrors();
       $("#avalilableFacilities").attr("hidden",true);
       $("#facilityInformations").attr("hidden",true);
       $('#lastEncounter').attr("hidden",true);
    });

    var getLastEncounterDate= function() {
        clearSearchBox();
        var selectedFacilityId = getSelectedFacilityId();
        var selectedFacilityName =getSelectedFacilityName();
        var targetUrl = "/facility/" + selectedFacilityId + "/lastEncounterDate";
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
               clearErrors();
               var result = {"facilityName":selectedFacilityName,"facilityId":selectedFacilityId,"encounterDate":getFormattedDate(response)};
               renderDataToTheGivenTemplate('#template_search_lastencounter',result,'#lastEncounter');
               $('#lastEncounter').attr("hidden",false);
            },
            error: function(e){
                showErrors(e);
            }
        });
    };


    $('#getVisitTypeButton').bind("click",function(e){
        clearErrors();
        clearErrors("#errorBlock1");
        disableAllResult();
        disableAllButtons();
        clearSearchBox();
        var selectedFacilityId = getSelectedFacilityId();
        var date = $('#visitDate').val();
        clearAllDatePickers();
        var targetUrl =  "/facility/" + selectedFacilityId + "/visitTypes/forDate?date=" + date;
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
               if(response.length==0){
                   showErrors("No visit for the date entered","#errorBlock1");
               }else{
                   var selectedDate = {"date":date};
                   renderDataToTheGivenTemplate("#template_date_selected_for_visit",selectedDate,"#dateSelectedForVisitType");
                   renderDataToTheGivenTemplate("#template_visit_type_with_count",response,'#visitTypeWithCount tbody');
                   $('#visitWithCount').attr("hidden", false);
               }
            },
            error: function(e){
                showErrors(e)
            }
        });
    });

    $('#getDiagnosisWithCountButton').bind("click",function(e){
        clearErrors();
        clearErrors("#errorBlock1");
        disableAllResult();
        disableAllButtons();
        clearSearchBox();
        var selectedFacilityId = getSelectedFacilityId();
        var startDate = $('#startDate').val();
        var endDate = $('#endDate').val();
        clearAllDatePickers();
        var targetUrl =  "/facility/" + selectedFacilityId + "/diagnosis/withinDates?startDate=" + startDate+ "&endDate=" + endDate;
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
               if(response.length==0){
                   showErrors("No diagnoses found for the selected dates","#errorBlock1");
               }else{
                    var dates={"startDate":startDate, "endDate":endDate};
                    renderDataToTheGivenTemplate("#template_for_selected_date_range",dates,"#dateRangeSelectedForDiagnosis");
                    renderDataToTheGivenTemplate("#template_diagnosis_name_with_count",response,'#diagnosisNameWithCount tbody');
                    $('#getDiagnosisWithCount').attr("hidden", false);
               }
            },
            error: function(e){
                showErrors(e)
            }
        });
    });

    $('#getEncounterTypesWithCountButton').bind("click",function(e){
        clearErrors();
        clearErrors("#errorBlock1");
        disableAllResult();
        disableAllButtons();
        clearSearchBox();
        var selectedFacilityId = getSelectedFacilityId();
        var startDate = $('#startDateForEncounter').val();
        var endDate = $('#endDateForEncounter').val();
        clearAllDatePickers();
        var targetUrl =  "/facility/" + selectedFacilityId + "/encounterTypes/withinDates?startDate=" + startDate+ "&endDate=" + endDate;
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
               if(response.length==0){
                 showErrors("No encounter types for the date entered","#errorBlock1");
               }else{
                 var dates = {"startDate":startDate, "endDate":endDate};
                 renderDataToTheGivenTemplate("#template_for_selected_date_range",dates,"#dateSelectedForEncounterType");
                 renderDataToTheGivenTemplate("#template_encounter_type_with_count",response,'#encounterWithCount tbody');
                 $('#encounterTypesWithCount').attr("hidden", false);

               }
            },
            error: function(e){
                showErrors(e)
            }
        });
    });

    $('#visitDate').datepicker({
             autoclose: true,
             onRender: function(dateTemp) {
                var nowTemp = new Date();
                var now = new Date(nowTemp.getFullYear(), nowTemp.getMonth(), nowTemp.getDate(), 0, 0, 0, 0);
                var date = new Date(dateTemp.getFullYear(), dateTemp.getMonth(), dateTemp.getDate(), 0, 0, 0, 0);
                return now.valueOf() < date.valueOf() ? 'disabled' : '';
             },
             format: 'dd/mm/yyyy'
    }).on('changeDate', function (e) {
       $('#getVisitTypeButton').attr("disabled",false);
       $('#visitWithCount').attr("hidden", true);
       clearErrors("#errorBlock1");
    });



    var createStartDatePicker = function(startDateId, endDateId, endDateChangeforDiagnosis){
        var startDatePicker = $(startDateId).datepicker({
          autoclose: true,
          onRender: function(dateTemp) {
            var nowTemp = new Date();
            var now = new Date(nowTemp.getFullYear(), nowTemp.getMonth(), nowTemp.getDate(), 0, 0, 0, 0);
            var date = new Date(dateTemp.getFullYear(), dateTemp.getMonth(), dateTemp.getDate(), 0, 0, 0, 0);
            return now.valueOf() < date.valueOf() ? 'disabled' : '';
          },
          format: 'dd/mm/yyyy'
        }).on('changeDate', function(ev) {
          if (ev.date.valueOf() < endDatePicker.date.valueOf()) {
            var newDate = new Date(ev.date)
            newDate.setDate(newDate.getDate());
            endDatePicker.setValue(newDate);
          }
          startDatePicker.hide();
          $(endDateId)[0].focus();
        }).data('datepicker');
        var endDatePicker = $(endDateId).datepicker({
          autoclose: true,
          onRender: function(dateTemp) {
            var nowTemp = new Date();
            var now = new Date(nowTemp.getFullYear(), nowTemp.getMonth(), nowTemp.getDate(), 0, 0, 0, 0);
            if(dateTemp.valueOf() >= startDatePicker.date.valueOf() && now.valueOf() >= dateTemp.valueOf()){
                return '';
            }
            else{
                return 'disabled';
            }
          },
          format: 'dd/mm/yyyy'
        }).on('changeDate', endDateChangeforDiagnosis
        ).data('datepicker');
    }

    var endDateChangeforDiagnosis= function(ev) {
      $('#getDiagnosisWithCountButton').attr("disabled",false);
      $('#getDiagnosisWithCount').attr("hidden", true);
      clearErrors("#errorBlock1");
    }

    var endDateChangeforEncounter= function(ev) {
      $('#getEncounterTypesWithCountButton').attr("disabled",false);
      $('#encounterTypesWithCount').attr("hidden", true);
      clearErrors("#errorBlock1");
    }

    createStartDatePicker('#startDate', '#endDate', endDateChangeforDiagnosis);
    createStartDatePicker('#startDateForEncounter', '#endDateForEncounter', endDateChangeforEncounter);

    var getFormattedDate = function(date){
        var d = new Date(date);
        var formattedDate = d.getDate() + "-" + (d.getMonth() + 1) + "-" + d.getFullYear();
        var hours = (d.getHours() < 10) ? "0" + d.getHours() : d.getHours();
        var minutes = (d.getMinutes() < 10) ? "0" + d.getMinutes() : d.getMinutes();
        var formattedTime = hours + ":" + minutes;
        formattedDate = formattedDate + ", " + formattedTime;
        return formattedDate;
    }


    var renderDataToTheGivenTemplate = function(templateId,data,divId){
       var template = $(templateId).html();
       Mustache.parse(template);
       var rendered = Mustache.render(template,data);
       $(divId).html(rendered);
    }

    var validateFacilityId = function(searchTxt){
        if(searchTxt.match(/^\d+$/)) {
            return true;
        }
        else{
            return false;
        }
    }

    var getFacilities = function(targetUrl){
        $.ajax({
           type: "GET",
           url: targetUrl,
           success: function(result){
                clearErrors();
                if(result.length==0){
                    showErrors("No facility is available for given facility id/name");
                }
                else{
                   $('#searchResultsContainerForFacility').hide();
                   var template = $('#template_search_facilities').html();
                   Mustache.parse(template);
                   var rendered = Mustache.render(template, result);
                   $('#searchResultsContainerForFacility').html(rendered);
                   $('#searchResultsContainerForFacility').show();
                   $("#avalilableFacilities").attr("hidden",false);

                }
           }
        });
    }

    var searchFacility = function(searchTxt){
        $('#lastEncounter').attr("hidden",true);
        $("#avalilableFacilities").attr("hidden",true);
        $("#facilityInformations").attr("hidden",true);
        var targetUrl;
        var selectedOption = $('input[name="searchBy"]:checked').val();
        if(selectedOption == "FacilityId"){
            if(validateFacilityId(searchTxt)){
                targetUrl = "/facility/search?id=" + searchTxt;
                getFacilities(targetUrl);
            }
            else{
                showErrors("Incorrect Facility Id. Facility ID contains only digits.");
            }
        }
        else if(selectedOption == "FacilityName"){
                targetUrl = "/facility/search?name=" + searchTxt;
                getFacilities(targetUrl);
        }

    }

    var disableAllButtons = function(){
        $('#getVisitTypeButton').attr("disabled", true);
        $('#getDiagnosisWithCountButton').attr("disabled", true);
        $('#getEncounterTypesWithCountButton').attr("disabled",true);
    }

    var disableAllResult = function(){
       $('#lastEncounter').attr("hidden", true);
       $('#getDiagnosisWithCount').attr("hidden", true);
       $('#encounterTypesWithCount').attr("hidden", true);
       $('#visitWithCount').attr("hidden",true);
    }

    var clearAllDatePickers = function(){
        $("#visitDate").val("");
        $("#startDate").val("");
        $("#endDate").val("");
        $("#startDateForEncounter").val("");
        $("#endDateForEncounter").val("");
    }

    var getSelectedFacilityId = function(){
        return $('input[name="selectedFacilities"]:checked').val();
    }

    var getSelectedFacilityName =  function(){
        return $('input[name="selectedFacilities"]:checked').attr('id');
    }

    var clearSearchBox = function(){
        document.getElementById("searchTxt").value = "";
    }
}