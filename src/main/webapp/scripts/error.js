var showErrors = function(errors, errorDivId) {
    var errorDiv = errorDivId || "#errorBlock";
    if(errors != null && errors.length > 0) {
      $(errorDiv).prop("hidden", false);
       var template = $('#template_errors').html();
       Mustache.parse(template);
       var rendered = Mustache.render(template, errors);
       $(errorDiv).html(rendered);
   }
};

var clearErrors = function(errorDivId) {
    var errorDiv = errorDivId || "#errorBlock";
    $(errorDiv).prop("hidden", true);
};