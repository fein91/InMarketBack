-- DROP TABLE counterparty;
-- CREATE TABLE counterparty (id int, name varchar(255), PRIMARY KEY (id));
--
-- DROP TABLE invoices;
-- CREATE TABLE invoices (id_from int, id_to int, value DECIMAL(18, 2), PRIMARY KEY(id_from, id_to));
--
-- DROP TABLE orderbook;
-- CREATE TABLE orderbook (id int, counterparty_fk int, price DECIMAL(18, 2), quantity DECIMAL(18, 2), date DATE);

INSERT INTO counterparty
values (11, 'test');
INSERT INTO counterparty
values (12, 'buyer1');
INSERT INTO counterparty
values (13, 'buyer2');
INSERT INTO counterparty
values (14, 'buyer3');

INSERT INTO counterparty
values (21, 'supplier1');
INSERT INTO counterparty
values (22, 'supplier2');
INSERT INTO counterparty
values (23, 'supplier3');
INSERT INTO counterparty
values (24, 'supplier4');


insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_percent)
values             (1111,  11,     12,      200, 0);
insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_percent)
values             (1112,   11,    13,      150, 0);
insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_percent)
values             (1113,   11,    14,      100, 0);
insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_percent)
values             (1114,   21,    11,      300, 0);
insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_percent)
values             (1115,   22,    11,      400, 0);
insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_percent)
values             (1116,   23,    11,      250, 0);
insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_percent)
values             (1117,   24,    11,      100, 0);

-- 0 ASK 1 BID 0 LIMIT 1 MARKET
insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
values                  (111, 21,              15,    200,     SYSDATE, 0,         1);
insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
values                  (112, 22,              14,    150,     SYSDATE, 0,         1);
insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
values                  (113, 23,              13,    150,     SYSDATE, 0,         1);
insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
values                  (114, 24,              12,    100,     SYSDATE, 0,         1);

