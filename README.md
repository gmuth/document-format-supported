# document-format-supported

This should become a collective effort to document supported formats of IPP printers.
Please contribute to this repository by sharing your printers attributes.

### Printers and pdl formats reported via IPP

|Make and Model                 |application                                                          |image|text |
|-------------------------------|---------------------------------------------------------------------|-----|-----|
|HP LaserJet 100 colorMFP M175nw|pdf, postscript, vnd.hp-PCL, vnd.hp-PCLXL                            |urf  |     |
|Xerox B210 Printer             |PCL, PCLm, octet-stream, postscript, vnd.hp-PCL, vnd.hp-PCLXL, x-QPDL|urf  |plain|

### How to contribute your printer attributes

1. Fork this repository.
2. Run script `./save_printer_attributes ipp://yourprinter` (requires Java)

   If no arguments are provided mdns is used for printer discovery.
3. Optionally `./generate_readme` to update this README.md. 
4. Add, commit and push new .bin file
5. Create a pull request on github.
