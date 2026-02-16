drop table if exists "vote";
drop table if exists "election";

create table if not exists "election" (
    id varchar(36) default random_uuid() primary key,
    "is_open" boolean not null default false,
    "started_at" datetime default null,
    "ended_at" datetime default null,
    "created_at" datetime not null default current_timestamp,
    "last_modified" datetime default current_timestamp on update current_timestamp
);

create table if not exists "vote" (
    id int auto_increment primary key,
    "election_id" varchar(36) not null,
    "user_id" varchar(255) not null,
    "item" varchar(255) not null,
    "voted_at" datetime not null default current_timestamp,

    foreign key("election_id") references "election" (id) on delete cascade
);

set @uuid1 = random_uuid();
set @uuid2 = random_uuid();

insert into "election"(id)values (@uuid1);
insert into "election"(id)values (@uuid2);

update "election" set "is_open" = true, "started_at" = current_timestamp where id = @uuid1;
update "election" set "started_at" = current_timestamp, "ended_at" = current_timestamp where id = @uuid2;
