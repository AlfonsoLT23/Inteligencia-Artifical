import math
import random
import numpy as np

def solucion_inicial(df_tiendas, matriz_distancias):
    """
    Genera una solución inicial asignando cada tienda al centro de distribución (CEDIS)
    más cercano según la matriz de distancias.

    Args:
        df_tiendas: DataFrame con información de las tiendas y CEDIS.
        matriz_distancias: Matriz con las distancias entre nodos.

    Returns:
        list[list[int]]: Lista de rutas, donde cada sublista contiene los índices
            de las tiendas asignadas a un CEDIS.
    """
    n_cedis = len(df_tiendas[df_tiendas["Tipo"] == "Centro de Distribución"])
    rutas = [[] for _ in range(n_cedis)]

    for i in range(len(df_tiendas)):
        tipo = df_tiendas.loc[i, "Tipo"]
        if tipo == "Tienda":
            distancias_a_cedis = matriz_distancias[i, :n_cedis]
            idx_min = np.argmin(distancias_a_cedis)  
            rutas[idx_min].append(i)
    return rutas

def costo_ruta(ruta, id_cedis, matriz_distancias, matriz_combustible):
    """
    Calcula el costo ponderado, de distancia y de gasolina para una ruta específica.

    Args:
        ruta: Lista con los índices de las tiendas en la ruta.
        id_cedis: Índice del CEDIS asociado a la ruta.
        matriz_distancias: Matriz con las distancias entre nodos.
        matriz_combustible: Matriz con el costo de gasolina entre nodos.

    Returns:
        - costo_ponderado: Costo total considerando los pesos α y β.
        - costo_distancia: Suma de distancias recorridas.
        - costo_gasolina: Suma de costos de combustible.
    """
    costo_distancia = 0
    costo_gasolina = 0
    actual = id_cedis
    alpha=0.3 
    beta=0.7
    
    for nodo in ruta:
        costo_distancia += matriz_distancias[actual][nodo]
        costo_gasolina += matriz_combustible[actual][nodo]
        actual = nodo
    
    costo_distancia += matriz_distancias[actual][id_cedis]
    costo_gasolina += matriz_combustible[actual][id_cedis]
    costo_ponderado = (alpha * costo_distancia) + (beta * costo_gasolina)
    return costo_ponderado, costo_distancia, costo_gasolina


def costo_total(rutas, matriz_distancias, matriz_combustible):
    """
    Calcula el costo total (ponderado, distancia y gasolina) para todas las rutas.

    Args:
        rutas: Lista de rutas, cada una asociada a un CEDIS.
        matriz_distancias: Matriz con las distancias entre nodos.
        matriz_combustible: Matriz con el costo de gasolina entre nodos.

    Returns:
        - total_ponderado: Suma total del costo ponderado.
        - total_distancia: Suma total de distancias.
        - total_gasolina: Suma total de costos de combustible.
    """
    total_ponderado = 0
    total_distancia = 0
    total_gasolina = 0
    
    for id_cedis, ruta in enumerate(rutas):
        costo_p, distancia, gasolina = costo_ruta(ruta, id_cedis, matriz_distancias, matriz_combustible)
        total_ponderado += costo_p
        total_distancia += distancia
        total_gasolina += gasolina
    
    return total_ponderado, total_distancia, total_gasolina

