-- DROP TABLE counterparty;
-- CREATE TABLE counterparty (id int, name varchar(255), PRIMARY KEY (id));
--
-- DROP TABLE invoices;
-- CREATE TABLE invoices (id_from int, id_to int, value DECIMAL(18, 2), PRIMARY KEY(id_from, id_to));
--
-- DROP TABLE orderbook;
-- CREATE TABLE orderbook (id int, counterparty_fk int, price DECIMAL(18, 2), quantity DECIMAL(18, 2), date DATE);

INSERT INTO counterparty
values (1, 'test');
-- INSERT INTO counterparty
-- values (2, 'supplyer');
-- INSERT INTO counterparty
-- values (3, 'buyer2');
-- INSERT INTO counterparty
-- values (4, 'buyer3');
--
-- insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value)
-- values (11, 2, 1, 100);
-- insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value)
-- values (12, 2, 3, 200);
-- insert into invoice(id, counterparty_from_fk, counterparty_to_fk, value)
-- values (13, 2, 4, 50);
--
-- insert into request(id, counterparty_fk, price, quantity, date, order_type)
-- values (111, 1, 32, 200, SYSDATE, 0);
-- insert into request(id, counterparty_fk, price, quantity, date, order_type)
-- values (112, 3, 31, 150, SYSDATE, 0);
-- insert into request(id, counterparty_fk, price, quantity, date, order_type)
-- values (113, 4, 30, 100, SYSDATE, 0);

