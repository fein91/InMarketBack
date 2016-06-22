angular.module('inmarket.make_prepay', ['ngRoute', 'ui.bootstrap'])

	.config(['$routeProvider', function($routeProvider) {
		$routeProvider.when('/make_prepay', {
			templateUrl: 'make_prepay/make_prepay.html'
		});
	}])

	.controller('MarketBidCtrl', ['$scope', '$rootScope', '$uibModal', 'orderRequestsService', 'invoices', function($scope, $rootScope, $uibModal, orderRequestsService, invoices) {
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

		$scope.openConfirmation = function() {
			var modalInstance = $uibModal.open({
				animation: true,
				templateUrl: 'make_prepay/orderRequestConfirmPopup.html',
				controller: 'OrderRequestConfirmPopupCtrl',
				size: 'sm',
				resolve: {
					orderRequest: function () {
						return {
							"quantity" : $scope.bidQty,
							"orderSide" : 0,
							"orderType" : 1,
							"counterparty" : {
								"id" : 11,
								"name" : "supplyer"
							}
						};
					}
				}
			});
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

	.controller('LimitBidCtrl', ['$scope', '$uibModal', 'orderRequestsService', function($scope, $uibModal, orderRequestsService) {
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

		$scope.openConfirmation = function() {
			var modalInstance = $uibModal.open({
				animation: true,
				templateUrl: 'make_prepay/orderRequestConfirmPopup.html',
				controller: 'OrderRequestConfirmPopupCtrl',
				size: 'sm',
				resolve: {
					orderRequest: function () {
						return {
							"price" : $scope.bidApr,
							"quantity" : $scope.bidQty,
							"orderSide" : 0,
							"orderType" : 0,
							"counterparty" : {
								"id" : 11,
								"name" : "supplyer"
							}
						};
					}
				}
			});
		};

		$scope.reset = function() {
			$scope.bidQty = '';
			$scope.bidApr = '';
			$scope.calculatedWithError = true;
			$scope.calculationErrorMsg = '';
			$scope.calculationCalled = false;
		};

	}])


	.controller('MakePrepayHistoryChartCtrl', ['$scope', function($scope) {
		console.log('MakePrepayHistoryChartCtrl inited');

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

