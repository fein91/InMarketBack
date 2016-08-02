angular.module('inmarket', [
    'ngRoute',
    'ngTable',
    'ngStorage',
    'ngSanitize',
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
    'inmarket.limitOrderDeleteConfirmPopupCtrl',
    'inmarket.limitOrderEditConfirmPopupCtrl',
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
    });

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
