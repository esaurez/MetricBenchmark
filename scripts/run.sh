#!/usr/bin/env bash
LATENCIES=(
"0"
"5"
"10"
"50"
"100"
)

for lat in "${LATENCIES[@]}"
do
    echo "Iteration with latency: [${lat}]"
    /home/ubuntu/addLatency/net_latency.sh ${lat}
    echo "Running evaluation for METRIC"
    mysql -u root --password=music -e "SET GLOBAL wsrep_on=OFF" test
    sleep 10
    echo "Starting METRIC server"
    cd /home/ubuntu/etdb/ETDB
    /usr/lib/jvm/java-8-openjdk-amd64/bin/java -classpath /usr/share/maven/boot/plexus-classworlds-2.x.jar -Dclassworlds.conf=/usr/share/maven/bin/m2.conf -Dmaven.home=/usr/share/maven -Dmaven.multiModuleProjectDirectory=/home/ubuntu/etdb/ETDB org.codehaus.plexus.classworlds.launcher.Launcher exec:java -Dexec.mainClass=com.att.research.mdbc.MdbcServer -Dexec.args="-c /home/ubuntu/config/base-0.json -u jdbc:mysql://localhost -p 30000 -s root -a music" &
    pid=$!
    sleep 10
    java -jar /home/ubuntu/etdb/MetricBenchmark/metricTarget/benchmarks.jar > /home/ubuntu/benchmark/results/metric_${lat}.txt
    echo "Killing METRIC server with PID"
    kill -9 ${pid}
    echo "Running evaluation for Gallera"
    mysql -u root --password=music -e "SET GLOBAL wsrep_on=ON" test
    sleep 10
    java -jar /home/ubuntu/etdb/MetricBenchmark/mariadbTarget/benchmarks.jar > /home/ubuntu/benchmark/results/gallera_${lat}.txt
done

