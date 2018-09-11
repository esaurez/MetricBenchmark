#/bin/sh
#
#
# Usage: ./net_latency <latency to add>
# 1/16/2018
#

IPS=(
'34.201.126.128'
#'54.224.118.57'
'54.197.20.196'
'172.31.49.114'
#'172.31.11.30'
'172.31.52.66'
)

LATENCY=0

if [ $# -gt 0 ]
  then
    LATENCY=$1
fi

echo "Setting latency to $LATENCY"

# delete all current rules
ssh metric2 "sudo tc qdisc del dev eth0 root"

if [ $LATENCY -eq 0 ]
 then exit 0
fi

#
ssh metric2 "sudo tc qdisc add dev eth0 root handle 1: prio"
ssh metric2 "sudo tc qdisc add dev eth0 parent 1:3 handle 30: tbf rate 20mbit buffer 16000 limit 30000"
ssh metric2 "sudo tc qdisc add dev eth0 parent 30:1 handle 31: netem  delay \"${LATENCY}ms\""
for i in "${IPS[@]}"
do
    echo "Adding latency for IP: [${i}]"
    ssh metric2 "sudo tc filter add dev eth0 protocol ip parent 1:0 prio 3 u32 match ip dst ${i} flowid 1:3"
done
