// PANELS
$(document).ready(function () {
    // panel close
    $('body').delegate("a.panel-close", 'click', function (e) {
        e.preventDefault();
        $(this).parent().parent().parent().fadeOut();
    });

    $('body').delegate("a.panel-minimize", "click", function(e) {
        e.preventDefault();
        var $target = $(this).parent().parent().next('.panel-body');
        if ($target.is(':visible')) {
            $('i', $(this)).removeClass('fa-caret-up').addClass('fa-caret-down');
        } else {
            $('i', $(this)).removeClass('fa-caret-down').addClass('fa-caret-up');
        }
        $target.slideToggle();
    });

    runHeight();
    runToTop();
    $(window).bind("load resize", function () {
        runHeight();
    });
});

function runToTop() {
    $(window).scroll(function () {
        if ($(this).scrollTop() < 100) {
            $('#totop').fadeOut();
        } else {
            $('#totop').fadeIn();
        }
    });
    $('#totop').on('click', function () {
        $('html, body').animate({scrollTop: 0}, 'fast');
        return false;
    });
};

function runHeight() {
    var winHeight = $(window).height();
    var docHeight = $(document).height();
    $("#sidebar").css('min-height', docHeight);
    $("#content").css('min-height', docHeight);
}