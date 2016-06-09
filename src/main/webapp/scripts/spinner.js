var counter = 0;
$(document).ajaxStart(function() {
    if(counter == 0) {
        var over = '<div id="overlay"><img id="loading" class = "loaderImage" src="/images/ajax-loader.gif" /></div>';
        $(over).appendTo('body');
    }
    counter ++;
});
$(document).ajaxStop(function() {
    counter --;
    if(counter == 0) {
        $('#overlay').remove();
    }
});