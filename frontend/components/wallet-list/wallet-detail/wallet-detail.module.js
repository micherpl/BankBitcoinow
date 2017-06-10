'use strict';

angular.module('walletDetail', [
  'ngRoute',
  'core.wallet'
]).config(['$locationProvider' ,'$routeProvider',
    function config($locationProvider, $routeProvider) {

        $routeProvider.
        // when('/wallets', {
        //     template: '<wallet-list></wallet-list>'
        // }).
        // when('/start', {
        //     template: '<start-page></start-page>'
        // }).
        // when('/test', {
        //     template: 'helllosdddddddddddddddddddddddddddddddddddddddddddd'
        // }).
        // when('/transactions', {
        //     template: '<transaction-list></transaction-list>'
        // }).
        when('/wallets/:walletId', {
            template: '<wallet-detail></wallet-detail>'
        }).
        otherwise('/start');
    }
]);;
