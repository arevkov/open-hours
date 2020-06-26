# Open Hours

Provides an API to convert restaurant open hours
from json to human readable (pretty print) format

## How to build
```shell script
$ sbt clean assembly
```

## Hot to run
```shell script
$ java -jar target/scala-2.12/openhours.jar
```

## Example (todo: Swagger)
```
curl -X POST http://localhost:8080/api/v1/prettyprint \
-H "Content-Type: application/json" \
-d '{"friday":[{"type":"open","value":10000},{"type":"close","value":30000}]}'
```

## API v2 proposal:

* Remove ambiguity on empty open-close hours:
  1. the days when a restaurant is closed should get empty array in JSON
  2. on the other hand provided example allows not to pass a day at all
     (let's assume both cases are allowed)

* Keep hours in a human-readable format - prefer HH:mm over Unix time.
  Why: JSON isn't a binary format, serializing time to Int32 doesn't any benefits
  
* Shifting overnight close hour to the next day looks counter-intuitive to me.
  If say opening on Friday and working up to Monday isn't a possible scenario,
  I would keep close hour in the same day
  Why: make it more intuitive to read
  
* Would reshape JSON in format (instead of using type/value fields):  
  { "friday": [{"open":"11:15", "close:"16:00"}] }
  Why: make reading/writing more straightforward
  
* The format doesn't take into account month/yearly cycles
  Say: "every saturday" or "31 Dec"


