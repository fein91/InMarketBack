angular.module('inmarket.make_prepay', ['ngRoute', 'chart.js'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/make_prepay', {
    templateUrl: 'make_prepay/make_prepay.html'
  });
}])

.controller('AskMarketCtrl', ['$scope', 'orderRequestsService', function($scope, orderRequestsService) {
		console.log('AskMarketCtrl inited');

		$scope.askQty = '';
		$scope.askApr = '';
		$scope.demandSatisfied = true;
		$scope.noCounterparties = false;

		$scope.submitMarketAskOrder = function() {
			var orderRequest = {
				"id" : 123456789,
				"quantity" : $scope.askQty,
				"orderSide" : 0,
				"orderType" : 1,
				"counterparty" : {
					"id" : 11,
					"name" : "test"
				}
			};

			orderRequestsService.process(orderRequest)
				.then(function successCallback(response){
					var orderResult = response.data;
					console.log('order result: ' + JSON.stringify(orderResult));
					$scope.askApr = orderResult.apr;
					$scope.satisfiedBidQty = orderResult.satisfiedDemand;
					if ($scope.askQty > $scope.satisfiedBidQty) {
						$scope.demandSatisfied = false;
					}

				}, function errorCallback(response) {
					console.log('got ' + response.status + ' error');
					$scope.noCounterparties = true;
				});
		}
	}])


.controller('MakePrepayHistoryChartCtrl', ['$scope', function($scope) {
	console.log('MakePrepayHistoryChartCtrl inited');

	self = this;

	$scope.maxPrice = 1000;
	$scope.minPrice = 200;
	$scope.avgPrice = 500;
	$scope.avgDeals = 10;
	
	line_labels_week = ["Пон", "Вт", "Ср", "Чт", "Пн", "Суб", "Вс"];
	line_labels_month = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31];
	line_data_week = [
		[65, 59, 80, 81, 56, 55, 40]
	];
	line_data_month = [
		[65, 59, 80, 81, 56, 55, 40, 43, 76, 86, 45, 23, 87,54, 45, 78, 40, 55, 76, 43, 66, 75, 33, 64,71, 62, 27, 90, 23, 69, 59]
	];

	self.onClick = function (points, evt) {
		console.log(points, evt);
	};

	self.drawWeekChart = function() {
		$scope.line_labels = line_labels_week;
		$scope.line_data = line_data_week;
		$scope.bar_labels = line_labels_week;
  		$scope.bar_data = line_data_week;
	};

	self.drawMonthChart = function() {
		$scope.line_labels = line_labels_month;
		$scope.line_data = line_data_month;
		$scope.bar_labels = line_labels_month;
  		$scope.bar_data = line_data_month;
	};

	self.drawWeekChart();


}])

.controller('MakePrepayPendingOrderCtrl', ['$scope', function($scope) {
	console.log('MakePrepayPendingOrderCtrl inited');

	self = this;
	
	$scope.pos_bar_data = [[25, 26, 27, 28, 29]];
	$scope.pos_bar_labels = ["0.6", "0.5", "0.4", "0.3", "0.2"];

	self.onClick = function (points, evt) {
		console.log(points, evt);
	};

	self.submit = function() {
		console.log("scope: " + $scope);
	}

}]);