angular.module('inmarket.proposal', ['ngRoute'])


.controller('ProposalCtrl', ['$scope', function($scope) {
	console.log('ProposalCtrl inited');
	$scope.to_pay = 1000;
	$scope.to_get = 2000;
}]);
