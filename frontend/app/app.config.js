'use strict';

angular.
module('App').
config(['$locationProvider' ,'$routeProvider', '$httpProvider',
    function config($locationProvider, $routeProvider, $httpProvider) {
	    $httpProvider.defaults.withCredentials = true;

        $locationProvider.hashPrefix('!');

        // $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

        $routeProvider.
        when('/wallets', {
            template: '<wallet-list></wallet-list>'
        }).
        when('/start', {
            template: '<start-page></start-page>',
        }).
        when('/registration', {
            template: '<registration></registration>'
        }).
        when('/transactions', {
            template: '<transaction-list></transaction-list>'
        }).
        when('/contacts', {
            template: '<contacts></contacts>'
        }).
        when('/wallets/:walletId', {
            template: '<wallet-detail></wallet-detail>'
        }).
        otherwise('/start');
    }
]);

