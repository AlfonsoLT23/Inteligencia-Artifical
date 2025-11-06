import pytest
import numpy as np
import pandas as pd
from sklearn.preprocessing import MinMaxScaler
from src.optimizador_pso import OptimizadorSensoresPSO

@pytest.fixture
def datos_de_prueba():
    """Genera un conjunto pequeño de datos normalizados de prueba."""
    rng = np.random.default_rng(42)
    datos = rng.random((10, 2))  
    columnas = ['Longitud', 'Latitud']
    return datos, columnas

@pytest.fixture
def escalador():
    """Crea un escalador MinMax ya entrenado en un dataset ficticio."""
    X = np.random.rand(10, 2)
    scaler = MinMaxScaler()
    scaler.fit(X)
    return scaler

def test_funcion_costo_particula_basica(datos_de_prueba):
    """Verifica que la función de costo devuelva valores no negativos."""
    datos, columnas = datos_de_prueba
    pso = OptimizadorSensoresPSO(
        n_sensores=2,
        datos=datos,
        n_particulas=5,
        iteraciones=3,
        escalador=None,
        columnas_caracteristicas=columnas
    )

    # Una partícula con valores dentro de [0,1]
    posicion = np.random.rand(pso.n_dimensiones)
    costo = pso._funcion_costo_particula(posicion)

    assert isinstance(costo, (float, np.floating))
    assert costo >= 0


def test_funcion_costo_particula_error_dimensional(datos_de_prueba):
    """Debe devolver np.inf si la partícula tiene tamaño incorrecto."""
    datos, columnas = datos_de_prueba
    pso = OptimizadorSensoresPSO(2, datos, 5, 3, None, columnas)

    posicion_invalida = np.random.rand(3)  
    costo = pso._funcion_costo_particula(posicion_invalida)
    assert np.isinf(costo)

def test_funcion_costo_lote(datos_de_prueba):
    """Verifica que la función de costo por lote funcione correctamente."""
    datos, columnas = datos_de_prueba
    pso = OptimizadorSensoresPSO(2, datos, 5, 3, None, columnas)

    lote = np.random.rand(4, pso.n_dimensiones)
    costos = pso._funcion_costo_lote(lote)

    assert isinstance(costos, np.ndarray)
    assert costos.shape == (4,)
    assert np.all(costos >= 0)

def test_ejecucion_optimizacion(datos_de_prueba):
    """Ejecuta una optimización pequeña para validar el flujo completo."""
    datos, columnas = datos_de_prueba
    pso = OptimizadorSensoresPSO(
        n_sensores=2,
        datos=datos,
        n_particulas=5,
        iteraciones=2,  
        escalador=None,
        columnas_caracteristicas=columnas
    )

    pso.ejecutar_optimizacion()

    assert pso.mejor_costo >= 0
    assert pso.mejor_posicion is not None
    assert len(pso.historial_costos) > 0

def test_obtener_ubicaciones_optimas(escalador, datos_de_prueba):
    """Comprueba que obtener_ubicaciones_optimas devuelva un DataFrame válido."""
    datos, columnas = datos_de_prueba
    pso = OptimizadorSensoresPSO(
        n_sensores=2,
        datos=datos,
        n_particulas=5,
        iteraciones=3,
        escalador=escalador,
        columnas_caracteristicas=columnas
    )

    # Simulamos una posición óptima ficticia
    pso.mejor_posicion = np.random.rand(pso.n_dimensiones)

    ubicaciones_df = pso.obtener_ubicaciones_optimas()

    assert isinstance(ubicaciones_df, pd.DataFrame)
    assert ubicaciones_df.shape == (2, len(columnas))
    assert set(ubicaciones_df.columns) == set(columnas)

def test_obtener_ubicaciones_optimas_error(escalador, datos_de_prueba):
    """Debe lanzar excepción si no se ha ejecutado la optimización aún."""
    datos, columnas = datos_de_prueba
    pso = OptimizadorSensoresPSO(
        n_sensores=2,
        datos=datos,
        n_particulas=5,
        iteraciones=3,
        escalador=escalador,
        columnas_caracteristicas=columnas
    )

    with pytest.raises(Exception):
        pso.obtener_ubicaciones_optimas()
