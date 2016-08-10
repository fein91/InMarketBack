angular.module('inmarket.limitOrderEditConfirmPopupCtrl', ['ui.bootstrap'])
    .controller('LimitOrderEditConfirmPopupCtrl', function ($scope, $uibModalInstance, orderRequestsService, popupData) {

        $scope.ok = function () {
            var recordForm = popupData.recordForm;
            var record = popupData.record;

            recordForm.quantity.$invalid = false;
            //TODO maybe we should just call update here
            orderRequestsService.updateOrder(record)
                .then(function successCallback(response) {
                    record.isEditing = false;
                    recordForm.$setPristine();

                    console.log("successful update");
                }, function errorCallback(response) {
                    recordForm.quantity.$invalid = true;
                    recordForm.$invalid = true;
                    recordForm.quantity.message = response.data.message;

                    console.log('got ' + response.status + ' error, msg=' + response.data.message);
                });

            $uibModalInstance.close();
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });