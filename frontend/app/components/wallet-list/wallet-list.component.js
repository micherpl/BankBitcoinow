
'use strict';

angular.module('walletList')
    .controller('newWalletModalController', function($uibModalInstance, key, $http, $rootScope) {

        var vm = this;

        vm.newWallet = {};
        vm.isNewWalletCreated = false;
        vm.isWalletCreationPending = false;

        vm.loggedInUser = $rootScope.loggedInUser;

        vm.generateWallet = function (){

            vm.isWalletCreationPending = true;

            var data = {
                email: vm.loggedInUser.email,
                alias: vm.newWallet.alias,
                password: vm.newWallet.password
            };

            var url = window.location.protocol+"//"+window.location.hostname+":8080/createWallet";

            $http.post(url, JSON.stringify(data)).success(function(response){
                vm.newWallet.address = response.address;
                vm.newWallet.privateKey = response.privateKey;

                vm.isNewWalletCreated = true;
                vm.isWalletCreationPending = false;

            }).error(function(response){
                console.log("error while creating new wallet");
            }).finally(function(response){
                // vm.test = vm.newWallet;
            });
        };

        vm.close = function () {
            $uibModalInstance.close(vm.isNewWalletCreated);
        };

    });


angular.module('walletList').component('walletList', {
    templateUrl: 'components/wallet-list/wallet-list.template.html',
    controllerAs: "walletListCtrl",
    controller: ['$uibModal', '$rootScope', '$http', '$location',
        function WalletListController($uibModal, $rootScope, $http, $location) {

            var vm = this;

            vm.authenticated = $rootScope.authenticated;

            if(!vm.authenticated){
                $location.path("/start");
            }

            vm.wallets = [];

            vm.totalBitcoinAmount = 0;

            vm.isTransactionPrepared = false;
            vm.preparedTransactionWalletPassword = "";
            vm.isPasswordPrompt = false;

            vm.btcPrice = 2850;
            vm.loggedInUser = $rootScope.loggedInUser;

            vm.isLoading = true;
            vm.isNewWalletModalOpened = false;

            vm.getUserWallets = function(){

                vm.isLoading = true;

                vm.wallets = [];
                var data = {
                    email: vm.loggedInUser.email
                };

                var url = window.location.protocol+"//"+window.location.hostname+":8080/getUserAddresses";

                $http.post(url, JSON.stringify(data)).success(function(response){

                    for(var i = 0; i < response.length; i++){
                        vm.wallets.push(response[i]);
                    }
                    vm.selectedWallet = vm.wallets[0];
                    vm.isLoading = false;
                    $rootScope.totalBitcoinAmount = vm.getTotalBitcoinAmount();

                    vm.totalBitcoinAmount = $rootScope.totalBitcoinAmount;
                }).error(function(response){
                    console.log("error while getting user addresses");
                });
            };

            vm.getUserWallets();

            var key = 1000;

            vm.createNewWalletModal = function() {
                vm.modalInstance = $uibModal.open({
                    controller: 'newWalletModalController as newWalletModalCtrl',
                    templateUrl: 'components/wallet-list/modal.html',
                    windowClass: 'center-modal',
                    resolve: {
                        key: function() {
                            return key;
                        }
                    }
                });
                vm.modalInstance.result.then(function(_isNewWalletCreated, _newWallet) {
                    if (_isNewWalletCreated === true) {
                        vm.getUserWallets();
                        // alert(_newWallet.address);
                        // vm.selectedWallet = _newWallet;
                    }
                });

                vm.modalInstance.opened.then(function () {
                    vm.isNewWalletModalOpened = true;
                });

                vm.modalInstance.result.finally(function () {
                    vm.isNewWalletModalOpened = false;
                });
            };

            vm.getTotalBitcoinAmount = function(){
                var totalBitcoinAmount = 0;
                for(var i = 0; i < vm.wallets.length; i++){
                    totalBitcoinAmount += vm.wallets[i].balance;
                }
                return totalBitcoinAmount;
            };


            vm.totalBitcoinAmount = $rootScope.totalBitcoinAmount;


            vm.amountToSend = 0;
            vm.destinationAddress = "";

            vm.deleteWallet = function(){

                var data = {
                    id : vm.selectedWallet.id.toString()
                };

                var url = window.location.protocol+"//"+window.location.hostname+":8080/deleteWallet";

                $http.post(url, JSON.stringify(data)).success(function(response){
                   alert("Wallet deleted");
                    vm.getUserWallets();
                }).error(function(response){
                    console.log("error while deleting wallet");
                });
            };




            vm.generateTransaction = function (){
                vm.isTransactionPrepared = false;

                vm.sourceAddress = vm.selectedWallet.address;

                var data = {
                    from : vm.sourceAddress,
                    to : vm.destinationAddress,
                    amount : vm.amountToSend
                };

                var url = window.location.protocol+"//"+window.location.hostname+":8080/prepareTransaction";

                $http.post(url, JSON.stringify(data)).success(function(response){

                    vm.isTransactionPrepared = true;
                    vm.preparedTransactionFee = response.fee;
                    vm.preparedTransactionId = response.id;
                }).error(function(response){
                    console.log("error while generating transaction");
                    console.log(response.message);
                    alert(response.message);
                });
            };


            vm.sendTransaction = function (){

                var data = {
                    id : vm.preparedTransactionId,
                    password : vm.preparedTransactionWalletPassword
                };

                var url = window.location.protocol+"//"+window.location.hostname+":8080/signTransaction";

                $http.post(url, JSON.stringify(data)).success(function(response){
                    vm.isTransactionPrepared = false;
                    vm.isPasswordPrompt = false;
                    vm.preparedTransactionWalletPassword = "";
                }).error(function(response){
                    console.log("error while signing transaction");
                    console.log(response.message);
                    alert(response.message);
                });
            };


        }
    ]

});