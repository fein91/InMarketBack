angular.module('inmarket.proposal', ['ngRoute'])

.controller('ProposalCtrl', ['$scope', function($scope) {
	console.log('ProposalCtrl inited');

	$scope.asks_sum = 1000;
	$scope.invoices_sum = 1500;
	$scope.bids_sum = 2000;
		self = this;
		self.counterpartyId = 11;

		self.init = function() {

		}

}]);
