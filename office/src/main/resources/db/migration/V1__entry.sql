create table entry
(
    id          bigint generated always as identity primary key,
    regatta_id  bigint      not null,
    sail_number varchar(20) not null,
    status      varchar(20) not null,
    version     bigint      not null,
    constraint entry_sail_number_unique_per_regatta unique (regatta_id, sail_number)
);
