'use strict';

angular.module('contacts').component('contacts', {
    templateUrl: 'components/contacts/contacts.template.html',
    controllerAs: "contactsCtrl",
    controller: ['Wallet',
        function ContactsController(Wallet) {


            // this.transactions = Transactions.query();


            // this.wallets = Wallet.query();
            this.loggedInUser = {
                "firstName": "Jakub",
                "lastName": "Słowik"
            };

            this.contacts =
                [
                    {
                        "id": "1",
                        "name": "Kantor",
                        "address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR"
                    },
                    {
                        "id": "2",
                        "name": "Michał Herman",
                        "address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR"
                    },
                    {
                        "id": "3",
                        "name": "Paweł Kozioł",
                        "address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR"
                    },
                    {
                        "id": "4",
                        "name": "Satoshi Nakamoto",
                        "address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR"
                    },
                ];

            this.getTotalBitcoinAmmount = function () {
                var totalBitcoinAmmount;
                for (var wallet in this.wallets) {
                    totalBitcoinAmmount += wallet.balance;
                }
                return totalBitcoinAmmount;
            };


        }]
});
