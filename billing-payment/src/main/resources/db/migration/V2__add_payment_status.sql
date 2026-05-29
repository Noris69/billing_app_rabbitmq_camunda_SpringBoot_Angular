alter table if exists payment."transaction"
    add column if not exists status varchar(30) not null default 'SUCCESS';

