from rest_framework import serializers
from .models import Incidencia, Operador, Usuario, Vehiculo 

class UsuarioSerializer(serializers.ModelSerializer):
    class Meta:
        model = Usuario
        fields = '__all__'
        
class VehiculoSerializer(serializers.ModelSerializer):
    class Meta:
        model = Vehiculo
        fields = '__all__'
        
class OperadorSerializer(serializers.ModelSerializer):
    class Meta:
        model = Operador
        fields = '__all__'

class IncidenciaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Incidencia
        fields = '__all__'

    def validate(self, data):
        vehiculo = data.get("vehiculo")
        operador = data.get("operador")

        if not Vehiculo.objects.filter(pk=vehiculo.pk).exists():
            raise serializers.ValidationError({
                "vehiculo": "El vehículo con esa placa no existe."
            })

        if not Operador.objects.filter(pk=operador.pk).exists():
            raise serializers.ValidationError({
                "operador": "El operador especificado no existe."
            })

        return data