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
admin = "BjhDxngcjdgpGA5CCzvE7Gdp35q2"
final = []
# data = {"name": "Mortimer 'Morty' Smith"}

def create_app(test_config=None):
    app = Flask(__name__)
    cred = credentials.Certificate('fbAdminConfig.json')
    firebase = firebase_admin.initialize_app(cred)
    pb = pyrebase.initialize_app(json.load(open('fbconfig.json')))
    db = pb.database()

    def update_friends_queue(uid, remove_id):
        temp = db.child("user").child(uid).child("queue").get()
        if temp.val() is None:
            temp = db.child("user").child(uid).child("friendlist").get()

        temp_list = []
        
        for f in temp.each():
            temp_list.append(f.val())
        
        temp_list.remove(remove_id)
        temp_list.append(remove_id)

        db.child("user").child(uid).child("queue").set(temp_list)
        

    def match_friend(uid):
        print("User: "+uid)
        
        friends = db.child("user").child(uid).child("queue").get()
        if friends.val() is None:
            friends = db.child("user").child(uid).child("friendlist").get()

        if friends.val() is not None:
            friendlist.clear()
            for f in friends.each():
                friendlist.append(f.val())
            
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
            matched[uid] = True
            print("Queue")
            print(friendlist)

            update_friends_queue(friend, uid)
            db.child("user").child(uid).child("queue").set(friendlist)

            return (uid, friend)
        else:
            matched[uid] = True
            return (uid, admin)

    @app.route("/")
    def index():
        print(final)
        return render_template('pages/list.html', final=final)

    @app.route("/select_cb")
    def select_cb():
        final.clear()
        user_list.clear()
        users = db.child("user").get()
        
        for user in users.val():
            matched[user] = False
            user_list.append(user)

        random.shuffle(user_list)
        
        for user in user_list:
            if matched[user] is False and user != admin:
                print("==========================")
                matched_pair = match_friend(user)
                print("Matched pair")
                print(matched_pair)
                print("==========================")
                final.append(matched_pair)

        return jsonify({
            'success': True
        })

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