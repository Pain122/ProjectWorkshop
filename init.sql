create database todos;

create table TODOS(
  id serial not null,
  PRIMARY KEY(id),
  name character varying NOT NULL,
  remainder_date bigint NOT NULL
);
