'use strict';

angular.module('registration').component('registration', {
    templateUrl: 'components/registration/registration.template.html',
    controller:  function RegistrationController($http, $location) {
	    var ctrl = this;

	    ctrl.email = "";
	    ctrl.password = "";
	    ctrl.serverErrors = {};

        ctrl.register = function() {
	        ctrl.serverErrors = {};

            var data = {
                email: ctrl.email,
                password: ctrl.password
            };

            $http.post('http://localhost:8080/registration', data).success(function(data){
	            $location.path('start');
            }).error(function(data){
                if (data.errors) {
	                for (var fieldName in data.errors.fields) {
		                ctrl.serverErrors[fieldName] = data.errors.fields[fieldName];
	                }
                } else {
                	console.log('Unknown response:', data);
                    alert('Unexpected error. Please check logs or try again later.');
                }
            });

        };
}


});
