CREATE TABLE purchase(
    id int auto_increment primary key,
    customer_id int not null,
    nfe varchar(255) null,
    price decimal(15, 2) not null,
    created_at datetime not null,

    foreign key (customer_id) references customer(id)
);


CREATE TABLE purchase_book(
    purchase_id int not null,
    book_id int not null,

    foreign key (purchase_id) references purchase(id),
    foreign key (book_id) references book(id),
    primary key (purchase_id, book_id)
)