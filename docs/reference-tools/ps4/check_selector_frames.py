"""Check actual screenshots for neighboring items painted over the PS4 frame.

Uses the original frame/highlight alpha masks and physical selector transforms
from capture-report.json. Baseline files are crafting-selected-{index}.png;
new files and selector_left/top/scale are named in report['captures'].
"""
import argparse
import json
import math
from pathlib import Path
from PIL import Image


def check(project, baseline, captures, report):
    art=project/'src/main/resources/assets/legacyconsole/textures/lce/ps4/crafting'
    frame=Image.open(art/'crafting_3slotlargev.png').convert('RGBA')
    highlight=Image.open(art/'craft_highlight_l_small.png').convert('RGBA')
    rows=[]
    for row in json.loads(report.read_text())['captures']:
        if not row['file'].startswith('crafting-size1-'):continue
        index=int(row['file'].split('selected')[1].split('.')[0])
        before=Image.open(baseline/f'crafting-selected-{index}.png').convert('RGB')
        after=Image.open(captures/row['file']).convert('RGB')
        assert before.size==after.size==(2560,1440)
        x,y,scale=row['selector_left'],row['selector_top'],row['selector_scale']
        old=new=count=0
        for px in range(math.floor(x),math.ceil(x+19*scale)):
            for py in range(math.floor(y+153*scale),math.ceil(y+235*scale)):
                u,v=math.floor((px+.5-x)/scale),math.floor((py+.5-y)/scale)
                if not(0<=u<19 and 153<=v<235):continue
                overlay=highlight.getpixel((u-6,v-140)) if u>=6 else (0,0,0,0)
                if frame.getpixel((u,v))!=(0,0,0,255) or overlay[3]!=0:continue
                old+=max(before.getpixel((px,py)))>4
                new+=max(after.getpixel((px,py)))>4
                count+=1
        rows.append(dict(selected=index,frame_pixels=count,
                         overwritten_in_1_0_1=old,overwritten_in_1_0_2=new))
    assert len(rows)==4
    assert sum(r['frame_pixels'] for r in rows)>100
    assert sum(r['overwritten_in_1_0_1'] for r in rows)>0
    assert sum(r['overwritten_in_1_0_2'] for r in rows)==0
    return dict(status='passed',method='Opaque original frame pixels outside the highlight at identical 1440p geometry',cases=rows)


if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__)
    for name in ['project-root','baseline-dir','capture-dir','report','output']:
        p.add_argument('--'+name,type=Path,required=True)
    a=p.parse_args();result=check(a.project_root,a.baseline_dir,a.capture_dir,a.report)
    a.output.write_text(json.dumps(result,indent=2)+'\n')
    print('Verified original selector-frame pixels in all four comparison cases')
