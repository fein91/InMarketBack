angular
    .module('inmarket.invoicesService', [])
    .service('invoicesService', function($http) {
        this.getAll = function() {
            var url = "json/invoices_data.json";
            console.log("get request produced: " + url);
            return $http.get(url);
        }

    });