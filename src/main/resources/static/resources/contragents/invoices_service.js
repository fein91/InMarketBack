angular
    .module('inmarket.invoicesService', [])
    .service('invoicesService', function ($http) {
        this.getAll = function () {
            var url = "json/invoices_data.json";
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
                daysMultUnpaidValueSum += item.daysToPayment * item.unpaidValue;
                unpaidValuesSum += item.unpaidValue;
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
        }

        var _MS_PER_DAY = 1000 * 60 * 60 * 24;
        this.dateDiffInDays =  function (a, b) {
            // Discard the time and time-zone information.
            var utc1 = Date.UTC(a.getFullYear(), a.getMonth(), a.getDate());
            var utc2 = Date.UTC(b.getFullYear(), b.getMonth(), b.getDate());

            return Math.floor((utc2 - utc1) / _MS_PER_DAY);
        };

    });