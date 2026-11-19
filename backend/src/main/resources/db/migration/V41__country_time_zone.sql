alter table country add column time_zone text not null default 'UTC';
update country set time_zone = 'Africa/Libreville' where code = 'GA';
update country set time_zone = 'Africa/Bamako' where code = 'ML';
