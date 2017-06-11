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
    .controller('newWalletModalController', function($uibModalInstance, key, $http) {


        var vm = this;

        vm.newWallet = {};

        vm.featureName = key;



        vm.generateWallet = function (){

            var parameter = {
                email : "ddd@ddd.pl",
                password: vm.newWallet.password
            };

            var url = "http://localhost:8080/generuj_adres";

            $http.post(url, JSON.stringify(parameter),{headers: {'Content-type' : 'application/json'}}).success(function(data){
                alert(1);
            }).error(function(data){
                alert(2);
            });


        };

        vm.close = function () {
            $uibModalInstance.close('yes');
        };

    });


angular.module('walletList').component('walletList', {
    templateUrl: 'components/wallet-list/wallet-list.template.html',
    controllerAs: "walletListCtrl",
    controller: ['Wallet', '$uibModal',
        function WalletListController(Wallet, $uibModal) {

            var vm = this;

            vm.test = 1111;

            vm.message = 'It works!';

            var key = 1000;

            vm.modal = function() {
                var modalInstance = $uibModal.open({
                    controller: 'newWalletModalController as newWalletModalCtrl',
                    templateUrl: 'components/wallet-list/modal.html',
                    windowClass: 'center-modal',
                    resolve: {
                        key: function() {
                            return key;
                        }
                    }
                });
                modalInstance.result.then(function(optionSelected) {
                    if (optionSelected === 'yes') {
                        console.log("Yes selected!")
                    }
                })
            };




            vm.wallets = Wallet.query();
            // this.orderProp = 'age';

            // this.transactions = Transactions.query();


            vm.loggedInUser = {
                "firstName": "Jakub",
                "lastName": "Słowik"
            };

            vm.getTotalBitcoinAmmount = function(){
                var totalBitcoinAmmount = 0;
                for(var wallet in vm.wallets){
                    totalBitcoinAmmount += wallet.balance;
                }
                return totalBitcoinAmmount;
            };

            vm.test = "tesst";
            vm.ammountToSend = 0;
            vm.btcPrice = 1240;

            vm.selectedWallet = {
                "id": 0,
                "balance": "0",
                "name": "",
                "address": "Please select your wallet"
            };

            vm.isRemoveSignVisible = false;

            vm.showRemoveSign = function () {
                vm.isRemoveSignVisible = true;
            };

            vm.hideRemoveSign = function () {
                vm.isRemoveSignVisible = false;
            };

            vm.deleteWallet = function (wallet) {
                var index = vm.wallets.indexOf(wallet);
                vm.wallets.splice(index);
            }

            vm.createNewWallet = function () {


                // var newWallet = {
                //     "id": "",
                //     "name": "New wallet",
                //     "balance": 0,
                //     "address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR"
                // };
                // vm.wallets.push(newWallet);
            }
        }
    ]

});