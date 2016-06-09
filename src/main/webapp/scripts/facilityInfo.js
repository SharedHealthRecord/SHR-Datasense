function FacilityInformations(){

    $(document).on('click','input[name=selectedFacilities]',function(e){
        $('#lastEncounter').attr("hidden",true);
        $("#submitButton").attr("hidden", false);
    });

    $("input[name=searchBy]").bind("click", function(e) {
       document.getElementById("searchTxt").value = "";
       clearErrors();
       $("#avalilableFacilities").attr("hidden",true);
       $('#lastEncounter').attr("hidden",true);
    });

    $('#facilityInfoForm').submit(function(e) {
        document.getElementById("searchTxt").value = "";
        var targetUrl = "/facilityInfo/encounter";
        var selectedFacility = $('input[name="selectedFacilities"]:checked').val();
        var selectedFacilityName =$('input[name="selectedFacilities"]:checked').attr('id');
        var postData ={"facilityId":selectedFacility};
        $.ajax({
            type: "POST",
            url: targetUrl,
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(postData),
            success: function(response){
                clearErrors();
                var d = new Date(response);
                var formattedDate = d.getDate() + "-" + (d.getMonth() + 1) + "-" + d.getFullYear();
                var hours = (d.getHours() < 10) ? "0" + d.getHours() : d.getHours();
                var minutes = (d.getMinutes() < 10) ? "0" + d.getMinutes() : d.getMinutes();
                var formattedTime = hours + ":" + minutes;

               formattedDate = formattedDate + " , " + formattedTime;
               var result = {"facilityName":selectedFacilityName,"facilityId":selectedFacility,"encounterDate":formattedDate}
               $('#searchResultsLastEncounter').hide();
               var template = $('#template_search_lastencounter').html();
               Mustache.parse(template);
               var rendered = Mustache.render(template, result);
               $('#searchResultsLastEncounter').html(rendered);
               $('#lastEncounter').attr("hidden",false);
               $('#searchResultsLastEncounter').show();
               $("#submitButton").attr("hidden", true);

            },
            dataType: "json",
            error: function(e){
                showErrors(e);
            }
        });
        e.preventDefault();
    });

}

var validateFacilityId = function(searchTxt){
    if(searchTxt.match(/^\d+$/)) {
        return true;
    }
    else{
        return false;
    }
}

var validateFacilityName = function(searchTxt){
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
                targetUrl = "/facilityInfo/searchById?id=" + searchTxt;
                getFacilities(targetUrl);
            }
            else{
                showErrors("Incorrect Facility Id. Facility ID contains only digits.")
            }
        }
        else if(selectedOption == "FacilityName"){
                targetUrl = "/facilityInfo/searchByName?name=" + searchTxt;
                getFacilities(targetUrl);
        }

}
var enableSubmitButton = function(){
  $("#submitButton").attr("hidden", false);
}