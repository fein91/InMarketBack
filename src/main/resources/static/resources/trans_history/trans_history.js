angular.module('inmarket.trans_history', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/trans_history', {
            templateUrl: 'partials/trans_history.html',
            controller: 'TransHistoryCtrl',
            access: {
                loginRequired: true
            }
        });
    }])


    .controller('TransHistoryCtrl', ['$scope', 'transHistoryService', 'NgTableParams', 'session', function ($scope, transHistoryService, NgTableParams, session) {
        console.log('TransHistoryCtrl inited');

        var self = this;

        self.init = function () {
            transHistoryService.getTransactionHistory(session.counterpartyId)
                .then(function successCallback(response) {
                    var bids = [];
                    var asks = [];

                    angular.forEach(response.data, function (historyMarketOrder) {
                        angular.forEach(historyMarketOrder.historyTrades, function (trade) {
                            trade.toPay = trade.unpaidInvoiceValue - trade.quantity - trade.discountValue;
                        });

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
