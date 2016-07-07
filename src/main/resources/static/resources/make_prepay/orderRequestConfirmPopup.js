angular.module('inmarket.orderRequestConfirmPopup', ['ui.bootstrap'])
    .controller('OrderRequestConfirmPopupCtrl', function ($scope, $uibModalInstance, orderRequestsService, orderRequest) {

        $scope.ok = function () {
            orderRequestsService.submitOrder(orderRequest);
            $uibModalInstance.close();
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });