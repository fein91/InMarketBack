describe('sorting the list of users', function() {

    var invoicesService;
    var $httpBackend;
    beforeEach(inject(function($injector) {
        var $injector = angular.injector([ 'inmarket.invoicesService']);
        $httpBackend = $injector.get('$httpBackend');
        invoicesService = $injector.get('invoicesService');
    }));



    iit('sorts in descending order by default', function() {
        expect(invoicesService.dateDiffInDays(new Date, new Date())).toEqual(0);
    });
});