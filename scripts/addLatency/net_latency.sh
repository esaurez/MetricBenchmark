#/bin/sh
#
#
# Usage: ./net_latency <latency to add>
# 1/16/2018
#

LATENCY=0

if [ $# -gt 0 ]
  then
    LATENCY=$1
fi

echo "Setting latency to $LATENCY"

echo "Setting latency setup to metric 1"
/home/ubuntu/addLatency/metric1_net_latency.sh ${LATENCY}

echo "Setting latency setup to metric 2"
/home/ubuntu/addLatency/metric2_net_latency.sh ${LATENCY}

echo "Setting latency setup to metric 3"
/home/ubuntu/addLatency/metric3_net_latency.sh ${LATENCY}
