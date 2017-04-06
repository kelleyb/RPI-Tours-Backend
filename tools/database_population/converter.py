'''
This is a quick-and-dirty script to go through a JSON file in the expected format
and generate SQL based on that. It's not meant to be perfect. There are almost
definitely vulnerabilities. As such, it's only meant to be used for testing, 
where we know what the data looks like.

Basically, this is just meant to populate a database.
'''
import json

def sanitize_string(sql_string):
    ## I can't think of others that might need to be done right now.
    ## But I'm making this extensible just in case.
    mapping = {
        '\'': '\'\''
    }
    for original in mapping:
        sql_string = sql_string.replace(original, mapping[original])

    return sql_string


tour_format = 'INSERT INTO tours (name, description) VALUES (\'{}\', \'{}\');'
cat_format = 'INSERT INTO categories (name, description) VALUES (\'{}\', \'{}\');'
tc_format = 'INSERT INTO tour_categories (tour_id, category_id) VALUES ({}, {});'
waypoint_format = 'INSERT INTO waypoints (lat, long, tour_id, ordering) VALUES ({}, {}, {}, {});'
landmark_format = 'INSERT INTO landmarks (name, description, lat, long) VALUES (\'{}\', \'{}\', {}, {});'
tl_format = 'INSERT INTO tour_landmarks (tour_id, landmark_id, ordering) VALUES ({},{},{});'
photo_format = 'INSERT INTO photos (url) VALUES (\'{}\');'
lp_format = 'INSERT INTO landmark_photos (landmark_id, photo_id) VALUES ({}, {});'

main_json = json.loads(open('tours.json').read())

sql_statements = []

tour_id = 0
cat_id = 0
landmark_id = 0
photo_id = 0

for cat_i in range(len(main_json['categories'])):
    cat = main_json['categories'][cat_i]

    ## cat_id represents the ID of the category in the database. 
    ## Indexing starts with 1 in SQL stuff, so just add 1 to cat_i
    cat_id += 1

    ## create category in database for current_category
    sql_statements.append(cat_format.format(sanitize_string(cat['name']), sanitize_string(cat['desc'])))

    for tour_i in range(len(cat['tours'])):
        tour_id += 1
        tour = cat['tours'][tour_i]
        sql_statements.append(tour_format.format(sanitize_string(tour['name']), sanitize_string(tour['desc'])))
        sql_statements.append(tc_format.format(tour_id, cat_id))

        for waypoint_i in range(len(tour['waypoints'])):
            waypoint = tour['waypoints'][waypoint_i]
            sql_statements.append(waypoint_format.format(waypoint[0], waypoint[1], tour_id, waypoint_i))

        for landmark_i in range(len(tour['landmarks'])):
            landmark_id += 1
            landmark = tour['landmarks'][landmark_i]
            l_name = str(landmark['name'])
            l_desc = str(landmark['description'])
            sql_statements.append(landmark_format.format(sanitize_string(l_name), sanitize_string(l_desc), landmark['coordinate'][0], landmark['coordinate'][1]))
            sql_statements.append(tl_format.format(tour_id, landmark_id, landmark_i))

            for photo_i in range(len(landmark['photos'])):
                photo_id += 1
                photo = landmark['photos'][photo_i]
                sql_statements.append(photo_format.format(sanitize_string(photo)))
                sql_statements.append(lp_format.format(landmark_id, photo_id))


output_sql = open('output.sql', 'w')
for statement in sql_statements:
    output_sql.write(statement + '\n')

output_sql.close()