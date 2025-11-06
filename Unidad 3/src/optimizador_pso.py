import numpy as np
import pyswarms as ps
from scipy.spatial.distance import cdist
import pandas as pd

class OptimizadorSensoresPSO:
    """
    Clase que implementa el algoritmo PSO (Particle Swarm Optimization)
    para encontrar las ubicaciones óptimas de los sensores.
    
    Cada partícula del enjambre representa un conjunto de ubicaciones de sensores.
    """
    def __init__(self, n_sensores, datos, n_particulas, iteraciones, escalador, columnas_caracteristicas):
        self.n_sensores = n_sensores
        self.datos = datos
        self.n_particulas = n_particulas
        self.iteraciones = iteraciones
        self.escalador = escalador
        self.columnas_caracteristicas = columnas_caracteristicas
        
        # Definir dimensiones del problema
        self.n_caracteristicas = datos.shape[1]
        self.n_dimensiones = self.n_sensores * self.n_caracteristicas
        
        self.mejor_costo = np.inf
        self.mejor_posicion = None
        self.historial_costos = []

    def _funcion_costo_particula(self, posicion_particula):
        """
        Calcula el costo (error) para una partícula individual.
        El costo es la suma de las distancias al cuadrado desde cada punto de dato
        hasta el sensor más cercano.
        """
        try:
            centroides = posicion_particula.reshape(self.n_sensores, self.n_caracteristicas)
        except ValueError as e:
            print(f"Error al reformar: {e}")
            return np.inf

        distancias = cdist(self.datos, centroides, 'euclidean')
        min_distancias = np.min(distancias, axis=1)
        costo = np.sum(min_distancias**2)
        return costo

    def _funcion_costo_lote(self, lote_particulas):
        """Evalúa la función de costo para un lote de partículas."""
        return np.array([self._funcion_costo_particula(p) for p in lote_particulas])

    def ejecutar_optimizacion(self):
        """Configura y ejecuta el algoritmo PSO."""
        print(f"Iniciando optimización PSO con {self.n_sensores} sensores...")
        print(f"Partículas: {self.n_particulas}, Iteraciones: {self.iteraciones}")
        
        opciones = {'c1': 0.5, 'c2': 0.3, 'w': 0.9}
        limite_inferior = np.zeros(self.n_dimensiones)
        limite_superior = np.ones(self.n_dimensiones)
        limites = (limite_inferior, limite_superior)

        optimizador = ps.single.GlobalBestPSO(
            n_particles=self.n_particulas,
            dimensions=self.n_dimensiones,
            options=opciones,
            bounds=limites
        )

        self.mejor_costo, self.mejor_posicion = optimizador.optimize(
            self._funcion_costo_lote,
            iters=self.iteraciones,
            verbose=True
        )
        
        self.historial_costos = optimizador.cost_history
        print(f"Optimización completada. Mejor costo encontrado: {self.mejor_costo:.4f}")
        print("-" * 30)

    def obtener_ubicaciones_optimas(self):
        """Devuelve las ubicaciones óptimas de sensores (en escala original)."""
        if self.mejor_posicion is None:
            raise Exception("Debe ejecutar la optimización primero.")
            
        ubicaciones_escaladas = self.mejor_posicion.reshape(self.n_sensores, self.n_caracteristicas)
        ubicaciones_originales = self.escalador.inverse_transform(ubicaciones_escaladas)
        
        ubicaciones_df = pd.DataFrame(ubicaciones_originales, columns=self.columnas_caracteristicas)
        return ubicaciones_df.round(4)
