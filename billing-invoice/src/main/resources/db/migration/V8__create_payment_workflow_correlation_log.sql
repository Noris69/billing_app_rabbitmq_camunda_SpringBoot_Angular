create table if not exists invoice.payment_workflow_correlation_log (
    id bigint primary key,
    event_id varchar(100),
    invoice_id bigint,
    invoice_reference varchar(255),
    payment_status varchar(50),
    correlation_status varchar(50) not null,
    error_message text,
    created_at timestamp not null
);

create index if not exists idx_payment_workflow_correlation_invoice
    on invoice.payment_workflow_correlation_log (invoice_id, created_at);

create index if not exists idx_payment_workflow_correlation_event
    on invoice.payment_workflow_correlation_log (event_id);
