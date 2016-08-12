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


    .controller('TransHistoryCtrl', ['$scope', 'transHistoryService', 'NgTableParams', 'session', 'invoicesService', function ($scope, transHistoryService, NgTableParams, session, invoicesService) {
        console.log('TransHistoryCtrl inited');

        var self = this;

        $scope.exportInvoicesBySource = function() {
            self.showDownloadExportedFileWindow(invoicesService.exportBySource(session.counterpartyId));
        };

        $scope.exportInvoicesByTarget = function() {
            self.showDownloadExportedFileWindow(invoicesService.exportByTarget(session.counterpartyId));
        };

        self.showDownloadExportedFileWindow = function (exportPromise) {
            exportPromise
                .then(function successCallback(response) {
                    var anchor = angular.element('<a/>');
                    angular.element(document.body).append(anchor);
                    anchor.attr({
                        href: 'data:attachment/csv;charset=utf-8,' + encodeURI(response.data),
                        target: '_blank',
                        download: 'export.csv'
                    })[0].click();
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        };

        self.init = function () {
            transHistoryService.getTransactionHistory(session.counterpartyId)
                .then(function successCallback(response) {
                    var bids = [];
                    var asks = [];

                    angular.forEach(response.data, function (historyMarketOrder) {
                        angular.forEach(historyMarketOrder.historyTrades, function (trade) {
                            trade.toPay = trade.unpaidInvoiceValue - trade.quantity - trade.discountValue;
                            trade.avgDiscountPerc = trade.discountValue / trade.invoice.value * 100;
                            trade.periodReturnMultQty = trade.periodReturn * trade.quantity;
                        });

                        historyMarketOrder.periodReturn = historyMarketOrder.historyTrades.sum('periodReturnMultQty') / historyMarketOrder.historyTrades.sum('quantity');

                        if ('ASK' === historyMarketOrder.side) {
                            asks.push(historyMarketOrder);
                        } else if ('BID' === historyMarketOrder.side) {
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
