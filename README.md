# java-filmorate
Template repository for Filmorate project.

![This is an image of database](https://github.com/vladbakumenko/java-filmorate/blob/main/filmorate_db.png)
link for [image of db](https://github.com/vladbakumenko/java-filmorate/blob/main/filmorate_db.png)

examples of basic database queries:
- get all films:
<br>SELECT *
<br>FROM films

- get film by id (parametr: int id):
<br>SELECT *
<br>FROM films AS f
<br>WHERE f.id = (entered id)

- get popular film (parametr: int count):
<br>SELECT *
<br>FROM films AS f
<br>LEFT OUTER JOIN likes_by_users AS l ON f.id = l.id_film
<br>GROUP BY f.id
<br>ORDER BY COUNT(l.id_film) DESC
<br>LIMIT count

- get all users:
<br>SELECT *
<br>FROM users

- get user by id (parametr: int id):
<br>SELECT *
<br>FROM users AS u
<br>WHERE u.id = (entered id)

- get friends by id (parametr: int id):
<br>SELECT *
<br>FROM users AS u
<br>WHERE u.id IN (SELECT id_friend
       <br>FROM friends AS f
       <br>WHERE f.id_user = (entered id)
       <br>AND f.id_status = 2 (подтверждённая дружба))
       
- get common friends by id user and id other user (parameters: int id, int otherId)
<br>SELECT *
<br>FROM users AS u
<br>WHERE u.id IN (SELECT id_friend
       <br>FROM friends AS f
       <br>WHERE f.id_user = (entered id)
       <br>AND f.id_status = 2 (подтверждённая дружба))
<br>AND u.id IN (SELECT id_friend
       <br>FROM friends AS f
       <br>WHERE f.id_user = (entered otherId)
       <br>AND f.id_status = 2 (подтверждённая дружба))
       


