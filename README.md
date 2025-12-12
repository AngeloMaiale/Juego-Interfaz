Nombre del juego
Conecta3. Juego con estética oscura y panel central redondeado, diseñado como ejemplo didáctico en Java.

Temática general
Es un rompecabezas casual donde intercambias fichas de colores para alinear tres o más del mismo tipo. Visualmente tiene un tablero central con fichas redondeadas, HUD superior 
con puntuación y tiempo, y diálogos estilizados que mantienen la misma apariencia que la ventana principal.

Instrucciones y mecánicas
El jugador selecciona una ficha con clic (o presiona y suelta) y la arrastra o suelta sobre una ficha adyacente para intercambiar. Si el intercambio forma una línea de 3, 4 o 5 
fichas iguales se elimina el grupo y se otorgan 100 / 200 / 500 puntos respectivamente. Las fichas desaparecidas hacen cascada: las de arriba caen y se rellenan desde arriba; 
las eliminaciones encadenadas se procesan automáticamente. Si tras una cascada no hay movimientos posibles el tablero se mezcla (shuffle) hasta garantizar al menos un movimiento.
Cuando se acaba el tiempo aparece un diálogo con opción de jugar otra partida o salir.

Funcionamiento general del código
El proyecto sigue un patrón MVC simple: GameModel contiene la lógica del tablero (matriz board, detección de runs, removeRunsAndCollapse, hasPossibleMove, shuffleUntilSolvable), 
GameView dibuja la interfaz (HUD, boardHolder, offsets y alpha para animaciones) y GameController orquesta la interacción (ratón, temporizador, animaciones y flujo de juego). 
Las animaciones (swap, disolución, caída) usan javax.swing.Timer para interpolar offsets y valores alpha; la vista mantiene matrices offsetX, offsetY y alpha para animar solo las 
fichas que cambian. Los diálogos personalizados están en StyledDialog y usan paneles redondeados y botones estilizados para mantener coherencia visual. El fondo exterior y el 
rectángulo del tablero se pintan por separado para evitar que el color del contenedor se mezcle con el del tablero.

Compilar y ejecutar
El proyecto consiste en un archivo Match3MVC.java y varios archivos clase que siguen el modelo MVC + un Styled Dialog. Para ejecutarlo se debe compilar el Match3MVC.java
