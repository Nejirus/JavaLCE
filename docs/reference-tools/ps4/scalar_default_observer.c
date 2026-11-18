#include <stdint.h>
#include <stddef.h>
#include <stdlib.h>

typedef void *(*with_fn)(void *,void *,void *);
typedef void (*default_fn)(void *,void *);
typedef void (*ctor_fn)(uintptr_t,uintptr_t,uintptr_t,uintptr_t,uintptr_t,uintptr_t);
struct event {uintptr_t kind,caller,owner,owner_vtable,definition,before,before_code,property,type_info,value,after,after_code;};
static struct event events[8192];
static size_t count;
static with_fn original_with;
static default_fn original_default;
static struct event *next(void){if(count>=8192)abort();return &events[count++];}
void configure(void *with,void *set_default){original_with=(with_fn)with;original_default=(default_fn)set_default;}
void *observe_with(void *state,void *property,void *boxed){
 struct event *event=next();event->kind=0;event->caller=(uintptr_t)__builtin_return_address(0);event->owner=*(uintptr_t *)((char *)state+0x18);event->owner_vtable=*(uintptr_t *)event->owner;event->definition=*(uintptr_t *)((char *)state+0x20);event->before=(uintptr_t)state;event->before_code=*(uint16_t *)((char *)state+0x28);event->property=(uintptr_t)property;event->type_info=*(uintptr_t *)((char *)boxed+8);event->value=*(uintptr_t *)((char *)boxed+16);
 void *result=original_with(state,property,boxed);event->after=(uintptr_t)result;event->after_code=result?*(uint16_t *)((char *)result+0x28):UINT64_MAX;return result;
}
void observe_default(void *owner,void *state){
 struct event *event=next();event->kind=1;event->caller=(uintptr_t)__builtin_return_address(0);event->owner=(uintptr_t)owner;event->owner_vtable=*(uintptr_t *)owner;event->definition=*(uintptr_t *)((char *)owner+0x188);event->before=*(uintptr_t *)((char *)owner+0x190);event->before_code=event->before?*(uint16_t *)(event->before+0x28):UINT64_MAX;event->after=(uintptr_t)state;event->after_code=state?*(uint16_t *)((char *)state+0x28):UINT64_MAX;event->property=0;event->type_info=0;event->value=0;original_default(owner,state);
}
size_t run(void *function,const uintptr_t args[6]){count=0;((ctor_fn)function)(args[0],args[1],args[2],args[3],args[4],args[5]);return count;}
const struct event *get_events(void){return events;}
