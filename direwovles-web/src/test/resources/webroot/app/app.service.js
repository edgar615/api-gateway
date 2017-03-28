var myService = angular.module('MyApp.services', ['ngResource', 'ngStorage']);


myService.factory('ShareService', function () {
    return {
    };
})
    /*********************************/
    /*************  基本配置  *************/
    /*********************************/

    /*************  API列表  *************/
    .factory(
    'APIService',
    function ($resource) {
        return $resource("http://localhost:8080/admin-api/common/dict/:id", {}, {
            page : {method : 'get',params : {id : "page"}},
            update : {method : "put"},
            items : {url : 'common/dict/items/:id',method : "get", isArray: true}
        });
    });
