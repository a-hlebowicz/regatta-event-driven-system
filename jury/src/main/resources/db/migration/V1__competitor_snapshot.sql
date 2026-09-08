create table competitor_snapshot
(
    entry_id    bigint primary key,
    regatta_id  bigint      not null,
    sail_number varchar(20) not null,
    version     bigint      not null
);
