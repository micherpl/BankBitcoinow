'use strict';

angular.module('transactionList').component('transactionList', {
    templateUrl: 'components/transaction-list/transaction-list.template.html',
    controllerAs: "txCtrl",
    controller: ['Wallet',
        function TransactionListController(Wallet) {



            var vm = this;
            // this.transactions = Transactions.query();


            this.wallets = Wallet.query();
            this.loggedInUser = {
                "firstName": "Jakub",
                "lastName": "Słowik"
            };

            this.transactions =
                [
                    {
                        "id": "1",
                        "tx_hash": "1e7d1b6a82fa07924195f13f20206f7240ba2d9002aa77288049ef26802cfa51",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.05,
                        "created_at": 1494403664,
                        "status": "Unconfirmed",
                        "confirmations": 0
                    },
                    {
                        "id": "2",
                        "tx_hash": "1e7d1b6a82fa07924195f13f20206f7240ba2d9002aa77288049ef26802cfa51",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.015,
                        "created_at": 1494403664,
                        "status": "Signed",
                        "confirmations": 0
                    },
                    {
                        "id": "3",
                        "tx_hash": "1e7d1b6a82fa07924195f13f20206f7240ba2d9002aa77288049ef26802cfa51",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.0495,
                        "created_at": 1494403664,
                        "status": "Signed",
                        "confirmations": 0
                    },
                    {
                        "id": "4",
                        "tx_hash": "1e7d1b6a82fa07924195f13f20206f7240ba2d9002aa77288049ef26802cfa51",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.025,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 14
                    },
                    {
                        "id": "5",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.015,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 15
                    },
                    {
                        "id": "6",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.0495,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 44
                    },{
                    "id": "7",
                    "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                    "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                    "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                    "amount": 0.0223,
                    "created_at": 1494403664,
                    "status": "Confirmed",
                    "confirmations": 56
                },
                    {
                        "id": "8",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.021,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 78
                    },
                    {
                        "id": "9",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.3,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 101
                    },
                    {
                        "id": "10",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.12,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 222
                    },
                    {
                        "id": "5",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.56,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 223
                    },
                    {
                        "id": "11",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.032,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 226
                    },{
                    "id": "12",
                    "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                    "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                    "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                    "amount": 1.1,
                    "created_at": 1494403664,
                    "status": "Confirmed",
                    "confirmations": 331
                },
                    {
                        "id": "13",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.25,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 532
                    },
                    {
                        "id": "14",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.64,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 631
                    },
                    {
                        "id": "15",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.18,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 642
                    },
                    {
                        "id": "16",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 2.11,
                        "created_at": 1494403664,
                        "status": "Unconfirmed",
                        "confirmations": 0
                    },
                    {
                        "id": "17",
                        "tx_hash": "0xeb699ebcbbbf026489a6e19b41d03080a657bf77ab638fde7470fd0124202ade",
                        "source_address": "1EdVuraZCVhS2zi7eAevVtpHvPQtHtWdAR",
                        "destination_address": "15gzCLV3QmzoGityuFQectEDpg7iP5ZNpo",
                        "amount": 0.8,
                        "created_at": 1494403664,
                        "status": "Confirmed",
                        "confirmations": 1234
                    }
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
