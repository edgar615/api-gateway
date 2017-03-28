appHelper = {

    componentsDir: 'app/modules',
    assetsDir: 'assets',

    componentView: function (componentName, viewName) {
        return this.componentsDir + '/' + componentName + '/views/' + viewName + '.html';
    },
    componentJs: function (componentName, jsName) {
        return this.componentsDir + '/' + componentName + '/js/' + jsName + '.js';
    },
    componentData: function (componentName, dataName) {
        return this.componentsDir + '/' + componentName + '/data/' + dataName + '.json';
    },

    assetPath: function (file_path) {
        return this.assetsDir + '/' + file_path;
    },
    lazyLoad : function(state, resources) {
        var baseNames = state.split(".");
        resources = resources || [];

        var url = "";
        var templateUrl = "app/modules/" + baseNames[0] + "/views";
        var ctrlUrl =  "app/modules/" + baseNames[0] + "/js";
        for(var i = 1; i < baseNames.length; i ++) {
            url += '/' + baseNames[i];
        }

        return {
            url: url,
            templateUrl: templateUrl + url + ".html",
            resolve: {
                resources: function ($ocLazyLoad) {
                    return $ocLazyLoad.load([resources]);
                },
                loadMyCtrl : ['$ocLazyLoad', function($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        ctrlUrl + url + ".js"
                    ]);
                }]
            }
        }
    }
};

angular.module('MyApp', ['ui.router', 'ngAnimate', 'MyApp.controllers', 'MyApp.directives', 'MyApp.services', 'oc.lazyLoad', 'chieffancypants.loadingBar', 'validation', 'validation.rule'])

    .run(
    ['$rootScope', '$state', '$stateParams',
        function ($rootScope, $state, $stateParams) {

            // It's very handy to add references to $state and $stateParams to the $rootScope
            // so that you can access them from any scope within your applications.For example,
            // <li ng-class="{ active: $state.includes('contacts.list') }"> will set the <li>
            // to active whenever 'contacts.list' or one of its decendents is active.
            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;
            $state.go("ui.blank");

            $rootScope.$on("update-title", function (event, data) {
                $rootScope.pageTitle = data.pageTitle;
                $rootScope.links = data.links;
            });
        }
    ]
)
    .constant('ASSETS', {
        'core': {
            'core': 'assets/js/core.js'
        },
        'bootstrap': {
            'css': 'libs/bootstrap/css/bootstrap.min.css',
            'js': appHelper.assetPath('libs/bootstrap/js/bootstrap.min.js')
        },
        get: function (fileName) {
            return appHelper.assetPath(fileName);
        },
        js: function (moduleName, fileName) {
            return appHelper.componentsDir + '/' + moduleName + '/js/' + fileName + '.js';
        },
        css: function (moduleName, fileName) {
            return appHelper.componentsDir + '/' + moduleName + '/css/' + fileName + '.css';
        }
    })
    .config(
    ['$stateProvider', '$urlRouterProvider', '$ocLazyLoadProvider', '$validationProvider', 'ASSETS',
        function ($stateProvider, $urlRouterProvider, $$ocLazyLoadProvider, $validationProvider, ASSETS) {

            //校验
            $validationProvider.showSuccessMessage = false;
            $validationProvider.setErrorHTML(function (msg) {
                return  "<span class='help-block'>" + msg + "</span>";
            });

            angular.extend($validationProvider, {
                validCallback: function (element){
                    $(element).parents('.form-group:first').removeClass('has-error');
                },
                invalidCallback: function (element) {
                    $(element).parents('.form-group:first').addClass('has-error');
                }
            });

            /********************************/
            /*** Redirects and Otherwise ****/
            /********************************/

            // Use $urlRouterProvider to configure any redirects (when) and invalid urls (otherwise).
            //$urlRouterProvider
            //    .when('/c?id', '/contacts/:id')
            //    .when('/user/:id', '/contacts/:id')
            //    .otherwise('/app/blank');
            $urlRouterProvider.when("", "/ui/blank");


            /****************************/
            /*** State Configurations ***/
            /*** 使用ui-Router实现路由跳转 ***/
            /****************************/

            // Use $stateProvider to configure your states.
            $stateProvider

            /************* Basic Layout *************/
                .state("ui", {
                    abstract: true,
                    url: "/app",
                    templateUrl: appHelper.componentView('layouts', 'basic-layout'),
                    resolve: {
                        resources: function ($ocLazyLoad) {
                            return $ocLazyLoad.load([
                                //ASSETS.js('layouts', 'layouts'),
                                //ASSETS.core.core,
                            ]);
                        }
                    }
                })
            /************* Blank *************/
                .state("ui.blank", {
                    url: "/blank",
                    templateUrl: appHelper.componentView("blank", "blank")
                })
            /************* wizard *************/
                .state("ui.form-wizard", {
                    url: "/form-wizard",
                    templateUrl: appHelper.componentView("ui/form", "form-wizard")
                })
            /************* info-box *************/
                .state("ui.info-box", {
                    url: "/info-box",
                    templateUrl: appHelper.componentView("ui/info-box", "index")
                })
            /************* carousel *************/
                .state("ui.carousel", {
                    url: "/carousel",
                    templateUrl: appHelper.componentView("ui/carousel", "index")
                })
            /************* tooltips *************/
                .state("ui.tooltips", {
                    url: "/tooltips",
                    templateUrl: appHelper.componentView("ui/tooltips", "index")
                })
                .state("ui.toastr", appHelper.lazyLoad("ui.toastr",[
                    ASSETS.js('ui/toastr', 'main')
                ]))
                .state("ui.sweet-alert", appHelper.lazyLoad("ui.sweet-alert",[
                    appHelper.assetPath("libs/sweet-alert/css/sweetalert.css"),
                    appHelper.assetPath("libs/sweet-alert/js/sweetalert.min.js"),
                ]))
                .state("ui.expandable", appHelper.lazyLoad("ui.expandable"))
                .state("ui.button", appHelper.lazyLoad("ui.button"))
                .state("ui.callout", appHelper.lazyLoad("ui.callout"))

                /*********************************/
                /*************  基本配置  *************/
                /*********************************/
                .state("api", {
                    abstract: true,
                    url: "/api",
                    templateUrl: appHelper.componentView('layouts', 'basic-layout'),
                    resolve: {
                        resources: function ($ocLazyLoad) {
                            return $ocLazyLoad.load([
                                //ASSETS.js('layouts', 'layouts'),
                                //ASSETS.core.core,
                            ]);
                        }
                    }
                })
            /*************  字典  *************/
                .state("api.api-list", appHelper.lazyLoad("api.api-list", [ASSETS.js('common','main')]))
                .state("api.api-add", appHelper.lazyLoad("api.api-add"))
                .state("api.api-edit", appHelper.lazyLoad("api.api-add"))

        }
    ]
);
