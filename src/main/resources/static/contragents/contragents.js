angular.module('inmarket.contragents', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/contragents', {
    templateUrl: 'contragents/contragents.html',
  });
}])


.controller('MyBuyersCtrl', ['$scope', 'invoicesService', 'NgTableParams', 'invoices', function($scope, invoicesService, NgTableParams, invoices) {
	console.log('MyBuyersCtrl inited');

	var self = this;
    self.counterpartyId = 11;
        
        self.buyerInvoices = invoices.buyerInvoices;
        
        $scope.checkboxes = { 'checked': false, invoices: {} };
        $scope.currentInvoicesPage = '';

        // watch for check all checkbox
        $scope.$watch('checkboxes.checked', function(value) {
            console.log(JSON.stringify(value) + 'checkboxes.checked');

            angular.forEach(self.buyerInvoices, function(item) {
                if (angular.isDefined(item.id)) {
                    $scope.checkboxes.invoices[item.id] = value;
                }
            });
        });

        // watch for data checkboxes
        $scope.$watch('checkboxes.invoices', function(values) {
            console.log(JSON.stringify(values) + 'checkboxes.invoices');

            var checked = 0, unchecked = 0,
                total = $scope.currentInvoicesPage.length;
            angular.forEach($scope.currentInvoicesPage, function(item) {
                checked   +=  ($scope.checkboxes.invoices[item.id]) || 0;
                unchecked += (!$scope.checkboxes.invoices[item.id]) || 0;
            });
            if ((unchecked == 0) || (checked == 0)) {
                $scope.checkboxes.checked = (checked == total);
            }
        }, true);

    self.init = function() {
        if (invoices.buyerInvoices.length == 0 ) {
            invoicesService.getBySourceId(self.counterpartyId)
                .then(function successCallback(response){
                    $scope.tableParams = new NgTableParams({
                        page: 1,            // show first page
                        count: 10           // count per page
                    }, {
                        total: response.data.length, // length of data
                        getData: function($defer, params) {
                            // use build-in angular filter
                            invoices.addAllBuyerInvoices(response.data);

                            params.total(self.buyerInvoices.length); // set total for recalc pagination
                            $defer.resolve($scope.currentInvoicesPage = self.buyerInvoices.slice((params.page() - 1) * params.count(), params.page() * params.count()));
                        }
                    });

                    console.log('init invoices from db');
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        } else {
            $scope.tableParams = new NgTableParams({
                page: 1,            // show first page
                count: 10           // count per page
            }, {
                total: self.buyerInvoices.length, // length of data
                getData: function($defer, params) {
                    // use build-in angular filter
                    params.total(self.buyerInvoices.length); // set total for recalc pagination
                    $defer.resolve($scope.currentInvoicesPage = self.buyerInvoices.slice((params.page() - 1) * params.count(), params.page() * params.count()));
                }
            });
        }
    };

    self.init();
}])

.controller('MySuppliersCtrl', ['$scope', 'invoicesService', 'NgTableParams', 'invoices', function($scope, invoicesService, NgTableParams, invoices) {
        console.log('MySuppliersCtrl inited');

        var self = this;
        self.counterpartyId = 11;
        self.supplierInvoices = invoices.supplierInvoices;

        $scope.checkboxes = { 'checked': false, invoices: {} };
        $scope.currentInvoicesPage = '';

        // watch for check all checkbox
        $scope.$watch('checkboxes.checked', function(value) {
            console.log(JSON.stringify(value) + 'checkboxes.checked');

            angular.forEach(self.supplierInvoices, function(item) {
                if (angular.isDefined(item.id)) {
                    $scope.checkboxes.invoices[item.id] = value;
                }
            });
        });

        // watch for data checkboxes
        $scope.$watch('checkboxes.invoices', function(values) {
            console.log(JSON.stringify(values) + 'checkboxes.invoices');

            var checked = 0, unchecked = 0,
                total = $scope.currentInvoicesPage.length;
            angular.forEach($scope.currentInvoicesPage, function(item) {
                checked   +=  ($scope.checkboxes.invoices[item.id]) || 0;
                unchecked += (!$scope.checkboxes.invoices[item.id]) || 0;
            });
            if ((unchecked == 0) || (checked == 0)) {
                $scope.checkboxes.checked = (checked == total);
            }
        }, true);

        self.init = function() {
            if (invoices.supplierInvoices.length == 0) {
                invoicesService.getByTargetId(self.counterpartyId)
                    .then(function successCallback(response){
                        $scope.tableParams = new NgTableParams({
                            page: 1,            // show first page
                            count: 10           // count per page
                        }, {
                            total: response.data.length, // length of data
                            getData: function($defer, params) {
                                // use build-in angular filter
                                invoices.addAllSupplierInvoices(response.data);

                                params.total(self.supplierInvoices.length); // set total for recalc pagination
                                $defer.resolve($scope.currentInvoicesPage = self.supplierInvoices.slice((params.page() - 1) * params.count(), params.page() * params.count()));
                            }
                        });

                        console.log('init invoices from db');
                    }, function errorCallback(response) {
                        console.log('got ' + response.status + ' error');
                    });
            } else {
                $scope.tableParams = new NgTableParams({
                    page: 1,            // show first page
                    count: 10           // count per page
                }, {
                    total: self.supplierInvoices.length, // length of data
                    getData: function($defer, params) {
                        // use build-in angular filter

                        params.total(self.supplierInvoices.length); // set total for recalc pagination
                        $defer.resolve($scope.currentInvoicesPage = self.supplierInvoices.slice((params.page() - 1) * params.count(), params.page() * params.count()));
                    }
                });
            }
        };

        self.init();
    }]);


