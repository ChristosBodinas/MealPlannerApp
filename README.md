# MealPlannerApp

A REST API for planning meals and workouts.

Users build a personal library of foods and exercises, organize them into plans spanning multiple days,
and get nutritional targets, total monetary costs, and shopping lists calculated automatically.

Built with Java 25, Spring Boot 4.1, MySQL 9.7, and OAuth2 (JWT).

## Features

- Build and manage a personal library of foods, each with its own nutritional values, reference units, and pricing data
  across one or more vendors.
- Build and manage a personal library of exercises, each with several effort levels and associated calorie burn rates.
- Create one or more meal plans, each with its own body measurements, weight loss/gain target, desired macronutrient
  ratios, and number of days. Daily and plan-wide nutritional targets will be calculated automatically for you.
- Add foods and exercises to each day of a plan, and have total nutritional values and monetary costs calculated
  automatically for each day and for the plan as a whole.
- Generate a shopping list for any plan with a single request.

## Tech Stack

| Area        | Technology                                             |
|-------------|--------------------------------------------------------|
| Language    | Java 25                                                |
| Framework   | Spring Boot 4.1 (Spring Web MVC)                       |
| Persistence | Spring Data JPA, MySQL 9.7                             |
| Security    | Spring Security, OAuth2 Resource Server                |
| Validation  | Jakarta Bean Validation                                |
| Mapping     | MapStruct                                              |
| Monitoring  | Spring Boot Actuator                                   |
| Testing     | Spring Boot Test, Spring Security Test, H2 (in-memory) |
| Build       | Maven                                                  |

## Getting Started

### Prerequisites

