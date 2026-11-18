"""Export the pinned mcps4 panorama, anvil/furnace and host scene resources.

Input directory contains original MediaOrbis.arc entries; no replacement art.
"""
import argparse, hashlib, json, re, struct
from pathlib import Path
from export_workstations import rgba
from read_fui import Fui


def export(source, project):
    assets = project / 'src/main/resources/assets/legacyconsole'
    textures = assets / 'textures/ps4/workstations'
    data = json.loads((assets / 'ps4_workstations.json').read_text())
    graphics, widgets = Fui(source / 'skinHDGraphics.fui'), Fui(source / 'skinHD.fui')
    game = Fui(source / 'skinHDGraphicsInGame.fui')
    def sha(b): return hashlib.sha256(b).hexdigest()
    def nodes(f, index, frame=0):
        d=f.defs[index]; fr=f.frames[d[1]+frame]
        return f.geometry[fr['start']:fr['start']+fr['count']]
    def compose(a,b):
        return [a[0]*b[0]+a[2]*b[1],a[1]*b[0]+a[3]*b[1],
                a[0]*b[2]+a[2]*b[3],a[1]*b[2]+a[3]*b[3],
                a[0]*b[4]+a[2]*b[5]+a[4],a[1]*b[4]+a[3]*b[5]+a[5]]
    def bitmap(f,name,index=None):
        image=f.images[f.exports[name][1] if index is None else index]; converted=rgba(image['bytes'])
        filename=name.lower(); (textures/(filename+'.png')).write_bytes(converted)
        data['sprites'][name]=dict(texture=filename,size=image['size'],
            source_sha256=sha(image['bytes']),sha256=sha(converted))
    def expand(f,name,m):
        if name in data['panels'] or name in data['sprites']:return [dict(symbol=name,matrix=m)]
        kind,index=f.exports[name]
        if kind==3:bitmap(f,name);return [dict(symbol=name,matrix=m)]
        assert kind==2
        out=[]
        for child in nodes(f,index):
            kind,index=child['header'][1]&255,child['header'][3]
            symbol=next((k for k,v in f.exports.items() if v==(kind,index)),None)
            matrix=compose(m,child['matrix'])
            if symbol is None and kind==3:
                symbol=name+'_bitmap_'+str(index);bitmap(f,symbol,index)
                out.append(dict(symbol=symbol,matrix=matrix))
            else:
                if symbol is None:
                    symbol=name+'_local_'+str(kind)+'_'+str(index)
                    f.exports[symbol]=(kind,index)
                out+=expand(f,symbol,matrix)
        return out
    def panel(f,name):
        d=f.defs[f.exports[name][1]]; child=nodes(f,f.exports[name][1])[0]
        assert child['header'][1]&255==1
        nd,_,np,nsh,nb,nv,*_=f.counts
        shapes=0x98+nd*28+np*132; batches=shapes+nsh*28; vertices=batches+nb*44
        _,first,count=struct.unpack_from('<3I',f.b,shapes+child['header'][3]*28)
        assert count==9
        pieces=[]
        for i in range(count):
            at=batches+(first+i)*44; _,_,image=struct.unpack_from('<3I',f.b,at)
            start,length=struct.unpack_from('<2I',f.b,at+36)
            points=[struct.unpack_from('<2f',f.b,vertices+(start+j)*8) for j in range(length)]
            xs,ys=zip(*points);raw=f.images[image]['bytes'];converted=rgba(raw)
            filename=name.lower()+'_'+str(i);(textures/(filename+'.png')).write_bytes(converted)
            pieces.append(dict(texture=filename,rect=[min(xs),min(ys),max(xs)-min(xs),max(ys)-min(ys)],
                matrix=struct.unpack_from('<6f',f.b,at+12),source_sha256=sha(raw),sha256=sha(converted)))
        data['panels'][name]=dict(grid=[struct.unpack('<f',struct.pack('<I',v))[0] for v in d[5:9]],pieces=pieces)
    panel(graphics,'FJ_panel9Grid_16')
    for name in graphics.exports:
        if name in ['ListButton_Norm','ListButton_Over','ListButton_Outline','FJ_MainMenuButton_Norm',
                    'MainMenuButton_Over','MainMenuButton_Outline','Tick','Slider_Graphic',
                    'FJ_Slider_Background','SliderBackgroundHighlight'] or 'Tickbox' in name:
            if graphics.exports[name][0]==3:bitmap(graphics,name)
    for name in ['AnvilMenu','FurnaceMenu']:
        f=Fui(source/(name+'1080.fui')); root=next(n for n in f.geometry if n['name']=='MainPanel')
        controls={};background=[];objects={}
        for n in nodes(f,root['header'][3]):
            kind,index=n['header'][1]&255,n['header'][3]
            if n['name']=='BackgroundPanel':
                for c in nodes(f,index):background+=expand(game,f.imports[c['header'][3]],c['matrix'])
            elif n['name']=='AnvilCross':
                objects[n['name']]=[leaf for c in nodes(f,index)
                    for leaf in expand(game,f.imports[c['header'][3]],compose(n['matrix'],c['matrix']))]
            elif n['name']:controls[n['name']]=dict(matrix=n['matrix'],symbol=f.imports[index] if kind==4 else 'local:'+str(index))
            elif kind==4:background+=expand(game,f.imports[index],n['matrix'])
        data['scenes'][name]=dict(root=root['matrix'][4:],controls=controls,static=background,
            objects=objects,sha256=sha(f.b))
    labels=Fui(source/'skinHDGraphicsLabels.fui')
    for name in ['FJ_Label_Green','FJ_Label_Red']:
        node=nodes(labels,labels.exports[name][1])[0]
        data['typography'][name]=dict(labels.textfields[node['header'][3]],matrix=node['matrix'])
    for name in ['FJ_Label_CheckBoxBlack','FJ_Label_CheckBoxGrey','FJ_Label_CheckBoxSelected','FJ_Label','FJ_Label_Selected']:
        node=nodes(graphics,graphics.exports[name][1])[0]
        data['typography'][name]=dict(graphics.textfields[node['header'][3]],matrix=node['matrix'])
    for name in ['InGameHostOptions','InGameInfoMenu','InGamePlayerOptions','InGameTeleportMenu']:
        f=Fui(source/(name+'1080.fui'));controls={n['name']:dict(matrix=n['matrix'],symbol=f.imports[n['header'][3]] if n['header'][1]&255==4 else 'local:'+str(n['header'][3])) for n in f.geometry if n['name']}
        background=[]
        if name in ['InGameHostOptions','InGamePlayerOptions']:
            n=next(n for n in f.geometry if n['name']=='BackgroundPanel')
            for c in nodes(f,n['header'][3]):background+=expand(graphics,f.imports[c['header'][3]],compose(n['matrix'],c['matrix']))
        else:
            n=next(n for n in f.geometry if n['name'].endswith('MenuPanel'))
            background=[dict(symbol='FJ_panel9Grid_16',matrix=n['matrix'])]
        objects={}
        if name=='InGamePlayerOptions':
            parent=next(n for n in f.geometry if n['name']=='PlayerIcon')
            icon=widgets.exports['FJ_PlayerIcons'][1]
            for frame in range(widgets.defs[icon][2]):
                placed=[n for n in nodes(widgets,icon,frame) if n['header'][0]&1]
                assert len(placed)==1
                n=placed[0]
                objects['PlayerIcon'+str(frame)]=expand(graphics,widgets.imports[n['header'][3]],compose(parent['matrix'],n['matrix']))
        data['scenes'][name]=dict(root=[0,0],controls=controls,static=background,objects=objects,sha256=sha(f.b))
    # Preserve the native English strings referenced by both host constructors.
    native=json.loads((source.parent/'english-full.json').read_text())
    if not native:native={}
    codes=['01de3701','ef311201','5e940e19','07a2d3b2','3decac90','2a7c56c4','d554d957',
        'c0d12a27','d324428d','4c920637','38c1c044','faaaeb73','4aac0bc3','78e0a40d',
        '6f7d8591','65eef68e','dd235a1d','4296e646','9e0a027d','65c86b6c','8be63389',
        '88f67c3e','0c1f0645','f7e7f9a1','da9c2af0','2a3b2570','2e30f7dd','ee5328fd',
        'bf2df1b6','6d72db6a','77569663','4c8183da','db7b95e9','f3a668fd']
    data['labels'].update({k:native[k] for k in codes})
    (assets/'ps4_workstations.json').write_text(json.dumps(data,indent=2)+'\n')
    platform=Fui(source/'skinHDOrbis.fui');pano=assets/'textures/ps4/panorama';pano.mkdir(exist_ok=True)
    panorama=dict(repository=data['repository'],revision=data['revision'],
        skin_sha256=sha(widgets.b),platform_sha256=sha(platform.b),
        frame_clock='0x0081a760 (frames / 30 seconds)',default_day='0x00828eb0, default true',
        source_viewport=[1920,1080],images={},frames={})
    for name,symbol in [('day','Panorama_Background_S'),('night','Panorama_Background_N')]:
        image=platform.images[platform.exports[symbol][1]];raw=image['bytes']
        assert raw.startswith(b'\xff\xd8');(pano/(name+'.jpg')).write_bytes(raw)
        panorama['images'][name]=dict(texture=name+'.jpg',size=image['size'],sha256=sha(raw))
        clip='Panorama' if name=='day' else 'Panorama_Night';d=widgets.defs[widgets.exports[clip][1]]
        panorama['frames'][name]=[nodes(widgets,widgets.exports[clip][1],i)[0]['matrix'][4] for i in range(d[2])]
    assert panorama['frames']['day']==panorama['frames']['night']
    (assets/'ps4_panorama.json').write_text(json.dumps(panorama,indent=2)+'\n')


if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--input-dir',type=Path,required=True);p.add_argument('--project-root',type=Path,required=True)
    a=p.parse_args();export(a.input_dir,a.project_root)
