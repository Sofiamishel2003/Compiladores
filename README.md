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

#### resultado.txt
Los resultados de las cadenas cómo: 
```markdown
(1+2)*3    
1+2         
9+        
(5+3       
4*(2+))    
+1         
(          
((1+2))
(((((1+2)*((3+4))))))
@3+4
3 + 5$2
````

Deberían devolver resultados cómo estos: 
Indicando errores léxicos, rechazando las cadenas con errores grámaticales y sintácticos
![image](https://github.com/user-attachments/assets/35cdcd11-0750-4284-96e1-bc4e454ad147)
![image](https://github.com/user-attachments/assets/db927654-11ed-4ad0-8943-6a7eed324ad6)
Indicando los errores léxicos pero no rechazando la cadena por ellos
![image](https://github.com/user-attachments/assets/b71b778f-1863-401c-8468-e93f4a20e226)

---
### 6. Crear imagenes de los autómatas ya con el archivo.dot
Desde la raiz del proyecto Compiladores
```bash
 cd \app\src\main\java
```
Luego se generan las imagenes con:
```bash
  dot -Tpng parser/automata.dot -o parser/automata.png
  dot -Tpng parser/automataLR1.dot -o parser/automataLR1.png
  dot -Tpng parser/automataLALR.dot -o parser/automataLALR.png
```
Y se esperan resulatados así: 
- *Autómata LR0*
  ![image](https://github.com/user-attachments/assets/1fae3822-6a64-4d36-9433-dc5658433e42)

- *Autómata LALR*
  ![image](https://github.com/user-attachments/assets/edff4d49-b9c2-4569-a769-b34b5b2bd7b7)


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

## 🧠 Consideraciones

* El archivo `lexer.yal` contiene las reglas léxicas en una sintaxis personalizada.
* `parser.yalp` define la gramática en formato BNF simplificado.
* Puedes editar estos archivos en la raíz para probar nuevas reglas o cadenas.

---