- JDK 25
- A MySQL 8+ database the app can connect to
- An OAuth2 provider that issues JWT access tokens. [Authentication Setup](#authentication-setup) walks through running
  Keycloak in Docker, which also requires [Docker](https://docs.docker.com/get-docker/).

Maven isn't required, since the project includes the Maven wrapper.

### Configuration

The app reads its settings from these environment variables:

| Variable           | Description                                                                            |
|--------------------|----------------------------------------------------------------------------------------|
| `DB_URL`           | JDBC URL of the MySQL database, e.g. `jdbc:mysql://localhost:3306/mealplanner`         |
| `DB_USERNAME`      | Database username                                                                      |
| `DB_PASSWORD`      | Database password                                                                      |
| `OAUTH_ISSUER_URI` | Issuer URI of the OAuth2 provider, e.g. `http://localhost:8081/realms/mealplanner-app` |

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/ChristosBodinas/MealPlannerApp.git
   cd MealPlannerApp
   ```

2. Create an empty database. The app creates the tables itself on first start.

   ```sql
   CREATE DATABASE mealplanner;
   ```

3. Set up an OAuth2 provider by following [Authentication Setup](#authentication-setup), then come back here.

4. Set the environment variables in the terminal you'll run the app from.

   **Windows (PowerShell):**

   ```powershell
   $env:DB_URL = "jdbc:mysql://localhost:3306/mealplanner"
   $env:DB_USERNAME = "<db-user>"
   $env:DB_PASSWORD = "<db-password>"
   $env:OAUTH_ISSUER_URI = "http://localhost:8081/realms/mealplanner-app"
   ```

   **macOS / Linux / Git Bash:**

   ```bash
   export DB_URL=jdbc:mysql://localhost:3306/mealplanner
   export DB_USERNAME=<db-user>
   export DB_PASSWORD=<db-password>
   export OAUTH_ISSUER_URI=http://localhost:8081/realms/mealplanner-app
   ```

   These only apply to the current terminal session. If you run the app from an IDE, set them in its run configuration
   instead.

5. Run the app:

   ```bash
   # macOS / Linux / Git Bash
   ./mvnw spring-boot:run

   # Windows (PowerShell)
   .\mvnw spring-boot:run
   ```

The API is now available at `http://localhost:8080`.

## Authentication Setup

The API doesn't issue tokens itself. It validates JWTs from an OAuth2 provider. These steps use Keycloak in Docker, but
any provider that meets the requirements at the end should work.

### 1. Start Keycloak

Requires [Docker](https://docs.docker.com/get-docker/).

```bash
docker run --name keycloak -p 8081:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.7.1 start-dev
```

Keycloak is ready when the log shows `Listening on`. Leave this terminal open, because closing it stops the container.
The admin console is at `http://localhost:8081`, and the `admin`/`admin` credentials are for local development only. On
later runs, `docker start keycloak` is enough.

### 2. Import the realm and create a user

The repository includes a ready-made realm export at [
`keycloak/mealplanner-app-realm.json`](keycloak/mealplanner-app-realm.json).
Importing it creates the `mealplanner-app` realm and its client, so there's nothing to configure by hand.

1. Open `http://localhost:8081` and log in with `admin`/`admin`.
2. Open the realm dropdown at the top left and click **Create realm**.
3. Drag `keycloak/mealplanner-app-realm.json` into the **Resource file** box, or use **Browse** to select it.
4. Click **Create**. The `mealplanner-app` realm should now be selected in the dropdown.

The export doesn't include users, so create one:

1. Select **Users** in the sidebar and click **Add user**.
2. Enter a username and click **Create**.
3. On the **Credentials** tab, click **Set password**, enter a password, and switch **Temporary** off.

<details>
<summary>Setting up the client manually instead</summary>

Create a realm named `mealplanner-app`, then create a client with these settings:

- Client ID: `mealplanner-app`
- Client authentication: **Off** (public client)
- Authentication flow: **Direct access grants** enabled

</details>

### 3. Point the app at Keycloak

Set the `OAUTH_ISSUER_URI` environment variable to `http://localhost:8081/realms/mealplanner-app` before starting the
app. See [Getting Started](#getting-started) for the commands.

### 4. Get a token

Send a `POST` to `http://localhost:8081/realms/mealplanner-app/protocol/openid-connect/token` with an
`application/x-www-form-urlencoded` body:

| Field        | Value                |
|--------------|----------------------|
| `grant_type` | `password`           |
| `client_id`  | `mealplanner-app`    |
| `username`   | your Keycloak user   |
| `password`   | that user's password |

Use the `access_token` from the response as a Bearer token in the `Authorization` header of your requests. Request the
token through `localhost`, the same host as the issuer URI. A token issued via `127.0.0.1` has a different issuer, and
the API will reject it with a 401.

The password grant is fine for local development and testing. A production setup would use the authorization code flow
with PKCE instead.

### Requirements for other providers

- Issues signed JWTs, with a reachable issuer URI or JWK set.
- The `sub` claim uniquely identifies the user. The app stores it as the user's `authId`.

## API Endpoints

All endpoints require authentication and can only operate on the authenticated user's own account, foods, exercises,
plans, days, and entries.
Requesting an entity that doesn't exist or belongs to another user returns `404 Not Found`.

### Searching and Pagination

All search endpoints accept the following query parameters:

| Parameter    | Default | Description                                                                                        |
|--------------|---------|----------------------------------------------------------------------------------------------------|
| `searchText` | (none)  | Case-insensitive match against the name (and brand, in foods' case). Omit it to return everything. |
| `page`       | `0`     | Zero-based page index.                                                                             |
| `size`       | `20`    | Number of items per page.                                                                          |
| `sort`       | `name`  | Field to sort the items by.                                                                        |

### Account

| Method  | Path       | Description                    |
|---------|------------|--------------------------------|
| `PATCH` | `/account` | Update your account details.   |
| `GET`   | `/account` | Retrieve your account details. |

### Foods

| Method   | Path              | Description                                                                       |
|----------|-------------------|-----------------------------------------------------------------------------------|
| `POST`   | `/foods`          | Create a food, optionally with reference units and vendor data.                   |
| `PUT`    | `/foods/{foodId}` | Update a food, along with its reference units and vendor data.                    |
| `DELETE` | `/foods/{foodId}` | Delete a food, along with its reference units and vendor data.                    |
| `GET`    | `/foods/{foodId}` | Retrieve a single food, along with its reference units and vendor data.           |
| `GET`    | `/foods`          | Search your foods by name or brand. Reference units and vendor data not included. |

### Exercises

| Method   | Path                      | Description                                                |
|----------|---------------------------|------------------------------------------------------------|
| `POST`   | `/exercises`              | Create an exercise, optionally with effort levels.         |
| `PUT`    | `/exercises/{exerciseId}` | Update an exercise, along with its effort levels.          |
| `DELETE` | `/exercises/{exerciseId}` | Delete an exercise, along with its effort levels.          |
| `GET`    | `/exercises/{exerciseId}` | Retrieve a single exercise, along with its effort levels.  |
| `GET`    | `/exercises`              | Search your exercises by name. Effort levels not included. |

### Plans

| Method   | Path                      | Description                                                                     |
|----------|---------------------------|---------------------------------------------------------------------------------|
| `POST`   | `/plans`                  | Create a plan and its days, and calculate nutritional targets.                  |
| `PATCH`  | `/plans/{planId}`         | Update a plan's parameters, and recalculate nutritional targets.                |
| `DELETE` | `/plans/{planId}`         | Delete a plan, along with every day and entry in it.                            |
| `GET`    | `/plans/{planId}`         | Retrieve a single plan, along with its days.                                    |
| `GET`    | `/plans`                  | Search your plans by name. Days not included.                                   |
| `GET`    | `/plans/{planId}/summary` | Retrieve a summary (plan goals, plan stats, daily stats) of the specified plan. |
| `GET`    | `/plans/{planId}/shop`    | Retrieve a shopping list for the specified plan.                                |

### Days

| Method   | Path                    | Description                                                                         |
|----------|-------------------------|-------------------------------------------------------------------------------------|
| `DELETE` | `/days/{dayId}`         | Delete all entries in the specified day.                                            |
| `GET`    | `/days/{dayId}`         | Retrieve all entries in the specified day. Foods and exercises not included.        |
| `GET`    | `/days/{dayId}/summary` | Retrieve a summary (daily goals, daily stats, category stats) of the specified day. |

### Entries

| Method   | Path                          | Description                                                          |
|----------|-------------------------------|----------------------------------------------------------------------|
| `POST`   | `/days/{dayId}/entries`       | Create a food or exercise entry in the specified day.                |
| `POST`   | `/days/{dayId}/entries/paste` | Duplicate an existing entry and place it in the specified day.       |
| `PATCH`  | `/entries/{entryId}`          | Update a food or exercise entry's parameters.                        |
| `DELETE` | `/entries/{entryId}`          | Delete an entry.                                                     |
| `GET`    | `/entries/{entryId}`          | Retrieve a single entry, along with its referenced food or exercise. |

## Roadmap

- Hosted API documentation (e.g. a static OpenAPI page) for easier demonstration of features.
- Endpoint that lets the user manually redistribute nutritional targets across a plan's days as they see fit. (e.g. have
  some days with more calories than others)
- Endpoint that allows the user to reorder the entries in a given day, including across different categories.
- Integration of external food database so there's less need to manually define each food.

## Testing

```bash
# macOS / Linux / Git Bash
./mvnw test

# Windows (PowerShell)
.\mvnw test
```

Tests use their own configuration in `src/test/resources/application.properties` and run against an in-memory H2
database, so they don't need MySQL, Keycloak, or the environment variables from [Configuration](#configuration).
Reports are written to `target/surefire-reports`.

The suite covers most repository methods and the service layer. Service tests run against the real MapStruct mappers
instead of mocks, so they exercise the entity-to-DTO mapping as well as the business logic. Controllers and the security
configuration are not covered yet.

## Known Limitations

- The app currently lacks proper user registration. New users are automatically added to the database using their
  Keycloak credentials.
- The app currently offers no way to delete a user account.