# Juego de ajedrez en Java

Implementación de un juego de ajedrez por consola en Java, con generación y validación de movimientos legales de acuerdo con las reglas estándar del ajedrez (coherentes con manuales federativos como el indicado):

- Movimiento legal de todas las piezas: rey, dama, torre, alfil, caballo y peón.
- Capturas, incluidos movimientos diagonales del peón.
- Reglas especiales:
  - Enroque corto y largo.
  - Captura al paso.
  - Promoción de peón (`q`, `r`, `b`, `n`).
- Validación de jaque: no se permiten jugadas que dejen al propio rey en jaque.
- Detección de final por:
  - Jaque mate.
  - Ahogado.

## Compilar

```bash
javac src/ChessGame.java
```

## Ejecutar

```bash
java -cp src ChessGame
```

## Formato de jugadas

- Usa coordenadas largas: `e2e4`, `g1f3`, `e7e8q`.
- También admite guión o espacios (`e2-e4`, `e2 e4`) porque se normaliza la entrada.
- Escribe `salir` para terminar la partida.
