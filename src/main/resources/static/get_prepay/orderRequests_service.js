angular
    .module('inmarket.orderRequestsService', [])
    .service('orderRequestsService', function($http) {
        this.process = function(orderRequest) {
            var url = "/orderRequest/process";
            console.log("post request produced: " + url);
            console.log("body: " + JSON.stringify(orderRequest));
            return $http.post(url, orderRequest);
        }
    });