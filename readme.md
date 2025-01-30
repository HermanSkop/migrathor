# Migrathor
Migrathor is a CLI tool to migrate database versions.
It is written in Java and uses JDBC to interact with the database.
## Installation
Download the jar file from the [releases page](https://github.com/HermanSkop/migrathor/releases).

## Usage
### Run the program
Run the jar file with the following command:
```bash
java -jar migrathor_x.x.x.jar path/to/your_config.properties
```
### Commands
- `migrate [version]` - Migrate the database to the specified version.
    - `-u` - Undo the given migration.
- `state` - Show the current state of the tool.
- `exit` - Exit the tool.

## Configuration
The configuration file should look similar to this:
```properties
db.url=jdbc:postgresql://localhost:5432/dbname
db.user=postgres
db.password=password (can be empty)
scripts.dir=/path/to/migration/scripts
```

### Script naming
- `vn.sql` - do migration script.
- `un.sql` - undo migration script.

Where n is a positive unique integer.

---
The sequence of scripts must be in ascending order, but not necessarily consecutive.

---
