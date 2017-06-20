'use strict';

angular.module('registration').component('registration', {
    templateUrl: 'components/registration/registration.template.html',
    controller:  function RegistrationController($http, $location, $rootScope) {


        var vm = this;

        vm.email = "";
        vm.password = "";
        vm.serverErrors = {};

        vm.register = function() {
            vm.serverErrors = {};

            var data = {
                email: vm.email,
                password: vm.password
            };

            $http.post(window.location.protocol+'//'+window.location.hostname+':8080/registration', data).success(function(data){
                $rootScope.isNewAccountRegistered = true;
                $rootScope.newAccountEmail = vm.email;
                $location.path('/start');
            }).error(function(data){
                if (data.errors) {
                    for (var fieldName in data.errors.fields) {
                        vm.serverErrors[fieldName] = data.errors.fields[fieldName];
                    }
                } else {
                    console.log('Unknown response:', data);
                    alert('Unexpected error. Please check logs or try again later.');
                }
            });
        }
    },
    controllerAs: "registrationCtrl"


});
