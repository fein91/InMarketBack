angular.module('inmarket', [
    'ngRoute',
    'ngTable',
    'ngStorage',
    'chart.js',
    'ui.bootstrap',
    'inmarket.auth',
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
    'inmarket.invoices'])

    .config(function ($routeProvider, $httpProvider) {
        $routeProvider
            .when('/support', {
                templateUrl: 'partials/support.html',
                access: {
                    loginRequired: true
                }
            })
            .when('/login', {
                templateUrl: 'login.html',
                controller: 'loginCtrl',
                controllerAs: 'controller'
            })
            .when('/', {
                templateUrl: 'login.html',
                controller: 'home',
                controllerAs: 'controller'
            })
            .when('/importExcelFile', {
                templateUrl: 'partials/import.html',
                access: {
                    loginRequired: true
                }
            })
            .otherwise({redirectTo: '/'});

        $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    })

    .controller('HeaderController', ['$scope', '$location', function ($scope, $location) {
        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    }])

    .run(function ($rootScope, $location, $sessionStorage, session) {
        $rootScope.$on('$routeChangeStart', function (event, next) {
            if (next.originalPath === "/login" && $sessionStorage.authenticated) {
                event.preventDefault();
            } else if (next.access && next.access.loginRequired && !$sessionStorage.authenticated) {
                event.preventDefault();
                $rootScope.$broadcast("event:auth-loginRequired", {});
            }
        });

        $rootScope.$on('event:auth-loginRequired', function (event, data) {
            session.invalidate();
            $sessionStorage.authenticated = false;
            $rootScope.authenticated = $sessionStorage.authenticated;

            $location.path('/login');
        });
    })

    .controller('uploadCtrl', ['$scope', '$http', 'session', function ($scope, $http, session) {

        $scope.uploadFile = function (files) {
            var fd = new FormData();
            //Take the first selected file
            fd.append("file", files[0]);

            $http.post('counterparties/' + session.counterpartyId + '/importInvoices', fd, {
                withCredentials: true,
                headers: {'Content-Type': undefined},
                transformRequest: angular.identity
            });
        }
    }])

    .controller('exportCtrl', ['$scope', '$http', 'session', function ($scope, $http, session) {

        $scope.exportData = function () {
            $http({
                method: 'POST',
                url: 'counterparties/' + session.counterpartyId + '/exportInvoices',
                headers: {'Content-Type': 'text/csv'}

            }).success(function (data, status, headers, config) {
                var anchor = angular.element('<a/>');
                anchor.attr({
                    href: 'data:attachment/csv;charset=utf-8,' + encodeURI(data),
                    target: '_blank',
                    download: 'export.csv'
                })[0].click();
            })
        }
    }]);

    //here comes some static js

    Array.prototype.sum = function (prop) {
        var total = 0;
        for ( var i = 0, _len = this.length; i < _len; i++ ) {
            total += this[i][prop]
        }
        return total
    };

    Array.prototype.avg = function (prop) {
        var total = 0;
        for ( var i = 0, _len = this.length; i < _len; i++ ) {
            total += this[i][prop]
        }
        return total / this.length
    };
