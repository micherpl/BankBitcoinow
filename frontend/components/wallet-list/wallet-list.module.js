'use strict';

angular.module('walletList', [
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
        //     template: 'hellloo'
        // }).
        // when('/transactions', {
        //     template: '<transaction-list></transaction-list>'
        // }).
        when('/wallets/', {
            template: '<wallet-detail></wallet-detail>'
        }).
        otherwise('/start');
    }
]);;
