angular.module('inmarket.contragents', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/contragents', {
            templateUrl: 'partials/contragents.html',
            access: {
                loginRequired: true
            }
        });
    }])


    .controller('MyBuyersCtrl', ['$scope', '$rootScope', 'invoicesService', 'NgTableParams', 'invoices', 'session', function ($scope, $rootScope, invoicesService, NgTableParams, invoices, session) {
        console.log('MyBuyersCtrl inited');

        var self = this;

        self.buyerInvoices = invoices.buyerInvoices;

        $scope.currentInvoicesPage = '';

        $scope.checkAll = function () {
            var isChecked = $scope.checkboxes.checked;
            console.log('check all ' + isChecked);

            angular.forEach(self.buyerInvoices, function (item) {
                if (angular.isDefined(item.id)) {
                    $scope.checkboxes.invoices[item.id] = isChecked;
                }
            });
        };

        // watch for data checkboxes
        $scope.$watch('checkboxes.invoices', function (values) {
            console.log(JSON.stringify(values) + 'checkboxes.invoices');

            var checked = 0, unchecked = 0,
                total = $scope.currentInvoicesPage.length;
            angular.forEach($scope.currentInvoicesPage, function (item) {
                checked += ($scope.checkboxes.invoices[item.id]) || 0;
                unchecked += (!$scope.checkboxes.invoices[item.id]) || 0;
            });
            if ((unchecked == 0) || (checked == 0)) {
                $scope.checkboxes.checked = (checked == total);
            }

            // firing an event downwards
            $rootScope.$broadcast('buyerProposalToChangeEvent', $scope.checkboxes.invoices);

        }, true);

        self.init = function () {
            if (invoices.buyerInvoices.length == 0) {
                invoicesService.getBySourceId(session.counterpartyId)
                    .then(function successCallback(response) {
                        $scope.buyersTableParams = new NgTableParams({}, {
                            dataset: response.data
                        });
                        invoices.addAllBuyerInvoices(response.data);
                        $scope.currentInvoicesPage = self.buyerInvoices.slice(($scope.buyersTableParams.page() - 1) * $scope.buyersTableParams.count(), $scope.buyersTableParams.page() * $scope.buyersTableParams.count());


                        angular.forEach(response.data, function (item) {
                            $scope.checkboxes.invoices[item.id] = true;
                        });

                        console.log('init invoices from db');
                    }, function errorCallback(response) {
                        console.log('got ' + response.status + ' error');
                    });
            } else {
                $scope.buyersTableParams = new NgTableParams({}, {
                    dataset: invoices.buyerInvoices
                });
                $scope.currentInvoicesPage = self.buyerInvoices.slice(($scope.buyersTableParams.page() - 1) * $scope.buyersTableParams.count(), $scope.buyersTableParams.page() * $scope.buyersTableParams.count());
            }
            $scope.checkboxes = invoices.buyerInvoicesCheckboxes;
        };

        self.init();
    }])

    .controller('MySuppliersCtrl', ['$scope', '$rootScope', 'invoicesService', 'NgTableParams', 'invoices', 'session', function ($scope, $rootScope, invoicesService, NgTableParams, invoices, session) {
        console.log('MySuppliersCtrl inited');

        var self = this;
        self.supplierInvoices = invoices.supplierInvoices;

        $scope.currentInvoicesPage = '';

        $scope.checkAll = function () {
            var isChecked = $scope.checkboxes.checked;
            console.log('check all ' + isChecked);

            angular.forEach(self.supplierInvoices, function (item) {
                if (angular.isDefined(item.id)) {
                    $scope.checkboxes.invoices[item.id] = isChecked;
                }
            });
        };

        // watch for data checkboxes
        $scope.$watch('checkboxes.invoices', function (values) {
            console.log(JSON.stringify(values) + 'checkboxes.invoices');

            var checked = 0, unchecked = 0,
                total = $scope.currentInvoicesPage.length;
            angular.forEach($scope.currentInvoicesPage, function (item) {
                checked += ($scope.checkboxes.invoices[item.id]) || 0;
                unchecked += (!$scope.checkboxes.invoices[item.id]) || 0;
            });
            if ((unchecked == 0) || (checked == 0)) {
                $scope.checkboxes.checked = (checked == total);
            }

            // firing an event downwards
            $rootScope.$broadcast('supplierProposalToChangeEvent', $scope.checkboxes.invoices);
        }, true);

        self.init = function () {
            if (invoices.supplierInvoices.length == 0) {
                invoicesService.getByTargetId(session.counterpartyId)
                    .then(function successCallback(response) {
                        $scope.suppliersTableParams = new NgTableParams({}, {
                            dataset: response.data
                        });

                        invoices.addAllSupplierInvoices(response.data);
                        $scope.currentInvoicesPage = self.supplierInvoices.slice(($scope.suppliersTableParams.page() - 1) * $scope.suppliersTableParams.count(), $scope.suppliersTableParams.page() * $scope.suppliersTableParams.count());


                        angular.forEach(response.data, function (item) {
                            $scope.checkboxes.invoices[item.id] = true;
                        });

                        console.log('init invoices from db');
                    }, function errorCallback(response) {
                        console.log('got ' + response.status + ' error');
                    });
            } else {
                $scope.suppliersTableParams = new NgTableParams({}, {
                    dataset: invoices.supplierInvoices
                });

                $scope.currentInvoicesPage = self.supplierInvoices.slice(($scope.suppliersTableParams.page() - 1) * $scope.suppliersTableParams.count(), $scope.suppliersTableParams.page() * $scope.suppliersTableParams.count());
            }
            $scope.checkboxes = invoices.supplierInvoicesCheckboxes;

        };

        self.init();
    }]);


