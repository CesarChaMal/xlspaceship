# CLAUDE.md - AI Assistant Guide for XL Spaceship

## Project Overview

**XL Spaceship** is a multiplayer battleship-style game implementation built with Play Framework (Scala). The application provides a RESTful API for a two-player space combat game where players place spaceships on a 16x16 grid and take turns firing salvos to destroy opponent ships.

### Key Features
- RESTful API for game management (create, join, play)
- Two-player turn-based gameplay
- Multiple spaceship types with unique shapes
- 16x16 game board with coordinate system
- Real-time game status tracking
- CORS-enabled for cross-origin requests

## Technology Stack

### Core Technologies
- **Scala**: 2.13.13
- **Play Framework**: 2.9.0
- **Build Tool**: SBT 1.9.7
- **JVM**: OpenJDK 11
- **Testing**: Specs2

### Key Dependencies
- **Guice**: Dependency injection framework (Play's built-in DI)
- **Play WS**: HTTP client for inter-service communication
- **Akka**: Actor system (2.6.21) for asynchronous processing
- **Scala XML**: XML processing (2.2.0)
- **SLF4J/Logback**: Logging framework

### Development Tools
- Docker support for containerized deployment
- Heroku deployment configuration (app.json)
- Shell scripts for common operations

## Architecture Overview

### Design Pattern
The application follows a **layered MVC architecture** with dependency injection:

```
Controllers → Services → Models
     ↓
  Formatters/Connectors
```

### Architectural Layers

1. **Controllers** (`app/controllers/`)
   - Handle HTTP requests/responses
   - Route handling and request validation
   - CORS header management
   - Thin layer delegating to services

2. **Services** (`app/services/`)
   - Business logic implementation
   - Game state management
   - Board operations and spaceship placement
   - In-memory game storage using mutable maps

3. **Models** (`app/models/`)
   - Domain entities (case classes)
   - Immutable data structures
   - Type-safe representation of game state

4. **Formatters** (`app/formatters/`)
   - JSON serialization/deserialization
   - Custom Play JSON formatters
   - Type-safe JSON conversion

5. **Connectors** (`app/connectors/`)
   - External service communication
   - HTTP client wrappers
   - API integration utilities

## Directory Structure

```
xlspaceship/
├── app/                          # Application source code
│   ├── connectors/               # External API connectors
│   │   ├── GameFormatter.scala
│   │   ├── GameRequestFormatter.scala
│   │   └── SalvoStatusFormatter.scala
│   ├── controllers/              # HTTP request handlers
│   │   ├── Application.scala     # Main game endpoints
│   │   ├── BaseController.scala  # Base controller trait
│   │   └── WSClientController.scala  # HTTP client operations
│   ├── formatters/               # JSON formatters
│   │   └── JsonFormatters.scala  # Play JSON implicit formatters
│   ├── models/                   # Domain models
│   │   ├── Board.scala           # Game board (16x16 grid)
│   │   ├── Cell.scala            # Individual board cell
│   │   ├── Coordinates.scala     # X,Y position
│   │   ├── Game.scala            # Game state
│   │   ├── GameRequest.scala     # Game creation request
│   │   ├── GameStatus.scala      # Current game status
│   │   ├── Hit.scala             # Salvo hit result
│   │   ├── Player.scala          # Player entity
│   │   ├── Protocol.scala        # Network protocol config
│   │   ├── Row.scala             # Board row
│   │   ├── Salvo.scala           # Attack salvo
│   │   ├── SalvoStatus.scala     # Salvo result
│   │   ├── Spaceship.scala       # Base spaceship
│   │   └── XLSpaceship.scala     # Extended spaceship with parts
│   └── services/                 # Business logic
│       ├── BoardService.scala    # Board management and masking
│       ├── GameService.scala     # Game lifecycle and rules
│       ├── IDGeneratorService.scala  # Unique ID generation
│       ├── PlayerService.scala   # Player creation/management
│       ├── SpaceshipService.scala    # Spaceship placement
│       └── WSClientService.scala     # HTTP client service
├── conf/                         # Configuration files
│   ├── application.conf          # Play app configuration
│   ├── logback.xml               # Detailed logging config
│   ├── logback_basic.xml         # Basic logging config
│   └── routes                    # HTTP route definitions
├── docs/                         # Documentation
│   ├── info.txt
│   └── spaceXL challenge.pdf     # Game specification
├── dumps/                        # Data dumps (game states)
├── project/                      # SBT build configuration
│   ├── build.properties          # SBT version
│   ├── plugins.sbt               # SBT plugins (Play, packager)
│   ├── repositories              # Maven repositories
│   └── offline.repositories
├── repository/                   # Local artifact repository
├── scripts/                      # Build/deployment scripts
│   ├── build.sh                  # Production build
│   ├── deploy.sh                 # Heroku deployment
│   ├── production.sh             # Staged production build
│   ├── server.sh                 # Development server
│   └── tests.sh                  # Test execution
├── scripts_game/                 # Game simulation scripts
│   ├── game/                     # Game state storage
│   ├── accept_salvo.sh           # Accept opponent salvo
│   ├── fire_salvo.sh             # Fire salvo at opponent
│   ├── get_status.sh             # Get current game status
│   ├── play_loop.sh              # Automated gameplay loop
│   ├── play_loop_lose.sh         # Lose scenario simulation
│   ├── play_loop_user123.sh      # User 123 game loop
│   ├── play_loop_user456.sh      # User 456 game loop
│   ├── play_loop_win.sh          # Win scenario simulation
│   ├── render_board.sh           # Board visualization
│   ├── start_game.sh             # Generic game starter
│   ├── start_game_user123.sh     # User 123 game setup
│   ├── start_game_user456.sh     # User 456 game setup
│   ├── start_server.sh           # Single server startup
│   └── start_servers.sh          # Multi-server startup
├── test/                         # Test suite
│   ├── controllers/
│   │   └── ApplicationSpec.scala # Controller tests
│   └── IntegrationSpec.scala     # Integration tests
├── tutorial/                     # Tutorial content
├── build.sbt                     # SBT build definition
├── Dockerfile                    # Docker container config
├── app.json                      # Heroku deployment config
├── LICENSE                       # License file
└── README.md                     # Project documentation
```

## Key Components

### Models (app/models/)

#### Core Entities
- **Game**: Represents a game session with players, state, and winner
  - Fields: `id`, `self`, `opponents`, `complete`, `winner`, `nextTurn`
  - Mutable state for game completion status

- **Player**: Represents a player in the game
  - Fields: `id`, `name`, `spaceships` (Option), `board`
  - Contains optional spaceship array and dedicated board

- **Board**: 16x16 grid game board
  - Fields: `id`, `rows` (Array[Row])
  - Rows indexed 0-15, each containing 16 cells

- **Cell**: Individual board position
  - Fields: `x`, `y`, `status`
  - Status values: `.` (empty), `*` (ship), `X` (hit), `-` (miss), `hit`, `kill`

- **XLSpaceship**: Spaceship with destructible parts
  - Fields: `name`, `parts` (ArrayBuffer[Coordinates]), `active`
  - Parts removed when hit; inactive when all parts destroyed

- **Coordinates**: Position on the board
  - Fields: `x`, `y` (both 0-15)
  - Used for spaceship placement and targeting

#### Request/Response Models
- **GameRequest**: Request to create/join a game
- **GameStatus**: Current state of the game for a player
- **Salvo**: Attack containing multiple coordinate hits
- **SalvoStatus**: Result of a salvo attack
- **Hit**: Individual shot result (position + status)

### Services (app/services/)

#### GameService
**Primary game logic coordinator**
- `createGame(GameRequest)`: Creates new game with initial player
- `joinGame(GameRequest)`: Adds second player to existing game
- `getGameStatus(gameID, requesterId)`: Returns player-specific view
- `acceptSalvo(gameID, Salvo)`: Processes incoming attack
- `createSalvo(gameId, requesterId)`: Generates random salvo
- Uses in-memory `Map[String, Game]` for game storage
- Implements board masking for fog-of-war

#### BoardService
**Board creation and manipulation**
- `createBoard()`: Generates new 16x16 board
- `maskOpponentBoard(Board)`: Hides opponent ship positions
- `maskSelfBoard(Board)`: Hides unhit ship positions
- `deepCloneBoard(Board)`: Creates independent board copy
- `allocateCoordinates(Board, config, range)`: Places spaceships
- `updateBoard(Board, coordinates, status)`: Updates cell states
- `saveBoard(playerId, Board)`: Stores board by player ID
- `findByPlayerId(playerId)`: Retrieves player's board

#### PlayerService
**Player creation and management**
- `createPlayerFromRequest(GameRequest)`: Creates player with ships
- Generates unique player IDs
- Creates and initializes player board
- Places spaceships randomly on board

#### SpaceshipService
**Spaceship configuration and placement**
- Loads spaceship shapes from `application.conf`
- Types: angle, aClass, bClass, sClass, winger, range
- Validates spaceship placement on board
- Creates XLSpaceship instances with parts

#### IDGeneratorService
**Unique ID generation**
- Atomic counter for sequential IDs
- Thread-safe ID generation
- Used for games, boards, and entities

#### WSClientService
**HTTP client for inter-service communication**
- Wraps Play WS client
- Handles salvo firing to opponent
- Manages HTTP requests/responses

### Controllers (app/controllers/)

#### Application
**Main game API endpoints**
- `GET /` - Health check endpoint
- `POST /xl-spaceship/protocol/game/new` - Create new game
- `POST /xl-spaceship/protocol/game/join` - Join existing game
- `PUT /xl-spaceship/protocol/game/:gameID` - Accept opponent salvo
- `GET /xl-spaceship/user/game/:gameID?player_id=X` - Get game status
- `OPTIONS` endpoints for CORS preflight

**Key Implementation Details:**
- CORS headers on all responses
- JSON request/response handling
- Extensive debug logging for troubleshooting
- Board masking before status responses

#### WSClientController
**Client-side operations**
- `PUT /xl-spaceship/user/game/:gameID/fire` - Fire salvo at opponent

## Configuration

### application.conf (conf/application.conf)

#### Spaceship Configurations
Six spaceship types defined with coordinate arrays:
- **range**: 15-cell large ship (3x5 rectangle)
- **winger**: 9-cell medium ship (wing shape)
- **angle**: 6-cell small ship (L-shape)
- **aClass**: 8-cell ship (A-shape)
- **bClass**: 10-cell ship (B-shape)
- **sClass**: 8-cell ship (S-shape)

Coordinates format: `{"x":0,"y":0}` relative to placement origin

#### Security Settings
- Application secret for session management
- Crypto secret for Play Framework
- Language configuration (English)

### routes (conf/routes)

HTTP endpoint mappings:
```
GET  /                                              Health check
POST /xl-spaceship/protocol/game/new               Create game
POST /xl-spaceship/protocol/game/join              Join game
PUT  /xl-spaceship/protocol/game/:gameID           Accept salvo
GET  /xl-spaceship/user/game/:gameID               Get status
PUT  /xl-spaceship/user/game/:gameID/fire          Fire salvo
OPTIONS endpoints for CORS                         Preflight
```

## Development Workflow

### Starting Development Server

```bash
./scripts/server.sh
# Equivalent to: sbt run
```

Server starts on `localhost:9000` by default.

### Running Tests

```bash
./scripts/tests.sh
# Runs the complete Specs2 test suite
```

### Building for Production

#### Binary Distribution
```bash
./scripts/build.sh
# Creates: ./target/universal/scala-dci-<VERSION>.zip
```

#### Staged Build
```bash
./scripts/production.sh
# Creates: ./target/universal/stage/
# Run with: ./target/universal/stage/bin/scala-dci
```

### Docker Deployment

```bash
docker build -t xlspaceship .
docker run -p 9000:9000 xlspaceship
```

Dockerfile:
- Uses OpenJDK 11 base image
- Installs SBT 1.9.7
- Compiles application during build
- Runs `sbt run` as CMD

### Heroku Deployment

```bash
./scripts/deploy.sh
```

Or use the Deploy to Heroku button in README.md.

## Game Simulation Workflow

### Two-Player Game Setup

#### Terminal 1: Start Server
```bash
./scripts_game/start_server.sh
# Launches server on localhost:9000
```

#### Terminal 2: Create Game (Player 1)
```bash
./scripts_game/start_game_user123.sh
# Creates game as user123
# Saves response to game/new_game_user123.json
```

#### Terminal 3: Join Game (Player 2)
```bash
./scripts_game/start_game_user456.sh
# Joins game as user456
# Saves response to game/new_game_user456.json
```

#### Terminal 4: Player 1 Attack Loop
```bash
./scripts_game/play_loop_user123.sh
# Continuously fires salvos as user123
```

#### Terminal 5: Player 2 Defense Loop
```bash
./scripts_game/play_loop_user456.sh
# Accepts salvos as user456
```

### Testing Scenarios

#### Win Simulation
```bash
./scripts_game/play_loop_win.sh
```

#### Lose Simulation
```bash
./scripts_game/play_loop_lose.sh
```

#### Check Game Status
```bash
./scripts_game/get_status.sh | jq
```

## API Endpoints

### POST /xl-spaceship/protocol/game/new
Create a new game session.

**Request:**
```json
{
  "player_id": "user123",
  "full_name": "User 123",
  "game_id": "",
  "protocol": {
    "hostname": "localhost",
    "port": 9000
  }
}
```

**Response:**
```json
{
  "user_id": "user123",
  "full_name": "User 123",
  "game_id": "Game-1",
  "starting": "user123"
}
```

### POST /xl-spaceship/protocol/game/join
Join an existing game.

**Request:**
```json
{
  "player_id": "user456",
  "full_name": "User 456",
  "game_id": "Game-1",
  "protocol": {
    "hostname": "localhost",
    "port": 9000
  }
}
```

**Response:**
```json
{
  "user_id": "user456",
  "full_name": "User 456",
  "game_id": "Game-1",
  "starting": "user123"
}
```

### GET /xl-spaceship/user/game/:gameID?player_id=X
Get current game status from player's perspective.

**Response:**
```json
{
  "self": {
    "id": "user123",
    "name": "User 123",
    "board": {
      "id": "Board-1",
      "rows": [...]
    },
    "spaceships": null
  },
  "opponent": {
    "id": "user456",
    "name": "User 456",
    "board": {
      "id": "Board-2",
      "rows": [...]
    },
    "spaceships": null
  },
  "nextTurn": "user456"
}
```

### PUT /xl-spaceship/protocol/game/:gameID
Accept incoming salvo from opponent.

**Request:**
```json
{
  "salvo": ["0x0", "1x1", "2x2", "3x3", "4x4"],
  "player_id": "user456"
}
```

**Response:**
```json
{
  "salvo": {
    "0x0": "miss",
    "1x1": "hit",
    "2x2": "hit",
    "3x3": "kill",
    "4x4": "miss"
  },
  "game": {
    "gameComplete": false,
    "won": true,
    "player_id": "user456"
  }
}
```

### PUT /xl-spaceship/user/game/:gameID/fire
Fire salvo at opponent (client-side endpoint).

**Request:**
```json
{
  "salvo": ["5x5", "6x6", "7x7", "8x8", "9x9"],
  "player_id": "user123"
}
```

## Code Conventions

### Scala Style

#### Case Classes for Models
```scala
case class Player(id: String, name: String, spaceships: Option[Array[XLSpaceship]], board: Board)
```
- Immutable by default
- Use `Option` for nullable fields
- Prefer `Array` for collections in models (JSON compatibility)

#### Services with Dependency Injection
```scala
@Singleton
class GameService @Inject()(
  val iDGeneratorService: IDGeneratorService,
  val playerService: PlayerService,
  val boardService: BoardService
) {
  // Service implementation
}
```
- Use `@Singleton` for stateful services
- Constructor injection with `@Inject()`
- Declare dependencies in constructor

#### Mutable Collections in Services
```scala
val games = scala.collection.mutable.Map[String, Game]()
```
- Services manage mutable state
- Use `scala.collection.mutable` for game storage
- `ArrayBuffer` for dynamic arrays
- `ListBuffer` for list operations

#### JSON Formatters
```scala
import formatters.JsonFormatters._

// Automatic formatting
implicit val coordinatesFormat: OFormat[Coordinates] = Json.format[Coordinates]

// Custom formatting
implicit val playerFormat: OFormat[Player] = new OFormat[Player] {
  def reads(json: JsValue): JsResult[Player] = Json.reads[Player].reads(json)
  def writes(player: Player): JsObject =
    if (player == null) Json.obj() else Json.writes[Player].writes(player)
}
```

### Logging

#### SLF4J Logger Pattern
```scala
import org.slf4j.LoggerFactory

class GameService @Inject()(...) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  logger.debug(s"[CONTEXT] Message with $variable")
  logger.warn(s"[WARNING] Issue description")
  logger.error(s"[ERROR] Error details", exception)
}
```

#### Log Message Format
- Use context prefixes: `[CONTEXT]`, `[DEBUG]`, `[STATUS]`, etc.
- String interpolation: `s"Message with $variable"`
- Include identifiers: game IDs, player IDs, board hashes
- Log before/after critical operations

### Error Handling

#### Pattern Matching
```scala
games.get(gameID) match {
  case Some(game) =>
    // Handle found game
  case None =>
    logger.warn(s"Game not found: $gameID")
    null  // or appropriate response
}
```

#### Validation in Controllers
```scala
json.validate[GameRequest] match {
  case JsSuccess(gameRequest, _) =>
    // Process valid request
  case JsError(errors) =>
    BadRequest(Json.obj("error" -> "Invalid request"))
}
```

### Testing

#### Specs2 Test Structure
```scala
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification with JsonMatchers {
  "Application" should {
    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beNone
    }
  }
}
```

## Common Tasks for AI Assistants

### Adding a New Endpoint

1. **Define route** in `conf/routes`:
   ```
   GET /xl-spaceship/user/game/:gameID/ships controllers.Application.getShips(gameID: String)
   ```

2. **Implement controller method** in `app/controllers/Application.scala`:
   ```scala
   def getShips(gameID: String) = Action { request =>
     val playerID = request.getQueryString("player_id").getOrElse("unknown")
     val ships = gameService.getPlayerShips(gameID, playerID)
     Ok(Json.toJson(ships)).withHeaders(corsHeaders: _*)
   }
   ```

3. **Add service method** in `app/services/GameService.scala`:
   ```scala
   def getPlayerShips(gameID: String, playerID: String): Option[Array[XLSpaceship]] = {
     games.get(gameID).flatMap { game =>
       if (game.self.id == playerID) game.self.spaceships
       else game.opponents.headOption.flatMap(_.spaceships)
     }
   }
   ```

4. **Add JSON formatter** if new model (in `app/formatters/JsonFormatters.scala`)

5. **Write tests** in `test/controllers/ApplicationSpec.scala`

### Adding a New Model

1. **Create case class** in `app/models/`:
   ```scala
   package models
   case class ShipStatus(name: String, hits: Int, destroyed: Boolean)
   ```

2. **Add JSON formatter** in `app/formatters/JsonFormatters.scala`:
   ```scala
   implicit val shipStatusFormat: OFormat[ShipStatus] = Json.format[ShipStatus]
   ```

3. **Use in services/controllers** with automatic JSON conversion

### Modifying Game Logic

1. **Locate logic** in `app/services/GameService.scala`
2. **Update method** preserving function signature if used by controllers
3. **Update tests** to cover new logic
4. **Test with game simulation scripts** in `scripts_game/`
5. **Check logs** in console for debug output

### Adding a New Spaceship Type

1. **Define shape** in `conf/application.conf`:
   ```
   spaceship {
     newtype = [{"x":0,"y":0},{"x":1,"y":0},{"x":0,"y":1}]
   }
   ```

2. **Update SpaceshipService** to load new configuration

3. **Test placement** with game simulation

### Debugging Common Issues

#### Board State Issues
- Check board hash logs: `System.identityHashCode(board)`
- Verify masking in `BoardService.maskOpponentBoard()`
- Confirm deep cloning in `BoardService.deepCloneBoard()`

#### JSON Serialization Issues
- Verify implicit formatters imported: `import formatters.JsonFormatters._`
- Check custom formatters for null handling
- Test with curl or game scripts

#### Game State Issues
- Check mutable map in `GameService.games`
- Verify player ID matching logic
- Review turn management in `Game.nextTurn`

#### Coordinate System
- X/Y positions: 0-15 (16x16 grid)
- Coordinate format: `"0x0"` to `"FxF"` (hex for 10-15)
- Decode function handles A-F conversion

### Best Practices

1. **Preserve Immutability**: Use case classes with `copy()` for updates
2. **Inject Dependencies**: Use constructor injection, avoid manual instantiation
3. **Log Extensively**: Debug-level logs with context prefixes
4. **Handle Nulls**: Use `Option` types, check for null in formatters
5. **Test with Scripts**: Use `scripts_game/` for integration testing
6. **CORS Headers**: Always include on API responses
7. **Validate Input**: Use `json.validate[T]` pattern matching
8. **Clone Boards**: Use `deepCloneBoard()` before masking
9. **Type Safety**: Leverage Scala's type system, avoid `Any`
10. **SBT Conventions**: Use `app/`, `test/`, `conf/` structure

## Git Workflow

### Branch Strategy
- Development on feature branches with prefix: `claude/claude-md-*`
- Main branch for production-ready code
- Create descriptive branch names

### Commit Messages
Follow convention from git log:
- `Adding opponent.` - Feature additions
- `App upgrade.` - Dependency/framework updates
- `Fixing the scripts` - Bug fixes

Use imperative mood, capitalize first word, end with period.

### Common Git Operations

#### Create and Push Feature Branch
```bash
git checkout -b claude/feature-name
# Make changes
git add .
git commit -m "Add new feature description."
git push -u origin claude/feature-name
```

#### Update from Main
```bash
git fetch origin
git merge origin/main
```

## Troubleshooting

### Server Won't Start
- Check Java version: `java -version` (needs 11+)
- Verify SBT installation: `sbt --version`
- Clear target: `sbt clean`
- Check port 9000 availability: `lsof -i :9000`

### Tests Failing
- Run individual test: `sbt "testOnly controllers.ApplicationSpec"`
- Check test logs in console
- Verify test dependencies in `build.sbt`

### Build Errors
- Clean and recompile: `sbt clean compile`
- Check Scala version compatibility (2.13.13)
- Verify plugin versions in `project/plugins.sbt`

### Docker Build Issues
- Check Docker daemon: `docker info`
- Build with no cache: `docker build --no-cache -t xlspaceship .`
- Check base image availability

### Game Logic Issues
- Enable debug logging in `conf/logback.xml`
- Use `scripts_game/get_status.sh` to inspect state
- Check board masking logs
- Verify coordinate decoding (A-F hex support)

## Resources

- **Play Framework Docs**: https://www.playframework.com/documentation/2.9.x/Home
- **Scala Docs**: https://docs.scala-lang.org/
- **SBT Docs**: https://www.scala-sbt.org/documentation.html
- **Specs2 Docs**: https://etorreborre.github.io/specs2/
- **Game Specification**: `docs/spaceXL challenge.pdf`

## Contact & Support

- Repository: https://github.com/CesarChaMal/xlspaceship
- Issues: Report on GitHub Issues
- License: See LICENSE file

---

**Last Updated**: 2025-11-13

**AI Assistant Notes**:
- This codebase uses mutable state management (in-memory maps)
- Board masking is critical for game fairness (fog-of-war)
- Coordinate system uses both numeric (0-9) and hex (A-F) notation
- Always test with game simulation scripts before committing
- Preserve existing logging patterns for consistency
