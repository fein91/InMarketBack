angular.module('inmarket.limit_orders', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/limit_orders', {
            templateUrl: 'partials/limit_orders.html',
            controller: 'LimitOrdersCtrl',
            access: {
                loginRequired: true
            }
        });
    }])

    .filter('lbrRemove', function() {
        return function (msg) {
            return _.replace(msg, new RegExp("<br>","g"),"");
        };
    })

    .directive('limitOrdersTable', function() {
        return {
            restrict: 'E',
            scope: {
                tableParams: '='
            },
            templateUrl: 'partials/limit_orders_table.html',
            controller: function($scope, $uibModal, invoices, orderRequestsService) {
                $scope.save = function(record, recordForm) {
                    record.invoicesChecked = record.side == 'ASK'
                        ? invoices.supplierInvoicesCheckboxes.invoices
                        : invoices.buyerInvoicesCheckboxes.invoices;

                    orderRequestsService.calculate(record)
                        .then(function successCallback(response) {
                            var modalInstance = $uibModal.open({
                                animation: true,
                                templateUrl: 'partials/limitOrderEditConfirmPopup.html',
                                controller: 'LimitOrderEditConfirmPopupCtrl',
                                size: 'sm',
                                resolve: {
                                    popupData: function () {
                                        return {
                                            "record" : record,
                                            "recordForm" : recordForm
                                        };
                                    }
                                }
                            });
                        }, function errorCallback(response) {
                            recordForm.quantity.$invalid = true;
                            recordForm.$invalid = true;
                            recordForm.quantity.message = response.data.message;

                            console.log('got ' + response.status + ' error, msg=' + response.data.message);
                        }

                    );
                };

                $scope.cancel = function(record, recordForm) {
                    orderRequestsService.getById(record.id)
                        .then(function successCallback(response) {
                            self.resetRow(record, recordForm);
                            var originalRecord = response.data;
                            angular.extend(record, originalRecord);
                        }, function errorCallback(response) {
                            console.log('got ' + response.status + ' error');
                        });
                };

                $scope.openDeleteConfirmPopup = function(record) {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'partials/limitOrderDeleteConfirmPopup.html',
                        controller: 'LimitOrderDeleteConfirmPopupCtrl',
                        size: 'sm',
                        resolve: {
                            popupData: function () {
                                return {
                                    "record" : record,
                                    "tableParams" : $scope.tableParams
                                };
                            }
                        }
                    });
                };

                self.resetRow = function(record, recordForm){
                    record.isEditing = false;
                    recordForm.$setPristine();
                };
            }
        };
    })

    .controller('LimitOrdersCtrl', ['$scope', 'orderRequestsService', 'NgTableParams', 'session', function ($scope, orderRequestsService, NgTableParams, session) {
        console.log('LimitOrdersCtrl inited');

        var self = this;

        self.init = function () {
            orderRequestsService.getOrderRequests(session.counterpartyId)
                .then(function successCallback(response) {
                    var bids = [];
                    var asks = [];

                    angular.forEach(response.data, function (limitOrder) {
                        if ('ASK' === limitOrder.side) {
                            asks.push(limitOrder);
                        } else if ('BID' === limitOrder.side) {
                            bids.push(limitOrder);
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

                    console.log('loaded order requests from db: ' + JSON.stringify(response.data));
                }, function errorCallback(response) {
                    console.log('got ' + response.status + ' error');
                });
        };

        self.init();
    }]);
