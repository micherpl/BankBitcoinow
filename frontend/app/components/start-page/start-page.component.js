'use strict';

angular.module('startPage').component('startPage', {
    templateUrl: 'components/start-page/start-page.template.html',
    controllerAs: "startPageCtrl",
    controller: function StartPageController($rootScope, $http, $location, $route) {

        var vm = this;

        // var authenticate = function(credentials, callback) {
        //
        //     var headers = credentials ? {
        //         authorization : "Basic "
        //         + btoa(credentials.username + ":"
        //             + credentials.password)
        //     } : {};
        //
        //     $http.get(window.location.protocol+'//'+window.location.hostname+':8080/user', {
        //         headers : headers
        //     }).then(function(response) {
        //         if (response) {
        //             // alert(1);
        //             $rootScope.authenticated = true;
        //         } else {
        //             // alert(2);
        //             $rootScope.authenticated = false;
        //         }
        //         callback && callback($rootScope.authenticated);
        //     }, function() {
        //         $rootScope.authenticated = false;
        //         callback && callback(false);
        //     });
        //
        // };
        //
        // authenticate();

        // vm.loginRequest = function() {
        //     authenticate(vm.credentials, function(authenticated) {
        //         if (authenticated) {
        //             console.log("Login succeeded")
        //             $location.path("/wallets");
        //             vm.error = false;
        //             $rootScope.authenticated = true;
        //         } else {
        //             console.log("Login failed")
        //             $location.path("/start");
        //             vm.error = true;
        //             $rootScope.authenticated = false;
        //         }
        //     })
        // };



        vm.credentials = {};


        vm.loginRequest = function() {

            var url = window.location.protocol+"//"+window.location.hostname+":8080/login";

            alert(url);
            $http({
                method: 'POST',
                url: url,
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                data: 'username='+encodeURIComponent(vm.credentials.email)+'&password='+encodeURIComponent(vm.credentials.password)}
            ).success(function(response){
                alert(111);
                alert(response);
            }).error(function(response){
                alert(222);
            });

            $rootScope.loggedInUser = vm.credentials;
            vm.error = false;
            $location.path("/wallets");
        };



        $rootScope.logout = function() {
            $http.post(window.location.protocol+'//'+window.location.hostname+':8080/logout', {}).finally(function() {
                $rootScope.authenticated = false;
                $location.path("/start");
            });
        };
    }
});
