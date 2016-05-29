angular.module('inmarket.contragents', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/contragents', {
    templateUrl: 'contragents/contragents.html',
    controller: 'ContragentsCtrl'
  });
}])


.controller('ContragentsCtrl', ['$scope', 'invoicesService', 'NgTableParams', function($scope, invoicesService, NgTableParams) {
	console.log('ContragentsCtrl inited');

	var self = this;
    self.counterpartyId = 1;

    self.init = function() {
        return invoicesService.getBySourceId(self.counterpartyId)
            .then(function successCallback(response){
                self.tableParams = new NgTableParams({}, {
                    //filterOptions: { filterFn: priceRangeFilter },
                    dataset: response.data.invoices
                });
                console.log('init invoices from db');
            }, function errorCallback(response) {
                console.log('got ' + response.status + ' error');
        });
    };

    self.init();
}]);


