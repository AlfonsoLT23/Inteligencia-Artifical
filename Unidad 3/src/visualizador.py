import matplotlib.pyplot as plt
import seaborn as sns

class Visualizador:
    """Clase encargada de graficar los resultados de la optimización."""
    def __init__(self, datos_originales, sensores_optimos_df):
        self.datos_originales = datos_originales
        self.sensores_optimos_df = sensores_optimos_df

    def graficar_mapa_sensores(self):
        """Muestra un mapa de los cultivos y las ubicaciones óptimas de los sensores."""
        print("Generando mapa de sensores...")
        plt.figure(figsize=(14, 9))
        
        sns.scatterplot(
            data=self.datos_originales,
            x='Longitud',
            y='Latitud',
            hue='Cultivo',
            style='Cultivo',
            s=100,
            alpha=0.7,
            palette='viridis'
        )
        
        plt.scatter(
            self.sensores_optimos_df['Longitud'],
            self.sensores_optimos_df['Latitud'],
            c='red',
            marker='X',
            s=250,
            label='Sensores Óptimos (PSO)',
            edgecolors='black',
            linewidth=1.5
        )
        
        plt.title('Mapa de Colocación Óptima de Sensores en Guasave', fontsize=16)
        plt.xlabel('Longitud', fontsize=12)
        plt.ylabel('Latitud', fontsize=12)
        plt.legend(loc='upper right', fontsize=11)
        plt.grid(True, linestyle='--', alpha=0.6)
        plt.show()

    def graficar_convergencia(self, historial_costos):
        """Muestra la evolución del costo durante las iteraciones."""
        print("Generando gráfico de convergencia...")
        plt.figure(figsize=(10, 6))
        plt.plot(historial_costos)
        plt.title('Convergencia del Algoritmo PSO', fontsize=16)
        plt.xlabel('Iteración', fontsize=12)
        plt.ylabel('Costo (Suma de distancias al cuadrado)', fontsize=12)
        plt.grid(True, linestyle='--', alpha=0.6)
        plt.show()
