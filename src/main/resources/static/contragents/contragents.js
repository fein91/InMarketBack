angular.module('inmarket.contragents', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/contragents', {
    templateUrl: 'contragents/contragents.html'
  });
}])


.controller('MyBuyersCtrl', ['$scope', '$rootScope', 'invoicesService', 'NgTableParams', 'invoices', function($scope, $rootScope, invoicesService, NgTableParams, invoices) {
	console.log('MyBuyersCtrl inited');

	var self = this;
    self.counterpartyId = 11;
        
        self.buyerInvoices = invoices.buyerInvoices;
        
        $scope.currentInvoicesPage = '';

        $scope.checkAll = function() {
            var isChecked = $scope.checkboxes.checked;
            console.log('check all ' + isChecked);

            angular.forEach(self.buyerInvoices, function(item) {
                if (angular.isDefined(item.id)) {
                    $scope.checkboxes.invoices[item.id] = isChecked;
                }
            });
        };

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

            // firing an event downwards
            $rootScope.$broadcast('buyerProposalToChangeEvent', $scope.checkboxes.invoices);

        }, true);

    self.init = function() {
        if (invoices.buyerInvoices.length == 0 ) {
            invoicesService.getBySourceId(self.counterpartyId)
                .then(function successCallback(response){
                    $scope.tableParams = new NgTableParams({}, {
                        dataset: response.data
                    });
                    invoices.addAllBuyerInvoices(response.data);
                    $scope.currentInvoicesPage = self.buyerInvoices.slice(($scope.tableParams.page() - 1) * $scope.tableParams.count(), $scope.tableParams.page() * $scope.tableParams.count());


                    angular.forEach(response.data, function(item) {
                        $scope.checkboxes.invoices[item.id] = true;
                    });

                    console.log('init invoices from db');
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        } else {
            $scope.tableParams = new NgTableParams({}, {
                dataset: invoices.buyerInvoices
            });
            $scope.currentInvoicesPage = self.buyerInvoices.slice(($scope.tableParams.page() - 1) * $scope.tableParams.count(), $scope.tableParams.page() * $scope.tableParams.count());
        }
        $scope.checkboxes = invoices.buyerInvoicesCheckboxes;
    };

    self.init();
}])

.controller('MySuppliersCtrl', ['$scope', 'invoicesService', 'NgTableParams', 'invoices', function($scope, invoicesService, NgTableParams, invoices) {
        console.log('MySuppliersCtrl inited');

        var self = this;
        self.counterpartyId = 11;
        self.supplierInvoices = invoices.supplierInvoices;

        $scope.currentInvoicesPage = '';

        $scope.checkAll = function() {
            var isChecked = $scope.checkboxes.checked;
            console.log('check all ' + isChecked);

                angular.forEach(self.supplierInvoices, function(item) {
                    if (angular.isDefined(item.id)) {
                        $scope.checkboxes.invoices[item.id] = isChecked;
                    }
                });
        };

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
            $scope.checkboxes = invoices.supplierInvoicesCheckboxes;

        };

        self.init();
    }]);


