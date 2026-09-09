alter table outbox_message
    add column event_type varchar(100) not null default '';

alter table outbox_message
    alter column event_type drop default;
