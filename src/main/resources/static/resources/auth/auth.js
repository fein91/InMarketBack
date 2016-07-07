angular.module('inmarket.auth', [])

.controller('LoginController', function($rootScope, $scope, AuthSharedService) {
    $scope.rememberMe = true;
    $scope.login = function() {
        $rootScope.authenticationError = false;
        AuthSharedService.login($scope.username, $scope.password, $scope.rememberMe);
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
})

.service('AuthSharedService', function($rootScope, $http, authService, session) {
    return {
        login: function(userName, password, rememberMe) {
            var config = {
                params: {
                    username: userName,
                    password: password,
                    rememberme: rememberMe
                },
                ignoreAuthModule: 'ignoreAuthModule'
            };
            $http.post('authenticate', '', config)
                .success(function(data, status, headers, config) {
                    authService.loginConfirmed(data);
                }).error(function(data, status, headers, config) {
                    $rootScope.authenticationError = true;
                    Session.invalidate();
                });
        }
    };
});