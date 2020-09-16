package com.flexsolution.resetpassword.patch;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class AlfrescoFileVisitor extends SimpleFileVisitor<Path> {

    private final Logger LOGGER = LoggerFactory.getLogger(AlfrescoFileVisitor.class);

    private boolean ignoreRoot;

    public AlfrescoFileVisitor (NodeService nodeService, FileFolderService fileFolderService,
                                ContentService contentService,
                                MimetypeService mimetypeService,
                                Properties globalProps,
                                Map<String, NodeRef> startLocationEntry,
                                Set<String> ignoreFiles,
                                boolean ignoreRoot) {
        this.nodeService = nodeService;
        this.fileFolderService = fileFolderService;
        this.contentService = contentService;
        this.mimetypeService = mimetypeService;
        this.globalProps = globalProps;
        this.ignoreFiles = ignoreFiles;
        this.ignoreRoot = ignoreRoot;

        IMPORTED.putAll(startLocationEntry);
    }

    private final String SPACES_PROPS_PATH = "alfresco/messages/bootstrap-spaces";

    private ResourceBundle bundle;

    private final NodeService nodeService;
    private final FileFolderService fileFolderService;
    private final ContentService contentService;
    private final MimetypeService mimetypeService;
    private final Properties globalProps;
    private final Set<String> ignoreFiles;

    private final Map<String, NodeRef> IMPORTED = new HashMap<>();

    @Override
    public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs) throws IOException {
        return copyToNode(dir, ContentModel.TYPE_FOLDER);
    }

    private String getLocalizedName (String fileNameExpr) {
        if(this.bundle == null) {
            Locale bindingLocale = I18NUtil.getLocale();
            this.bundle = ResourceBundle.getBundle(SPACES_PROPS_PATH, bindingLocale);
        }

        return this.bundle.getString(fileNameExpr);
    }

    private FileVisitResult copyToNode (Path fileOrDir, QName type) {
        if(!IMPORTED.containsKey(fileOrDir.toString()) || ignoreRoot == false) {

            NodeRef parentNode;

            if(ignoreRoot == false){
                parentNode = IMPORTED.get(fileOrDir.toString());
                ignoreRoot = true;
            }else {
                parentNode = IMPORTED.get(fileOrDir.getParent().toString());
            }

            String importedFileName = fileOrDir.getFileName().toString();

            if(importedFileName.startsWith("$")) {
                importedFileName = getLocalizedName(importedFileName.substring(1));
            }

            NodeRef targetNode = fileFolderService.searchSimple(parentNode, importedFileName);

            if(targetNode == null) {

                final FileInfo newNode = fileFolderService.create(parentNode, importedFileName, type);

                targetNode = newNode.getNodeRef();

                if(ContentModel.TYPE_CONTENT.equals(type)) {
                    copyContent(fileOrDir, newNode);
                }
                LOGGER.debug("Node {} has been imported. File/folder name {}", targetNode, importedFileName);
            } else {
                LOGGER.debug("Node {} already exists (SKIP). File/folder name {}", targetNode, importedFileName);
            }

            setNodeTitle(targetNode, importedFileName);

            IMPORTED.put(fileOrDir.toString(), targetNode);

        }

        LOGGER.debug("Imported file/dir path={}", fileOrDir.toString());
        return FileVisitResult.CONTINUE;
    }

    private void copyContent (Path file, FileInfo newNode) {
        try {
            final ContentWriter writer = contentService.getWriter(newNode.getNodeRef(), ContentModel.PROP_CONTENT, true);

            final String importedFileName = file.getFileName().toString();
            writer.setMimetype(mimetypeService.getMimetype(FilenameUtils.getExtension(importedFileName)));

            try (
                    final InputStream is = Files.newInputStream(file);
                    final OutputStream os = writer.getContentOutputStream();
            ) {
                IOUtils.copy(is, os);
            }
        } catch (IOException e) {
            throw new AlfrescoRuntimeException(e.getMessage());
        }

        LOGGER.debug( "Content copied file path={}", file.toString());
    }


    private void setNodeTitle (NodeRef nodeRef, String filename) {
        final String title = this.globalProps.getProperty("title." + filename);

        LOGGER.debug("Title for file/folder {} is {}", filename, title);

//        if(title != null){
//            this.nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, title);
//        }

        Optional.ofNullable(title)
                .ifPresent(t -> this.nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, t));

    }

    @Override
    public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) throws IOException {

        if(ignoreFiles != null && ignoreFiles.contains(file.getFileName().toString())) {
            LOGGER.info("File {} has been ignored", file.getFileName().toString());
            return FileVisitResult.CONTINUE;
        }
        return copyToNode(file, ContentModel.TYPE_CONTENT);
    }

    @Override
    public FileVisitResult visitFileFailed (Path file, IOException exc) throws IOException {
        throw new AlfrescoRuntimeException(exc.getMessage());
    }

    @Override
    public FileVisitResult postVisitDirectory (Path dir, IOException exc) throws IOException {
        return super.postVisitDirectory(dir, exc);
    }
}
