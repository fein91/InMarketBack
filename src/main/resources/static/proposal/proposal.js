angular.module('inmarket.proposal', ['ngRoute'])


.controller('ProposalCtrl', ['$scope', '$rootScope', 'invoices', function($scope, $rootScope, invoices) {
	console.log('ProposalCtrl inited');

		self = this;
		$scope.buyer_invoices_sum = 0;
		$scope.supplier_invoices_sum = 0;
		$scope.asks_sum = 0;
		$scope.bids_sum = 0;

		// listen for the event in the relevant $scope
		$rootScope.$on('buyerProposalToChangeEvent', function (event, data) {
            console.log('buyerProposalToChangeEvent catched');

            recalculateBuyerProposal(data);
		});

		$rootScope.$on('supplierProposalToChangeEvent', function (event, data) {
            console.log('supplierProposalToChangeEvent catched');

			recalculateSupplierProposal(data);
		});

		recalculateBuyerProposal = function(data) {
            var sum = 0;
            angular.forEach(invoices.buyerInvoices, function(item) {
                if (data[item.id]) {
                    sum = sum + item.value;
                }
            });
            $scope.buyer_invoices_sum = sum;
		};

        recalculateSupplierProposal = function(data) {
            var sum = 0;
            angular.forEach(invoices.supplierInvoices, function(item) {
                if (data[item.id]) {
                    sum = sum + item.value;
                }
            });
            $scope.supplier_invoices_sum = sum;
        };

}]);
