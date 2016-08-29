angular
    .module('inmarket.invoicesService', [])
    .service('invoicesService', function ($http) {
        this.getAll = function (counterpartyId) {
            var url = "counterparties/" + counterpartyId + "/invoices";
            console.log("get request produced: " + url);
            return $http.get(url);
        };

        this.getBySourceId = function (sourceId) {
            var url = "counterparties/" + sourceId + "/invoicesBySource";
            console.log("get request produced: " + url);
            return $http.get(url);
        };

        this.getByTargetId = function (targetId) {
            var url = "counterparties/" + targetId + "/invoicesByTarget";
            console.log("get request produced: " + url);
            return $http.get(url);
        };

        this.calculateAvgDaysToPayment = function (invoices) {
            var daysMultUnpaidValueSum = 0;
            var unpaidValuesSum = 0;
            angular.forEach(invoices, function (item) {
                var unpaidValue = item.value - item.prepaidValue;
                daysMultUnpaidValueSum += item.daysToPayment * unpaidValue;
                unpaidValuesSum += unpaidValue;
            });
            return daysMultUnpaidValueSum / unpaidValuesSum;
        };

        this.import = function (counterpartyId, files) {
            var fd = new FormData();
            //Take the first selected file
            fd.append("file", files[0]);

            return $http.post('counterparties/' + counterpartyId + '/importInvoices', fd, {
                withCredentials: true,
                headers: {'Content-Type': undefined},
                transformRequest: angular.identity
            });
        };

        this.exportBySource = function(counterpartyId) {
            return $http({
                method: 'POST',
                url: 'counterparties/' + counterpartyId + '/exportInvoicesBySource',
                headers: {'Content-Type': 'text/csv'}
            });
        };

        this.exportByTarget = function(counterpartyId) {
            return $http({
                method: 'POST',
                url: 'counterparties/' + counterpartyId + '/exportInvoicesByTarget',
                headers: {'Content-Type': 'text/csv'}
            });
        };

        var _MS_PER_DAY = 1000 * 60 * 60 * 24;
        this.dateDiffInDays =  function (a, b) {
            // Discard the time and time-zone information.
            var utc1 = Date.UTC(a.getFullYear(), a.getMonth(), a.getDate());
            var utc2 = Date.UTC(b.getFullYear(), b.getMonth(), b.getDate());

            return Math.floor((utc2 - utc1) / _MS_PER_DAY);
        };

    });