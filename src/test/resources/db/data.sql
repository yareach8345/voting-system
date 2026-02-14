drop table if exists "vote_record";
drop table if exists "vote";

create table if not exists "vote" (
    id varchar(36) default random_uuid() primary key,
    "is_open" boolean not null default false,
    "started_at" datetime default null,
    "ended_at" datetime default null,
    "created_at" datetime not null default current_timestamp
);

create table if not exists "vote_record" (
    id int auto_increment primary key,
    "vote_id" varchar(36) not null,
    "user_id" varchar(255) not null,
    "item" varchar(255) not null,
    "voted_at" datetime not null default current_timestamp,

    foreign key("vote_id") references "vote"(id) on delete cascade
);

set @uuid1 = random_uuid();
set @uuid2 = random_uuid();

insert into "vote"(id) values (@uuid1);
insert into "vote"(id) values (@uuid2);

update "vote" set "is_open" = true, "started_at" = current_timestamp where id = @uuid1;
update "vote" set "started_at" = current_timestamp, "ended_at" = current_timestamp where id = @uuid2;
