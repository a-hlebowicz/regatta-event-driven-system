create table regatta
(
    id                         bigint generated always as identity primary key,
    name                       varchar(200) not null,
    venue                      varchar(200) not null,
    boat_class                 varchar(100) not null,
    start_date                 date         not null,
    end_date                   date         not null,
    protest_time_limit_minutes integer      not null,
    status                     varchar(20)  not null
);

create table regatta_discard_threshold
(
    regatta_id bigint  not null references regatta (id),
    threshold  integer not null
);

create index regatta_discard_threshold_regatta_index on regatta_discard_threshold (regatta_id);

alter table entry
    add constraint entry_regatta_fk foreign key (regatta_id) references regatta (id);
