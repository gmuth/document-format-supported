package de.gmuth.ipp;

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

    static Path ippMessagePath = Path.of("printers");
    private static IppMessageRepository instance;
    private static Logger logger = Logging.getLogger(IppMessageRepository.class);

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
                .map(IppMessageRepository::getIppResponse);
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

    private static String getFilenameFor(IppResponse ippResponse) {
        IppString makeAndModel = ippResponse.getPrinterGroup().getValue("printer-make-and-model");
        return makeAndModel.getText().replaceAll("[/\\\\:*?\"<>|]", "_");
    }

    static IppResponse getIppResponse(Path path) {
        try {
            IppResponse ippResponse = new IppResponse();
            ippResponse.decode(Files.readAllBytes(path));
            logger.info(String.format("Reading %-40s -> %s ", path.getFileName(), ippResponse));
            return ippResponse;
        } catch (IOException ioException) {
            throw new UncheckedIOException("Failed to get ipp response from " + path, ioException);
        }
    }

    public static void main(String[] args) {
        Logging.configure(Level.INFO);
        try {
            IppMessageRepository ippMessageRepository = new IppMessageRepository();
            Stream<IppResponse> ippResponses = ippMessageRepository.findAllIppResponses();
            long count = ippResponses.count();
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
        Logging.flush();
    }

}