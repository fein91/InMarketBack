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
                var processedBuyers = [];

                angular.forEach(invoices.buyerInvoices, function (buyerInvoice) {
                    //calculate invoices sum
                    if (buyerInvoicesCheckboxes[buyerInvoice.id]) {
                        invoices_sum = invoices_sum + buyerInvoice.value;

                        console.log('buyer invoice: ' + JSON.stringify(buyerInvoice));

                        if (processedBuyers.indexOf(buyerInvoice.source.id) < 0) {
                            processedBuyers.push(buyerInvoice.source.id);

                            orderRequestsService.getOrderRequests(buyerInvoice.target.id)
                                .then(function successCallback(response) {
                                    var orderRequests = response.data;
                                    console.log('order: ' + JSON.stringify(orderRequests));

                                    angular.forEach(orderRequests, function (orderRequest) {
                                        if (orderRequest.orderSide == 'ASK') {
                                            orders_sum = orders_sum + Math.min(orderRequest.quantity, buyerInvoice.value);
                                            console.log('asks sum increased: ' + orders_sum);
                                        }
                                    });

                                    //TODO remove it from here
                                    $scope.asks_sum = orders_sum;
                                }, function errorCallback(response) {
                                    console.log('got ' + response.status + ' error');
                                });
                        }
                    }
                });
                $scope.buyer_invoices_sum = invoices_sum;
            };

            recalculateSupplierProposal = function (supplierInvoicesCheckboxes) {
                var invoices_sum = 0;
                var orders_sum = 0;
                var processedSuppliers = [];

                //calculate invoices sum
                angular.forEach(invoices.supplierInvoices, function (supplierInvoice) {
                    if (supplierInvoicesCheckboxes[supplierInvoice.id]) {
                        invoices_sum = invoices_sum + supplierInvoice.value;

                        console.log('supplier invoice: ' + JSON.stringify(supplierInvoice));

                        if (processedSuppliers.indexOf(supplierInvoice.source.id) < 0) {
                            processedSuppliers.push(supplierInvoice.source.id);

                            orderRequestsService.getOrderRequests(supplierInvoice.source.id)
                                .then(function successCallback(response) {
                                    var orderRequests = response.data;
                                    console.log('order: ' + JSON.stringify(orderRequests));

                                    angular.forEach(orderRequests, function (orderRequest) {
                                        if (orderRequest.orderSide == 'BID') {
                                            orders_sum = orders_sum + Math.min(orderRequest.quantity, supplierInvoice.value);
                                            console.log('bids sum increased: ' + orders_sum);
                                        }
                                    });

                                    //TODO remove it from here
                                    $scope.bids_sum = orders_sum;
                                }, function errorCallback(response) {
                                    console.log('got ' + response.status + ' error');
                                });
                        }
                    }
                });
                $scope.supplier_invoices_sum = invoices_sum;
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
