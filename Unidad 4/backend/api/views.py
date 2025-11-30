from django.shortcuts import render
from rest_framework import generics
from .models import Incidencia, Operador, Usuario, Vehiculo 
from .serializers import IncidenciaSerializer, OperadorSerializer, UsuarioSerializer, VehiculoSerializer
from django.core.mail import send_mail
from django.core.mail import EmailMessage
from django.conf import settings

class UsuarioListCreate(generics.ListCreateAPIView):
    queryset = Usuario.objects.all()
    serializer_class = UsuarioSerializer


class UsuarioRetrieveUpdateDestroy(generics.RetrieveUpdateDestroyAPIView):
    queryset = Usuario.objects.all()
    serializer_class = UsuarioSerializer
    lookup_field = "pk"

class VehiculoListCreate(generics.ListCreateAPIView):
    queryset = Vehiculo.objects.all()
    serializer_class = VehiculoSerializer
    
class VehiculoRetrieveUpdateDestroy(generics.RetrieveUpdateDestroyAPIView):
    queryset = Vehiculo.objects.all()
    serializer_class = VehiculoSerializer
    lookup_field = "pk"
    
class OperadorListCreate(generics.ListCreateAPIView):
    queryset = Operador.objects.all()
    serializer_class = OperadorSerializer
    
class OperadorRetrieveUpdateDestroy(generics.RetrieveUpdateDestroyAPIView):
    queryset = Operador.objects.all()
    serializer_class = OperadorSerializer
    lookup_field = "pk"
    

class IncidenciaListCreate(generics.ListCreateAPIView):
    queryset = Incidencia.objects.all()
    serializer_class = IncidenciaSerializer

    def perform_create(self, serializer):
        incidencia = serializer.save()
        usuario = incidencia.vehiculo.usuario
        if usuario and usuario.email:
            asunto = "Nueva incidencia registrada"

            mensaje_html = f"""
                <p>Hola {usuario.nombre_completo},</p>
                <p>Se ha registrado la siguiente incidencia en tu vehículo <b>{incidencia.vehiculo.placa}</b>:</p>
                <p>{incidencia.descripcion_incidencia}</p>
                <p><img src="cid:imagen_incidencia"></p>
            """

            correo = EmailMessage(
                subject=asunto,
                body=mensaje_html,
                from_email=settings.DEFAULT_FROM_EMAIL,
                to=[usuario.email],
            )
            correo.content_subtype = "html"  

            if incidencia.evidencia_fotografia:
                correo.attach_file(incidencia.evidencia_fotografia.path)

            correo.send(fail_silently=False)

    
class IncidenciaRetrieveUpdateDestroy(generics.RetrieveUpdateDestroyAPIView):
    queryset = Incidencia.objects.all()
    serializer_class = IncidenciaSerializer
    lookup_field = "pk"
    

def enviar_email_usuario(usuario_email, asunto, mensaje):
    send_mail(
        subject=asunto,
        message=mensaje,
        from_email=None,  
        recipient_list=[usuario_email],
        fail_silently=False,
    )
