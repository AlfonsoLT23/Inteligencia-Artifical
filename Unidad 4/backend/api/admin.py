from django.contrib import admin
from .models import Incidencia, Operador, Usuario, Vehiculo

# Register your models here.

admin.site.register(Incidencia)
admin.site.register(Operador)
admin.site.register(Usuario)
admin.site.register(Vehiculo)
