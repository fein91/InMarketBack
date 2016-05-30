angular.module('inmarket.make_prepay', ['ngRoute'])

	.config(['$routeProvider', function($routeProvider) {
		$routeProvider.when('/make_prepay', {
			templateUrl: 'make_prepay/make_prepay.html',
			controller: 'MakePrepayCtrl'
		});
	}])

	.controller('MarketBidCtrl', ['$scope', 'orderRequestsService', function($scope, orderRequestsService) {
		console.log('MarketBidCtrl inited');

		$scope.bidQty = '';
		$scope.bidApr = '';
		$scope.demandSatisfied = true;
		$scope.noCounterparties = false;

		$scope.submitBidMarketOrder = function() {
			var orderRequest = {
				"id" : 123456789,
				"quantity" : $scope.bidQty,
				"orderSide" : 1,
				"orderType" : 1,
				"counterparty" : {
					"id" : 11,
					"name" : "supplyer"
				}
			};

			orderRequestsService.process(orderRequest)
				.then(function successCallback(response){
					var orderResult = response.data;
					console.log('order result: ' + JSON.stringify(orderResult));
					$scope.bidApr = orderResult.apr;
					$scope.satisfiedBidQty = orderResult.satisfiedDemand;
					if ($scope.bidQty > $scope.satisfiedBidQty) {
						$scope.demandSatisfied = false;
					}

				}, function errorCallback(response) {
					console.log('got ' + response.status + ' error');
				});
		}

		$scope.reset = function() {
			$scope.bidQty = '';
			$scope.bidApr = '';
			$scope.demandSatisfied = true;
			$scope.noCounterparties = false;
		}
	}])

	.controller('LimitBidCtrl', ['$scope', 'orderRequestsService', function($scope, orderRequestsService) {
		console.log('LimitBidCtrl inited');
		$scope.bidQty = '';
		$scope.bidApr = '';

		$scope.submitLimitBidOrder = function() {
			if ($scope.bidQty && $scope.bidApr) {
				var orderRequest = {
					"id" : 123456789,
					"price" : $scope.bidApr,
					"quantity" : $scope.bidQty,
					"orderSide" : 1,
					"orderType" : 0,
					"counterparty" : {
						"id" : 11,
						"name" : "supplyer"
					}
				};

				orderRequestsService.process(orderRequest)
					.then(function successCallback(response){
						var orderResult = response.data;
						console.log('order result: ' + JSON.stringify(orderResult));
						$scope.satisfiedBidQty = orderResult.satisfiedDemand;

					}, function errorCallback(response) {
						console.log('got ' + response.status + ' error');
					});
			}

		}

	}])


	.controller('MakePrepayCtrl', ['$scope', function($scope) {
		console.log('MakePrepayCtrl inited');

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

