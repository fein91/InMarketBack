'use strict';

// Declare app level module which depends on views, and components
angular.module('inmarket', [
  'ngRoute',
  'ngTable',
  'chart.js',
  'ui.bootstrap',
  'inmarket.proposal',
  'inmarket.contragents',
  'inmarket.get_prepay',
  'inmarket.make_prepay',
  'inmarket.trans_history',
  'inmarket.invoicesService',
  'inmarket.transHistoryService',
  'inmarket.orderRequestsService'
])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/contragents'})
  .when('/support', {
    templateUrl: 'support/support.html'
  });
}])

.controller('HeaderController', ['$scope', '$location', function($scope, $location) {
	$scope.isActive = function (viewLocation) { 
        return viewLocation === $location.path();
    };
}]);
