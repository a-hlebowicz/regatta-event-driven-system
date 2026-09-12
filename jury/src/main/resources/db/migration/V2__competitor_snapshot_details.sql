alter table competitor_snapshot
    add column competitor_id   bigint,
    add column competitor_name varchar(200),
    add column active          boolean not null default true;

alter table competitor_snapshot
    alter column sail_number drop not null;
