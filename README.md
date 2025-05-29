# 🛠️ Compilador en Java: YALex + YAPar

Este proyecto implementa un compilador en Java que incluye análisis léxico (YALex) y análisis sintáctico (YAPar), con soporte para generación de AFD, parsing LR(0) y LALR(1), y visualización de errores.

## 📁 Estructura del Proyecto

```markdown

Compiladores/
├── app/
│   └── src/
│       └── main/
│           └── java/
│               ├── clases/
|                    ├── AFDGenerator.java
|                    ├── ASTBuilder.java
|                    ├── ASTNode.java
|                    ├── Lexer.java
|                    ├── RegexConverter.java
|                    ├── Stack.java
|                    ├── Symbol.java
|                    └── YalParser.java
│               ├── parser/
|                    ├── automata/
|                        ├── AutomataLALR.java
|                        ├── AutomataLR(0).java
|                        ├── Estado.java
|                        ├── EstadoLALR.java
|                        ├── Item.java
|                        ├── ItemLALR.java
|                        ├── LALRTableGenerator.java
|                        ├── LR0TableGenerator.java
|                        ├── NucleoEstado.java
|                        └── YalpParser.java
|                    ├── utils/
|                        └── GramaticaUtils.java
|                    ├── automata.dot
|                    ├── automataLALR.dot
|                    ├── automataLR1.dot
|                    └── Yapar.java
│               └── Main.java
├── cadenas.txt
├── lexer.yal
├── parser.yalp
├── resultado.txt
├── build.gradle
├── README.md
└── ...

````

- `lexer.yal`: archivo con definiciones léxicas
- `parser.yalp`: archivo con la gramática
- `cadenas.txt`: cadenas de prueba para validar
- `resultado.txt`: salida con los análisis de errores LR(0) y LALR
- `app/src/main/java/`: contiene el código fuente

---

## ▶️ Instrucciones para compilar y ejecutar

### 1. Posicionarse en la raíz del proyecto

```bash
cd Compiladores
````

### 2. Ir a la carpeta con el código fuente

```bash
cd app/src/main/java
```

### 3. Compilar todos los archivos

```bash
javac parser/Yapar.java parser/*.java parser/automata/*.java parser/utils/*.java clases/*.java
```

> Esto generará todos los autómatas y clases necesarias para el compilador, incluyendo `Lexer.java` y las tablas LR(0)/LALR(1).

### 4. Regresar a la raíz

```bash
cd ../../..
```

### 5. Ejecutar YAPar con los archivos fuente

```bash
java -cp src/main/java parser.Yapar parser.yalp -l lexer.yal -o theparser
```

Esto:

* Lee `lexer.yal` y `parser.yalp`
* Construye el AFD del lexer
* Genera autómatas LR(0) y LALR(1)
* Analiza las cadenas de `cadenas.txt`
* Escribe los resultados y errores en `resultado.txt`

---

## 📦 Requisitos

* Java 11 o superior
* Editor recomendado: Visual Studio Code (con soporte para Java y Gradle)
* Gradle (opcional, si deseas automatizar la compilación)

---

## 🧠 Consideraciones

* El archivo `lexer.yal` contiene las reglas léxicas en una sintaxis personalizada.
* `parser.yalp` define la gramática en formato BNF simplificado.
* Puedes editar estos archivos en la raíz para probar nuevas reglas o cadenas.

---

