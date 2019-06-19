FROM ibmjava:sdk
RUN cd /opt && \
    wget http://www-eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz && \
    tar -xvzf apache-maven-3.3.9-bin.tar.gz && \
    mv apache-maven-3.3.9 maven
ENV M2_HOME "/opt/maven"
ENV PATH "${M2_HOME}/bin:${PATH}"
COPY ./ /profiling
WORKDIR /profiling
RUN echo `ls`;
RUN mvn install:install-file -Dfile=/opt/ibm/java/jre/lib/tools/monitoring-api.jar -DgroupId=com.ibm.java.diagnostics.healthcenter -DartifactId=com.ibm.java.diagnostics.healthcenter -Dversion=1.0 -Dpackaging=jar
RUN mvn clean install
WORKDIR /profiling/target
CMD java -jar MicrocliomateJavaProfilingLanguageServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar
