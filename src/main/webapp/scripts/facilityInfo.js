function FacilityInformations() {
    $("#searchTxt").keyup(function(event){
        if(event.keyCode == 13){
            $("#searchBtn").click();
        }
    });

    $("#searchBtn").click(function() {
        var searchText = $("#searchTxt").val();
        searchFacility(searchText);
    });

    $(document).on('click','input[name=selectedFacilities]',function(e){
        $('#lastEncounter').attr("hidden",true);
        $("#encounterDate").attr("hidden", false);
    });

    $("input[name=searchBy]").bind("click", function(e) {
       document.getElementById("searchTxt").value = "";
       clearErrors();
       $("#avalilableFacilities").attr("hidden",true);
       $('#encounterDate').attr("hidden",true);
       $('#lastEncounter').attr("hidden",true);
    });

    $('#getEncounterDate').bind("click", function(e) {
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
               $('#searchResultsLastEncounter').hide();
               var template = $('#template_search_lastencounter').html();
               Mustache.parse(template);
               var rendered = Mustache.render(template, result);
               $('#searchResultsLastEncounter').html(rendered);
               $('#lastEncounter').attr("hidden",false);
               $('#searchResultsLastEncounter').show();
               $("#encounterDate").attr("hidden", true);
            },
            error: function(e){
                showErrors(e);
            }
        });
        e.preventDefault();
    });

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
                    showErrors("No facility is available for given facility id/name")
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
        var targetUrl;
        var selectedOption = $('input[name="searchBy"]:checked').val();
        if(selectedOption == "FacilityId"){
            if(validateFacilityId(searchTxt)){
                targetUrl = "/facility/search?id=" + searchTxt;
                getFacilities(targetUrl);
            }
            else{
                showErrors("Incorrect Facility Id. Facility ID contains only digits.")
            }
        }
        else if(selectedOption == "FacilityName"){
                targetUrl = "/facility/search?name=" + searchTxt;
                getFacilities(targetUrl);
        }

    }
}