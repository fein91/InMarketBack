angular.module('inmarket.limit_orders', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/limit_orders', {
            templateUrl: 'partials/limit_orders.html',
            controller: 'LimitOrdersCtrl',
            access: {
                loginRequired: true
            }
        });
    }])


    .controller('LimitOrdersCtrl', ['$scope', '$uibModal', 'orderRequestsService', 'NgTableParams', 'session', function ($scope, $uibModal, orderRequestsService, NgTableParams, session) {
        console.log('LimitOrdersCtrl inited');

        var self = this;

        self.init = function () {
            orderRequestsService.getOrderRequests(session.counterpartyId)
                .then(function successCallback(response) {
                    var bids = [];
                    var asks = [];

                    angular.forEach(response.data, function (limitOrder) {
                        if ('ASK' === limitOrder.orderSide) {
                            asks.push(limitOrder);
                        } else if ('BID' === limitOrder.orderSide) {
                            bids.push(limitOrder);
                        }
                    });

                    $scope.bidsTableParams = new NgTableParams({}, {
                        //filterOptions: { filterFn: priceRangeFilter },
                        dataset: bids
                    });
                    $scope.asksTableParams = new NgTableParams({}, {
                        //filterOptions: { filterFn: priceRangeFilter },
                        dataset: asks
                    });

                    console.log('loaded order requests from db: ' + JSON.stringify(response.data));
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        };

        $scope.save = function(record, recordForm) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/limitOrderEditConfirmPopup.html',
                controller: 'LimitOrderEditConfirmPopupCtrl',
                size: 'sm',
                resolve: {
                    popupData: function () {
                        return {
                            "record" : record,
                            "recordForm" : recordForm
                        };
                    }
                }
            });
        };

        $scope.cancel = function(record, recordForm) {
            orderRequestsService.getById(record.id)
                .then(function successCallback(response) {
                    self.resetRow(record, recordForm);
                    var originalRecord = response.data;
                    angular.extend(record, originalRecord);
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        };

        $scope.delAsk = function(record) {
            self.openDeleteConfirmPopup(record, $scope.asksTableParams);
        };

        $scope.delBid = function(record) {
            self.openDeleteConfirmPopup(record, $scope.bidsTableParams);
        };

        self.openDeleteConfirmPopup = function(record, tableParams) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/limitOrderDeleteConfirmPopup.html',
                controller: 'LimitOrderDeleteConfirmPopupCtrl',
                size: 'sm',
                resolve: {
                    popupData: function () {
                        return {
                            "record" : record,
                            "tableParams" : tableParams
                        };
                    }
                }
            });
        };

        self.resetRow = function(record, recordForm){
            record.isEditing = false;
            recordForm.$setPristine();
        };

        self.init();
    }]);
