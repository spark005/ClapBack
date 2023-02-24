import firebase_admin
import pyrebase
import json
import os
import sys
import requests
from auth.auth import AuthError, check_token
from firebase_admin import credentials, auth
from flask import Flask, render_template, request, jsonify, abort, session, redirect, url_for
from flask_socketio import SocketIO, send, emit, join_room, leave_room
from flask_session import Session

users = [{'uid': 1, 'name': 'Ilhoon Lee'}]

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret'
app.config['SESSION_TYPE'] = 'filesystem'
Session(app)
socketio = SocketIO(app)
cred = credentials.Certificate('fbAdminConfig.json')
firebase = firebase_admin.initialize_app(cred)
pb = pyrebase.initialize_app(json.load(open('fbconfig.json')))

@app.route("/")
def index():
    session.clear()
    return render_template('pages/login.html')

@app.route("/selectroom")
def room():
    return render_template('pages/room.html')

@app.route("/chatlist", methods=['GET', 'POST'])
def chat():
    if(request.method=='POST'):
        username = request.form['username']
        room = request.form['room']
        #Store the data in session
        session['username'] = username
        session['room'] = room
        return render_template('pages/chat.html', session = session)
    else:
        if(session.get('username') is not None):
            return render_template('pages/chat.html', session = session)
        else:
            return redirect(url_for('room'))

# @socketio.on('connect')
# def test_connect():
#     print('connected')

# @socketio.on('disconnect')
# def test_disconnect():
#     print('Client disconnected')

@socketio.on('join', namespace='/chat')
def join(message):
    room = session.get('room')
    join_room(room)
    emit('status', {'msg': session.get('username') + ' has entered the room'}, room=room)

@socketio.on('text', namespace='/chat')
def text(message):
    room = session.get('room')
    emit('message', {'msg': session.get('username') + ' : ' + message['msg']}, room=room)

@socketio.on('left', namespace='/chat')
def left(message):
    room = session.get('room')
    username = session.get('username')
    leave_room(room)
    session.clear()
    emit('status', {'msg': username + ' has left the room.'}, room=room)

@app.route("/api/userinfo")
@check_token
def userinfo():
    if request.user:
        users = request.user
    return jsonify({
        "success": True,
        "data": users
    })

@app.route("/api/signup", methods=['POST'])
def sign_up():
    body = request.get_json()
    email = body.get('email', None)
    username = body.get('username', None)
    password = body.get('password', None)
    print(email, username, password)
    if email is None or password is None:
        abort(400, {'message': 'Missing email or password'})
    try:
        user = auth.create_user(
            email=email,
            password=password
        )
        return jsonify({
            "success": True,
            "userId": user.uid
        })
    except Exception as e:
        print(e)
        abort(400, {'message': str(e)})

@app.route("/api/login", methods=['POST'])
def login():
    body = request.get_json()
    email = body.get('email', None)
    password = body.get('password', None)
    try:
        user = pb.auth().sign_in_with_email_and_password(email, password)
        jwt = user['idToken']
        return jsonify({
            "success": True,
            "token": jwt
        }), 200
    except Exception as e:
        print(e)
        error_json = e.args[1]
        error = json.loads(error_json)['error']['message']            
        abort(400, {'message': error.split(" ")[0]})

@app.errorhandler(400)
def bad_request(error):
    return jsonify({
        'success': False,
        'error': 400,
        'message': error.description['message']
    }), 400

@app.errorhandler(404)
def not_found(error):
    return jsonify({
        'success': False,
        'error': 404,
        'message': 'Resource could not be found'
    }), 404

@app.errorhandler(422)
def unprocessable(error):
    return jsonify({
        'success': False,
        'error': 422,
        'message': error.description['message']
    }), 422

@app.errorhandler(405)
def wrong_approach(error):
    return jsonify({
        "success": False,
        "error": 405,
        "message": "Method not allowed"
    }), 405

@app.errorhandler(AuthError)
def authentication_error(error):
    print(error.error)
    return jsonify({
        'success': False,
        'error': error.error['code'],
        'message': error.error['description']
    }), error.status_code
    
if __name__ == '__main__':
    socketio.run(app, host='0.0.0.0', port=int(os.environ.get("PORT", 8080)), debug=True)