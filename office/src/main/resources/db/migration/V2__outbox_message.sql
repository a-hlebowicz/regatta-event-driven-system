create table outbox_message
(
    id          bigint generated always as identity primary key,
    topic       varchar(200)             not null,
    message_key varchar(100)             not null,
    payload     text                     not null,
    created_at  timestamp with time zone not null,
    sent_at     timestamp with time zone
);

create index outbox_message_unsent_index on outbox_message (id) where sent_at is null;
