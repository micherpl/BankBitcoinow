'use strict';

// Register `phoneList` component, along with its associated controller and template
angular.module('startPage').component('startPage', {
    templateUrl: 'components/start-page/start-page.template.html',
    controllerAs: "startPageCtrl",
    controller: function StartPageController($rootScope, $http, $location, $route) {

        var vm = this;

        var authenticate = function(credentials, callback) {

            var headers = credentials ? {
                authorization : "Basic "
                + btoa(credentials.username + ":"
                    + credentials.password)
            } : {};

            $http.get('http://localhost:8080/user', {
                headers : headers
            }).then(function(response) {
                if (response) {
                    // alert(1);
                    $rootScope.authenticated = true;
                } else {
                    // alert(2);
                    $rootScope.authenticated = false;
                }
                callback && callback($rootScope.authenticated);
            }, function() {
                $rootScope.authenticated = false;
                callback && callback(false);
            });

        };

        authenticate();

        vm.credentials = {};
        vm.loginRequest = function() {
            authenticate(vm.credentials, function(authenticated) {
                if (authenticated) {
                    console.log("Login succeeded")
                    $location.path("/wallets");
                    vm.error = false;
                    $rootScope.authenticated = true;
                } else {
                    console.log("Login failed")
                    $location.path("/start");
                    vm.error = true;
                    $rootScope.authenticated = false;
                }
            })
        };

        $rootScope.logout = function() {
            $http.post('http://localhost:8080/logout', {}).finally(function() {
                $rootScope.authenticated = false;
                $location.path("/start");
            });
        };
    }
});
