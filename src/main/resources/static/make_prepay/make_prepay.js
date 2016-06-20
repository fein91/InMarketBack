angular.module('inmarket.make_prepay', ['ngRoute'])

	.config(['$routeProvider', function($routeProvider) {
		$routeProvider.when('/make_prepay', {
			templateUrl: 'make_prepay/make_prepay.html',
			controller: 'MakePrepayCtrl'
		});
	}])

	.controller('MarketBidCtrl', ['$scope', '$rootScope', 'orderRequestsService', 'invoices', function($scope, $rootScope, orderRequestsService, invoices) {
		console.log('MarketBidCtrl inited');

		$scope.bidQty = '';
		$scope.bidApr = '';
		$scope.demandSatisfied = true;
		$scope.calculationCalled = false;
		$scope.calculatedWithError = true;
		$scope.calculationErrorMsg = false;

		$scope.calculateBidMarketOrder = function() {
			if ($scope.bidQty) {
				$scope.calculationCalled = true;
				var orderRequest = {
					"id" : 123456789,
					"quantity" : $scope.bidQty,
					"orderSide" : 0,
					"orderType" : 1,
					"counterparty" : {
						"id" : 11,
						"name" : "supplyer"
					}
				};

				orderRequestsService.calculate(orderRequest)
					.then(function successCallback(response){
						var orderResult = response.data;
						console.log('order result: ' + JSON.stringify(orderResult));
						$scope.bidApr = orderResult.apr;
						$scope.satisfiedBidQty = orderResult.satisfiedDemand;
						if ($scope.bidQty > $scope.satisfiedBidQty) {
							$scope.demandSatisfied = false;
						}

						$scope.calculatedWithError = false;
					}, function errorCallback(response) {
						console.log('got ' + response.status + ' error');
						$scope.calculatedWithError = true;
						$scope.calculationErrorMsg = response.data.message;
					});
			}
		};

		$scope.submitBidMarketOrder = function() {
			if ($scope.bidQty) {
				var orderRequest = {
					"id" : 123456789,
					"quantity" : $scope.bidQty,
					"orderSide" : 0,
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

						$rootScope.$broadcast('buyerProposalToChangeEvent', invoices.buyerInvoicesCheckboxes.invoices);
						$rootScope.$broadcast('supplierProposalToChangeEvent', invoices.supplierInvoicesCheckboxes.invoices);

					}, function errorCallback(response) {
						console.log('got ' + response.status + ' error');
						$scope.calculatedWithError = true;
						$scope.calculationErrorMsg = response.data.message;
					});
			}
		};

		$scope.reset = function() {
			$scope.bidQty = '';
			$scope.bidApr = '';
			$scope.demandSatisfied = true;
			$scope.calculatedWithError = true;
			$scope.calculationErrorMsg = '';
			$scope.calculationCalled = false;
		};
	}])

	.controller('LimitBidCtrl', ['$scope', 'orderRequestsService', function($scope, orderRequestsService) {
		console.log('LimitBidCtrl inited');
		$scope.bidQty = '';
		$scope.bidApr = '';
		$scope.calculationCalled = false;
		$scope.calculatedWithError = true;
		$scope.calculationErrorMsg = false;

		$scope.calculateLimitBidOrder = function() {
			if ($scope.bidQty && $scope.bidApr) {
				$scope.calculationCalled = true;
				var orderRequest = {
					"price" : $scope.bidApr,
					"quantity" : $scope.bidQty,
					"orderSide" : 0,
					"orderType" : 0,
					"counterparty" : {
						"id" : 11,
						"name" : "supplyer"
					}
				};

				orderRequestsService.calculate(orderRequest)
					.then(function successCallback(response){
						var orderResult = response.data;
						console.log('order result: ' + JSON.stringify(orderResult));
						$scope.satisfiedBidQty = orderResult.satisfiedDemand;

						$scope.calculatedWithError = false;
					}, function errorCallback(response) {
						console.log('got ' + response.status + ' error');
						$scope.calculatedWithError = true;
						$scope.calculationErrorMsg = response.data.message;
					});
			}
		}

		$scope.submitLimitBidOrder = function() {
			if ($scope.bidQty && $scope.bidApr) {
				var orderRequest = {
					"price" : $scope.bidApr,
					"quantity" : $scope.bidQty,
					"orderSide" : 0,
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
						$scope.calculatedWithError = true;
						$scope.calculationErrorMsg = response.data.message;
					});
			}
		};

		$scope.reset = function() {
			$scope.bidQty = '';
			$scope.bidApr = '';
			$scope.calculatedWithError = true;
			$scope.calculationErrorMsg = '';
			$scope.calculationCalled = false;
		};

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

