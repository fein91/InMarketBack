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

        self.init();
    }]);
