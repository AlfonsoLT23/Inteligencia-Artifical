import pandas as pd
import numpy as np
import time
import folium
import random
import matplotlib.pyplot as plt
from recocido_simulado import (
    solucion_inicial, costo_total, costo_ruta, optimizacion_recocido_simulado
)

# --- Cargar datos ---
ruta_nodos = "data/datos_distribucion_tiendas.csv"
ruta_matriz_distancias = "data/matriz_distancias.csv"
ruta_matriz_combustible = "data/matriz_costos_combustible.csv"

df_tiendas = pd.read_csv(ruta_nodos)
df_distancias = pd.read_csv(ruta_matriz_distancias)
df_combustible = pd.read_csv(ruta_matriz_combustible)

matriz_distancias = df_distancias.to_numpy()
matriz_combustible = df_combustible.to_numpy()

# --- Solución inicial ---
rutas_inicial = solucion_inicial(df_tiendas, matriz_distancias)
costo_inicial_pond, distancia_inicial, gasolina_inicial = costo_total(rutas_inicial, matriz_distancias, matriz_combustible)

print("=== SOLUCIÓN INICIAL ===")
print(f"Costo ponderado total: {costo_inicial_pond:.2f}")
print(f"Distancia total: {distancia_inicial:.2f}")
print(f"Gasolina total: {gasolina_inicial:.2f}")
for i, ruta in enumerate(rutas_inicial):
    costo_ruta_pond, distancia_ruta, gasolina_ruta = costo_ruta(ruta, i, matriz_distancias, matriz_combustible)
    print(f"Ruta {i}: {ruta} - Costo: {costo_ruta_pond:.2f}, Distancia: {distancia_ruta:.2f}, Gasolina: {gasolina_ruta:.2f}")

# --- Ejecución y medición de tiempo ---
print("\n=== OPTIMIZANDO CON RECOCIDO SIMULADO ===")
inicio_tiempo = time.time()

best_rutas, best_cost_pond, best_distancia, best_gasolina, historial_costos, historial_distancias, historial_gasolinas, historial_mejora = optimizacion_recocido_simulado(
    df_tiendas, matriz_distancias, matriz_combustible,
    t0=1000, factor_enfriamiento=0.99, iteraciones=1000, tf=1e-6
)

fin_tiempo = time.time()
tiempo_ejecucion = fin_tiempo - inicio_tiempo

# --- Resultados finales ---
print(f"\n=== SOLUCIÓN OPTIMIZADA ===")
print(f"Tiempo total: {tiempo_ejecucion:.2f} segundos")
print(f"Costo ponderado total: {best_cost_pond:.2f}")
print(f"Distancia total: {best_distancia:.2f}")
print(f"Gasolina total: {best_gasolina:.2f}")
for i, ruta in enumerate(best_rutas):
    costo_ruta_pond, distancia_ruta, gasolina_ruta = costo_ruta(ruta, i, matriz_distancias, matriz_combustible)
    print(f"Ruta {i}: {ruta} - Costo: {costo_ruta_pond:.2f}, Distancia: {distancia_ruta:.2f}, Gasolina: {gasolina_ruta:.2f}")

# --- Comparación ---
print(f"\n=== COMPARACIÓN ===")
print(f"Costo inicial: {costo_inicial_pond:.2f}")
print(f"Costo optimizado: {best_cost_pond:.2f}")
print(f"Mejora costo: {costo_inicial_pond - best_cost_pond:.2f} ({((costo_inicial_pond - best_cost_pond) / costo_inicial_pond * 100):.2f}%)")

print(f"\nDistancia inicial: {distancia_inicial:.2f}")
print(f"Distancia optimizada: {best_distancia:.2f}")
print(f"Mejora distancia: {distancia_inicial - best_distancia:.2f} ({((distancia_inicial - best_distancia) / distancia_inicial * 100):.2f}%)")

