import sys
from functools import wraps
from firebase_admin import auth
from flask import request

class AuthError(Exception):
    def __init__(self, error, status_code):
        self.error = error
        self.status_code = status_code

def check_token(f):
    @wraps(f)
    def wrap(*args, **kwargs):
        if not request.headers.get('Authorization'):
            raise AuthError({
                'code': 'no_token',
                'description': 'No token provided'
            }, 401)
        try:
            user = auth.verify_id_token(request.headers['Authorization'])
            request.user = user
        except:
            print(sys.exc_info())
            raise AuthError({
                'code': 'invalid_token',
                'description': 'Invalid token provided'
            }, 401)
        
        return f(*args, **kwargs)
    return wrap