#!/usr/bin/env python3
"""Capture original submerged clay/sand/gravel patches with controlled blocks."""
import argparse
import gzip
import hashlib
import json
import platform
from pathlib import Path

from native_ores import OreOracle,P,U,I,D


class DepositOracle(OreOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.game.w(0x140b438,'Q',self.blocks[82])
        self.w(self.statevt,'Q',self.callback(P,[P],lambda s:self.read(self.blocks[self.read(s+8,'I')>>4]+0x170,'Q')))
        def block_state(level,pos):
            return self.state(self.get_block(level,*[self.read(pos+i*4,'i') for i in range(3)]))
        self.w(self.vt+0x38,'Q',self.callback(P,[P,P],block_state))
        def set_state(level,pos,state,flags,unused):
            packed=self.read(state+8,'I')
            return self.set_block(level,*[self.read(pos+i*4,'i') for i in range(3)],packed>>4,packed&15,flags)
        self.w(self.vt+0x160,'Q',self.callback(I,[P,P,P,I,I],set_state))
        self.patch(0x3edc80,None,[I],lambda flag:None)
        self.patch(0xaf4a20,P,[P],lambda app:self.rules if self.profile=='protected' else 0)

    def intersects(self,rules,minimum,maximum):
        box=[self.read(minimum+i*4,'i') for i in range(3)]+[self.read(maximum+i*4,'i') for i in range(3)]
        self.rule_queries.append(box)
        return 1

    def get_block(self,world,x,y,z):
        if self.profile=='dry':
            initial=3
        elif y>=64:
            initial=8 if self.profile=='flowing' else 9
        else:
            choices=[3,3|(1<<16),3|(2<<16),2,82,13,1,0]
            initial=choices[(x*17+y*11+z*31)%len(choices)]
        state=self.changed.get((x,y,z),initial)
        import struct
        self.read_hash.update(struct.pack('<4i',x,y,z,state))
        self.read_count+=1
        return ((state&65535)<<4)|((state>>16)&15)

    def deposit(self,kind,seed,position,profile,maximum_radius):
        self.reset_world(profile,128,position)
        feature=self.alloc(32)
        state=82 if kind=='clay' else 12 if kind=='sand' else 13
        self.w(feature+0x10,'Q',self.blocks[state])
        self.w(feature+0x18,'I',maximum_radius)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        pos=self.alloc(12)
        for i,v in enumerate(position):self.w(pos+4*i,'i',v)
        result=self.call(0x181400 if kind=='clay' else 0x5c3e70,I,[P,P,P,P],[feature,self.world,self.random,pos])&255
        after=self.call(0x58fd50,D,[P],[self.random])
        return {'kind':kind,'seed':seed,'position':position,'profile':profile,'maximum_radius':maximum_radius,
                'state':state,'result':bool(result),**self.world_result(),'random_after':after}


parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):
    parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=DepositOracle(args.eboot,args.libc)
cases=[]
for seed in [0,1,-987654321,9223372036854775807]:
    for kind,radius in [('clay',4),('sand',7),('gravel',6)]:
        for profile in ['dry','water','flowing','protected']:
            for position in [[0,64,0],[-2479,64,2501]]:
                cases.append(oracle.deposit(kind,seed,position,profile,radius))
    print('Original deposit seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x181400','0x5c3e70'],'input_kind':'Controlled block/material and generation-rule callbacks; not PS4 world saves','cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/'ps4-deposits.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
