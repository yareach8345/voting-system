use voting_system;

# 정상동작을 가정한 테스트 코드

insert into election(is_open) values (true);

select * from election;

SELECT @id := id FROM election;

insert into vote(election_id, user_id, item) values(@id, 'user1', '0.0');
insert into vote(election_id, user_id, item) values(@id, 'user2', '1.1');
insert into vote(election_id, user_id, item) values(@id, 'user3', '0.0');
insert into vote(election_id, user_id, item) values(@id, 'user4', '0.2');

select * from vote;
select * from vote;
select election_id, item, count(*) `count` from vote group by election_id, item;

delete from vote where id = @id;
select count(*) from election;
select count(*) from vote;