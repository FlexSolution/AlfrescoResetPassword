FROM alfresco/alfresco-content-repository-community:6.2.0-ga

ARG TOMCAT_DIR=/usr/local/tomcat

USER root


# Copy Dockerfile to avoid an error if no AMPs exist
COPY Dockerfile _amps/*.amp $TOMCAT_DIR/amps/
RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
              $TOMCAT_DIR/amps $TOMCAT_DIR/webapps/alfresco -directory -nobackup -force

USER ${USERNAME}