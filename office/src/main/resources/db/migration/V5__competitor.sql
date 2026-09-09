create table competitor
(
    id             bigint generated always as identity primary key,
    first_name     varchar(100) not null,
    last_name      varchar(100) not null,
    club           varchar(200) not null,
    licence_number varchar(50)  not null,
    constraint competitor_licence_number_unique unique (licence_number)
);

alter table entry
    add column competitor_id bigint not null;

alter table entry
    add constraint entry_competitor_unique_per_regatta unique (regatta_id, competitor_id);
