
## Usage

### Automatic discovery

Run  `java -jar printer-attributes.jar` without arguments to discover printers having Bonjour, AirPrint or Mopria enabled.

### printer-uri with known IP or hostname

Run `java -jar printer-attributes.jar ipp://yourprinter ipp://otherprinter` to query specific printers.

### Generate README.md

by running `java -cp printer-attributes.jar de.gmuth.md.GenerateReadme` or `./mvnw`
