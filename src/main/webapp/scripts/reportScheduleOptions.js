function ReportScheduleOptions(orgUnits,formErrors,success) {
   var periodId = "#periodType", startDtId = "#startDate", displayPeriodId =  "#reportingPeriod";
   var dtCh= "/",  minYear=1900, maxYear=2100;
   var applicableOrgUnits = [];
   var isChecked=false;
   var isDateSelected=false;
   var self = this;


   $(periodId).change(function() {
        self.validateInput($(this).val());
   });

   $('#scheduleStartDate').datepicker({
       autoclose: true,
       onRender: function(date) {
            var nowTemp = new Date();
            var now = new Date(nowTemp.getFullYear(), nowTemp.getMonth(), nowTemp.getDate(), 0, 0, 0, 0);
            return date.valueOf() < now.valueOf() ? 'disabled' : '';
       },
       format: 'dd/mm/yyyy'
   });

   $('#startDate').datepicker({
         autoclose: true,
         onRender: function(dateTemp) {
            var nowTemp = new Date();
            var now = new Date(nowTemp.getFullYear(), nowTemp.getMonth(), nowTemp.getDate(), 0, 0, 0, 0);
            var date = new Date(dateTemp.getFullYear(), dateTemp.getMonth(), dateTemp.getDate(), 0, 0, 0, 0);
            return now.valueOf() < date.valueOf() ? 'disabled' : '';
         },
         format: 'dd/mm/yyyy'
   }).on('changeDate', function (ev) {
       if(self.validateInput($("#periodType").val()))
            isDateSelected = true;
       else
            isDateSelected = false
       self.disableSubmitAndPreview();
   });

    $("form input[type=submit]").click(function() {
            $("input[type=submit]", $(this).parents("form")).removeAttr("clicked");
            $(this).attr("clicked", "true");
    });

   $('#reportScheduleForm').submit(function(e) {
        var val = $("input[type=submit][clicked=true]").val();
        if (val == "Submit") {
           var validationResult = self.validateInput($("#periodType").val());
           if (validationResult) {
               $('#periodType').removeAttr('disabled');
               if ($('input[type=radio]:checked').val() == "repeat"){
                   var cronExp = calculateCronExp();
                   $('#cronExp').val(cronExp);
               }
           } else {
               e.preventDefault();
           }
         }
        else if (val == "Preview") {
            var configId = $("#configId").val();
            var targetUrl = "/dhis2/reports/schedule/" + configId + "/preview";
            var data = $(this).serialize() + '&periodType=' + $('#periodType').val();
            $.ajax({
                type: "POST",
                url: targetUrl,
                data: data,
                dataType: 'json',
                success: function(response){
                    if(response.formErrors != null && response.formErrors.length > 0) {
                        showErrors(response.formErrors);
                    } else {
                        getDhisNames(response);
                    }
                },
                error : function(e){
                    showErrors("Error occured" + e);
                }
            });
            e.preventDefault();
        }
   });

   $('#cron-dow').multiselect({
       includeSelectAllOption: true
   });

   $("#loadScheduleStatus, #tabViewScheduledJobs").bind("click", function() {
       clearSuccess();
       var configId = $("#configId").val();
       var targetUrl = "/dhis2/reports/schedule/" + configId + "/jobs";
       $.ajax({
            type: "GET",
            url: targetUrl,
            success: function(results){
                var template = $('#template_scheduled_jobs_results').html();
                Mustache.parse(template);
                var rendered = Mustache.render(template, results);
                $('#reportScheduleStatus tbody').html(rendered);
            },
            error : function(e){
                showErrors("Error occured" + e);
            }
       });
   });

   $("input[name=scheduleType]").bind("click", function(e) {
        var val = $('input[type=radio]:checked').val();
        if (val == "repeat") {
            $('#recurringSchedule').removeAttr("hidden");
            $('#onceSchedule').attr("hidden",true);
            $(startDtId).val("");
        } else if (val == "once") {
            $('#recurringSchedule').attr("hidden", true);
            $('#onceSchedule').removeAttr("hidden");

        }
        self.disableSubmitAndPreview();
   });

        this.disableSubmitAndPreview = function() {

          if ($('input[type=radio]:checked').val() === "once"){
            if (isChecked && isDateSelected) {
              $("#preview").attr("disabled", false);
              $("#submit").attr("disabled", false);
            }
            else{
              $("#submit").attr("disabled", true);
              $("#preview").attr("disabled", true);
            }
          }
          else if ($('input[type=radio]:checked').val() === "repeat"){
                if(isChecked){
                    $("#submit").attr("disabled", false);
                }
                $("#preview").attr("disabled", true);
          }
      }

   this.validateInput = function(periodValue) {
       var dateString = $(startDtId).val();
       $(displayPeriodId).text('');
       if($('input[type=radio]:checked').val() === "repeat"){
            return true;
       }
       if (dateString === '') {
          showErrors("Please select a valid date.");
          return false;
       }

       var inputDate = stringToDate(dateString);
       if (inputDate == null) {
            $(displayPeriodId).text('Invalid Period');
            return false;
       } else {
            var result = false;
            if (periodValue == "Daily") {
                result = validateDailyReportingPeriod(inputDate);
            } else if (periodValue === 'Monthly') {
                result = validateMonthlyReportingPeriod(inputDate);
            } else if (periodValue === "Yearly") {
                result = validateYearlyReportingPeriod(inputDate);
            } else if (periodValue === "Quarterly") {
               result = validateQuarterlyReportingPeriod(inputDate);
            }
            return result;
       }
   };

   $('#schedulePeriod').on('change', function() {
        var ddl = document.getElementById("schedulePeriod");
         var selectedValue = ddl.options[ddl.selectedIndex].value;
            if (selectedValue == "daily") {
                $("#dow").attr("hidden", true);
                $("#cron-dow").attr("required", false);
                $("#dom").attr("hidden", true);
                $("#cron-dom").attr("required", false);
                $("#cronFrequency").attr("hidden", true);
                $("#cronFrequency").attr("required", false);
           } else if (selectedValue == "weekly") {
                $("#dow").attr("hidden", false);
                $("#cron-dow").attr("required", true);
                $("#dom").attr("hidden", true);
                $("#cron-dom").attr("required", false);
                $("#cronFrequency").attr("hidden", true);
                $("#cronFrequency").attr("required", false);
           } else if (selectedValue == "monthly") {
                $("#dom").attr("hidden", false);
                $("#cron-dom").attr("required", true);
                $("#cronFrequency").attr("hidden", false);
                $("#cronFrequency").attr("required", true);
                $("#dow").attr("hidden", true);
                $("#cron-dow").attr("required", false);
           }
   });


   var calculateCronExp = function() {
        var cronSec = "0";
        var cronMin = cronHour = cronDay = cronMonth = cronYear = "*";
        var cronDow = "?";
        var ddl = document.getElementById("schedulePeriod");
        var selectedValue = ddl.options[ddl.selectedIndex].value;
        if (selectedValue == "weekly"){
            cronDow = $("#cron-dow").val();
            cronDay = "?";
        } else if(selectedValue == "monthly"){
            cronDay = $("#cron-dom").val();
            if ($('#cronFrequency').val() > 1) {
                cronMonth = "1/" + $('#cronFrequency').val();
            }
        }
        else {
            cronDay = "*";
        }
        cronMin = $("#min").val();
        cronHour = $("#hour").val();
        return [cronSec, cronMin, cronHour, cronDay, cronMonth, cronDow, cronYear].join(" ");
   }

   var validateYearlyReportingPeriod =function(reportingDate) {
        var currentDate = new Date();
        var currentYear = currentDate.getFullYear();
        var reportingYear = reportingDate.getFullYear();
        if (reportingYear >= currentYear) {
            showErrors("Please select a valid date of past years. Reporting period must be in the past.");
            return false;
        }
        $(displayPeriodId).text("Reporting Period:" + reportingYear);
        return true;
   };

   var validateDailyReportingPeriod = function(reportingDate) {
        var currentDate = new Date();
        currentDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
        if (reportingDate >= currentDate) {
            showErrors("Please select a valid date. Reporting period must be in the past.");
            return false;
        }
        return true;
   };

   var validateMonthlyReportingPeriod = function(reportingDate) {
        var currentDate = new Date();
        var currentYear = currentDate.getFullYear();
        var currentMonth = currentDate.getMonth() + 1;

        var reportingYear = reportingDate.getFullYear();
        if (reportingYear > currentYear) {
            showErrors("Please select a valid date of past months. Reporting period must be in the past.");
            return false;
        }

        var reportingMonth = reportingDate.getMonth() + 1;
        if (reportingMonth >= currentMonth && reportingYear === currentYear) {
            showErrors("Please select a valid date of past months. Reporting period must be in the past.");
            return false;
        }

        if (reportingMonth.toString().length < 2) {
           reportingMonth = "0" + reportingMonth.toString();
        }
        var text = reportingMonth + "/" + reportingYear;
        $(displayPeriodId).text("Reporting Period:" + text);
        return true;
   };

   var validateQuarterlyReportingPeriod = function(reportingDate) {
        var quarterNumber = parseInt(reportingDate.getMonth() / 3) + 1;
        var lastMonth = quarterNumber*3;
        var lastDateOfQuarter = new Date(reportingDate.getFullYear(), lastMonth, 0);

        var today = new Date();
        today.setHours(0,0,0,0);

        if (lastDateOfQuarter >= today ) {
            showErrors("Please select a valid date of past quarters. Reporting period must be in the past");
            return false;
        }

        var text = lastDateOfQuarter.getFullYear() + "Q" + quarterNumber;
        $(displayPeriodId).text("Reporting Period:" + text);

        return true;
   };

   var stringToDate = function(dateString) {
       var regex = /^(?:(?:31(\/|-|\.)(?:0?[13578]|1[02]))\1|(?:(?:29|30)(\/|-|\.)(?:0?[1,3-9]|1[0-2])\2))(?:(?:1[6-9]|[2-9]\d)?\d{2})$|^(?:29(\/|-|\.)0?2\3(?:(?:(?:1[6-9]|[2-9]\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\d|2[0-8])(\/|-|\.)(?:(?:0?[1-9])|(?:1[0-2]))\4(?:(?:1[6-9]|[2-9]\d)?\d{2})$/;
       if (regex.test(dateString)) {
           var parts = dateString.split("/");
           var dt = new Date(parts[1] + "/" + parts[0] + "/" + parts[2]);
           var result = (dt.getDate() == parts[0] && dt.getMonth() + 1 == parts[1] && dt.getFullYear() == parts[2]);
           return result ? dt : null;
       }
       return null;
   };

   var fetchOrgUnits = function() {
       var datasetId = $("#datasetIdEl").val();
       var targetUrl = "/dhis2/reports/" + datasetId + "/applicableOrgUnits";
       $.get(targetUrl).done(function(results) {
           if (results) {
              if (results.hasOwnProperty('organisationUnits')) {
                 self.applicableOrgUnits = results.organisationUnits;
                 showApplicableOrgUnits(orgUnits);
              }
           }
       }).fail(function(){
            showErrors("Unable to fetch dataset.");
            $('#createReportSchedule').attr('hidden', true);
       });
   };

   var showApplicableOrgUnits = function(availableOrgUnits){
         var availableApplicableOrgUnits = getAvailableApplicableOrgUnits(availableOrgUnits, self.applicableOrgUnits);
         if(availableApplicableOrgUnits.length==0){
            showErrors("This report is not applicable to any of the available facilities.");
            $('#createReportSchedule').attr('hidden', true);
         }
         else{
             var template = $('#template_applicable_org_units').html();
             Mustache.parse(template);
             var rendered = Mustache.render(template,availableApplicableOrgUnits);
             $('#applicableOrgUnits').html(rendered);
             $('#applicableOrgUnits').attr('hidden',false);

             var selectAll = $("input[name='selectAllFacilities']");
             var checkboxes = $("input[name='selectedFacilities']");

             selectAll.click(function () {
                 if (selectAll.is(":checked")) {
                     $("input[name='selectedFacilities']").prop('checked', true)
                 } else {
                     $("input[name='selectedFacilities']").prop('checked', false)
                 }
                 if(availableApplicableOrgUnits.length == $('input[name="selectedFacilities"]:checked').length){
                     selectAll.prop('checked',true);
                     $("#selectAllLabel").text("Unselect All")
                 }
                 else{
                     $("#selectAllLabel").text("Select All")
                 }
                 if($('input[name="selectedFacilities"]:checked').length == 0){
                     selectAll.prop('checked',false);
                 }
             });


             checkboxes.click(function() {
                 if(availableApplicableOrgUnits.length == $('input[name="selectedFacilities"]:checked').length){
                     selectAll.prop('checked',true);
                     $("#selectAllLabel").text("Unselect All")
                 }
                 else{
                     $("#selectAllLabel").text("Select All")
                 }
                 if($('input[name="selectedFacilities"]:checked').length == 0){
                     selectAll.prop('checked',false);
                 }
                 if(checkboxes.is(":checked")){
                     isChecked = true;
                 }
                 else{
                     isChecked = false;
                 }
                 self.disableSubmitAndPreview();
             });
         }
   }

    var getAvailableApplicableOrgUnits = function(availableOrgUnits, applicableOrgUnits){
         var filteredOrgUnits = availableOrgUnits.filter(function(availableUnit) {
            return applicableOrgUnits.find(function(applicableUnit) {
                return applicableUnit.id == availableUnit.orgUnitId})
         });
         return filteredOrgUnits;
    }

   var getDhisNames = function(response) {
       var dataElements = {};
       var comboOptions = {};
       var deferredCalls = [];
       var defferedApply = [];
       $.each(response.reports, function(index, report){
          $.each(report.results.dataValues, function(index, item) {
              if(!(item.dataElement in  dataElements)) {
                  dataElements[item.dataElement] = "";
                  deferredCalls.push($.ajax({
                      url: "/dhis2/dataSets/dataElements/" + item.dataElement,
                      success: function(rs) {
                         if(rs.name != "(default)") {
                             dataElements[item.dataElement] = rs.name;
                         }
                      },
                      error: function(rs) {
                         showErrors("Unable fetch data from DHIS");
                      }
                  }));
              }
              if(!(item.categoryOptionCombo in  comboOptions)) {
                  comboOptions[item.categoryOptionCombo] = "";
                  deferredCalls.push($.ajax({
                      url: "/dhis2/dataSets/categoryOptionCombos/" + item.categoryOptionCombo,
                      success: function(rs) {
                         if(rs.name != "(default)") {
                            comboOptions[item.categoryOptionCombo] = rs.name;
                         }
                      },
                      error: function(rs) {
                         showErrors("Unable fetch data from DHIS");
                      }
                  }));
              }
          });

          defferedApply.push($.when.apply($, deferredCalls).done(function() {
              $.each(report.results.dataValues, function(index, item) {
                  item.name = dataElements[item.dataElement] + " " + comboOptions[item.categoryOptionCombo];
              });
          }));
       });
       $.when.apply($, defferedApply).done(function() {
           var template = $('#template_report_preview').html();
           Mustache.parse(template);
           var rendered = Mustache.render(template, response);
           $('#previewModalContent').html(rendered);
           $('#previewModal').modal('show', {backdrop: 'static'});
           var tableId = $('[id^="facility-table-"]').eq(0).attr('id');
           var firstFacilityId = tableId.split("-").pop();
           toggleFacilityDetails(firstFacilityId,true);
       });
   };

   fetchOrgUnits();
   showErrors(formErrors);
   showSuccess(success);
}

var toggleFacilityDetails = function(facilityId, show) {
   var tableId = "facility-table-" + facilityId;
   var viewButtonId = "view-facility-" + facilityId;
   var closeButtonId = "close-facility-" + facilityId;
   if(!show) {
       $("#" + tableId).prop("hidden", true);
       $("#" + closeButtonId).prop("hidden", true);
       $("#" + viewButtonId).prop("hidden", false);
   } else {
       $("#" + tableId).prop("hidden", false);
       $("#" + viewButtonId).prop("hidden", true);
       $("#" + closeButtonId).prop("hidden", false);
   }
};

var getPeriodType = function(){
       var periodTypeToPeriodUnitMap = {};
       periodTypeToPeriodUnitMap["Daily"] = "day(s)";
       periodTypeToPeriodUnitMap["Monthly"] = "month(s)";
       periodTypeToPeriodUnitMap["Quarterly"] = "quarter(s)";
       periodTypeToPeriodUnitMap["Weekly"] = "week(s)";
       periodTypeToPeriodUnitMap["Yearly"] = "year(s)";

       var periodType = $("#periodType").val();
       return periodTypeToPeriodUnitMap[periodType] || "unknown";
}

function printDiv(divName){
    var contents = document.getElementById(divName).innerHTML;
    var frame1 = document.createElement('iframe');
    frame1.name = "frame1";
    frame1.style.position = "absolute";
    frame1.style.top = "-1000000px";
    document.body.appendChild(frame1);
    var frameDoc = frame1.contentWindow ? frame1.contentWindow : frame1.contentDocument.document ? frame1.contentDocument.document : frame1.contentDocument;
    frameDoc.document.open();
    frameDoc.document.write('<html><head></head>');
    frameDoc.document.write('<body>');
    frameDoc.document.write(contents);
    frameDoc.document.write('</body></html>');
    frameDoc.document.close();
    setTimeout(function () {
        window.frames["frame1"].focus();
        window.frames["frame1"].print();
        document.body.removeChild(frame1);
    }, 500);
    return false;
}


