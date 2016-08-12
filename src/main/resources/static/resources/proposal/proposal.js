angular.module('inmarket.proposal', ['ngRoute'])


    .controller('ProposalCtrl', ['$scope', '$rootScope', 'counterpartyService', 'orderRequestsService', 'invoices', 'session',
        function ($scope, $rootScope, counterpartyService, orderRequestsService, invoices, session) {
            console.log('ProposalCtrl inited');

            self = this;
            $scope.buyer_invoices_sum = 0;
            $scope.supplier_invoices_sum = 0;
            $scope.asks_sum = 0;
            $scope.bids_sum = 0;

            // listen for the event in the relevant $scope
            $rootScope.$on('buyerProposalToChangeEvent', function (event, buyerInvoicesCheckboxes) {
                console.log('buyerProposalToChangeEvent catched');

                recalculateBuyerProposal(buyerInvoicesCheckboxes);
            });

            $rootScope.$on('supplierProposalToChangeEvent', function (event, supplierInvoicesCheckboxes) {
                console.log('supplierProposalToChangeEvent catched');

                recalculateSupplierProposal(supplierInvoicesCheckboxes);
            });

            recalculateBuyerProposal = function (buyerInvoicesCheckboxes) {
                var invoices_sum = 0;
                var orders_sum = 0;

                var invoicesSumByTarget = groupAndSumInvoicesByTarget(invoices.buyerInvoices, buyerInvoicesCheckboxes);

                angular.forEach(invoicesSumByTarget, function (invoiceValue, invoiceTarget) {
                    //calculate invoices sum
                    invoices_sum += invoiceValue;

                    orderRequestsService.getOrderRequests(invoiceTarget)
                        .then(function successCallback(response) {
                            var orderRequests = response.data;
                            console.log('order: ' + JSON.stringify(orderRequests));

                            angular.forEach(orderRequests, function (orderRequest) {
                                if (orderRequest.side == 'ASK') {
                                    orders_sum += Math.min(orderRequest.quantity, invoiceValue);
                                    console.log('asks sum increased: ' + orders_sum);
                                }
                            });

                            $scope.asks_sum = orders_sum;
                        }, function errorCallback(response) {
                            console.log('got ' + response.status + ' error');
                        });
                });
                $scope.buyer_invoices_sum = invoices_sum;
                if (invoicesSumByTarget.length === 0) {
                    $scope.asks_sum = 0;
                }
            };

            recalculateSupplierProposal = function (supplierInvoicesCheckboxes) {
                var invoices_sum = 0;
                var orders_sum = 0;

                var invoicesSumBySource = groupAndSumInvoicesBySource(invoices.supplierInvoices, supplierInvoicesCheckboxes);

                //calculate invoices sum
                angular.forEach(invoicesSumBySource, function (invoiceValue, invoiceSource) {
                    invoices_sum += invoiceValue;

                    orderRequestsService.getOrderRequests(invoiceSource)
                        .then(function successCallback(response) {
                            var orderRequests = response.data;
                            console.log('order: ' + JSON.stringify(orderRequests));

                            angular.forEach(orderRequests, function (orderRequest) {
                                if (orderRequest.side == 'BID') {
                                    orders_sum += Math.min(orderRequest.quantity, invoiceValue);
                                    console.log('bids sum increased: ' + orders_sum);
                                }
                            });

                            $scope.bids_sum = orders_sum;
                        }, function errorCallback(response) {
                            console.log('got ' + response.status + ' error');
                        });
                });
                $scope.supplier_invoices_sum = invoices_sum;
                if (invoicesSumBySource.length === 0) {
                    $scope.bids_sum = 0;
                }
            };

            //invoice.target must be the same
            groupAndSumInvoicesBySource = function (invoices, invoicesCheckboxes) {
                var result = [];
                angular.forEach(invoices, function (invoice) {
                        if (invoicesCheckboxes[invoice.id]) {
                            result[invoice.source.id] = (result[invoice.source.id] || 0) + invoice.value - invoice.prepaidValue;
                        }
                    }
                );
                return result;
            };

            //invoice.source must be the same
            groupAndSumInvoicesByTarget = function (invoices, invoicesCheckboxes) {
                var result = [];
                angular.forEach(invoices, function (invoice) {
                        if (invoicesCheckboxes[invoice.id]) {
                            result[invoice.target.id] = (result[invoice.target.id] || 0) + invoice.value - invoice.prepaidValue;
                        }
                    }
                );
                return result;
            };

            self.init = function () {
                //orderRequestsService.getOrderRequests(counterpartyId)
                //    .then(function successCallback(response){
                //        var orderRequests = response.data;
                //        console.log(JSON.stringify(orderRequests));
                //
                //    }, function errorCallback(response) {
                //        console.log('got ' + response.status + ' error');
                //    });
            }

            self.init();

        }]);
