package de.gmuth.ipp.client;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.ipp.core.IppResponse;
import de.gmuth.ipp.core.IppString;
import de.gmuth.log.Logging;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IppMessageRepository {

    public static Path ippMessagePath = Path.of("printers");
    private static IppMessageRepository instance;
    private static final Logger logger = Logging.getLogger(IppMessageRepository.class);

    public static IppMessageRepository getInstance() {
        if (instance == null) instance = new IppMessageRepository();
        return instance;
    }

    private IppMessageRepository() {
        try {
            if (!Files.exists(ippMessagePath)) {
                logger.info("Creating ipp message directory " + ippMessagePath);
                Files.createDirectory(ippMessagePath);
            }
            logger.info("Using ipp message directory: " + ippMessagePath);
            long numberOfResponses = findAllIppResponsePaths(".res").count();
            logger.info(numberOfResponses + " ipp responses available.");
        } catch (IOException ioException) {
            logger.severe("Failed to initialize IppMessageRepository: " + ioException.getMessage());
            throw new UncheckedIOException(ioException);
        }
    }

    private Stream<Path> findAllIppResponsePaths(String endsWith) throws IOException {
        return Files.list(ippMessagePath)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(endsWith));
    }

    public Stream<IppResponse> findAllIppResponses() throws IOException {
        return findAllIppResponsePaths(".res")
                .map(this::getIppResponse);
    }

    public void saveIppResponse(IppResponse ippResponse, Boolean saveText) throws IOException {
        String filename = getFilenameFor(ippResponse);

        Path resPath = ippMessagePath.resolve(filename + ".res");
        if (Files.notExists(resPath)) Files.createFile(resPath);
        ippResponse.saveBytes(resPath.toFile());

        if (saveText) {
            Path txtPath = ippMessagePath.resolve(filename + ".txt");
            if (Files.notExists(txtPath)) Files.createFile(txtPath);
            ippResponse.saveText(txtPath.toFile());
        }
    }

    public static String getFilenameFor(IppResponse ippResponse) {
        IppString makeAndModel = ippResponse.getPrinterGroup().getValue("printer-make-and-model");
        return makeAndModel.getText().replaceAll("[/\\\\:*?\"<>|]", "_");
    }

    private IppResponse getIppResponse(Path path) {
        try {
            IppResponse ippResponse = new IppResponse();
            ippResponse.decode(Files.readAllBytes(path));
            logger.info(String.format("Reading %s -> %s ", path.getFileName(), ippResponse));
            return ippResponse;
        } catch (IOException ioException) {
            throw new UncheckedIOException("Failed to get ipp response from " + path, ioException);
        }
    }

    public IppResponse getIppResponse(String makeAndModel) {
        return getIppResponse(ippMessagePath.resolve(makeAndModel + ".res"));
    }

    public static void main(String[] args) {
        Logging.configure(Level.INFO);
        try {
            IppMessageRepository ippMessageRepository = getInstance();
            Stream<IppResponse> ippResponses = ippMessageRepository.findAllIppResponses();

            boolean normalize = true;
            if (normalize) {
                logger.info("Rewrite ipp responses normalized.");
                ippResponses.forEach(ippResponse -> {
                    try {
                        boolean modified = new AttributesNormalizer(ippResponse).normalize();
                        if (modified) ippMessageRepository.saveIppResponse(ippResponse, true);
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }
                });
            } else {
                ippResponses.collect(Collectors.toList());
            }
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
        Logging.flush();
    }

}