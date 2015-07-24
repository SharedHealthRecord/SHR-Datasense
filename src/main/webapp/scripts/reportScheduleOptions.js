function ReportScheduleOptions(periodEle, startDateEle, reportingPeriodEle) {
   var periodId = "#periodType", startDtId = "#startDate", displayPeriodId =  "#reportingPeriod";
   var dtCh= "/",  minYear=1900, maxYear=2100;
   var applicableOrgUnits = [];

   var self = this;
   $(periodId).change(function() {
        self.validateInput($(this).val());
   });

   $('#startDate').datepicker({
         autoclose: true,
         format: 'dd/mm/yyyy'
   }).on('changeDate', function (ev) {
       self.validateInput($("#periodType").val());
   });

   $('#reportScheduleForm').submit(function(e) {
       var validationResult = self.validateInput($("#periodType").val());
       if (validationResult) {
           $('#periodType').removeAttr('disabled');
       } else {
           e.preventDefault();
       }
   });



   $("#loadScheduleStatus").bind("click", function() {
       var datasetId = $("#datasetIdEl").val();
       var targetUrl = "/dhis2/reports/schedule/" + datasetId + "/jobs";
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
       });
   };

   fetchOrgUnits();

}

