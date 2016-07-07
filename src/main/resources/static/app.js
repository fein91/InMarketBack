'use strict';

// Declare app level module which depends on views, and components
angular.module('inmarket', [
  'ngRoute',
  'ngTable',
  'chart.js',
  'ui.bootstrap',
  'inmarket.proposal',
  'inmarket.orderRequestConfirmPopup',
  'inmarket.contragents',
  'inmarket.get_prepay',
  'inmarket.make_prepay',
  'inmarket.trans_history',
  'inmarket.limit_orders',
  'inmarket.invoicesService',
  'inmarket.transHistoryService',
  'inmarket.orderRequestsService',
  'inmarket.counterpartyService',
  'inmarket.invoices'
  ])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/contragents'})
  .when('/support', {
    templateUrl: 'support/support.html'
  })
  .when('/importExcelFile', {
    templateUrl: 'import/import.html'
  })
  ;
}])

.controller('HeaderController', ['$scope', '$location', function($scope, $location) {
  $scope.isActive = function (viewLocation) {
    return viewLocation === $location.path();
  };
}])

.controller('uploadCtrl', ['$scope', '$http', function($scope, $http) {
  $scope.uploadFile = function(files) {
    var fd = new FormData();
        //Take the first selected file
        fd.append("file", files[0]);

        $http.post('upload', fd, {
          withCredentials: true,
          headers: {'Content-Type': undefined },
          transformRequest: angular.identity
        });
      }
    }])
.controller('exportCtrl', ['$scope', '$http', function($scope, $http) {

  $scope.exportData = function(){
    $http({
      method: 'POST',
      url: '/exportInvoices',
      headers: {'Content-Type': 'text/csv'}

    }).success(function(data, status, headers, config) {
     var anchor = angular.element('<a/>');
     anchor.attr({
       href: 'data:attachment/csv;charset=utf-8,' + encodeURI(data),
       target: '_blank',
       download: 'export.xlsx'
     })[0].click();
   })
  }
}]);
