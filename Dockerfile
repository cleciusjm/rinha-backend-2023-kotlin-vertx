FROM ibm-semeru-runtimes:open-20-jre

ENV JVM_OPTS="-Xtune:virtualized -Xmx256m -XX:+IdleTuningGcOnIdle -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+ExitOnOutOfMemoryError"

COPY dockerRun.sh /opt/run.sh
COPY target/rinha-backend-2023-kotlin-vertx-1.0.0-SNAPSHOT-jar-with-dependencies.jar /opt/app.jar

RUN /bin/sh -c '/opt/run.sh --run_type=short &' ; sleep 15 ; pkill -9 -f '/opt/run.sh'

CMD ["/opt/run.sh"]