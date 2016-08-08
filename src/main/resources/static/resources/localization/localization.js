angular.module('inmarket.localization', [])
    .filter('i18n', ['localizedTexts', function (localizedTexts) {
        return function (text) {
            if (localizedTexts.hasOwnProperty(text)) {
                return localizedTexts[text];
            }
            return text;
        };
    }]);

angular.module('inmarket.localization')
    .value('localizedTexts', {
        'LIMIT': 'Отложенная заявка',
        'MARKET': 'Рыночная сделка',
        'EXECUTED_LIMIT' : 'Исполнена отложенная заявка'
    });