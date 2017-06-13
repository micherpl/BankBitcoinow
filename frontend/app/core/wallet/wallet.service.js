'use strict';

angular.module('core.wallet').factory('Wallet', ['$resource',
    function ($resource) {
        return $resource('../../mock-data/:walletId.json', {}, {
            query: {
                method: 'GET',
                params: {walletId: 'wallets'},
                isArray: true
            }
        });
    }
]);


/**
 * Created by Jakub on 23.04.2017.
 */
