
## Usage

### Automatic discovery

Run  `java -jar printer-attributes.jar` without arguments to discover printers having Bonjour, AirPrint or Mopria enabled.

### printer-uri with known IP or hostname

Run `java -jar printer-attributes.jar ipp://yourprinter ipp://otherprinter` to query specific printers.

### Generate README.md

by running `java -cp printer-attributes.jar de.gmuth.md.GenerateReadme` or `./mvnw`

### Save raw IPP unmodified

Using the inspect workflow of the ipp-client library you can save unmodified original printer and job attributes.

```
    java -cp printer-attributes.jar de.gmuth.ipp.client.InspectPrinters # automatic discovery
    java -cp printer-attributes.jar de.gmuth.ipp.client.InspectPrinters ipp://yourprinter ipp://otherprinter
```
