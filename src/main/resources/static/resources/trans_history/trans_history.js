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


    .controller('TransHistoryCtrl', ['$scope', 'transHistoryService', 'NgTableParams', function ($scope, transHistoryService, NgTableParams) {
        console.log('TransHistoryCtrl inited');

        var self = this;

        self.init = function () {
            return transHistoryService.getAll()
                .then(function successCallback(response) {
                    self.tableParams = new NgTableParams({}, {
                        //filterOptions: { filterFn: priceRangeFilter },
                        dataset: response.data.history
                    });
                    console.log('init trans history from db');
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        };

        self.init();
    }]);
