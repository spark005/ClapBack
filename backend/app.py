import firebase_admin
import pyrebase
import json
import os
import sys
import requests
from auth.auth import AuthError, check_token
from firebase_admin import credentials, auth
from flask import Flask, render_template, request, jsonify, abort

users = [{'uid': 1, 'name': 'Ilhoon Lee'}]

def create_app(test_config=None):
    app = Flask(__name__)
    cred = credentials.Certificate('fbAdminConfig.json')
    firebase = firebase_admin.initialize_app(cred)
    pb = pyrebase.initialize_app(json.load(open('fbconfig.json')))

    @app.route("/")
    def index():
        return render_template('pages/login.html')

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