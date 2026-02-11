use voting_system;

# 정상동작을 가정한 테스트 코드

insert into vote(is_open) values (true);

select * from vote;

SELECT @id := id FROM vote;

insert into vote_record(vote_id, user_id, item) values(@id, 'user1', '0.0');
insert into vote_record(vote_id, user_id, item) values(@id, 'user2', '1.1');
insert into vote_record(vote_id, user_id, item) values(@id, 'user3', '0.0');
insert into vote_record(vote_id, user_id, item) values(@id, 'user4', '0.2');

select * from vote;
select * from vote_record;
select vote_id, item, count(*) `count` from vote_record group by vote_id, item;

delete from vote where id = @id;
select count(*) from vote;
select count(*) from vote_record;