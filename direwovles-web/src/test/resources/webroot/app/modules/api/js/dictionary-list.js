function dictionariesCtrl($scope, DictService, $ocLazyLoad) {
    //$ocLazyLoad.load('app/app.service.js');

    $scope.$emit("update-title", {
        pageTitle: "API列表",
        links: [
            {
                url: "common.api-list",
                name: "API列表"
            }
        ]
    });
    $scope.pagination = DictService.page();
    $scope.$on("page", function(event, data) {
        $scope.pagination = DictService.page({page : data});
    });

    $scope.remove = function(id) {
        DictService.delete({id : id}, function() {
            $scope.pagination = DictService.page();
        });
    };
}
dictionariesCtrl.$inject = [ '$scope', 'DictService', '$ocLazyLoad'];
