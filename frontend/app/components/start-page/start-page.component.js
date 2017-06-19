'use strict';

angular.module('startPage').component('startPage', {
    templateUrl: 'components/start-page/start-page.template.html',
    controllerAs: "startPageCtrl",
    controller: function StartPageController($rootScope, $http, $location) {

        var vm = this;

        $rootScope.authenticated = false;

        vm.credentials = {};

        $rootScope.loggedInUser = vm.credentials;

        vm.isNewAccountRegistered = $rootScope.isNewAccountRegistered;
        vm.newAccountEmail = $rootScope.newAccountEmail;

        vm.login = function(){

            var url = window.location.protocol+"//"+window.location.hostname+":8080/login";
            //
            // $http.post(url, JSON.stringify(data)).success(function(response){
            //     vm.newWallet.address = response.address;
            //     vm.newWallet.privateKey = response.privateKey;
            //
            //     vm.isNewWalletCreated = true;
            //     vm.isWalletCreationPending = false;

                $rootScope.isNewAccountRegistered = false;
                vm.isNewAccountRegistered = false;
                $rootScope.authenticated = true;
                $location.path("/wallets");
            // }).error(function(response){
            //     console.log("error while creating new wallet");
            // }).finally(function(response){
            //     // vm.test = vm.newWallet;
            // });
            //

        };

        $rootScope.logout = function() {
            $http.post(window.location.protocol+'//'+window.location.hostname+':8080/logout', {}).finally(function() {
                $rootScope.authenticated = false;
                $location.path("/start");
            });
        };
    }
});
