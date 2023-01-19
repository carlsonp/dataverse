FROM bitnami/solr:8
USER root
COPY *.xml /
RUN cp -R /opt/bitnami/solr/server/solr/configsets/_default/conf /var/tmp \
    && cp /*.xml /var/tmp/conf \
    && chmod -R 775 /var/tmp \
    && chown -R 1001:root /var/tmp

# RUN echo "name=collection1" > /var/solr/data/collection1/core.properties

# increase the number of file descriptors and max processes
RUN echo "solr soft nproc 65000" >> /etc/security/limits.conf && \
    echo "solr hard nproc 65000" >> /etc/security/limits.conf && \
    echo "solr soft nofile 65000" >> /etc/security/limits.conf && \
    echo "solr hard nofile 65000" >> /etc/security/limits.conf

# increase Header s
RUN sed -i "s/name=\"solr.jetty.request.header.size\" default=\"8192\"/name=\"solr.jetty.request.header.size\" default=\"102400\"/g" /opt/bitnami/solr/server/etc/jetty.xml

USER 1001