CREATE TABLE customer_roles(
     customer_id int not null,
     role varchar(255) not null,
     foreign key (customer_id) references customer(id)
);