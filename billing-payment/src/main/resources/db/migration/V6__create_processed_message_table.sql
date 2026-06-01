create table if not exists payment.processed_message (
    event_id varchar(100) primary key,
    event_type varchar(100),
    processed_at timestamp not null default current_timestamp
);
