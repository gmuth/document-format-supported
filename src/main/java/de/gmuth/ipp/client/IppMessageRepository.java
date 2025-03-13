package de.gmuth.ipp.client;

/**
 * Copyright (c) 2024-2025 Gerhard Muth
 */

import de.gmuth.ipp.core.IppResponse;
import de.gmuth.ipp.core.IppString;
import de.gmuth.log.Logging;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    public List<IppResponse> allIppResponses;
    private List<String> allMakeAndModels;
    
    private IppMessageRepository() {
        try {
            if (!Files.exists(ippMessagePath)) {
                logger.info("Creating IPP message directory " + ippMessagePath);
                Files.createDirectory(ippMessagePath);
            }
            logger.info("Using IPP message directory: " + ippMessagePath);
            allIppResponses = findAllIppResponses().collect(Collectors.toList());
            allMakeAndModels = allIppResponses.stream()
                    .map(IppMessageRepository::getPrinterMakeAndModel)
                    .collect(Collectors.toList());
            long numberOfResponses = allIppResponses.size();
            logger.info(numberOfResponses + " IPP responses available.");
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
                .map(this::readIppResponse);
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

    public static String getPrinterMakeAndModel(IppResponse ippResponse) {
        return ((IppString) (ippResponse.getPrinterGroup().getValue("printer-make-and-model"))).getText();
    }

    public static String getFilenameFor(IppResponse ippResponse) {
        return getPrinterMakeAndModel(ippResponse).replaceAll("[/\\\\:*?\"<>|]", "_");
    }

    private IppResponse readIppResponse(Path path) {
        try {
            IppResponse ippResponse = new IppResponse();
            ippResponse.decode(Files.readAllBytes(path));
            logger.info(String.format("Reading %-35s %s ", path.getFileName(), ippResponse));
            return ippResponse;
        } catch (IOException ioException) {
            throw new UncheckedIOException("Failed to get ipp response from " + path, ioException);
        }
    }

    public IppResponse getIppResponse0(String makeAndModel) {
        return readIppResponse(ippMessagePath.resolve(makeAndModel + ".res"));
    }

    public boolean makeAndModelExists(String makeAndModel) {
        return allMakeAndModels.contains(makeAndModel);
    }

    private void rewriteAllNormalized() {
        logger.info("Rewrite all IPP responses normalized.");
        allIppResponses.forEach(ippResponse -> {
            try {
                boolean modified = new AttributesNormalizer(ippResponse).normalize();
                if (modified) saveIppResponse(ippResponse, true);
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
        });
    }

    public static void main(String[] args) {
        Logging.configure(Level.INFO, true);
        try {
            IppMessageRepository ippMessageRepository = getInstance();
            boolean normalize = false;
            if (normalize) ippMessageRepository.rewriteAllNormalized();
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
        Logging.flush();
    }

}