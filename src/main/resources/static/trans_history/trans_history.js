angular.module('inmarket.trans_history', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/trans_history', {
            templateUrl: 'trans_history/trans_history.html',
            controller: 'TransHistoryCtrl'
        });
    }])


    .controller('TransHistoryCtrl', ['$scope', 'transHistoryService', 'NgTableParams', function ($scope, transHistoryService, NgTableParams) {
        console.log('TransHistoryCtrl inited');

        var self = this;
        self.counterpartyId = 11;

        self.init = function () {
            transHistoryService.getTransactionHistory(self.counterpartyId)
                .then(function successCallback(response) {
                    var bids = [];
                    var asks = [];

                    angular.forEach(response.data, function(historyMarketOrder) {
                        if ('ASK' === historyMarketOrder.orderSide) {
                            asks.push(historyMarketOrder);
                        } else if ('BID' === historyMarketOrder.orderSide) {
                            bids.push(historyMarketOrder);
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
                    console.log('init trans history from db: ' + JSON.stringify(response.data));
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        };

        self.init();
    }]);
