function FacilityInformation(facilityId, facilityName) {
    $('#getVisitTypeButton').bind("click",function(e){
        clearErrors();
        disableAllResult();
        var date = $('#visitDate').val();
        var targetUrl =  "/facility/" + facilityId + "/visitTypes/forDate?date=" + date;
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
               if(response.length==0){
                   showErrors("No visit for the date entered");
               }else{
                   var selectedDate = {"date":date};
                   renderDataToTemplate("#template_date_selected_for_visit",selectedDate,"#dateSelectedForVisitType");
                   renderDataToTemplate("#template_visit_type_with_count",response,'#visitTypeWithCount tbody');
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
        disableAllResult();
        var startDate = $('#diagnosisStartDate').val();
        var endDate = $('#diagnosisEndDate').val();
        var targetUrl =  "/facility/" + facilityId + "/diagnosis/withinDates?startDate=" + startDate+ "&endDate=" + endDate;
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
               if(response.length==0){
                   showErrors("No diagnoses found for the selected dates");
               }else{
                    var dates={"startDate":startDate, "endDate":endDate};
                    renderDataToTemplate("#template_for_selected_date_range",dates,"#dateRangeSelectedForDiagnosis");
                    renderDataToTemplate("#template_diagnosis_name_with_count",response,'#diagnosisNameWithCount tbody');
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
        disableAllResult();
        var startDate = $('#encounterStartDate').val();
        var endDate = $('#encounterEndDate').val();
        var targetUrl =  "/facility/" + facilityId + "/encounterTypes/withinDates?startDate=" + startDate+ "&endDate=" + endDate;
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
               if(response.length==0){
                 showErrors("No encounter types for the date entered");
               }else{
                 var dates = {"startDate":startDate, "endDate":endDate};
                 renderDataToTemplate("#template_for_selected_date_range",dates,"#dateSelectedForEncounterType");
                 renderDataToTemplate("#template_encounter_type_with_count",response,'#encounterWithCount tbody');
                 $('#encounterTypesWithCount').attr("hidden", false);
               }
            },
            error: function(e){
                showErrors(e)
            }
        });
    });

    $('#getDrugsWithCountButton').bind("click",function(e){
        clearErrors();
        disableAllResult();
        var startDate = $('#drugStartDate').val();
        var endDate = $('#drugEndDate').val();
        var targetUrl =  "/facility/" + facilityId + "/prescribedDrugs/withinDates?startDate=" + startDate+ "&endDate=" + endDate;
        $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(response){
                var freeTextCount = response.freetextCount[0];
                var drugCount = response.codedDrugCount[0];
                if(freeTextCount.count==0 && drugCount.count==0){
                  showErrors("No drugs prescribed to be displayed");
                }
                else{
                    var dates = {"startDate":startDate, "endDate":endDate};
                    var noncodedDrugWithCount = response.nonCodedDrugsWithCount;
                    var codedDrugWithCount = response.codedDrugWithCount;
                    renderDataToTemplate("#template_for_selected_date_range",dates,"#dateSelectedForprescribedDrugs");
                    renderDataToTemplate("#template_drug_count",freeTextCount,"#freetextCount");
                    renderDataToTemplate("#template_noncoded_drug_with_count",noncodedDrugWithCount,"#nonCodedDrugs tbody")
                    renderDataToTemplate("#template_drug_count",drugCount,"#DrugCount");
                    renderDataToTemplate("#template_coded_drug_with_count",codedDrugWithCount,"#CodedDrugs tbody")
                    $('#prescribedDrugs').attr("hidden",false);
                }
            },
            error: function(e){
                showErrors(e)
            }
        });
    });

    $('#visitDate').datepicker({
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
       $(this).datepicker('hide');
       clearErrors();
    });

    var createStartEndDatePicker = function(startDateId, endDateId, endDateChange){
        var startDatePicker = $(startDateId).datepicker({
          onRender: function(dateTemp) {
            var nowTemp = new Date();
            var now = new Date(nowTemp.getFullYear(), nowTemp.getMonth(), nowTemp.getDate(), 0, 0, 0, 0);
            var date = new Date(dateTemp.getFullYear(), dateTemp.getMonth(), dateTemp.getDate(), 0, 0, 0, 0);
            return now.valueOf() < date.valueOf() ? 'disabled' : '';
          },
          format: 'dd/mm/yyyy'
        }).on('changeDate', function(ev) {
           var newDate = new Date(ev.date)
           newDate.setDate(newDate.getDate());
           endDatePicker.setValue(newDate);
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
        }).on('changeDate', endDateChange).data('datepicker');
    }

    var endDateChangeforDiagnosis= function(ev) {
      $('#getDiagnosisWithCountButton').attr("disabled",false);
      $('#getDiagnosisWithCount').attr("hidden", true);
      $(this).datepicker('hide');
      clearErrors();
    }

    var endDateChangeforEncounter= function(ev) {
      $('#getEncounterTypesWithCountButton').attr("disabled",false);
      $('#encounterTypesWithCount').attr("hidden", true);
      $(this).datepicker('hide');
      clearErrors();
    }
    var endDateChangeforDrug= function(ev) {
          $('#getDrugsWithCountButton').attr("disabled",false);
          $('#prescribedDrugs').attr("hidden", true);
          $('.panel-collapse.in').collapse('hide');
          $(this).datepicker('hide');
          clearErrors();
        }

    var getFormattedDate = function(date){
        var d = new Date(date);
        var formattedDate = d.getDate() + "-" + (d.getMonth() + 1) + "-" + d.getFullYear();
        var hours = (d.getHours() < 10) ? "0" + d.getHours() : d.getHours();
        var minutes = (d.getMinutes() < 10) ? "0" + d.getMinutes() : d.getMinutes();
        var formattedTime = hours + ":" + minutes;
        formattedDate = formattedDate + ", " + formattedTime;
        return formattedDate;
    }

    var renderDataToTemplate = function(templateId,data,divId){
       var template = $(templateId).html();
       Mustache.parse(template);
       var rendered = Mustache.render(template,data);
       $(divId).html(rendered);
    }

    var disableAllResult = function(){
       $('#lastEncounter').attr("hidden", true);
       $('#getDiagnosisWithCount').attr("hidden", true);
       $('#encounterTypesWithCount').attr("hidden", true);
       $('#visitWithCount').attr("hidden",true);
       $('#prescribedDrugs').attr("hidden",true);
    }

    createStartEndDatePicker('#diagnosisStartDate', '#diagnosisEndDate', endDateChangeforDiagnosis);
    createStartEndDatePicker('#encounterStartDate', '#encounterEndDate', endDateChangeforEncounter);
    createStartEndDatePicker('#drugStartDate', '#drugEndDate', endDateChangeforDrug);

}