'use strict';

angular.module('registration').component('registration', {
    templateUrl: 'components/registration/registration.template.html',
    controller:  function RegistrationController($http) {


        var vm = this;

        vm.user = {
            email: "",
            password: ""
        };

        vm.register = function () {


            alert(3);
            var data = {
                email: vm.user.email,
                password: vm.user.password
            };

            var config = {
                headers: {
                    'Content-Type': 'application/json'
                }
            };


            $http.get('http://localhost:8080/registration').success(function (data) {
                alert(1);

            }).error(function (data) {
                alert(2);
            });
        };
    },
    controllerAs: "registrationCtrl"


});
