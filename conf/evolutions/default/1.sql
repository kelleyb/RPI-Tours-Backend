# --- !Ups

CREATE TABLE tours (
	id bigint(20) NOT NULL AUTO_INCREMENT,
	name varchar(255) NOT NULL,
	description varchar(10000) NOT NULL,
);

CREATE TABLE waypoints (
	id bigint(20) NOT NULL AUTO_INCREMENT,
	lat double precision NOT NULL,
	long double precision NOT NULL,
	tour_id bigint(20) NOT NULL AUTO_INCREMENT,
	ordering bigint(20) NOT NULL AUTO_INCREMENT
);

CREATE TABLE categories (
	id bigint(20) NOT NULL AUTO_INCREMENT,
	name varchar(255) NOT NULL,
	description varchar(10000) NOT NULL
);

CREATE TABLE landmarks (
	id bigint(20) NOT NULL AUTO_INCREMENT,
	name varchar(255) NOT NULL,
	description varchar(10000) NOT NULL,
	lat double precision NOT NULL,
	long double precision NOT NULL
);

CREATE TABLE photos (
	id bigint(20) NOT NULL AUTO_INCREMENT,
	url varchar(1000) NOT NULL
);

-- mappings

CREATE TABLE landmark_photos (
	landmark_id bigint(20) NOT NULL,
	photo_id bigint(20) NOT NULL
);

CREATE TABLE tour_categories (
	tour_id bigint(20) NOT NULL,
	category_id bigint(20) NOT NULL
);

CREATE TABLE tour_landmarks (
	tour_id bigint(20) NOT NULL,
	landmark_id bigint(20) NOT NULL,
	ordering bigint(20) NOT NULL
);

CREATE TABLE tour_waypoints (
	tour_id bigint(20) NOT NULL,
	waypoint_id bigint(20) NOT NULL
);

# --- !Downs

DROP TABLE tours;
DROP TABLE waypoints;
DROP TABLE categories;
DROP TABLE landmarks;
DROP TABLE photos;
DROP TABLE landmark_photos;
DROP TABLE tour_categories;
DROP TABLE tour_landmarks;
DROP TABLE tour_waypoints;