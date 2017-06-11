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

angular.module('walletDetail').component('walletDetail', {
    templateUrl: 'components/wallet-list/wallet-detail/wallet-detail.template.html',
    controllerAs: "walletDetailCtrl",
    controller: ['Wallet',
        function WalletDetailController(Wallet) {

            this.wallets = Wallet.query();
            // this.orderProp = 'age';


            // this.transactions = Transactions.query();


            this.loggedInUser = {
                "firstName": "Jakub",
                "lastName": "Słowik"
            };



            this.getTotalBitcoinAmmount = function(){
                var totalBitcoinAmmount = 0;
                for(var wallet in this.wallets){
                    totalBitcoinAmmount += wallet.balance;
                }
                return totalBitcoinAmmount;
            };



            this.test = "tesst";
            this.ammountToSend = 0;
            this.btcPrice = 1240;

            this.selectedWallet = {
                "id": 0,
                "balance": "0",
                "name": "",
                "address": "Please select your wallet"
            };

            this.isRemoveSignVisible = false;

            this.showRemoveSign = function () {
                this.isRemoveSignVisible = true;
            };

            this.hideRemoveSign = function () {
                this.isRemoveSignVisible = false;
            };


            this.deleteWallet = function (wallet) {
                var index = this.wallets.indexOf(wallet);
                this.wallets.splice(index);
            }
            //
            // this.toggleRemoveSign = function() {
            //     this.isRemoveSignVisible = this.isRemoveSignVisible === false ? true: false;
            // };


            // this.togglePersonalMenu = function() {
            //     document.getElementById("myDropdown").classList.toggle("show");
            // }


            this.createNewWallet = function () {
                var newWallet = {
                    "id": "",
                    "name": "New wallet",
                    "balance": 0,
                    "address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR"
                };
                this.wallets.push(newWallet);
            }
        }
    ]
});

