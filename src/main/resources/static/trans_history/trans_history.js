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
                    $scope.tableParams = new NgTableParams({}, {
                        //filterOptions: { filterFn: priceRangeFilter },
                        dataset: response.data
                    });
                    console.log('init trans history from db: ' + JSON.stringify(response.data));
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        };

        self.init();
    }]);
