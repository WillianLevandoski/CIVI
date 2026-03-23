# Goldberg Globe JavaFX

Aplicação Java 21 com Maven e JavaFX 3D para gerar uma primeira versão funcional de um globo baseado em Goldberg polyhedron `GP(m,n)`.

## Como rodar

```bash
mvn javafx:run
```

## O que significam `m` e `n`

Os parâmetros `m` e `n` definem a família icosaédrica `GP(m,n)`.

- `T = m² + m*n + n²`
- pentágonos = `12`
- hexágonos = `10 * (T - 1)`
- faces totais = `10 * T + 2`
- vértices = `20 * T`
- arestas = `30 * T`


## Valor padrão adotado

A aplicação agora inicia com `m = 1` e `n = 1`, o que gera `T = 3`.

Esse valor reduz drasticamente a quantidade de células renderizadas na abertura, ajudando a diagnosticar problemas de exibição antes de testar malhas mais pesadas.

Para `GP(1,1)`:

- `T = 3`
- pentágonos = `12`
- hexágonos = `20`
- faces totais = `32`
- vértices = `60`
- arestas = `90`

## Estado atual

Esta entrega implementa a estrutura completa do projeto, as fórmulas clássicas, validação, seleção visual, rotação, zoom e uma malha inicial funcional sobre a esfera.

- Etapa 1: concluída com células distribuídas e identificáveis na esfera
- Etapa 2: preparada na arquitetura com `IcosahedronBuilder` e `GoldbergMeshBuilder` para evoluir a dual geodésica real
- Etapa 3: concluída com picking, destaque visual e painel lateral

## Exemplo com `m=1` e `n=1`

Para `GP(1,1)`:

- `T = 3`
- pentágonos = `12`
- hexágonos = `20`
- faces totais = `32`
- vértices = `60`
- arestas = `90`

Na interface, informe `m = 1` e `n = 1`, clique em **Gerar** e depois clique em uma célula para ver o ID e o tipo.
