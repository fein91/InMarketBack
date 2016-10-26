describe('invoicesService tests', function() {

    beforeEach(module('inmarket.invoicesService'));

    var invoicesService;
    var $httpBackend;
    beforeEach(inject(function($injector) {
        $httpBackend = $injector.get('$httpBackend');
        invoicesService = $injector.get('invoicesService');
    }));

    it('checks that diff between same dates == 0', function() {
        var currentDate = new Date();
        expect(invoicesService.dateDiffInDays(currentDate, currentDate)).toEqual(0);
    });

    it('checks that diff between 30.04.2016 and 02.05.2016 == 3', function() {
        var first = new Date(2016, 4, 30);
        var second = new Date(2016, 5, 2);
        expect(invoicesService.dateDiffInDays(first, second)).toEqual(3);
    });
});