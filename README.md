# CIVI Hex Sphere (Java)

Projeto refeito em Java (Swing/AWT), com a lógica de geração da esfera hex/pentagonal portada do algoritmo de referência abaixo.

Fonte do algoritmo original:

- https://stackoverflow.com/a/46787885
- Autor: Spektre (com modificações da comunidade, conforme Timeline do post)
- Licença: CC BY-SA 3.0
- Data de recuperação: 2026-03-24

## Como executar

```bash
mvn -DskipTests compile
java -cp target/classes com.civi.globe.Main
```

## Controles

- `↑` / `↓`: inclinar em X
- `←` / `→`: girar em Y

## Estrutura

- `Main.java`: app Swing + renderização 2D em perspectiva.
- `HexSphereBuilder.java`: porta do algoritmo `hex_sphere(N, R)` para Java.
- `PointTable.java`: tabela de pontos com deduplicação.
- `HexCell.java`: célula hexagonal/pentagonal (6 índices, último duplicado para pentágonos).
- `Vec3.java`: vetor 3D simples.
