angular.module('inmarket.limitOrderDeleteConfirmPopupCtrl', ['ui.bootstrap'])
    .controller('LimitOrderDeleteConfirmPopupCtrl', function ($scope, $uibModalInstance, orderRequestsService, popupData) {

        $scope.ok = function () {
            var record = popupData.record;
            var tableParams = popupData.tableParams;

            orderRequestsService.removeOrder(record.id)
                .then(function successCallback(response) {
                    _.remove(tableParams.settings().dataset, function(item) {
                        return record === item;
                    });

                    tableParams.reload().then(function(data) {
                        if (data.length === 0 && tableParams.total() > 0) {
                            tableParams.page(tableParams.page() - 1);
                            tableParams.reload();
                        }
                    });

                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });

            $uibModalInstance.close();
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });