from django.urls import path
from . import views
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('usuarios/', views.UsuarioListCreate.as_view(), name='usuario-list-create'),
    path('usuarios/<int:pk>/', views.UsuarioRetrieveUpdateDestroy.as_view(), name='usuario-retrieve-update-destroy'),
    path('vehiculos/', views.VehiculoListCreate.as_view(), name='vehiculo-list-create'),
    path('vehiculos/<str:pk>/', views.VehiculoRetrieveUpdateDestroy.as_view(), name='vehiculo-retrieve-update-destroy'),
    path('operadores/', views.OperadorListCreate.as_view(), name='operador-list-create'),
    path('operadores/<int:pk>/', views.OperadorRetrieveUpdateDestroy.as_view(), name='operador-retrieve-update-destroy'),
    path('incidencias/', views.IncidenciaListCreate.as_view(), name='incidencia-list-create'),
    path('incidencias/<int:pk>/', views.IncidenciaRetrieveUpdateDestroy.as_view(), name='incidencia-retrieve-update-destroy'),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

