'use strict';

// Register `phoneList` component, along with its associated controller and template
angular.module('startPage').component('startPage', {
    templateUrl: 'components/start-page/start-page.template.html',
    controller: function StartPageController() {

            this.loginRequest = function(){


                alert(this.password);
            }
        }
});
