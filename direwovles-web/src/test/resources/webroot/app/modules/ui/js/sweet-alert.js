function sweetAlertCtrl($scope, $ocLazyLoad, ASSETS) {
    $ocLazyLoad.load([
        ASSETS.js('ui/sweet-alert','sweet.custom')
    ]);
}
sweetAlertCtrl.$inject = [ '$scope','$ocLazyLoad', 'ASSETS'];

