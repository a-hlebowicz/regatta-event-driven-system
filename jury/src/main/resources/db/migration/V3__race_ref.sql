create table race_ref
(
    race_id                    bigint primary key,
    regatta_id                 bigint      not null,
    race_number                integer,
    status                     varchar(20) not null,
    closed_at                  timestamp with time zone,
    protest_time_limit_minutes integer,
    version                    bigint      not null
);

create index race_ref_regatta_index on race_ref (regatta_id);
