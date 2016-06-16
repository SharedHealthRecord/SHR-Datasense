var showSuccess = function(success, successDivId) {
    var successDiv = successDivId || "#successBlock";
    if(success != null && success.length > 0) {
      $(successDiv).attr("hidden", false);
       var template = $('#template_success').html();
       Mustache.parse(template);
       var rendered = Mustache.render(template, success);
       $(successDiv).html(rendered);
   }
};

var clearSuccess = function(successDivId) {
    var successDiv = successDivId || "#successBlock";
    $(successDiv).prop("hidden", true);
};