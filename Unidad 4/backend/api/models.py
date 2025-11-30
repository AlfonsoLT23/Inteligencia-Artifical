from django.db import models
from django.db.models.signals import post_save
from django.dispatch import receiver

# Create your models here.

class Usuario(models.Model):
    TIPO_USUARIO_CHOICES = [
        ('alumno', 'Alumno'),
        ('empleado', 'Empleado'),
        ('otro', 'Otro'),
    ]

    nombre_completo = models.CharField(max_length=100)
    tipo_usuario = models.CharField(max_length=10, choices=TIPO_USUARIO_CHOICES)
    telefono = models.CharField(max_length=15, blank=True, null=True)
    email = models.CharField(max_length=100, blank=True, null=True)
    fecha_registro = models.DateField(auto_now_add=True)
    discapacitado = models.BooleanField(default=False) # 1 si / 0 no
    estado = models.BooleanField(default=True)  # 1 activo / 0 inactivo

    def __str__(self):
        return self.nombre_completo


class Vehiculo(models.Model):
    placa = models.CharField(max_length=20, primary_key=True)
    usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE)
    marca = models.CharField(max_length=50, blank=True, null=True)
    modelo = models.CharField(max_length=50, blank=True, null=True)
    color = models.CharField(max_length=30, blank=True, null=True)
    anio = models.IntegerField(blank=True, null=True)
    estado = models.BooleanField(default=True)

    def __str__(self):
        return f"{self.placa} - {self.marca} {self.modelo}"



class Operador(models.Model):
    nombre_completo = models.CharField(max_length=100)
    telefono = models.CharField(max_length=15, blank=True, null=True)
    email = models.CharField(max_length=100, blank=True, null=True)
    estado = models.BooleanField(default=True)

    def __str__(self):
        return self.nombre_completo


class Incidencia(models.Model):
    vehiculo = models.ForeignKey(Vehiculo, on_delete=models.CASCADE)
    operador = models.ForeignKey(Operador, on_delete=models.CASCADE)
    descripcion_incidencia = models.CharField(max_length=255)
    fecha_hora = models.DateTimeField(auto_now_add=True)
    evidencia_fotografia = models.ImageField(upload_to='incidencias/')
    latitud = models.DecimalField(max_digits=10, decimal_places=7, blank=True, null=True)
    longitud = models.DecimalField(max_digits=10, decimal_places=7, blank=True, null=True)

    def __str__(self):
        return f"Incidencia #{self.id} - {self.vehiculo.placa}"


@receiver(post_save, sender=Incidencia)
def cambiar_estado(sender, instance, created, **kwargs):
    if not created:
        return

    usuario = instance.vehiculo.usuario

    total_incidencias = Incidencia.objects.filter(
        vehiculo__usuario=usuario
    ).count()

    if total_incidencias >= 3:
        usuario.estado = False
        usuario.save()