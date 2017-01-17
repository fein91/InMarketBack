angular.module('inmarket.contragents', ['ngRoute', 'cgBusy'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/contragents', {
            templateUrl: 'partials/contragents.html',
            access: {
                loginRequired: true
            }
        });
    }])

    .directive('contragentsTable', function() {
        return {
            restrict: 'E',
            scope: {
                tableParams: '=',
                checkboxes: '=',
                invoices: '=',
                currentInvoicesPage: '=',
                showImport: '@',
                event: '@'
            },
            templateUrl: 'partials/contragents_table.html',
            controller: function($scope, $rootScope, $route, $uibModal, invoicesService, invoices, session) {

                    //$scope.invoiceCounterpartyName = $scope.showImport ? 'Покупатель / # Инвойса' : 'Поставщик / # Инвойса';

                    $scope.import = function(files) {
                        $scope.importPromise = invoicesService.import(session.counterpartyId, files);
                        $scope.importPromise.then(function successCallback(response) {
                            invoices.cleanUp();
                            $route.reload();
                        }, function errorCallback(response) {
                            console.log('got ' + response.status + ' error, msg=' + response.data.message);

                            var modalInstance = $uibModal.open({
                                animation: true,
                                templateUrl: 'partials/error_popup.html',
                                controller: function($scope, $uibModalInstance, errorMsg) {
                                        $scope.errorMsg = errorMsg;

                                        $scope.close = function () {
                                            $uibModalInstance.close();
                                        }
                                    },
                                size: 'sm',
                                resolve: {
                                    errorMsg: function () {
                                        return response.data.message;
                                    }
                                }
                            });
                        });
                    };

                    $scope.saveInvoices = function() {
                        invoicesService.updateCheckedInvoices(session.counterpartyId, $scope.checkboxes.invoices)
                            .then(function successCallback(response) {
                                console.log('successfully updated');
                            }, function errorCallback(response) {
                                console.log('got ' + response.status + ' error');
                            });
                    };

                    $scope.checkAll = function () {
                        var isChecked = $scope.checkboxes.checked;
                        console.log('check all ' + isChecked);

                        angular.forEach($scope.invoices, function (item) {
                            if (angular.isDefined(item.id)) {
                                $scope.checkboxes.invoices[item.id] = isChecked;
                            }
                        });
                    };

                    $scope.calculateAvgDaysToPayment = function(invoices) {
                        return invoicesService.calculateAvgDaysToPayment(invoices);
                    };

                    // watch for data checkboxes
                    $scope.$watch('checkboxes.invoices', function (values) {
                        console.log(JSON.stringify(values) + 'checkboxes.invoices');

                        //TODO fast hack please fix it
                        var currentInvoicesPage = $scope.currentInvoicesPage ? $scope.currentInvoicesPage : [];
                        var checked = 0, unchecked = 0,
                            total = currentInvoicesPage.length;
                        angular.forEach(currentInvoicesPage, function (item) {
                            checked += ($scope.checkboxes.invoices[item.id]) || 0;
                            unchecked += (!$scope.checkboxes.invoices[item.id]) || 0;
                        });
                        if ((unchecked == 0) || (checked == 0)) {
                            $scope.checkboxes.checked = (checked == total);
                        }

                        // firing an event downwards
                        $rootScope.$broadcast($scope.event, $scope.checkboxes.invoices);

                    }, true);
            }
        }
    })

    .controller('ContragentsCtrl', ['$scope', '$rootScope', 'invoicesService', 'NgTableParams', 'invoices', 'session', function ($scope, $rootScope, invoicesService, NgTableParams, invoices, session) {
        console.log('ContragentsCtrl inited');

        var self = this;
        $scope.buyerInvoices = invoices.buyerInvoices;
        $scope.supplierInvoices = invoices.supplierInvoices;

        self.init = function () {
            invoices.cleanUp();

            var counterpartyId = session.counterpartyId;
            invoicesService.getAll(counterpartyId)
                .then(function successCallback(response) {
                    var buyerInvoices = [];
                    var supplierInvoices = [];
                    angular.forEach(response.data, function (invoice) {
                            invoice.unpaidValue = invoice.value - invoice.prepaidValue;
                            //TODO ugly hack
                            invoice.prepaidValue = invoice.prepaidValue - invoice.discountValue;

                            invoice.daysToPayment = invoicesService.dateDiffInDays(new Date(), new Date(invoice.paymentDate));

                            if (counterpartyId == invoice.source.id) {
                                $scope.buyerCheckboxes.invoices[invoice.id] = invoice.sourceChecked;
                                buyerInvoices.push(invoice);
                            } else if (counterpartyId == invoice.target.id) {
                                $scope.supplierCheckboxes.invoices[invoice.id] = invoice.targetChecked;
                                supplierInvoices.push(invoice);
                            }}
                        );


                    $scope.buyersTableParams = new NgTableParams({
                        group : "target.name"
                    }, {
                        dataset: buyerInvoices
                    });
                    $scope.suppliersTableParams = new NgTableParams({
                        group: 'source.name'
                    }, {
                        dataset: supplierInvoices
                    });

                    invoices.addAllBuyerInvoices(buyerInvoices);
                    $scope.currentBuyerInvoicesPage = $scope.buyerInvoices.slice(($scope.buyersTableParams.page() - 1) * $scope.buyersTableParams.count(), $scope.buyersTableParams.page() * $scope.buyersTableParams.count());

                    invoices.addAllSupplierInvoices(supplierInvoices);
                    $scope.currentSupplierInvoicesPage = $scope.supplierInvoices.slice(($scope.suppliersTableParams.page() - 1) * $scope.suppliersTableParams.count(), $scope.suppliersTableParams.page() * $scope.suppliersTableParams.count());

                    console.log('init invoices from db');
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });

            $scope.buyerCheckboxes = invoices.buyerInvoicesCheckboxes;
            $scope.supplierCheckboxes = invoices.supplierInvoicesCheckboxes;
        };

        self.init();
    }]);