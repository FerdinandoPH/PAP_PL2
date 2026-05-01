from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

class Entrada(db.Model):
    __tablename__ = 'entradas'

    id = db.Column(db.Integer, primary_key=True)
    nombre_usuario = db.Column(db.String(), nullable=False)
    nombre_fase = db.Column(db.String(), nullable=False)
    fecha = db.Column(db.DateTime, nullable=False)
    entrada = db.Column(db.String(), nullable=False)
    salida = db.Column(db.String(), nullable=False)

    def __init__(self, nombre_usuario, nombre_fase, fecha, entrada, salida):
        self.nombre_usuario = nombre_usuario
        self.nombre_fase = nombre_fase
        self.fecha = fecha
        self.entrada = entrada
        self.salida = salida
    def __repr__(self):
        return f'<Entrada {self.id} - Usuario: {self.nombre_usuario}, Fecha: {self.fecha}, Fase: {self.nombre_fase}, Entrada: {self.entrada}, Salida: {self.salida}>'