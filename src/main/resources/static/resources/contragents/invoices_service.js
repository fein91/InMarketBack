angular
    .module('inmarket.invoicesService', [])
    .service('invoicesService', function ($http) {
        this.getAll = function () {
            var url = "json/invoices_data.json";
            console.log("get request produced: " + url);
            return $http.get(url);
        }

        this.getBySourceId = function (sourceId) {
            var url = "counterparties/" + sourceId + "/invoicesBySource";
            console.log("get request produced: " + url);
            return $http.get(url);
        }

        this.getByTargetId = function (targetId) {
            var url = "counterparties/" + targetId + "/invoicesByTarget";
            console.log("get request produced: " + url);
            return $http.get(url);
        }

    });