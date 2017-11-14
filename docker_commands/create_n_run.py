#!/usr/bin/env python

import subprocess

#subprocess.call("docker")
out=""
for i in range(5):
    out+=subprocess.check_output(['docker','run','-i','-t','ubuntu','/bin/bash'])

print out

file=open("containers.txt","w")
file.write(out);
file.close()