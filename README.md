# Goldberg Globe JavaFX

Projeto Java 21 com Maven e JavaFX 3D que gera uma primeira versão funcional de um globo baseado na família icosaédrica `GP(m,n)`.

## Como rodar

```bash
mvn javafx:run
```

## O que significam `m` e `n`

Os parâmetros `m` e `n` controlam a frequência Goldberg:

- `T = m² + m*n + n²`
- pentágonos = `12`
- hexágonos = `10 * (T - 1)`
- faces totais = `10 * T + 2`
- vértices = `20 * T`
- arestas = `30 * T`

A aplicação valida `m >= 0`, `n >= 0` e bloqueia `m = 0` com `n = 0`.

## Estado da implementação

A entrega atual já está executável e separada em duas partes:

- núcleo matemático e geração da malha
- visualização 3D interativa em JavaFX

A geometria usa uma primeira aproximação funcional:

1. posiciona os 12 pentágonos nos vértices normalizados do icosaedro
2. distribui os hexágonos restantes sobre a esfera
3. constrói uma vizinhança local
4. gera polígonos pentagonais e hexagonais tangentes à esfera

Essa base deixa o projeto pronto para evoluir para uma dual geodésica mais precisa sem quebrar a arquitetura.

## Controles

- mouse drag: rotacionar
- scroll do mouse: zoom
- setas do teclado: navegar pelo globo
- `+` ou `=`: zoom in
- `-`: zoom out
- clique em uma célula: destacar e mostrar ID/tipo

## Aparência atual

- globo preto
- linhas das células em branco
- destaque em preto e branco

## Exemplo com `m=1` e `n=1`

Para `GP(1,1)`:

- `T = 3`
- pentágonos = `12`
- hexágonos = `20`
- faces totais = `32`
- vértices = `60`
- arestas = `90`

Uso:

1. execute `mvn javafx:run`
2. informe `m = 1` e `n = 1`
3. clique em **Gerar**
4. clique em um hexágono para ver o `ID` e o `tipo`
