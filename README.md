# document-format-supported

This is a community project to document supported formats of IPP printers.
Please contribute to this repository by sharing your printers attributes.

## Formats reported via IPP
|Make and Model                 |application                                                          |image    |text |
|-------------------------------|---------------------------------------------------------------------|---------|-----|
|HP LaserJet 100 colorMFP M175nw|pdf, postscript, vnd.hp-PCL, vnd.hp-PCLXL                            |urf      |     |
|HP LaserJet Pro MFP M127fw     |PCLm, octet-stream                                                   |jpeg, urf|     |
|Xerox B210 Printer             |PCL, PCLm, octet-stream, postscript, vnd.hp-PCL, vnd.hp-PCLXL, x-QPDL|urf      |plain|

## Usage

### Automatic discovery

Run  `java -jar printer-attributes.jar` without arguments to discover printers having Bonjour, AirPrint or Mopria enabled.

### printer-uri with known IP or hostname

Run `java -jar printer-attributes.jar ipp://yourprinter ipp://otherprinter` to query specific printers.

### MacOS native

Run `printer-attributes-arm64` if you don't have Java installed.

### Generate README.md

by running `java -cp printer-attributes.jar de.gmuth.md.GenerateReadme` or `./mvnw`

### Contribute your printer attributes

To share your saved printer attributes (ipp response file)
- create an issue and attach the `.res` file
- or send a personal message via email attaching the `.res` file
- or fork this repo, add the `.res` file and create a pull request.

[Attributes are normalized](https://github.com/gmuth/document-format-supported/blob/main/src/main/java/de/gmuth/ipp/AttributesNormalizer.java)
by hardcoding private or volatile attribute values.
