angular.module('inmarket.invoices', []).factory('invoices', function(){
    var invoices = {};

    invoices.buyerInvoices = [];
    invoices.supplierInvoices = [];

    invoices.addBuyerInvoice = function(invoice){
        invoices.buyerInvoices.push(invoice);
    };

    invoices.addSupplierInvoice = function(invoice){
        invoices.supplierInvoices.push(invoice);
    };

    invoices.addAllBuyerInvoices = function(toAdd){
        angular.forEach(toAdd, function(item) {
            invoices.buyerInvoices.push(item);
        });
    };

    invoices.addAllSupplierInvoices = function(toAdd){
        angular.forEach(toAdd, function(item) {
            invoices.supplierInvoices.push(item);
        });
    };

    return invoices;
});