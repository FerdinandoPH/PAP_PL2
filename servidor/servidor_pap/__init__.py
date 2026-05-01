from flask import Flask, render_template, request, jsonify
from servidor_pap.models import db, Entrada
from datetime import datetime
from functools import wraps
import os
from dotenv import load_dotenv
load_dotenv()

def requiere_basic_auth(f):
    @wraps(f)
    def decorador(*args, **kwargs):
        auth = request.authorization
        if not auth or auth.username != 'pap' or auth.password != 'pap2026':
            return jsonify({'error': 'No autorizado'}), 401, {'WWW-Authenticate': 'Basic realm="API"'}
        return f(*args, **kwargs)
    return decorador

def crear_app():
    app = Flask(__name__)
    app.config['SQLALCHEMY_DATABASE_URI'] = os.getenv('DATABASE_URL')
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

    db.init_app(app)

    with app.app_context():
        db.create_all()

    @app.route('/')
    def index():
        return render_template('index.html')

    @app.route('/api/registrar_entrada', methods=['POST'])
    @requiere_basic_auth
    def registrar_entrada():
        try:
            data = request.get_json()
            nueva_entrada = Entrada(
                nombre_usuario=data['nombre_usuario'],
                nombre_fase=data['nombre_fase'],
                fecha=datetime.fromisoformat(data['fecha']),
                entrada=data['entrada'],
                salida=data['salida']
            )
            db.session.add(nueva_entrada)
            db.session.commit()
            #Si hay 100 entradas, eliminar la más antigua
            if Entrada.query.count() > 100:
                entrada_mas_antigua = Entrada.query.order_by('fecha').first()
                db.session.delete(entrada_mas_antigua)
                db.session.commit()
            return {'message': 'Entrada registrada correctamente'}, 201
        except Exception as e:
            db.session.rollback()
            return {'error': str(e)}, 500
        
    @app.route('/api/obtener_entradas', methods=['GET'])
    def obtener_entradas():
        try:
            entradas = Entrada.query.order_by(db.desc('fecha')).limit(100).all()
            lista_entradas = [
                {
                    'id': entrada.id,
                    'nombre_usuario': entrada.nombre_usuario,
                    'nombre_fase': entrada.nombre_fase,
                    'fecha': entrada.fecha.isoformat(),
                    'entrada': entrada.entrada,
                    'salida': entrada.salida
                }
                for entrada in entradas
            ]
            return {'entradas': lista_entradas}, 200
        except Exception as e:
            return {'error': str(e)}, 500
    return app
