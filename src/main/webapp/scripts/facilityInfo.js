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

    $("input[name=searchBy]").bind("click", function(e) {
       document.getElementById("searchTxt").value = "";
       clearErrors();
       $("#searchResult").attr("hidden",true);
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
                    showErrors("No facility is available for given facility id/name");
                }
                else{
                   $('#searchResultsContainerForFacility').hide();
                   var template = $('#template_search_facilities').html();
                   Mustache.parse(template);
                   var rendered = Mustache.render(template, result);
                   $('#searchResultsContainerForFacility').html(rendered);
                   $('#searchResultsContainerForFacility').show();
                   $("#searchResult").attr("hidden",false);

                }
           }
        });
    }

    var searchFacility = function(searchTxt){
        $("#searchResult").attr("hidden",true);
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
}