use voting_system;

SET SQL_SAFE_UPDATES = 0;
truncate vote_record;
truncate vote;
SET SQL_SAFE_UPDATES = 1;

insert into vote(id) values ('testing');

select * from vote;

update vote set is_open = true where id = 'testing';

insert into vote_record(vote_id, user_id, item) values('testing', 'user1', '0.0');
insert into vote_record(vote_id, user_id, item) values('testing', 'user2', '1.1');
insert into vote_record(vote_id, user_id, item) values('testing', 'user3', '0.0');
insert into vote_record(vote_id, user_id, item) values('testing', 'user4', '0.2');

select * from vote;
select * from vote_record;
select vote_id, item, count(*) `count` from vote_record group by vote_id, item;

delete from vote where id = 'testing';
select count(*) from vote;
select count(*) from vote_record;