/* Analysis boundary for stopping a constructor at its material base call.
 * Both base bodies execute separately afterward, with these captured args.
 * No longjmp crosses a Python callback or C++ unwinder. Single-threaded only. */
#include <stdint.h>
#include <setjmp.h>
typedef void (*ctor_fn)(uintptr_t,uintptr_t,uintptr_t,uintptr_t,uintptr_t,uintptr_t);
static jmp_buf saved;
static uintptr_t result[4];
static void stop(uintptr_t self,uintptr_t material,uintptr_t color,uintptr_t kind) {
    result[0]=self;result[1]=material;result[2]=color;result[3]=kind;
    longjmp(saved,1);
}
void material_base(uintptr_t self,uintptr_t material,uintptr_t color) {
    stop(self,material,color,0x91fb0);
}
void material_color_base(uintptr_t self,uintptr_t material,uintptr_t color) {
    stop(self,material,color,0x92150);
}
int capture_material(uintptr_t ctor,const uintptr_t args[6],uintptr_t out[4]) {
    for(int i=0;i<4;i++)result[i]=0;
    if(setjmp(saved)==0)((ctor_fn)ctor)(args[0],args[1],args[2],args[3],args[4],args[5]);
    for(int i=0;i<4;i++)out[i]=result[i];
    return result[0]!=0;
}
