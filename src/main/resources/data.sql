-- DROP TABLE counterparty;
-- CREATE TABLE counterparty (id int, name varchar(255), PRIMARY KEY (id));
--
-- DROP TABLE invoices;
-- CREATE TABLE invoices (id_from int, id_to int, value DECIMAL(18, 2), PRIMARY KEY(id_from, id_to));
--
-- DROP TABLE orderbook;
-- CREATE TABLE orderbook (id int, counterparty_fk int, price DECIMAL(18, 2), quantity DECIMAL(18, 2), date DATE);

INSERT INTO counterparty
values (1, 'supplyer');
INSERT INTO counterparty
values (2, 'buyer1');
INSERT INTO counterparty
values (3, 'buyer2');
INSERT INTO counterparty
values (4, 'buyer3');

insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_percent)
values             (11,  1,     2,      200, 0);
insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_percent)
values             (12,   1,    3,      150, 0);
insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value, prepaid_percent)
values             (13,   1,    4,      100, 0);

-- 0 ASK 1 BID 0 LIMIT 1 MARKET
insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
values                  (111, 2,              32,    200,     SYSDATE, 0,         0);
insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
values                  (112, 3,              31,    150,     SYSDATE, 0,         0);
insert into order_request(id, counterparty_fk, price, quantity, date, order_type, order_side)
values                  (113, 4,              30,    100,     SYSDATE, 0,         0);

