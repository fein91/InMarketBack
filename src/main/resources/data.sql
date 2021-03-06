-- DROP TABLE counterparty;
-- CREATE TABLE counterparty (id int, name varchar(255), PRIMARY KEY (id));
--
-- DROP TABLE invoices;
-- CREATE TABLE invoices (id_from int, id_to int, value DECIMAL(18, 2), PRIMARY KEY(id_from, id_to));
--
-- DROP TABLE orderbook;
-- CREATE TABLE orderbook (id int, counterparty_fk int, price DECIMAL(18, 2), quantity DECIMAL(18, 2), date DATE);

INSERT INTO counterparty(id, name, login)
values (11, 'test', 'test');
INSERT INTO counterparty(id, name, login)
values (12, 'buyer1', 'buyer1');
INSERT INTO counterparty(id, name, login)
values (13, 'buyer2', 'buyer2');
INSERT INTO counterparty(id, name)
values (14, 'buyer3');

INSERT INTO counterparty(id, name, login)
values (21, 'supplier1', 'supplier1');
INSERT INTO counterparty(id, name, login)
values (22, 'supplier2', 'supplier2');
INSERT INTO counterparty(id, name, login)
values (23, 'supplier3', 'supplier3');
INSERT INTO counterparty(id, name, login)
values (24, 'supplier4', 'supplier4');
INSERT INTO counterparty(id, name, login)
values (25, 'supplier5', 'supplier5');

--
-- insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_value, payment_date)
-- values             (1111,  11,     12,      200, 0,  to_date('29-07-16', 'DD-MM-YY'));
-- insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_value, payment_date)
-- values             (1112,   11,    13,      150, 0,  to_date('30-07-16', 'DD-MM-YY'));
-- insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_value, payment_date)
-- values             (1113,   11,    14,      100, 0,  to_date('28-07-16', 'DD-MM-YY'));
--
-- insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_value, payment_date)
-- values             (1114,   21,    11,      300, 0,  to_date('27-07-16', 'DD-MM-YY'));
-- insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_value, payment_date)
-- values             (1115,   22,    11,      100, 0,  to_date('26-07-16', 'DD-MM-YY'));
-- insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_value, payment_date)
-- values             (1116,   23,    11,      250, 0,  to_date('25-07-16', 'DD-MM-YY'));
-- insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_value, payment_date)
-- values             (1117,   24,    11,      100, 0,  to_date('29-07-16', 'DD-MM-YY'));
--
-- -- 0 ASK 1 BID 0 LIMIT 1 MARKET
-- insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
-- values                  (111, 21,              15,    200,     SYSDATE, 0,         1);
-- insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
-- values                  (112, 22,              14,    50,     SYSDATE, 0,         1);
-- insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
-- values                  (113, 23,              13,    150,     SYSDATE, 0,         1);
-- insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
-- values                  (114, 24,              12,    50,     SYSDATE, 0,         1);
--
-- insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
-- values                  (115, 12,              19,    150,     SYSDATE, 0,         0);
-- insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
-- values                  (116, 13,              18,    100,     SYSDATE, 0,         0);

-- insert into history_order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
-- values                           (111, 11,              15,    200,     SYSDATE, 0,         1);

-- insert into history_trade(id, invoice_fk, processingOrderRequestFk, quantity, discountValue)
-- values (111111, 1114, 111, 100, 5);
