# Snapshots of Configuration #

Suppose the physical machines are se017e, se018e, se019e, .....
Suppose the virtual machines are vm1, vm2, ....., vm56
On se017e, the VM instance names are vm8, vm16, ...., vm56
On se018e, the VM instance names are vm1, vm9, ...., vm49
......

When using 8 machines on my VM instance setting, for example, the NFS mounting looks as follows: /localfs/contextNFS is DM-NFS (see SC’13 paper for details)

```
[root@vm1 ~]# df -h
Filesystem            Size  Used Avail Use% Mounted on
/dev/sda1             986M  880M   56M  95% /
se017e:/localfs/contextNFS/0
                      211G   65G  136G  33% /localfs/contextNFS/0 se018e:/localfs/contextNFS/1
                      211G   63G  138G  32% /localfs/contextNFS/1 se019e:/localfs/contextNFS/2
                      211G  8.4G  192G   5% /localfs/contextNFS/2 se020e:/localfs/contextNFS/3
                      211G  8.8G  192G   5% /localfs/contextNFS/3 se021e:/localfs/contextNFS/4
                      211G  9.0G  192G   5% /localfs/contextNFS/4 se022e:/localfs/contextNFS/5
                      211G  8.8G  192G   5% /localfs/contextNFS/5 se023e:/localfs/contextNFS/6
                      211G  8.5G  192G   5% /localfs/contextNFS/6 se024e:/localfs/contextNFS/7
                      211G   72G  129G  36% /localfs/contextNFS/7 se024e:/cloudNFS      211G   
                             72G  129G  36% /cloudNFS tmpfs                1000M     0 1000M   0% /ramfs
```

```
[root@se017 ~]# xm list
Name                                        ID   Mem VCPUs      State   Time(s)
Domain-0                                     0  1852     8     r----- 348510.6
vm16                                       121  2048     8     -b----   2552.8
vm24                                       122  2048     8     -b----   2523.5
vm32                                       123  2048     8     -b----   2594.8
vm40                                       124  2048     8     -b----  66280.0
vm48                                       125  2048     8     -b----  60234.0
vm56                                       126  2048     8     -b----  72054.2
vm8                                        120  2048     8     -b----   2661.7
```

```
[root@se018 ~]# xm list
Name                                        ID   Mem VCPUs      State   Time(s)
Domain-0                                     0  1852     8     r----- 346153.9
vm1                                        135  2048     8     -b----   3502.4
vm17                                       130  2048     8     -b----   2677.3
vm25                                       131  2048     8     -b----   2604.2
vm33                                       132  2048     8     -b----  69699.2
vm41                                       133  2048     8     -b----  61153.7
vm49                                       134  2048     8     -b----  53181.6
vm9                                        129  2048     8     -b----   2613.4
```

```
[root@vm1 ~]# cat /etc/sysconfig/network-scripts/ifcfg-eth0
DEVICE=eth0
BOOTPROTO=static
HWADDR=00:00:0A:00:06:01
ONBOOT=yes
TYPE=Ethernet
NETMASK=255.255.0.0
IPADDR=10.0.6.1
HOSTNAME="vm1"
```

```
[root@vm2 ~]# cat /etc/sysconfig/network-scripts/ifcfg-eth0
DEVICE=eth0
BOOTPROTO=static
HWADDR=00:00:0A:00:06:02
ONBOOT=yes
TYPE=Ethernet
NETMASK=255.255.0.0
IPADDR=10.0.6.2
HOSTNAME="vm2"
```

```
[root@se018 ~]# cat /etc/sysconfig/network-scripts/ifcfg-eth0
DEVICE="eth0"
BOOTPROTO="static"
HWADDR="00:23:AE:FC:E4:C8"
ONBOOT="yes"
TYPE="Ethernet"
IPADDR=10.0.2.18
NETMASK=255.255.0.0
```