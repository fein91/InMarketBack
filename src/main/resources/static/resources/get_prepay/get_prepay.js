angular.module('inmarket.get_prepay', ['ngRoute', 'chart.js'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/get_prepay', {
            templateUrl: 'partials/get_prepay.html',
            access: {
                loginRequired: true
            }
        });
    }])

    .controller('BidMarketCtrl', ['$scope', '$rootScope', '$uibModal', 'orderRequestsService', 'invoices', 'session', function ($scope, $rootScope, $uibModal, orderRequestsService, invoices, session) {
        console.log('BidMarketCtrl inited');

        $scope.bidQty = '';
        $scope.bidApr = '';
        $scope.satisfiedBidQty = '';
        $scope.avgDaysToPayment = '';
        $scope.avgDiscountPerc = '';
        $scope.discountSum = '';
        $scope.demandSatisfied = true;
        $scope.calculationCalled = false;
        $scope.calculatedWithError = true;
        $scope.calculationErrorMsg = false;

        $scope.calculateMarketBidOrder = function () {
            if ($scope.bidQty) {
                $scope.calculationCalled = true;
                var orderRequest = {
                    "quantity": $scope.bidQty,
                    "orderSide": 1,
                    "orderType": 1,
                    "date": new Date(),
                    "counterparty": {
                        "id": session.counterpartyId,
                        "name": "test"
                    },
                    "invoicesChecked": invoices.buyerInvoicesCheckboxes.invoices
                };

                orderRequestsService.calculate(orderRequest)
                    .then(function successCallback(response) {
                        var orderResult = response.data;
                        console.log('order result: ' + JSON.stringify(orderResult));
                        $scope.bidApr = orderResult.apr;
                        $scope.satisfiedBidQty = orderResult.satisfiedDemand;
                        $scope.avgDaysToPayment = orderResult.avgDaysToPayment;
                        $scope.avgDiscountPerc = orderResult.avgDiscountPerc;
                        $scope.discountSum = orderResult.avgDiscountPerc;
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

        $scope.openConfirmation = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/orderRequestConfirmPopup.html',
                controller: 'OrderRequestConfirmPopupCtrl',
                size: 'sm',
                resolve: {
                    orderRequest: function () {
                        return {
                            "quantity": $scope.bidQty,
                            "orderSide": 1,
                            "orderType": 1,
                            "date": new Date(),
                            "counterparty": {
                                "id": session.counterpartyId,
                                "name": "test"
                            },
                            "invoicesChecked": invoices.buyerInvoicesCheckboxes.invoices
                        };
                    }
                }
            });
        };

        $scope.reset = function () {
            $scope.bidQty = '';
            $scope.bidApr = '';
            $scope.satisfiedBidQty = '';
            $scope.avgDaysToPayment = '';
            $scope.avgDiscountPerc = '';
            $scope.discountSum = '';
            $scope.demandSatisfied = true;
            $scope.calculatedWithError = true;
            $scope.calculationErrorMsg = '';
            $scope.calculationCalled = false;
        };
    }])


    .controller('GetPrepayHistoryChartCtrl', ['$scope', function ($scope) {
        console.log('GetPrepayHistoryChartCtrl inited');

        self = this;

        $scope.maxPrice = 1000;
        $scope.minPrice = 200;
        $scope.avgPrice = 500;
        $scope.avgDeals = 10;

        line_labels_week = ["Пон", "Вт", "Ср", "Чт", "Пн", "Суб", "Вс"];
        line_labels_month = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31];
        line_data_week = [
            [65, 59, 80, 81, 56, 55, 40]
        ];
        line_data_month = [
            [65, 59, 80, 81, 56, 55, 40, 43, 76, 86, 45, 23, 87, 54, 45, 78, 40, 55, 76, 43, 66, 75, 33, 64, 71, 62, 27, 90, 23, 69, 59]
        ];

        self.onClick = function (points, evt) {
            console.log(points, evt);
        };

        self.drawWeekChart = function () {
            $scope.line_labels = line_labels_week;
            $scope.line_data = line_data_week;
            $scope.bar_labels = line_labels_week;
            $scope.bar_data = line_data_week;
        };

        self.drawMonthChart = function () {
            $scope.line_labels = line_labels_month;
            $scope.line_data = line_data_month;
            $scope.bar_labels = line_labels_month;
            $scope.bar_data = line_data_month;
        };

        self.drawWeekChart();


    }])

    .controller('BidLimitCtrl', ['$scope', '$uibModal', 'orderRequestsService', 'invoices', 'session', function ($scope, $uibModal, orderRequestsService, invoices, session) {
        console.log('BidLimitCtrl inited');

        self = this;

        $scope.bidQty = '';
        $scope.bidApr = '';
        $scope.calculationCalled = false;
        $scope.calculatedWithError = true;
        $scope.calculationErrorMsg = false;

        $scope.calculateLimitBidOrder = function () {
            if ($scope.bidQty && $scope.bidApr) {
                $scope.calculationCalled = true;
                var orderRequest = {
                    "price": $scope.bidApr,
                    "quantity": $scope.bidQty,
                    "orderSide": 1,
                    "orderType": 0,
                    "date": new Date(),
                    "counterparty": {
                        "id": session.counterpartyId,
                        "name": "supplyer"
                    },
                    "invoicesChecked": invoices.buyerInvoicesCheckboxes.invoices
                };

                orderRequestsService.calculate(orderRequest)
                    .then(function successCallback(response) {
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

        $scope.openConfirmation = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/orderRequestConfirmPopup.html',
                controller: 'OrderRequestConfirmPopupCtrl',
                size: 'sm',
                resolve: {
                    orderRequest: function () {
                        return {
                            "price": $scope.bidApr,
                            "quantity": $scope.bidQty,
                            "orderSide": 1,
                            "orderType": 0,
                            "date": new Date(),
                            "counterparty": {
                                "id": session.counterpartyId,
                                "name": "supplyer"
                            },
                            "invoicesChecked": invoices.buyerInvoicesCheckboxes.invoices
                        };
                    }
                }
            });
        };

        $scope.reset = function () {
            $scope.bidQty = '';
            $scope.bidApr = '';
            $scope.calculatedWithError = true;
            $scope.calculationErrorMsg = '';
            $scope.calculationCalled = false;
        };


    }]);