function dictionaryAddCtrl($scope, DictService, $state) {
    $scope.$emit("update-title", {
        pageTitle: "字典列表",
        links: [
            {
                url: "common.dictionary-list",
                name: "字典列表"
            },
            {
                url: "common.dictionary-add",
                name: "新增字典"
            }
        ]
    });

    $scope.save = function () {
        DictService.save($scope.dict, function () {
            //MessageService.saveSuccess();
            //LocationTo.path("/sys/dict/list");
            $state.transitionTo('common.dictionary-list')
        }, function () {
        });
    };
}
dictionaryAddCtrl.$inject = [ '$scope', 'DictService', '$state'];

