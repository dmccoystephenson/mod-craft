# API Endpoint Reference

All endpoints are relative to `http://localhost:8080`.

Request and response bodies are JSON. Each endpoint below lists its success response. A mod is returned with the fields of the `POST /api/mods` request body plus its generated `id`; a modpack is returned in the shape shown in the `GET /api/modpacks/{id}/build` example response.

## Error Responses

`404` and `409` responses have a JSON body with a single `error` field:

```json
{
  "error": "Mod not found with id: 42"
}
```

`400` responses (validation failures) have a JSON body with one entry per invalid field, mapping the field name to its message:

```json
{
  "name": "must not be blank",
  "minecraftVersion": "must not be blank"
}
```

Errors not raised by the API itself — for example a malformed JSON body, a non-numeric ID in the path, or an unexpected server error — use Spring Boot's default error body instead:

```json
{
  "timestamp": "2026-10-20T08:38:49.681+00:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/mods/abc"
}
```

---

## Mod Endpoints

### GET /api/mods

**Description:** Returns all mods. Supports optional query parameters for filtering.  
**Query parameters:**
- `minecraftVersion` – filter by exact Minecraft version (e.g. `?minecraftVersion=1.20.1`)
- `search` – filter by partial name match, case-insensitive (e.g. `?search=opti`)

The filters do not combine: if both are given, `minecraftVersion` is applied and `search` is ignored.

**Response:** `200 OK` with a JSON array of mods (empty if none match).

**Example:**
```
GET /api/mods?minecraftVersion=1.20.1
```

---

### GET /api/mods/{id}

**Description:** Returns the mod with the given ID.  
**Path parameter:** `id` – mod ID  
**Response:** `200 OK` with the mod.  
**Errors:** `404` if the mod does not exist.

**Example:**
```
GET /api/mods/3
```

---

### POST /api/mods

**Description:** Creates a new mod.  
**Request body (JSON):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | yes | Mod name |
| `version` | string | yes | Mod version |
| `author` | string | no | Author name |
| `description` | string | no | Short description |
| `fileUrl` | string | no | Download URL for the mod JAR |
| `minecraftVersion` | string | yes | Target Minecraft version |

**Response:** `201 Created` with the created mod, including its generated `id`.  
**Errors:** `400` if a required field is missing or blank.

**Example:**
```json
{
  "name": "OptiFine",
  "version": "HD_U_I5",
  "author": "sp614x",
  "description": "Performance and graphics improvements",
  "fileUrl": "https://example.com/optifine.jar",
  "minecraftVersion": "1.20.1"
}
```

---

### PUT /api/mods/{id}

**Description:** Updates all fields of an existing mod.  
**Path parameter:** `id` – mod ID  
**Request body:** Same fields as `POST /api/mods`.  
**Response:** `200 OK` with the updated mod.  
**Errors:**
- `400` if a required field is missing or blank.
- `404` if the mod does not exist.

---

### DELETE /api/mods/{id}

**Description:** Deletes the mod with the given ID.  
**Path parameter:** `id` – mod ID  
**Response:** `204 No Content` with an empty body.  
**Errors:**
- `404` if the mod does not exist.
- `500` (Spring Boot's default error body) if the mod is still in a modpack. Remove it from every modpack first with `DELETE /api/modpacks/{id}/mods/{modId}`. See [#14](https://github.com/dmccoystephenson/mod-craft/issues/14).

---

## Modpack Endpoints

### GET /api/modpacks

**Description:** Returns all modpacks. Supports the same `minecraftVersion` and `search` query parameters as `GET /api/mods`.  
**Response:** `200 OK` with a JSON array of modpacks, each including its `mods` (empty if none match).

---

### GET /api/modpacks/{id}

**Description:** Returns the modpack with the given ID.  
**Path parameter:** `id` – modpack ID  
**Response:** `200 OK` with the modpack, including its `mods`.  
**Errors:** `404` if the modpack does not exist.

---

### POST /api/modpacks

**Description:** Creates a new modpack.  
**Request body (JSON):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | yes | Modpack name |
| `description` | string | no | Short description |
| `minecraftVersion` | string | yes | Target Minecraft version |

**Response:** `201 Created` with the created modpack, including its generated `id` and an empty `mods` array.  
**Errors:** `400` if a required field is missing or blank.

**Example:**
```json
{
  "name": "My Pack",
  "description": "A performance-focused modpack",
  "minecraftVersion": "1.20.1"
}
```

---

### PUT /api/modpacks/{id}

**Description:** Updates the name, description, and Minecraft version of a modpack.  
**Path parameter:** `id` – modpack ID  
**Request body:** Same fields as `POST /api/modpacks`.  
**Response:** `200 OK` with the updated modpack. Its `mods` are left unchanged.  
**Errors:**
- `400` if a required field is missing or blank.
- `404` if the modpack does not exist.

---

### DELETE /api/modpacks/{id}

**Description:** Deletes the modpack with the given ID. The mods it contained are not deleted.  
**Path parameter:** `id` – modpack ID  
**Response:** `204 No Content` with an empty body.  
**Errors:** `404` if the modpack does not exist.

---

### POST /api/modpacks/{id}/mods/{modId}

**Description:** Adds a mod to a modpack.  
**Path parameters:** `id` – modpack ID, `modId` – mod ID  
**Response:** `200 OK` with the updated modpack, including its `mods`.  
**Errors:**
- `404` if the modpack or mod does not exist.
- `409` if the mod is already in the modpack.

---

### DELETE /api/modpacks/{id}/mods/{modId}

**Description:** Removes a mod from a modpack. The mod itself is not deleted.  
**Path parameters:** `id` – modpack ID, `modId` – mod ID  
**Response:** `200 OK` with the updated modpack, including its remaining `mods`.  
**Errors:**
- `404` if the modpack does not exist, the mod does not exist, or the mod is not in the modpack.

---

### GET /api/modpacks/{id}/build

**Description:** Assembles and returns the modpack including all of its mods.  
**Path parameter:** `id` – modpack ID  
**Response:** `200 OK` with the modpack, including all of its `mods`.  
**Errors:** `404` if the modpack does not exist.

**Example response:**
```json
{
  "id": 1,
  "name": "My Pack",
  "description": "A performance-focused modpack",
  "minecraftVersion": "1.20.1",
  "mods": [
    {
      "id": 3,
      "name": "OptiFine",
      "version": "HD_U_I5",
      "author": "sp614x",
      "description": "Performance and graphics improvements",
      "fileUrl": "https://example.com/optifine.jar",
      "minecraftVersion": "1.20.1"
    }
  ]
}
```
