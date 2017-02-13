# --- !Ups

CREATE TABLE tours (
    id bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    description varchar(10000) NOT NULL,
    last_updated varchar(255) NOT NULL DEFAULT now()
);

CREATE TABLE waypoints (
    id bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    lat double precision NOT NULL,
    long double precision NOT NULL,
    tour_id bigint NOT NULL AUTO_INCREMENT,
    ordering bigint NOT NULL AUTO_INCREMENT
);

CREATE TABLE categories (
    id bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    description varchar(10000) NOT NULL,
    last_updated varchar(255) NOT NULL DEFAULT now()
);

CREATE TABLE landmarks (
    id bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    description varchar(10000) NOT NULL,
    lat double precision NOT NULL,
    long double precision NOT NULL
);

CREATE TABLE photos (
    id bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    url varchar(1000) NOT NULL
);

-- mappings

CREATE TABLE landmark_photos (
    landmark_id bigint NOT NULL,
    photo_id bigint NOT NULL
);

CREATE TABLE tour_categories (
    tour_id bigint NOT NULL,
    category_id bigint NOT NULL
);

CREATE TABLE tour_landmarks (
    tour_id bigint NOT NULL,
    landmark_id bigint NOT NULL,
    ordering bigint NOT NULL
);

-- Create a basic tour
INSERT INTO tours (name, description) 
VALUES ('Freshman Residence Halls', 
    'Hey! This is a tour of the freshman residence halls!');

-- Make a second tour to make sure we're implementing things correctly
INSERT INTO tours (name, description) 
VALUES ('Second Tour', 
    'This tour shouldn''t show up if you''re getting tours for a specific category! It''s unmapped!');

-- Make a category this tour will be a part of
INSERT INTO categories (name, description) 
VALUES ('General Tours', 
    'This section consists of four athletic tours that go around campus!');

-- Map the tour to the category
INSERT INTO tour_categories (tour_id, category_id) VALUES (1, 1);

-- Make a few waypoints
INSERT INTO waypoints (lat, long, tour_id, ordering) 
    VALUES (42.73064179, 73.67553949, 1, 0);

INSERT INTO waypoints (lat, long, tour_id, ordering) 
    VALUES (42.72898, -73.67414, 1, 1);

INSERT INTO waypoints (lat, long, tour_id, ordering)
    VALUES (42.72848, -73.67455, 1, 2);

-- Make a couple landmarks
INSERT INTO landmarks (name, description, lat, long)
    VALUES ('RPI Admissions', 'This is the admissions building.', 42.73064179, -73.67553949);

INSERT INTO landmarks (name, description, lat, long) 
    VALUES ('Barton Residence Hall', 
        'Barton was first opened in Fall 2000 and in addition to being our newest residence hall, it also has the distinction of being the campus'' only freshman only residence hall.', 
        42.73064179, -73.67553949);

INSERT INTO landmarks (name, description, lat, long) 
    VALUES ('Commons Dining Hall', 
        'Situated at the center of the first-year residence halls, the Commons offers several dining stations. These include the popular Asian Pacifica, a savory grill program, pasta prepared to order, a deli and Theme Cuisine.', 
        42.73064179, -73.67553949);

-- Map the landmarks to the example tour
INSERT INTO tour_landmarks (tour_id, landmark_id, ordering)
    VALUES (1, 1, 0);

INSERT INTO tour_landmarks (tour_id, landmark_id, ordering)
    VALUES (1, 2, 1);

INSERT INTO tour_landmarks (tour_id, landmark_id, ordering)
    VALUES (1, 3, 2);

-- Give each landmark a picture
INSERT INTO photos (url) VALUES ('http://www.example.com/image.jpg');
INSERT INTO photos (url) VALUES ('http://www.example.com/image.jpg');
INSERT INTO photos (url) VALUES ('http://www.example.com/image.jpg');

INSERT INTO landmark_photos (landmark_id, photo_id) VALUES (1, 1);
INSERT INTO landmark_photos (landmark_id, photo_id) VALUES (2, 2);
INSERT INTO landmark_photos (landmark_id, photo_id) VALUES (3, 3);

# --- !Downs

DROP TABLE tours;
DROP TABLE waypoints;
DROP TABLE categories;
DROP TABLE landmarks;
DROP TABLE photos;
DROP TABLE landmark_photos;
DROP TABLE tour_categories;
DROP TABLE tour_landmarks;