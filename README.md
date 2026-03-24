# CIVI Icosahedral Hex Globe

Projeto Java 21 + Maven + JavaFX 3D que gera um globo composto por células geométricas reais usando a abordagem prática do Stack Overflow: recorte triangular de grade hexagonal 2D, 20 faces de um icosaedro e mapeamento para triângulos esféricos com coordenadas baricêntricas + double slerp.

## Como executar

```bash
mvn javafx:run
```

## Controles

- Arrastar com o mouse: rotaciona o globo
- Scroll: zoom
- `W`, `A`, `S`, `D` ou setas: rotação
- `Q` / `E`: giro axial
- `+` / `-`: zoom
- `R`: reset da câmera

## Matemática usada

1. Um icosaedro fornece 12 vértices e 20 faces triangulares regulares sobre a esfera.
2. Em um triângulo 2D base `(-0.5, 0)`, `(0.5, 0)`, `(0, sqrt(3)/2)`, é criado um recorte de grade hexagonal em função de `resolution`.
3. Cada centro e cada vértice 2D é mapeado para o triângulo esférico correspondente com coordenadas baricêntricas e interpolação esférica dupla (`slerp`).
4. As bordas compartilhadas dos patches são soldadas e os fragmentos são reunidos em células finais predominantemente hexagonais, com os 12 pentágonos inevitáveis nos vértices do icosaedro.

## Estrutura principal

- `Main.java`: inicialização JavaFX e cena 3D
- `Vec2.java` / `Vec3.java`: matemática vetorial
- `IcosahedronBuilder.java`: icosaedro base
- `HexGridTriangleBuilder.java`: patch hexagonal 2D recortado
- `SphericalMapper.java`: baricêntricas + `slerp`
- `Cell.java`: célula navegável/indexável
- `GlobeMesh.java`: coleção final e arestas renderizáveis
- `GlobeMeshBuilder.java`: montagem global da malha
- `CameraController.java` / `InputController.java`: navegação

A estrutura já deixa o projeto pronto para evoluir para ray picking, identificação de célula por mouse e navegação baseada em adjacência.
