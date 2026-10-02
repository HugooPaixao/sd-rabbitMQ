### Comandos necessários para rodar a aplicação

Adicione as imagens no caminho clientes/c1 e clientes/c2 ou use as que já estão lá.

Constrói e sobe a aplicação:

```bash
docker compose up -d --build
```

Enviar as imagens:
```bash
docker compose run --rm newtask1 & docker compose run --rm newtask2
```

Conferir as imagens em cinza abrindo as pastas servidores/s1 e servidores/s2 ou com:
```bash
ls servidores/s1 servidores/s2
```

Para e remove os containers:
```bash
docker compose down
```