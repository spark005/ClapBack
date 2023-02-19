# ClapBack

## ClapBack Reference

### Motivation
- Keeping up with friends after high school/college is hard. No longer is interaction forced amongst peers through group projects and seating arrangements, which in turn makes building and maintaining friendships significantly more difficult. ClapBack remedies this issue by providing the user a messaging system that randomly generates an individual every day to chat with, providing no other alternative but making it fun through given prompts and incentives. There are many messaging apps on the market, but none purposely manages the user’s contact with their added friends and encourages keeping contact with past relationships.

### Backend
#### Getting Started
- Base URL: (I will work on this!) 
- Authentication: This app has a single role.
    - Roles: 
        - 1. User: can perform all actions

#### Development Setup
To start and run the local development server,
1. Initialize and activate a virtualenv:
```
$ cd YOUR_PROJECT_DIRECTORY_PATH/backend/
$ python -m venv env
$ source env/bin/activate
```
2. Install the dependencies:
```
$ pip3 install -r requirements.txt
```
3. Run the development server:
```
$ python app.py
```

#### Error Handling
Errors are returned as JSON objects in the following format:
```
{
    "success": False, 
    "error": 404,
    "message": "Resource could not be found"
}
```
The API will return five error types when requests fail:
- 400: Bad request (Specific message varies by error)
- 404: Resource Not Found
- 405: Method Not Allowed
- 422: Not Processable (Specific message varies by error)
- Authentication Error: varies by error

#### Endpoints
##### GET /
- General:
    - Main page for this web app.
    - Returns a jinja template that renders login.html.
- Sample: `curl 127.0.0.1:8080/`

##### GET /api/userinfo
- General:
    - Checks whether the user has an authorized token.
    - If the user does, the api will return the user's information.
    - If not, the api will throw an Authentication Error. 
- Sample: `curl 127.0.0.1:8080/api/userinfo` or `curl 127.0.0.1:8080/api/userinfo -H "Authorization: {INSERT_TOKEN}"`
```
{
  "success": true
  "data": {USER_DATA}
}
```

##### POST /api/signup
- General:
    - Creates an account with the submitted credentials.
    - Returns the user's uid when successful.
- Sample: `curl 127.0.0.1:8080/api/signup -X POST -H "Content-Type: application/json" -d '{"email": "YOUR_EMAIL", "username": "YOUR_USERNAME", "password": "YOUR_PASSWORD"}'`
```
{
  "success": true
  "userId": {USER_ID}
}
```

##### POST /api/login
- General:
    - Authenticates user.
    - If the user is authenticated, the api will return a token.
- Sample: `curl 127.0.0.1:8080/api/login -X POST -H "Content-Type: application/json" -d '{"email": "YOUR_EMAIL", "password": "YOUR_PASSWORD"}'`
```
{
  "success": true
  "token": {TOKEN}
}
```