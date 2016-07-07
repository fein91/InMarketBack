angular
    .module('inmarket.transHistoryService', [])
    .service('transHistoryService', function ($http) {
        this.getAll = function () {
            var url = "json/transh_history_data.json";
            console.log("get request produced: " + url);
            return $http.get(url);
        }

        this.getTransactionHistory = function (counterpartyId) {
            var url = "counterparties/" + counterpartyId + "/transactionHistory";
            console.log("get request produced: " + url);
            return $http.get(url);
        }
    });