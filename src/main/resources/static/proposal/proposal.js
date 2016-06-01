angular.module('inmarket.proposal', ['ngRoute'])


.controller('ProposalCtrl', ['$scope', 'invoices', function($scope, invoices) {
	console.log('ProposalCtrl inited');
	$scope.to_pay = 1000;
	$scope.to_get = 2000;
        $scope.invoices = invoices.buyerInvoices;

}]);
