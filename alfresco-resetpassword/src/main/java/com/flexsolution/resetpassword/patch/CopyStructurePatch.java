package com.flexsolution.resetpassword.patch;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class CopyStructurePatch extends AbstractPatch {

    private final Logger LOGGER = LoggerFactory.getLogger(CopyStructurePatch.class);

    protected String classpathLocaiton;
    protected String copyToXpath;

    protected Repository repository;
    protected FileFolderService fileFolderService;
    protected ContentService contentService;
    protected MimetypeService mimetypeService;
    protected Properties globalProps;
    protected Set<String> ignoreFiles;
    protected boolean ignoreRoot = true;

    @Override
    public String applyInternal () throws Exception {


        NodeRef companyHome = repository.getCompanyHome();

        NodeRef dataDictionary = searchService.selectNodes(companyHome, copyToXpath, null, namespaceService, false).get(0);


        ResourceLoader resourceLoader = new DefaultResourceLoader(getClass().getClassLoader());
        Resource bootstrapFolder = resourceLoader.getResource(classpathLocaiton);

        final String startLocationPath = bootstrapFolder.getFile().getPath();
        Files.walkFileTree(Paths.get(startLocationPath),
                new AlfrescoFileVisitor(
                        nodeService,
                        fileFolderService,
                        contentService,
                        mimetypeService,
                        globalProps,
                        Collections.singletonMap(startLocationPath, dataDictionary),
                        ignoreFiles,
                        ignoreRoot
                )
        );


        return "STRUCTURE COPY PATCH FINISHED SUCCESSFULLY";
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository (Repository repository) {
        this.repository = repository;
    }

    public void setFileFolderService (FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setContentService (ContentService contentService) {
        this.contentService = contentService;
    }

    public void setMimetypeService (MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    public void setClasspathLocaiton (String classpathLocaiton) {
        this.classpathLocaiton = classpathLocaiton;
    }


    public void setCopyToXpath (String copyToXpath) {
        this.copyToXpath = copyToXpath;
    }

    public void setGlobalProps (Properties globalProps) {
        this.globalProps = globalProps;
    }

    public void setIgnoreFiles (Set<String> ignoreFiles) {
        this.ignoreFiles = ignoreFiles;
    }

    public void setIgnoreRoot (boolean ignoreRoot) {
        this.ignoreRoot = ignoreRoot;
    }

    public boolean isIgnoreRoot () {
        return ignoreRoot;
    }
}
