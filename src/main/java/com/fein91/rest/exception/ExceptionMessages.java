package com.fein91.rest.exception;

public enum ExceptionMessages {
    ONLY_LIMIT_ORDER_REQUEST_CAN_BE_UPDATED("Only limit order request can be updated",
            "Только лимитная заявка может быть редактирована"),
    ORDER_REQUEST_COUNTERPARTY_ISNT_FILLED("Order request counterparty isn't filled",
            "В заявке не указан контрагент"),
    ORDER_REQUEST_QUANTITY_ISNT_FILLED("Order request quantity isn't filled",
            "В заявке не указан объем"),
    ORDER_REQUEST_QUANTITY_INCORRECT_VALUE("Order request quantity incorrect value: %s",
            "В заявке указан не корректный объем: %s"),
    ORDER_REQUEST_TYPE_ISNT_FILLED("Order request type isn't filled",
            "В заявке указан не корректный тип сделки"),
    ORDER_REQUEST_SIDE_ISNT_FILLED("Order request side isn't filled",
            "В заявке указана не корректная сторона сделки"),
    LIMIT_ORDER_REQUEST_PRICE_ISNT_FILLED("Limit order request price isn't filled",
            "В заявке не указана цена"),
    LIMIT_ORDER_REQUEST_PRICE_INCORRECT_VALUE("Limit order request price incorrect value: %s",
            "В заявке указана не корректная цена: %s"),
    SUPPLIERS_ORDERS_SUM_NO_ENOUGH("Requested order quantity: %s cannot be satisfied. " +
            "Available order quantity: %s. " +
            "Please process unsatisfied quantity: %s as limit order.",
            "На запрашиваемую сумму %s невозможно осуществить рыночную сделку. \n" +
                    "Ваши поставщики готовы получить предоплаты только на сумму=%s грн.\n" +
                    "Воспользуйтесь отложенной заявкой на остаток %s грн."),
    BUYERS_ORDERS_SUM_NO_ENOUGH("Requested order quantity: %s cannot be satisfied. " +
            "Available order quantity: %s. " +
            "Please process unsatisfied quantity: %s as limit order.",
            "На запрашиваемую сумму %s невозможно осуществить рыночную сделку. \n" +
                    "Ваши покупатели готовы сделать предоплаты только на сумму=%s грн.\n" +
                    "Воспользуйтесь отложенной заявкой на остаток %s грн."),
    REQUESTED_ORDER_QUANTITY_IS_GREATER_THAN_AVAILABLE_QUANTITY("Requested order quantity: %s is greater than available quantity = invoices - discounts: %s",
            "Заявка не может быть размещена.\n" +
                    "Запрашиваемая сумма: %s превышает сумму инвойсов с учетом скидок. \n" +
                    "Максимальная доступная сумма: %s"),
    NO_BUYER_INVOICES_WERE_FOUND_WHILE_PROCESSING_ORDER_REQUEST("No buyer invoices were found while processing order request",
            "У вас нет покупателей чтобы заказать предоплату."),
    NO_SUPPLIER_INVOICES_WERE_FOUND_WHILE_PROCESSING_ORDER_REQUEST("No supplier invoices were found while processing order request",
            "У вас нет поставщиков чтобы получить предоплату."),
    NO_SUITABLE_ORDER_REQUESTS_WERE_FOUND("No suitable order requests were found",
            "В данный момент нет контрагентов, желающих провести рыночную сделку. Воспользуйтесь отложенной заявкой."),
    EXCEPTION_WHILE_IMPORT_OCCURRED("Exception while import occurred",
            "Произошла ошибка во время импорта. Проверьте пожалуйста входные данные."),

    ;

    private String message;
    private String localizedMessage;

    ExceptionMessages(String message, String localizedMessage) {
        this.message = message;
        this.localizedMessage = localizedMessage;
    }

    public String getMessage() {
        return message;
    }

    public String getLocalizedMessage() {
        return localizedMessage;
    }
}
