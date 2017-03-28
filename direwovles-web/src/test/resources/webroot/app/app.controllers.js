var myApp = angular.module('MyApp.controllers', ['ui.router', 'oc.lazyLoad', 'ngStorage']);


myApp.controller('MainCtrl', ['$scope', 'cfpLoadingBar', '$localStorage', '$ocLazyLoad', 'ASSETS', function ($scope, cfpLoadingBar, $localStorage, $ocLazyLoad, ASSETS) {
    cfpLoadingBar.start();

    $scope.start = function () {
        cfpLoadingBar.start();
    };


    $scope.$storage = $localStorage.$default({
        miniSidebar: false
    });


    $scope.minibar = function (argument) {
        // $scope.$storage.miniSidebar = argument;
        console.log(argument);
    }

    $ocLazyLoad.load([
        ASSETS.js('layouts', 'layout-main')
    ]);
    /* $scope.$on('$viewContentLoaded',
     function(event){
     var winHeight = $(window).height();
     var docHeight = $(document).height();
     $("#sidebar").css('min-height', docHeight);
     $("#content").css('min-height', docHeight);
     });*/
}])

    .controller("leftSideBarCtrl", function ($scope, $http) {
        var leftSideBarCtrl = this;
        // 加载菜单
    })
;

