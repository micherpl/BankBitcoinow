'use strict';

angular.module('transactionList').component('transactionList', {
    templateUrl: 'components/transaction-list/transaction-list.template.html',
    controllerAs: "transactionListCtrl",
    controller: ['$http','$rootScope', '$location',
        function TransactionListController($http, $rootScope, $location) {

            var vm = this;

            vm.authenticated = $rootScope.authenticated;

            if(!vm.authenticated) {
                $location.path("/start");
            }


            vm.btcPrice = 2850;
            vm.totalBitcoinAmount = $rootScope.totalBitcoinAmount;
            vm.loggedInUser = {
                email: $rootScope.loggedInUser.email?$rootScope.loggedInUser.email:""
            };

            vm.transactions = [];

            vm.isLoading = false;


            vm.getUserTransactions = function(){
                vm.isLoading = true;

                var data = {
                    email: vm.loggedInUser.email
                };

                var url = window.location.protocol+"//"+window.location.hostname+":8080/getUserTransactions";

                $http.post(url, JSON.stringify(data)).success(function(response){

                    for(var i = 0; i < response.length; i++){
                        vm.transactions.push(response[i]);
                    }
                    vm.isLoading = false;

                }).error(function(response){
                    alert(2);
                }).finally(function(response){

                });
            };


            vm.getUserTransactions();


            vm.getTotalBitcoinAmount = function () {
                return vm.loggedInUser.totalBitcoinAmount;
            };


        }]
});