def generar_vecino(rutas):
    """
    Genera una nueva solución intercambiando tiendas entre rutas o entre la misma ruta.

    El método selecciona aleatoriamente dos rutas (pueden ser la misma ruta) y dos tiendas dentro de ellas
    para intercambiarlas. En caso de que una ruta esté vacía, se transfiere una tienda
    desde otra ruta.

    Args:
        rutas: Lista de rutas, cada una asociada a un CEDIS.

    Returns:
        list[list[int]]: Nueva lista de rutas modificada.
    """
    rutas_vecinas = [list(r) for r in rutas]  
    m = len(rutas_vecinas)
    r1 = random.randint(0, m - 1)
    posibles_rutas = [i for i in range(m) if len(rutas_vecinas[i]) > 0 and i != r1]
    
    if not posibles_rutas:
        return rutas_vecinas
    
    r2 = random.choice(posibles_rutas)

    # Caso: la ruta r1 está vacía
    if len(rutas_vecinas[r1]) == 0:
        nodo = rutas_vecinas[r2].pop(random.randint(0, len(rutas_vecinas[r2]) - 1))
        rutas_vecinas[r1].append(nodo)
        return rutas_vecinas
        
    # Caso general: intercambio entre rutas
    posibles_rutas = [i for i in range(m) if len(rutas_vecinas[i]) > 0]
    r2 = random.choice(posibles_rutas)    
    idxNodo1 = random.randint(0, len(rutas_vecinas[r1]) - 1)
    nodo1 = rutas_vecinas[r1][idxNodo1]
    idxNodo2 = random.randint(0, len(rutas_vecinas[r2]) - 1)
    
    while r1 == r2 and idxNodo1 == idxNodo2:
        idxNodo2 = random.randint(0, len(rutas_vecinas[r2]) - 1)
    
    nodo2 = rutas_vecinas[r2][idxNodo2]
    rutas_vecinas[r1][idxNodo1] = nodo2
    rutas_vecinas[r2][idxNodo2] = nodo1
    return rutas_vecinas


def optimizacion_recocido_simulado(df_tiendas, matriz_distancias, matriz_combustible,
                                    t0, factor_enfriamiento, iteraciones, tf):
    """
    Optimiza las rutas de distribución mediante el algoritmo de Recocido Simulado.

    Args:
        df_tiendas: DataFrame con información de las tiendas y CEDIS.
        matriz_distancias: Matriz con las distancias entre nodos.
        matriz_combustible: Matriz con el costo de gasolina entre nodos.
        t0: Temperatura inicial.
        factor_enfriamiento: Factor de disminución de temperatura.
        iteraciones: Número de iteraciones por temperatura.
        tf: Temperatura final.

    Returns:
        - mejor: Rutas optimizadas.
        - mejor_costo_pond: Costo ponderado mínimo encontrado.
        - mejor_distancia: Distancia total mínima encontrada.
        - mejor_gasolina: Costo total de gasolina mínimo encontrado.
        - historial_costos: Evolución del costo ponderado.
        - historial_distancias: Evolución de la distancia total.
        - historial_gasolinas: Evolución del costo de combustible.
        - historial_mejora: Porcentaje de mejora acumulada.
    """
    
    actual = solucion_inicial(df_tiendas, matriz_distancias)
    actual_costo_pond, actual_distancia, actual_gasolina = costo_total(actual, matriz_distancias, matriz_combustible)
    mejor = [list(r) for r in actual] 
    mejor_costo_pond = actual_costo_pond
    mejor_distancia = actual_distancia
    mejor_gasolina = actual_gasolina
    t = t0

    # Listas para las graficas
    historial_costos = []
    historial_distancias = []
    historial_gasolinas = []
    historial_mejora = []

    iter_global = 0
    while t > tf:
        for _ in range(iteraciones):
            vecino = generar_vecino(actual)
            vecino_costo_pond, vecino_distancia, vecino_gasolina = costo_total(vecino, matriz_distancias, matriz_combustible)
            delta = vecino_costo_pond - actual_costo_pond  

            if delta < 0 or random.random() < math.exp(-delta / t):
                actual = vecino
                actual_costo_pond = vecino_costo_pond
                actual_distancia = vecino_distancia
                actual_gasolina = vecino_gasolina

                if actual_costo_pond < mejor_costo_pond:
                    mejor = [list(r) for r in actual]
                    mejor_costo_pond = actual_costo_pond
                    mejor_distancia = actual_distancia
                    mejor_gasolina = actual_gasolina

            historial_costos.append(actual_costo_pond)
            historial_distancias.append(actual_distancia)
            historial_gasolinas.append(actual_gasolina)
            mejora = ((historial_costos[0] - mejor_costo_pond) / historial_costos[0]) * 100
            historial_mejora.append(mejora)
            
            iter_global += 1

        t *= factor_enfriamiento  

    return mejor, mejor_costo_pond, mejor_distancia, mejor_gasolina, historial_costos, historial_distancias, historial_gasolinas, historial_mejora
