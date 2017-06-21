'use strict';

angular.module('startPage').component('startPage', {
    templateUrl: 'components/start-page/start-page.template.html',
    controllerAs: "startPageCtrl",
    controller: function StartPageController($rootScope, $http, $location, $httpParamSerializerJQLike) {

        var vm = this;

        $rootScope.authenticated = false;

        vm.credentials = {};

        $rootScope.loggedInUser = vm.credentials;

        vm.isNewAccountRegistered = $rootScope.isNewAccountRegistered;
        vm.newAccountEmail = $rootScope.newAccountEmail;

        vm.login = function(){

            var url = window.location.protocol+"//"+window.location.hostname+":8080/login";

            var data = $httpParamSerializerJQLike({
                username: vm.credentials.email,
                password: vm.credentials.password
            });

            var config = {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            };

            $http.post(url, data, config).success(function(response){
                $rootScope.isNewAccountRegistered = false;
                vm.isNewAccountRegistered = false;
                $rootScope.authenticated = true;
                $location.path("/wallets");
            }).error(function(response){
                console.log("error while logging in");

                if (response === "Authentication failure") {
                    alert("Invalid e-mail or password");
                }
            });

        };

        $rootScope.logout = function() {
            $http.post(window.location.protocol+'//'+window.location.hostname+':8080/logout', {}).finally(function() {
                $rootScope.authenticated = false;
                $location.path("/start");
            });
        };
    }
});
