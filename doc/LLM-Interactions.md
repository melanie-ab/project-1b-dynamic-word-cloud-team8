## Prompts
- Rewrite project 1b in purely functional Scala 3
- Remove mutable state
- Use scanLeft
- Add trait-based modularity

## Changes Made
- Replaced mutable Map with immutable Map
- Used scanLeft for accumulation
- Removed Observer pattern
- Added SIGPIPE handling

## Reflection
Using scanLeft eliminated the need for mutable counters and simplified the streaming behavior.

