angular.module('inmarket.get_prepay', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/get_prepay', {
    templateUrl: 'get_prepay/get_prepay.html',
    controller: 'GetPrepayCtrl'
  });
}])


.controller('GetPrepayCtrl', ['$scope', function($scope) {
	console.log('GetPrepayCtrl inited');


	$scope.maxPrice = 700;
	$scope.minPrice = 300;
	$scope.avgPrice = 600;
	$scope.avgDeals = 15;
	
	$scope.line_labels = ["January", "February", "March", "April", "May", "June", "July"];
	$scope.line_series = ['Series A'];
	$scope.line_data = [
		[55, 75, 33, 98, 56, 78, 15]
	];

	$scope.bar_labels = ['2006', '2007', '2008', '2009', '2010', '2011', '2012'];
  	$scope.bar_series = ['Series A', 'Series B'];

  	$scope.bar_data = [
	    [65, 59, 80, 81, 56, 55, 40],
	    [28, 48, 40, 19, 86, 27, 90]
  	];
	
	$scope.onClick = function (points, evt) {
		console.log(points, evt);
	};
}]);
