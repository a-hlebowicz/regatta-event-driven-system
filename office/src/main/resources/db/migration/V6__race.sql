create table race
(
    id            bigint generated always as identity primary key,
    regatta_id    bigint      not null references regatta (id),
    race_number   integer     not null,
    planned_start timestamp with time zone not null,
    status        varchar(20) not null,
    closed_at     timestamp with time zone,
    version       bigint      not null,
    constraint race_number_unique_per_regatta unique (regatta_id, race_number)
);

create table race_finish
(
    id       bigint generated always as identity primary key,
    race_id  bigint      not null references race (id),
    entry_id bigint      not null,
    position integer,
    code     varchar(20) not null,
    constraint race_finish_entry_unique_per_race unique (race_id, entry_id),
    constraint race_finish_position_only_when_finished
        check ((code = 'FINISHED' and position is not null) or (code <> 'FINISHED' and position is null))
);

create index race_finish_race_index on race_finish (race_id);
