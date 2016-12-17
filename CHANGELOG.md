# Change Log

## [0.2.1]
### Added
- If you use the macro from clojure (and not clojurescript), it will define a map containing both the generated css and the map between original selectors and localised selectors.

## [0.2.0]
### Added
- [BREAKING] ```defstyle``` doesn't take an array of style rules anymore, but multiple style rules one after the other.
  (to be more consistent with garden)
- Support for media-query and keyframes
- Support compiler options
- Add style component (to document, see tests for now)
