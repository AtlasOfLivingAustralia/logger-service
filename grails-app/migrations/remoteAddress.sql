-- add an artifical primary key to the table and a unique constraint to the ip column

alter table remote_address drop primary key;

alter table remote_address add id int(11) not null auto_increment primary key;

alter table remote_address add constraint ra_ip_unique unique (ip);