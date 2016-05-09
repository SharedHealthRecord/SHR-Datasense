var showErrors = function(errors) {
    if(errors != null && errors.length > 0) {
       var template = $('#template_errors').html();
       Mustache.parse(template);
       var rendered = Mustache.render(template, errors);
       $('#errorBlock').html(rendered);
   }
};