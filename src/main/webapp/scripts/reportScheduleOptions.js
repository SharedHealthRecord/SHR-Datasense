function ReportScheduleOptions(formErrors) {
   var periodId = "#periodType", startDtId = "#startDate", displayPeriodId =  "#reportingPeriod";
   var dtCh= "/",  minYear=1900, maxYear=2100;
   var applicableOrgUnits = [];
   var isChecked=false;
   var isDateSelected=false;
   var self = this;

   $(periodId).change(function() {
        self.validateInput($(this).val());
   });

   var checkboxes = $("input[type='checkbox']");
   checkboxes.click(function() {
        if(checkboxes.is(":checked")){
            isChecked = true;
        }
        else{
            isChecked = false;
        }
        self.disableSubmitAndPreview();
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
            return now.valueOf() <= date.valueOf() ? 'disabled' : '';
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
            loading();
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
                complete: function(){
                   $('#overlay').remove();
                }
            });
            e.preventDefault();
        }
   });

   $("#loadScheduleStatus").bind("click", function() {
       var configId = $("#configId").val();
       var targetUrl = "/dhis2/reports/schedule/" + configId + "/jobs";
       $.get(targetUrl).done(function(results) {
           var template = $('#template_scheduled_jobs_results').html();
           Mustache.parse(template);
           var rendered = Mustache.render(template, results);
           $('#reportScheduleStatus tbody').html(rendered);
       });
   });

   $("input[name=selectedFacilities]").bind("click", function(e) {
        var orgUnitId = $(e.target).attr("data-orgunit");
        var arrayLength = self.applicableOrgUnits.length;
        var found = false;
        for (var i = 0; i < arrayLength; i++) {
            var orgUnit = self.applicableOrgUnits[i];
            if (orgUnit.id === orgUnitId) {
                found = true;
            }
        }
        if (!found)  {
            alert("The selected organization is not applicable for this report.");
            e.preventDefault();
        }
   });

   $("input[name=scheduleType]").bind("click", function(e) {
        var val = $('input[type=radio]:checked').val();
        if (val == "repeat") {
            $('#recurringSchedule').removeAttr("hidden");
        } else {
            $('#recurringSchedule').attr("hidden", true);
        }
        self.disableSubmitAndPreview();
   });

   this.disableSubmitAndPreview = function() {
          if (isChecked && isDateSelected) {
              $("#submit").attr("disabled", false);
              if ($('input[type=radio]:checked').val() === "once")
                $("#preview").attr("disabled", false);
              else
                $("#preview").attr("disabled", true);
          }
          else {
              $("#submit").attr("disabled", true);
              $("#preview").attr("disabled", true);
          }
      }

   this.validateInput = function(periodValue) {
       var dateString = $(startDtId).val();
       $(displayPeriodId).text('');
       if (dateString === '') {
          alert("Please select a valid date.");
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
            alert("Please select a valid date of past years. Reporting period must be in the past.");
            return false;
        }
        $(displayPeriodId).text("Reporting Period:" + reportingYear);
        return true;
   };

   var validateDailyReportingPeriod = function(reportingDate) {
        var currentDate = new Date();
        currentDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
        if (reportingDate >= currentDate) {
            alert("Please select a valid date. Reporting period must be in the past.");
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
            alert("Please select a valid date of past months. Reporting period must be in the past.");
            return false;
        }

        var reportingMonth = reportingDate.getMonth() + 1;
        if (reportingMonth >= currentMonth) {
            alert("Please select a valid date of past months. Reporting period must be in the past.");
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
            alert("Please select a valid date of past quarters. Reporting period must be in the past");
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
              }
           }
       }).fail(function(){
            showErrors("Unable to fetch dataset.");
            $('#createReportSchedule').attr('hidden', true);
       });
   };

   var loading = function(){
    var over = '<div id="overlay">' +
                '<img id="loading" class = "loaderImage" src="/images/ajax-loader.gif">' +
                '</div>';
    $(over).appendTo('body');
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
       });
   };

   fetchOrgUnits();
   showErrors(formErrors);
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
