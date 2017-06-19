'use strict';

angular.module('contacts').component('contacts', {
    templateUrl: 'components/contacts/contacts.template.html',
    controllerAs: "contactsCtrl",
    controller: ['$rootScope', '$location',
        function ContactsController($rootScope, $location) {


            var vm = this;

            vm.authenticated = $rootScope.authenticated;

            if(!vm.authenticated){
                $location.path("/start");
            };

            vm.btcPrice = 2850;
            vm.totalBitcoinAmount = $rootScope.totalBitcoinAmount?$rootScope.totalBitcoinAmount:0;
            vm.loggedInUser = {
                email: $rootScope.loggedInUser.email?$rootScope.loggedInUser.email:""
            };

            vm.contacts =
                [
                    {
                        "id": "1",
                        "name": "Kantor",
                        "address": "mhcxhd6pEEGNojxm6EYsoyp7JEkhoTvK9t"
                    },
                    {
                        "id": "2",
                        "name": "Michał Herman",
                        "address": "mgHFjNEUh2SJb5VZaC9csjT4Dd6MuGuki3"
                    },
                    {
                        "id": "3",
                        "name": "Paweł Kozioł",
                        "address": "mkrVYTRXBHucW1z9T1enEs7rcuVdm96Wyw"
                    },
                    {
                        "id": "4",
                        "name": "Satoshi Nakamoto",
                        "address": "n1AXxNPAUoGpSTbNjCFnpsmod5XVMGWxrj"
                    }
                ];

        }]
});
