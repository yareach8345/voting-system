use voting_system;

# 초기화가 목적일 경우 아래의 주석을 해제하시오.
# If you want to reset the tables, remove the comments.
# テーブルを初期化する場合は、下のコメントを外してください。
-- drop table if exists vote_record;
-- drop table if exists vote;

create table if not exists vote (
    id varchar(255) primary key,
    is_open bool not null default false,
    started_at datetime default null,
    ended_at datetime default null,
    created_at datetime not null default current_timestamp
);

# write 시 vote의 is_open을 필히 확인 할 것
# Before inserting a vote record, make sure vote.is_open is true.
# insertの場合、voteテーブルのis_openカラムを確認してください。
create table if not exists vote_record (
    id int auto_increment primary key,
    vote_id varchar(255) not null,
    user_id varchar(255) not null,
    item varchar(255) not null,	#여러 시스템에 쓰일 예정이라 후보 특정 불가능. 투표 시스템에서 필히 regex를 사용해 걸러낼것!
    voted_at datetime not null default current_timestamp,

    #중복 투표 방지
    unique key unique_vote(user_id, vote_id),

    #외래키 설정
    #user_id는 외부시스템 영역이므로 테이블 생성 및 외래키 설정은 하지 않음
    foreign key(vote_id) references vote(id) on delete cascade,

    #인덱싱
    index idx_vote_item (vote_id, item)
);