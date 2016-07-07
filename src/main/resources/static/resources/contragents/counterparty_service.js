angular
    .module('inmarket.counterpartyService', [])
    .service('counterpartyService', function ($http) {
        this.calculateProposals = function (counterpartyId) {
            var url = "counterparties/" + counterpartyId + "/calculateProposals";
            console.log("get request produced: " + url);
            return $http.get(url);
        }
    });