angular.module('inmarket.invoices', []).factory('invoices', function () {
    var invoices = {};

    invoices.buyerInvoices = [];
    invoices.supplierInvoices = [];
    invoices.buyerInvoicesCheckboxes = {'checked': false, invoices: {}};
    invoices.supplierInvoicesCheckboxes = {'checked': false, invoices: {}};

    invoices.addBuyerInvoice = function (invoice) {
        invoices.buyerInvoices.push(invoice);
    };

    invoices.addSupplierInvoice = function (invoice) {
        invoices.supplierInvoices.push(invoice);
    };

    invoices.addAllBuyerInvoices = function (toAdd) {
        angular.forEach(toAdd, function (item) {
            invoices.buyerInvoices.push(item);
        });
    };

    invoices.getBuyerInvoices = function () {
        return invoices.buyerInvoices;
    };

    invoices.addAllSupplierInvoices = function (toAdd) {
        angular.forEach(toAdd, function (item) {
            invoices.supplierInvoices.push(item);
        });
    };

    invoices.cleanUp = function() {
        invoices.buyerInvoices = [];
        invoices.supplierInvoices = [];
        invoices.buyerInvoicesCheckboxes = {'checked': false, invoices: {}};
        invoices.supplierInvoicesCheckboxes = {'checked': false, invoices: {}};
    };

    return invoices;
});