# User Guide

## Prerequisites

- Java 17 or newer installed on the host machine.
- The mod-craft JAR built and running (see [README.md](README.md) for installation steps).
- An HTTP client such as `curl`, Postman, or your browser for `GET` requests.

## First Steps

After starting the application the API is available at `http://localhost:8080`. All resources are under the `/api` prefix.

### Verify the service is running

```
curl http://localhost:8080/api/mods
```

An empty JSON array (`[]`) confirms the service is up.

## Common Scenarios

### 1. Add a mod

```
curl -X POST http://localhost:8080/api/mods \
  -H "Content-Type: application/json" \
  -d '{
    "name": "OptiFine",
    "version": "HD_U_I5",
    "author": "sp614x",
    "description": "Performance and graphics improvements",
    "fileUrl": "https://example.com/optifine.jar",
    "minecraftVersion": "1.20.1"
  }'
```

### 2. Create a modpack

```
curl -X POST http://localhost:8080/api/modpacks \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Pack",
    "description": "A performance-focused modpack",
    "minecraftVersion": "1.20.1"
  }'
```

### 3. Add a mod to a modpack

Replace `{modpackId}` and `{modId}` with the IDs returned by the previous calls.

```
curl -X POST http://localhost:8080/api/modpacks/{modpackId}/mods/{modId}
```

### 4. Build (assemble) a modpack

```
curl http://localhost:8080/api/modpacks/{modpackId}/build
```

The response contains the full modpack object including all of its assembled mods.

### 5. Search for mods by name

```
curl "http://localhost:8080/api/mods?search=opti"
```

### 6. Filter mods by Minecraft version

```
curl "http://localhost:8080/api/mods?minecraftVersion=1.20.1"
```

### 7. Remove a mod from a modpack

```
curl -X DELETE http://localhost:8080/api/modpacks/{modpackId}/mods/{modId}
```

## Permissions

mod-craft is a standalone REST API with no built-in authentication. Access control should be handled at the network or proxy layer (e.g. a reverse proxy that restricts access to trusted clients).
