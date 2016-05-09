var showErrors = function(errors) {
    if(errors != null && errors.length > 0) {
      $("#errorBlock").prop("hidden", false);
       var template = $('#template_errors').html();
       Mustache.parse(template);
       var rendered = Mustache.render(template, errors);
       $('#errorBlock').html(rendered);
   }
};

var clearErrors = function(errors) {
    $("#errorBlock").prop("hidden", true);
};