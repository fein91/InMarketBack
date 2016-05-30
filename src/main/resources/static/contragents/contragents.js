angular.module('inmarket.contragents', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/contragents', {
    templateUrl: 'contragents/contragents.html',
    controller: 'MyBuyersCtrl'
  });
}])


.controller('MyBuyersCtrl', ['$scope', 'invoicesService', 'NgTableParams', function($scope, invoicesService, NgTableParams) {
	console.log('MyBuyersCtrl inited');

	var self = this;
    self.counterpartyId = 11;

    self.init = function() {
        return invoicesService.getBySourceId(self.counterpartyId)
            .then(function successCallback(response){
                self.tableParams = new NgTableParams({}, {
                    //filterOptions: { filterFn: priceRangeFilter },
                    dataset: response.data
                });
                console.log('init invoices from db');
            }, function errorCallback(response) {
                console.log('got ' + response.status + ' error');
        });
    };

    self.init();
}])

.controller('MySuppliersCtrl', ['$scope', 'invoicesService', 'NgTableParams', function($scope, invoicesService, NgTableParams) {
        console.log('MySuppliersCtrl inited');

        var self = this;
        self.counterpartyId = 11;

        self.init = function() {
            return invoicesService.getByTargetId(self.counterpartyId)
                .then(function successCallback(response){
                    self.tableParams = new NgTableParams({}, {
                        //filterOptions: { filterFn: priceRangeFilter },
                        dataset: response.data
                    });
                    console.log('init invoices from db');
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        };

        self.init();
    }]);


