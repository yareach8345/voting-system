drop table if exists vote;
drop table if exists vote_record;

create table if not exists vote (
    id varchar(36) default random_uuid() primary key,
    is_open boolean not null default false,
    started_at datetime default null,
    ended_at datetime default null,
    created_at datetime not null default current_timestamp
);

create table if not exists vote_record (
    id int auto_increment primary key,
    vote_id varchar(36) not null,
    user_id varchar(255) not null,
    item varchar(255) not null,
    voted_at datetime not null default current_timestamp,

    foreign key(vote_id) references vote(id) on delete cascade
);

insert into vote(id) values (default);
insert into vote(id) values (default);
