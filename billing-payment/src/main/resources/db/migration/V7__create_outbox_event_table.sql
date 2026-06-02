create table if not exists payment.outbox_event (
    id bigint primary key,
    aggregate_type varchar(100) not null,
    aggregate_id bigint not null,
    event_type varchar(100) not null,
    exchange_name varchar(150) not null,
    routing_key varchar(150) not null,
    payload text not null,
    trace_id varchar(64),
    span_id varchar(64),
    created_at timestamp not null,
    published_at timestamp,
    attempts integer not null default 0,
    last_error text
);

create index if not exists idx_payment_outbox_pending
    on payment.outbox_event (published_at, attempts, created_at);
