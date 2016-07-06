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
        var selectedFacility = $('input[name="selectedFacilities"]:checked').val();
        var selectedFacilityName =$('input[name="selectedFacilities"]:checked').attr('id');
        var result = {"facilityName":selectedFacilityName,"facilityId":selectedFacility}

        $("#accordion").attr("hidden",false);
        disableAllResult();
        $("#visitDate").val("");
        $("#startDate").val("");
        $("#endDate").val("");
        $('#getVisitTypeButton').attr("disabled", true);
        $('#getDiagnosisWithCountButton').attr("disabled", true);
        clearErrors("#errorBlock1");
         var template = $('#template_facility_name').html();
         Mustache.parse(template);
         var rendered = Mustache.render(template, result);
         $('#selectedFacilityName').html(rendered);
         getLastEncounterDate();

    });

    $("input[name=searchBy]").bind("click", function(e) {
       document.getElementById("searchTxt").value = "";
       clearErrors();
       $("#avalilableFacilities").attr("hidden",true);
       $("#accordion").attr("hidden",true);
       $('#lastEncounter').attr("hidden",true);
    });

    var getLastEncounterDate= function() {
        document.getElementById("searchTxt").value = "";
        var selectedFacility = $('input[name="selectedFacilities"]:checked').val();
        var selectedFacilityName =$('input[name="selectedFacilities"]:checked').attr('id');
        var targetUrl = "/facility/" + selectedFacility + "/lastEncounterDate";
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
                clearErrors();
                var d = new Date(response);
                var formattedDate = d.getDate() + "-" + (d.getMonth() + 1) + "-" + d.getFullYear();
                var hours = (d.getHours() < 10) ? "0" + d.getHours() : d.getHours();
                var minutes = (d.getMinutes() < 10) ? "0" + d.getMinutes() : d.getMinutes();
                var formattedTime = hours + ":" + minutes;

               formattedDate = formattedDate + ", " + formattedTime;
               var result = {"facilityName":selectedFacilityName,"facilityId":selectedFacility,"encounterDate":formattedDate}
               $('#lastEncounter').hide();
               var template = $('#template_search_lastencounter').html();
               Mustache.parse(template);
               var rendered = Mustache.render(template, result);
               $('#lastEncounter').html(rendered);
               $('#lastEncounter').attr("hidden",false);
               $('#lastEncounter').show();
            },
            error: function(e){
                showErrors(e);
            }
        });
        e.preventDefault();
    };

    $('#getVisitTypeButton').bind("click",function(e){
        clearErrors();
        clearErrors("#errorBlock1");
        disableAllResult();
        document.getElementById("searchTxt").value = "";
        var selectedFacility = $('input[name="selectedFacilities"]:checked').val();
        var date = $('#visitDate').val();
        document.getElementById("visitDate").value = "";
        var targetUrl =  "/facility/" + selectedFacility + "/visitTypes/forDate?date=" + date;
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
               if(response.length==0){
                   showErrors("No visit for the date entered","#errorBlock1");
               }else{
                   var dateTemplate = $("#template_date_selected_for_visit").html();
                   Mustache.parse(dateTemplate);
                   var htmlRendered = Mustache.render(dateTemplate,{"date":date})
                   $("#dateSelectedForVisitType").html(htmlRendered);
                   var template = $("#template_visit_type_with_count").html();
                   Mustache.parse(template);
                   var rendered = Mustache.render(template,response);
                   $('#visitTypeWithCount tbody').html(rendered);
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
        document.getElementById("searchTxt").value = "";
        var selectedFacility = $('input[name="selectedFacilities"]:checked').val();
        var startDate = $('#startDate').val();
        var endDate = $('#endDate').val();
        document.getElementById("startDate").value = "";
        document.getElementById("endDate").value = "";
        var targetUrl =  "/facility/" + selectedFacility + "/diagnosis/withinDates?startDate=" + startDate+ "&endDate=" + endDate;
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
               if(response.length==0){
                   showErrors("No diagnoses found for the selected dates","#errorBlock1");
               }else{
                   var dateTemplate = $("#template_date_selected_for_diagnosis").html();
                   Mustache.parse(dateTemplate);
                   var htmlRendered = Mustache.render(dateTemplate,{"startDate":startDate, "endDate":endDate})
                   $("#dateRangeSelectedForDiagnosis").html(htmlRendered);
                   var template = $("#template_diagnosis_name_with_count").html();
                   Mustache.parse(template);
                   var rendered = Mustache.render(template,response);
                   $('#diagnosisNameWithCount tbody').html(rendered);
                   $('#getDiagnosisWithCount').attr("hidden", false);
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

    var startDatePicker = $('#startDate').datepicker({
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
      $('#endDate')[0].focus();
    }).data('datepicker');
    var endDatePicker = $('#endDate').datepicker({
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
    }).on('changeDate', function(ev) {
      $('#getDiagnosisWithCountButton').attr("disabled",false);
      $('#getDiagnosisWithCount').attr("hidden", true);
      clearErrors("#errorBlock1");
    }).data('datepicker');

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
        $("#accordion").attr("hidden",true);
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

    var disableAllResult = function(){
        $('#getDiagnosisWithCount').attr("hidden", true);
        $('#visitWithCount').attr("hidden",true);
    }
}