print(f"\nGasolina inicial: {gasolina_inicial:.2f}")
print(f"Gasolina optimizada: {best_gasolina:.2f}")
print(f"Mejora gasolina: {gasolina_inicial - best_gasolina:.2f} ({((gasolina_inicial - best_gasolina) / gasolina_inicial * 100):.2f}%)")

# --- Crear mapa ---
lat_media = df_tiendas["Latitud_WGS84"].mean()
lon_media = df_tiendas["Longitud_WGS84"].mean()
mapa = folium.Map(location=[lat_media, lon_media], zoom_start=12)

coords = df_tiendas[["Latitud_WGS84", "Longitud_WGS84"]].values

for i, fila in df_tiendas[df_tiendas["Tipo"] == "Centro de Distribución"].iterrows():
    folium.Marker(
        location=[fila["Latitud_WGS84"], fila["Longitud_WGS84"]],
        popup=f"{fila['Nombre']} (CEDIS {i})",
        icon=folium.Icon(color="red", icon="home")
    ).add_to(mapa)

for i, fila in df_tiendas[df_tiendas["Tipo"] == "Tienda"].iterrows():
    folium.Marker(
        location=[fila["Latitud_WGS84"], fila["Longitud_WGS84"]],
        popup=f"{fila['Nombre']} (Tienda {i})",
        icon=folium.Icon(color="blue", icon="shopping-cart")
    ).add_to(mapa)

colores = [
    "#FF0000",      
    "#0000FF",     
    "#00FF00",     
    "#FFFF00",     
    "#FF00FF",     
    "#00FFFF",     
    "#FFA500",     
    "#800080",     
    "#000000",     
    "#FFFFFF"      
]

random.shuffle(colores)

for id_cedis, ruta in enumerate(best_rutas):
    cedis_coord = coords[id_cedis]
    color_ruta = colores[id_cedis % len(colores)]
    puntos_ruta = [cedis_coord] + [coords[n] for n in ruta] + [cedis_coord]
    folium.PolyLine(
        puntos_ruta, color=color_ruta, weight=3, opacity=0.8,
        tooltip=f"Ruta CEDIS {id_cedis}"
    ).add_to(mapa)

mapa.save("mapa_rutas_optimizadas.html")
print("\nMapa generado: mapa_rutas_optimizadas.html")

# --- CREAR GRAFICAS ---

fig, axes = plt.subplots(2, 2, figsize=(12, 8))
fig.suptitle("Evolución del Recocido Simulado", fontsize=16, fontweight="bold")

# --- Costo Ponderado ---
axes[0, 0].plot(historial_costos, color='blue', linewidth=1.5)
axes[0, 0].set_title("Costo Ponderado Total")
axes[0, 0].set_xlabel("Iteraciones")
axes[0, 0].set_ylabel("Costo")
axes[0, 0].grid(True)

# --- Distancia Total ---
axes[0, 1].plot(historial_distancias, color='orange', linewidth=1.5)
axes[0, 1].set_title("Distancia Total")
axes[0, 1].set_xlabel("Iteraciones")
axes[0, 1].set_ylabel("Distancia")
axes[0, 1].grid(True)

# --- Consumo de Gasolina ---
axes[1, 0].plot(historial_gasolinas, color='green', linewidth=1.5)
axes[1, 0].set_title("Consumo de Gasolina")
axes[1, 0].set_xlabel("Iteraciones")
axes[1, 0].set_ylabel("Gasolina")
axes[1, 0].grid(True)

# --- Mejora porcentual ---
axes[1, 1].plot(historial_mejora, color='purple', linewidth=1.5)
axes[1, 1].set_title("Mejora respecto al inicio (%)")
axes[1, 1].set_xlabel("Iteraciones")
axes[1, 1].set_ylabel("Mejora (%)")
axes[1, 1].grid(True)

plt.tight_layout(rect=[0, 0, 1, 0.95])
plt.show()
