from cargador_datos import CargadorDatos
from optimizador_pso import OptimizadorSensoresPSO
from visualizador import Visualizador

if __name__ == "__main__":
    
    # --- CONFIGURACIÓN DEL PROYECTO ---
    RUTA_ARCHIVO = 'data/datos_cultivos_guasave.csv'
    N_SENSORES = 5  
    
    # --- CONFIGURACIÓN DE PSO ---
    N_PARTICULAS = 50
    ITERACIONES = 100
    
    # Cargar y Preprocesar Datos
    cargador = CargadorDatos(RUTA_ARCHIVO)
    cargador.cargar_y_preprocesar()

    # Configurar y Ejecutar PSO
    optimizador = OptimizadorSensoresPSO(
        n_sensores=N_SENSORES,
        datos=cargador.datos_procesados,
        n_particulas=N_PARTICULAS,
        iteraciones=ITERACIONES,
        escalador=cargador.escalador,
        columnas_caracteristicas=cargador.columnas_caracteristicas
    )
    optimizador.ejecutar_optimizacion()

    # Obtener Resultados
    ubicaciones_optimas_df = optimizador.obtener_ubicaciones_optimas()
    
    print("--- UBICACIONES ÓPTIMAS DE SENSORES ---")
    print(ubicaciones_optimas_df)
    print("-" * 30)

    # Visualizar Resultados
    visualizador = Visualizador(cargador.datos_originales, ubicaciones_optimas_df)
    visualizador.graficar_mapa_sensores()
    visualizador.graficar_convergencia(optimizador.historial_costos)