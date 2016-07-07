angular.module('inmarket', [
    'ngRoute',
    'ngTable',
    'chart.js',
    'ui.bootstrap',
    'inmarket.auth',
    'inmarket.proposal',
    'inmarket.contragents',
    'inmarket.get_prepay',
    'inmarket.make_prepay',
    'inmarket.trans_history',
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

    .controller('loginCtrl', function ($rootScope, $http, $location, $route, session) {
        var self = this;

        self.tab = function (route) {
            return $route.current && route === $route.current.controller;
        };

        var authenticate = function (credentials, callback) {

            var headers = credentials ? {
                authorization: "Basic "
                + btoa(credentials.username + ":"
                + credentials.password)
            } : {};

            $http.get('user', {
                headers: headers
            }).success(function (data) {
                if (data.name) {
                    $rootScope.authenticated = true;
                    session.create({
                        'login' : data.login,
                        'counterpartyId' : data.id
                    });
                } else {
                    console.log('set auth to false')
                    $rootScope.authenticated = false;
                    Session.invalidate();
                }
                callback && callback($rootScope.authenticated);
            }).error(function () {
                console.log('set auth to false due to error')
                $rootScope.authenticated = false;
                callback && callback(false);
            });

        }

        authenticate();

        self.credentials = {};
        self.login = function () {
            authenticate(self.credentials, function (authenticated) {
                if (authenticated) {
                    console.log("Login succeeded")
                    $location.path("/");
                    self.error = false;
                    $rootScope.authenticated = true;
                } else {
                    console.log("Login failed")
                    $location.path("/login");
                    self.error = true;
                    $rootScope.authenticated = false;
                }
            })
        };

        self.logout = function () {
            $http.post('logout', {}).finally(function () {
                $rootScope.authenticated = false;
                console.log('turn off user');
                $location.path("/login");
            });
        }

    })

    .controller('home', function ($http, $rootScope, $location) {
        if (!$rootScope.authenticated) {
            $location.path("/login");
        } else {
            $location.path("/contragents");
        }
    })

    .controller('HeaderController', ['$scope', '$location', function ($scope, $location) {
        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    }])

    .run(function ($rootScope, $location) {
        $rootScope.$on('$routeChangeStart', function (event, next) {
            if (next.originalPath === "/login" && $rootScope.authenticated) {
                event.preventDefault();
            } else if (next.access && next.access.loginRequired && !$rootScope.authenticated) {
                event.preventDefault();
                $rootScope.$broadcast("event:auth-loginRequired", {});
            }
        });

        $rootScope.$on('event:auth-loginRequired', function (event, data) {
            //Session.invalidate();
            $rootScope.authenticated = false;
            $location.path('/login');
        });
    });
