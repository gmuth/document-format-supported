
## Usage

### Bonjour

If your printer has Bonjour or AirPrint enabled run  `java -jar printer-attributes.jar` without arguments. The tool will use mdns to discovery your printers.

### Hostname or print-uri

You can query specific printers by running `java -jar printer-attributes.jar ipp://yourprinter ipp://otherprinter`.

### MacOS native

If you don't have Java installed you can run `printer-attributes-arm64`

### Generate Readme

You can generate this Readme file with a fresh list of document formats by running:

`java -cp printer-attributes.jar de.gmuth.ipp.GenerateReadme`

or simply

`./mvnw`

