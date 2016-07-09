angular.module('inmarket.auth', [])

    //.controller('LoginController', function ($rootScope, $scope, AuthSharedService) {
    //    $scope.rememberMe = true;
    //    $scope.login = function () {
    //        $rootScope.authenticationError = false;
    //        AuthSharedService.login($scope.username, $scope.password, $scope.rememberMe);
    //    }
    //})

    .controller('home', function ($http, $rootScope, $location) {
        if (!$rootScope.authenticated) {
            $location.path("/login");
        } else {
            $location.path("/contragents");
        }
    })

    .controller('loginCtrl', function ($rootScope, $scope, $http, $location, $route, session) {
        var self = this;

        $scope.session = session;

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
                        'login': data.login,
                        'counterpartyId': data.id
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

    .service('session', function () {
        this.create = function (data) {
            this.id = data.id;
            this.login = data.login;
            this.counterpartyId = data.counterpartyId;
            this.name = data.name;
            this.userRoles = [];
            angular.forEach(data.authorities, function (value, key) {
                this.push(value.name);
            }, this.userRoles);
        };
        this.invalidate = function () {
            this.id = null;
            this.login = null;
            this.name = null;
            this.userRoles = null;
        };
        return this;
    });

//.service('AuthSharedService', function ($rootScope, $http, authService, session) {
//    return {
//        login: function (userName, password, rememberMe) {
//            var config = {
//                params: {
//                    username: userName,
//                    password: password,
//                    rememberme: rememberMe
//                },
//                ignoreAuthModule: 'ignoreAuthModule'
//            };
//            $http.post('authenticate', '', config)
//                .success(function (data, status, headers, config) {
//                    authService.loginConfirmed(data);
//                }).error(function (data, status, headers, config) {
//                    $rootScope.authenticationError = true;
//                    Session.invalidate();
//                });
//        }
//    };
//});