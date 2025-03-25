## 📌 Descripción General

Este proyecto implementa una herramienta completa de análisis léxico, que emula el funcionamiento de herramientas como **Yalex**, utilizando expresiones regulares para definir tokens y generando un **AFD (Autómata Finito Determinista)** directamente desde estas expresiones.

El sistema toma expresiones regulares (con una sintaxis estilo Yalex), las convierte en postfix, construye un árbol sintáctico abstracto (AST), calcula los conjuntos `firstpos`, `lastpos`, `followpos`, y finalmente genera un lexer en Java que reconoce los tokens definidos.

---

## 🔧 Tecnologías y Lenguajes

- Java (JDK 17+ recomendado)
- Graphviz (opcional, para visualización del AFD en `.dot` y `.png`)
  
---

## Pre-Requisito
### Windows
1. Descargar Graphviz en este link: https://graphviz.org/download/ 
2. Agregar el /bin a las variables de entorno y reiniciar la computadora
### MAC
```java
brew install graphviz 
```
---


## 🔄 Flujo del Programa

1. **Lectura del archivo `.yal`**  
   `YalParser` lee y convierte las definiciones de tokens a postfix.

2. **Conversión de Regex → Postfix**  
   Usando `RegexConverter`, se aplica el algoritmo de Shunting Yard.

3. **Construcción del AST**  
   `ASTBuilder` construye un árbol sintáctico, usando nodos con `firstpos`, `lastpos`, y `nullable`.

4. **Cálculo de Followpos**  
   Se procesan los nodos `^` (concatenación) y `*` para definir transiciones futuras.

5. **Construcción del AFD**  
   `AFDGenerator` transforma los conjuntos `followpos` en un autómata completo determinista.

6. **Generación del archivo `Lexer.java`**  
   Se genera código Java que representa el lexer, incluyendo la tabla de transiciones y estados finales.

7. **Prueba del lexer**  
   Se prueba con una cadena de entrada como `"1+2"` para validar el funcionamiento del analizador léxico.

---

## 🧪 Ejecución y Pruebas

### Paso 1: Ajustar el `path` al archivo `.yal`

En `Main.java`, asegúrate de colocar la ruta correcta del archivo `lexer.yal`:

```java
String rutaArchivo = "C:\\ruta\\completa\\lexer.yal";
```

### Paso 2: Ejecutar `Main.java`

Este archivo generará:

- El autómata `.dot` en `/media/other/`
- La imagen `.png` del autómata en `/media/img/`
- El archivo `Lexer.java` generado a partir del AFD

### Paso 3: Probar el Lexer

Edita la línea del `Main.java` con el input que desees:

```java
Lexer lexer = new Lexer("1+2");
System.out.println(lexer.tokenize());
```

### Resultado Esperado:

```bash
[NUM: "1"]
[PLUS: "+"]
[NUM: "2"]
```

---

## ✅ Características Implementadas

- Soporte completo para expresiones regulares con:
  - Concatenación (`^`)
  - Alternancia (`|`)
  - Cerradura de Kleene (`*`)
  - Caracteres escapados (`\n`, `\t`, `\u0000`)
- Tokens con identificadores como `NUM`, `PLUS`, `MINUS`, `LPAREN`, `RPAREN`, etc.
- AFD representado como estructura de transiciones (DFA)
- Exportación del lexer como archivo `.java` compilable
- Simulación completa del lexer
- Generación de gráficas en Graphviz

---

## 📸 Visualización de Autómatas

Los autómatas generados por el compilador se almacenan en los siguientes formatos:

![image](https://github.com/user-attachments/assets/41b3ff19-ce11-4021-a8ad-c64bb058d891)
