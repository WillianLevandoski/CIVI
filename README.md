# CIVI Hex Sphere (Java)

Projeto refeito em Java (JavaFX), com a lógica de geração da esfera hex/pentagonal portada do algoritmo de referência abaixo.

Fonte do algoritmo original:

- https://stackoverflow.com/a/46787885
- Autor: Spektre (com modificações da comunidade, conforme Timeline do post)
- Licença: CC BY-SA 3.0
- Data de recuperação: 2026-03-24

## Como executar

```bash
mvn javafx:run
```

## Controles

- `↑` / `↓`: inclinar em X
- `←` / `→`: girar em Y

## Textura por hexágono

- O renderer aplica uma textura em cada hexágono usando o arquivo:
  - `src/main/resources/textures/hex-tile.png`
- O fundo rosa/magenta da textura é removido automaticamente (chroma key) durante o carregamento.
- A imagem é esticada para caber no hexágono e é recortada pelo contorno do polígono, sem vazar para os vizinhos.
- Se o arquivo não existir, o app faz fallback para o preenchimento por cor.

## Estrutura

- `Main.java`: app JavaFX + renderização 2D em perspectiva.
- `HexSphereBuilder.java`: porta do algoritmo `hex_sphere(N, R)` para Java.
- `PointTable.java`: tabela de pontos com deduplicação.
- `HexCell.java`: célula hexagonal/pentagonal (6 índices, último duplicado para pentágonos).
- `Vec3.java`: vetor 3D simples.
