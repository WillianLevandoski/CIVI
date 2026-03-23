# CIVI Goldberg Globe

Aplicação Java 21 + Maven + JavaFX 3D que renderiza um globo técnico com células predominantemente hexagonais deformadas sobre uma esfera, inspirada em Goldberg polyhedra / malha geodésica dual.

## Requisitos

- Java 21
- Maven 3.9+

## Executar

```bash
mvn javafx:run
```

## Controles

- Mouse drag: rotaciona o globo
- Scroll: zoom in / zoom out
- Setas: giram os eixos X e Y
- `+` / `=`: zoom in
- `-`: zoom out
- `R`: reseta a câmera
- Clique em uma célula: seleciona e destaca

## Estrutura

- `app`: bootstrap (`AppLauncher`, `GlobeApp`)
- `controller`: cena, câmera e input
- `domain`: malha, célula e tipos
- `math`: vetores e fórmula de Goldberg
- `service`: geração, projeção, validação e seleção
- `ui`: renderização 3D, wireframe das células e HUD mínima

## Observações

- A malha usa 12 regiões especiais de curvatura e distribui as demais células como hexágonos ou hexágonos deformados sobre a esfera.
- A renderização combina preenchimento muito escuro com contornos 3D brancos separados para reforçar o contraste em fundo preto.
- A base foi organizada para evoluir a geração matemática sem acoplar geometria, renderização e interação.
