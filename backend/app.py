import firebase_admin
import pyrebase
import json
import os
import sys
import requests
import random
from auth.auth import AuthError, check_token
from firebase_admin import credentials, auth
from flask import Flask, render_template, request, jsonify, abort, redirect, url_for

user_list = []
friendlist = []
matched = {}
# pairs = [("1234", "5678"), ("10101", "20202")]
admin = "BjhDxngcjdgpGA5CCzvE7Gdp35q2"
final = []
# data = {"name": "Mortimer 'Morty' Smith"}

def create_app(test_config=None):
    app = Flask(__name__)
    cred = credentials.Certificate('fbAdminConfig.json')
    firebase = firebase_admin.initialize_app(cred)
    pb = pyrebase.initialize_app(json.load(open('fbconfig.json')))
    db = pb.database()

    def match_friend(uid):
        print("User: "+uid)
        
        friends = db.child("user").child(uid).child("queue").get()
        if friends.val() is None:
            friends = db.child("user").child(uid).child("friendlist").get()

        if friends.val() is not None:
            friendlist.clear()
            for f in friends.each():
                friendlist.append(f.val())
            # friendlist = friends.val()
            
            print("FriendList")
            print(friendlist)

            friend = friendlist.pop(0)
            temp = friend

            while matched[friend]:
                friendlist.append(friend)
                friend = friendlist.pop(0)
                if (temp is friend):
                    pair = (uid, admin)
                    return pair
            friendlist.append(friend)
            
            matched[friend] = True
            print("Queue")
            print(friendlist)
            # db.child("user").child(uid).child("queue").set(friendlist)

            return (uid, friend)
        else:
            return (uid, admin)

    @app.route("/")
    def index():
        # return render_template('pages/login.html')
        return render_template('pages/list.html', pairs=final)

    @app.route("/select_cb")
    def select_cb():
        user_list.clear()
        users = db.child("user").get()
        
        for user in users.val():
            matched[user] = False
            user_list.append(user)

        random.shuffle(user_list)
        
        for user in user_list:
            if (matched[user] is False):
                matched_pair = match_friend(user)
                print("Matched pair")
                print(matched_pair)
                # final.append(matched_pair)

        return redirect(url_for('index'))

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
    
    return app

app = create_app()

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get("PORT", 8080)))