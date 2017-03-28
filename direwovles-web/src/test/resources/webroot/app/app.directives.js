var myDirective = angular.module('MyApp.directives', ['ngStorage']);


myDirective.directive('appMetisMenu', function () {
    return {
        restrict: 'AE',
        link: function postLink(scope, iElement, iAttrs) {
            $('#menu').metisMenu();
            $('#menu > li > a:first').addClass('active');
        }
    };
})
    .directive('appLeftMenu', function () {
        return {
            restrict: 'AE',
            link: function postLink(scope, iElement, iAttrs) {
                iElement.click(function() {
                    //$("#menu a").removeClass("active");
                    iElement.addClass("active");
                });
            }
        };
    })

    /*
     * app-form-toggle指令,实现查询表单的显示和隐藏的切换
     * 参数:minus 是否隐藏,如果设置了样式form-minus,则查询表单默认为隐藏状态
     * 用法:默认隐藏表单:<form role="form" class="form-horizontal form-minus" app-form-toggle>
     *      默认显示表单:<form role="form" class="form-horizontal" app-form-toggle>
     * */
    .directive('appFormToggle', function () {
        return {
            restrict: 'AE',
            scope: {
                'minus': '@'
            },
            link: function postLink(scope, iElement, iAttrs) {
                var formMinus = "form-minus";
                if (iElement.hasClass(formMinus)) {
                    iElement.find('.title').prepend('<i class="fa fa-angle-right"></i><label class="blank5">&nbsp;</label>');
                } else {
                    iElement.find('.title').prepend('<i class="fa fa-angle-down"></i><label class="blank5">&nbsp;</label>');
                }

                iElement.find('.title').click(function () {
                    if (iElement.hasClass(formMinus)) {
                        iElement.find('.title').find('i').removeClass("fa-angle-right").addClass("fa-angle-down");
                        iElement.removeClass("form-minus");
                    } else {
                        iElement.find('.title').find('i').removeClass("fa-angle-down").addClass("fa-angle-right");
                        iElement.addClass("form-minus");
                    }
                });

            }
        };
    })
    /*
     * app-page-title指令,将元素自动添加到 #page-header 元素中
     * 用法: <div class="page-title" app-page-title>
     * */
    .directive('appPageTitle', function () {
        return {
            restrict: 'AE',
            link: function postLink(scope, iElement, iAttrs) {
                console.log($('#page-header').size());
                $('#page-header .page-title').remove();
                iElement.prependTo('#page-header');
            }
        };
    })
/**    ed-simple-page 简单分页指令,值显示上一页,下一页
 * 用法
 * <div ed-simple-page page-size="10"
 pagination="pagination" query-param="queryParam" query-url="sys/i18n/pagination"
 share-key="RoleList" block-el=".panel-body">
 参数 : page-size:每页数量,默认值10;
 pagination:数据源
 query-param:查询条件
 query-url:分页查询的Rest API
 share-key:页面缓存的key,默认值null,不使用这个属性则代表不启用缓存
 block-el:blockUI的DOM元素,默认值null,不使用这个属性则代表不启用BlockUI
 */
.directive('appSimplePage', function ($http, $location) {
    return {
        restrict: 'A',
        scope: {
            totalPages: '=',
            page: '=',
            eventName : '@'
        },
        templateUrl: 'app/shared/simple_page.html',
        link: function postLink(scope, iElement, iAttrs) {

            scope.p = "";
            scope.eventName = scope.eventName || "page";
            iElement.find(".page-input").keypress(function(event) {
                return (event.charCode >=48 && event.charCode <= 57) || event.keyCode == 8;

            });
//            scope.pageSize = scope.pageSize || 10;
//            scope.$on("query", function () {
//                scope.gotoPage(1);
//            });
//            scope.$on("reload", function () {
//                scope.gotoPage(scope.queryParam.page);
//            });
            scope.enterPageValue = function ($event) {
                if ($event.keyCode == "13") {
                    var _p = new Number(scope.p).valueOf();
                    if (_p == NaN) {
                        return false;
                    }
                    scope.$emit(scope.eventName, _p);
                }
            };
            scope.gotoPage = function (page) {
                if (typeof page != 'number') {
                    return;
                }
                scope.$emit(scope.eventName, page);
            };
//            scope.query = function (queryParam) {
//                angular.extend(queryParam, {
////                    timestamp: new Date().getTime()
//                });
//                if (scope.blockEl) {
//                    App.blockUI(scope.blockEl);
//                }
//                $http({
//                    method: 'GET',
//                    url: scope.url,
//                    params: queryParam
//                }).success(function (data, status, headers, config) {
//                    scope.pagination = data;
//                    if (scope.blockEl) {
//                        App.unblockUI(scope.blockEl);
//                    }
//                    scope.p = "";
//                }).error(function (data, status, headers, config) {
//                    if (scope.blockEl) {
//                        App.unblockUI(scope.blockEl);
//                    }
//                });
//            };
//            if (scope.shareKey && $.localStorage.get(scope.shareKey)) {
//                angular.extend(scope.queryParam, $.localStorage.get(scope.shareKey));
//                scope.query(scope.queryParam);
//            } else {
//                scope.gotoPage(1);
//            }
        }
    };
})
;
