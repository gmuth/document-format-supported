# document-format-supported

This is a community project to document supported formats of IPP printers.
Please contribute to this repository by sharing your printers attributes.

## Formats reported via IPP
|Make and Model                 |application                                                          |image|text |
|-------------------------------|---------------------------------------------------------------------|-----|-----|
|HP LaserJet 100 colorMFP M175nw|pdf, postscript, vnd.hp-PCL, vnd.hp-PCLXL                            |urf  |     |
|Xerox B210 Printer             |PCL, PCLm, octet-stream, postscript, vnd.hp-PCL, vnd.hp-PCLXL, x-QPDL|urf  |plain|

## Usage

### Bonjour

If your printer has Bonjour, AirPrint or Mopria enabled run  `java -jar printer-attributes.jar` without arguments. The tool will use mdns to discover your printers.

### Hostname or print-uri

You can query specific printers by running `java -jar printer-attributes.jar ipp://yourprinter ipp://otherprinter`.

### MacOS native

If you don't have Java installed you can run `printer-attributes-arm64`

### Generate Readme

You can generate this Readme file with a fresh list of document formats by running:

`java -cp printer-attributes.jar de.gmuth.md.GenerateReadme`

or simply

`./mvnw`


### Contribute your printer attributes

To share your saved printer attributes (ipp response file)
- create an issue and attach the `.res` file
- or send a personal message via email attaching the `.res` file
- or fork this repo, add the `.res` file and create a pull request.

[Attributes are normalized](https://github.com/gmuth/document-format-supported/blob/main/src/main/java/de/gmuth/ipp/AttributesNormalizer.java)
by hardcoding private or volatile attribute values.
