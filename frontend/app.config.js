'use strict';

angular.
  module('App').
  config(['$locationProvider' ,'$routeProvider',
    function config($locationProvider, $routeProvider) {
      $locationProvider.hashPrefix('!');

      $routeProvider.
        when('/wallets', {
          template: '<wallet-list></wallet-list>'
        }).
        when('/start', {
          template: '<start-page></start-page>'
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

