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


    .controller('LimitOrdersCtrl', ['$scope', 'orderRequestsService', 'NgTableParams', 'session', function ($scope, orderRequestsService, NgTableParams, session) {
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

        $scope.saveAsk = function(record, recordForm) {
            orderRequestsService.updateOrder(record)
                .then(function successCallback(response) {
                    self.resetRow(record, recordForm);
                    console.log("successful update");
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error, msg=' + response.data.message);
                })
        };

        $scope.cancelAsk = function(record, recordForm) {
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
            orderRequestsService.removeOrder(record.id)
                .then(function successCallback(response) {
                    _.remove($scope.asksTableParams.settings().dataset, function(item) {
                        return record === item;
                    });

                    $scope.asksTableParams.reload().then(function(data) {
                        if (data.length === 0 && $scope.asksTableParams.total() > 0) {
                            $scope.asksTableParams.page($scope.asksTableParams.page() - 1);
                            $scope.asksTableParams.reload();
                        }
                    });

                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        };

        self.resetRow = function(record, recordForm){
            record.isEditing = false;
            recordForm.$setPristine();
        };

        self.init();
    }]);
