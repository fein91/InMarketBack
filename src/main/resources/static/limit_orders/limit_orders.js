angular.module('inmarket.limit_orders', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/limit_orders', {
            templateUrl: 'limit_orders/limit_orders.html',
            controller: 'LimitOrdersCtrl'
        });
    }])


    .controller('LimitOrdersCtrl', ['$scope', 'orderRequestsService', 'NgTableParams', function ($scope, orderRequestsService, NgTableParams) {
        console.log('LimitOrdersCtrl inited');

        var self = this;
        self.counterpartyId = 11;

        self.init = function () {
            orderRequestsService.getOrderRequests(self.counterpartyId)
                .then(function successCallback(response) {
                    var bids = [];
                    var asks = [];

                    angular.forEach(response.data, function(limitOrder) {
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

        self.init();
    }]);
