angular.module('inmarket.proposal', ['ngRoute'])


.controller('ProposalCtrl', ['$scope', '$rootScope', 'invoices', function($scope, $rootScope, invoices) {
	console.log('ProposalCtrl inited');

		self = this;
		$scope.buyer_invoices_sum = 0;
		$scope.supplier_invoices_sum = 0;
		$scope.asks_sum = 0;
		$scope.bids_sum = 0;

        $scope.invoices = invoices.buyerInvoices;

		// listen for the event in the relevant $scope
		$rootScope.$on('buyerProposalToChangeEvent', function (event, data) {
			recalculateBuyerProposal(data);
		});

		$rootScope.$on('supplierProposalToChangeEvent', function (event, data) {
			console.log(data); // 'Data to send'
		});

		recalculateBuyerProposal = function(data) {
			buyer_invoices_sum
		}

}]);
