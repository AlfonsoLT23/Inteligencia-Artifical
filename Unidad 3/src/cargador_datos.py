import pandas as pd
from sklearn.preprocessing import MinMaxScaler

class CargadorDatos:
    """
    Clase para cargar y preprocesar los datos de los cultivos.
    
    Realiza las siguientes tareas:
    1. Carga el archivo CSV.
    2. Convierte columnas categóricas ('Cultivo') a numéricas (One-Hot Encoding).
    3. Escala todas las características al rango [0, 1], necesario para PSO,
       ya que el algoritmo es sensible a las escalas de los datos.
    """
    def __init__(self, ruta_archivo):
        self.ruta_archivo = ruta_archivo
        self.datos_originales = None
        self.datos_procesados = None
        self.escalador = None
        self.columnas_caracteristicas = None

    def cargar_y_preprocesar(self):
        """Carga y preprocesa los datos."""
        print(f"Cargando datos desde '{self.ruta_archivo}'...")
        # Cargar datos
        self.datos_originales = pd.read_csv(self.ruta_archivo)
        
        # 1. Convertir la columna 'Cultivo' en variables dummy (One-Hot Encoding)
        datos_codificados = pd.get_dummies(self.datos_originales, columns=['Cultivo'])
        
        # Guardar los nombres de las columnas
        self.columnas_caracteristicas = datos_codificados.columns
        
        # 2. Escalar todas las características al rango [0, 1]
        print("Aplicando escalado MinMax a los datos...")
        self.escalador = MinMaxScaler()
        self.datos_procesados = self.escalador.fit_transform(datos_codificados)
        
        print(f"Datos preprocesados con {self.datos_procesados.shape[1]} características:")
        print(list(self.columnas_caracteristicas))
        print("-" * 30)
