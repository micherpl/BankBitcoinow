// 'use strict';
//
// // Register `phoneDetail` component, along with its associated controller and template
// angular.
//   module('walletDetail').
//   component('walletDetail', {
//     templateUrl: 'components/wallet-detail/wallet-list.template.html',
//     controller: ['$routeParams', 'Wallet',
//       function WalletDetailController($routeParams, Wallet) {
//         var self = this;
//         self.wallet = Wallet.get({walletId: $routeParams.walletId}, function(wallet) {
//           // self.setImage(phone.images[0]);
//         });
//
//         // self.setImage = function setImage(imageUrl) {
//         //   self.mainImageUrl = imageUrl;
//         // };
//       }
//     ]
//   });



'use strict';

angular.module('walletList')
    .controller('newWalletModalController', function($uibModalInstance, key, $http, $rootScope) {

        var vm = this;

        vm.newWallet = {};
        vm.isNewWalletCreated = false;
        vm.isWalletCreationPending = false;

        vm.generateWallet = function (){

            vm.isWalletCreationPending = true;

            var data = {
                email: "test@mail.com",
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
                alert(2);
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
    controller: ['Wallet', '$uibModal', '$rootScope', '$http',
        function WalletListController(Wallet, $uibModal, $rootScope, $http) {

            var vm = this;

            vm.wallets = [];
            vm.loggedInUser = $rootScope.loggedInUser;

            vm.isLoading = true;
            vm.isNewWalletModalOpened = false;

            vm.getUserWallets = function(){

                vm.isLoading = true;

                vm.wallets = [];
                var data = {
                    email: "test@mail.com"
                };

                var url = window.location.protocol+"//"+window.location.hostname+":8080/getUserAddresses";

                $http.post(url, JSON.stringify(data)).success(function(response){

                    for(var i = 0; i < response.length; i++){
                        vm.wallets.push(response[i]);
                    }
                    vm.selectedWallet = vm.wallets[0];

                    vm.isLoading = false;
                }).error(function(response){
                    alert(2);
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
                var totalBitcoinAmmount = 0;
                for(var i = 0; i < vm.wallets.length; i++){
                    totalBitcoinAmmount += vm.wallets[i].balance;
                }
                return totalBitcoinAmmount;
            };

            vm.getTotalBitcoinAmount();

            vm.ammountToSend = 0;
            vm.btcPrice = 2900;



            vm.deleteWallet = function(){

                var data = {
                    id : vm.selectedWallet.id.toString()
                };

                var url = window.location.protocol+"//"+window.location.hostname+":8080/deleteWallet";

                $http.post(url, JSON.stringify(data)).success(function(response){
                   alert("wallet deleted");
                    vm.getUserWallets();
                }).error(function(response){
                    alert(2);
                });
            };

        }
    ]

});