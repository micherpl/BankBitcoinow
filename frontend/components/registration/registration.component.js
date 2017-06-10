'use strict';

angular.module('registration').component('registration', {
    templateUrl: 'components/registration/registration.template.html',
    controller:  function RegistrationController($http) {
        this.username = "";
        this.password = "";

        var vm = this;

        vm.register = function() {

            // var data = {
            //     username: this.username,
            //     password: this.password
            // };
            //
            // var config = {
            //     headers : {
            //         'Content-Type': 'application/json'
            //     }
            // };


            // $http.get('http://localhost:8080/registration').success(function(data){
            //     alert(1);
            //     vm.ResponseDetails = data;
            //
            // }).error(function(data){
            //     alert(2);
            //     vm.ResponseDetails = data.data;
            // });

        };
}


});
