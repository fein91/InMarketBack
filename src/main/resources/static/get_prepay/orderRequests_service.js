angular
    .module('inmarket.orderRequestsService', [])
    .service('orderRequestsService', function($http) {
        this.process = function(orderRequest) {
            var url = "/orderRequests/process";
            console.log("post request produced: " + url);
            console.log("body: " + JSON.stringify(orderRequest));
            return $http.post(url, orderRequest);
        }

        this.getOrderRequests = function(counterpartyId) {
            var url = "/orderRequests?counterpartyId=" + counterpartyId;
            console.log("post request produced: " + url);
            return $http.get(url);
        }
    });