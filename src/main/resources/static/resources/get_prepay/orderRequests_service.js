angular
    .module('inmarket.orderRequestsService', [])
    .service('orderRequestsService', function ($http, $rootScope, $location, invoices) {
        this.process = function (orderRequest) {
            var url = "/orderRequests/process";
            console.log("post request produced: " + url);
            console.log("body: " + JSON.stringify(orderRequest));
            return $http.post(url, orderRequest);
        };

        this.calculate = function (orderRequest) {
            var url = "/orderRequests/calculate";
            console.log("post request produced: " + url);
            console.log("body: " + JSON.stringify(orderRequest));
            return $http.post(url, orderRequest);
        };

        this.getOrderRequests = function (counterpartyId) {
            var url = "/counterparties/" + counterpartyId + "/orderRequests";
            console.log("get request produced: " + url);
            return $http.get(url);
        };

        this.getById = function(orderId) {
            var url = "/orderRequests?orderId=" + orderId;
            console.log("get request produced: " + url);
            return $http.get(url);
        };

        this.submitOrder = function (orderRequest) {
            if (orderRequest.quantity) {
                this.process(orderRequest)
                    .then(function successCallback(response) {
                        var orderResult = response.data;
                        console.log('order result: ' + JSON.stringify(orderResult));

                        invoices.cleanUp();
                        $location.path("/contragents");

                        //$rootScope.$broadcast('buyerProposalToChangeEvent', invoices.buyerInvoicesCheckboxes.invoices);
                        //$rootScope.$broadcast('supplierProposalToChangeEvent', invoices.supplierInvoicesCheckboxes.invoices);

                    }, function errorCallback(response) {
                        console.log('got ' + response.status + ' error');
                    });
            }
        };

        this.updateOrder = function(orderRequest) {
            var url = "/orderRequests/update";
            console.log("post request produced: " + url);
            console.log("body: " + JSON.stringify(orderRequest));
            return $http.put(url, orderRequest);
        };

        this.removeOrder = function(orderId) {
            var url = "/orderRequests?orderId=" + orderId;
            console.log("delete request produced: " + url);
            return $http.delete(url);
        };
    });