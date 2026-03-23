# CIVI Goldberg Globe

Aplicação Java 21 + Maven + JavaFX 3D que renderiza um globo técnico com células predominantemente hexagonais deformadas sobre uma esfera, inspirada na lógica de Goldberg polyhedra / dual geodésica.

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

## Arquitetura

- `app`: bootstrap da aplicação
- `controller`: cena, câmera e input
- `domain`: malha, célula e tipos
- `math`: vetores e fórmula de Goldberg
- `service`: geração, projeção, validação e seleção
- `ui`: renderização 3D e HUD mínima

## Observações

A versão atual prioriza uma base funcional e extensível: o globo inicia pronto, a navegação funciona, o zoom é limitado, a seleção exibe metadados e a malha já assume 12 regiões especiais de curvatura, com predominância de células hexagonais deformadas.
