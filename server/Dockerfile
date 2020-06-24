#*******************************************************************************
# Copyright (c) 2019 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# Contributors:
#     IBM Corporation - initial API and implementation
#*******************************************************************************
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
CMD java -jar CodewindJavaProfilingLanguageServer-0.9.0-jar-with-dependencies.jar
