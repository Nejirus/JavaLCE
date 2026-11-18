/* Enter the original final registry-enumeration fragment with its supplied
 * node sentinel/frame fields. Exit before unrelated registry initialization. */
.text
.globl run_state_index_body
.type run_state_index_body,@function
run_state_index_body:
 push %rbp
 mov %rsp,%rbp
 push %rbx
 push %r12
 push %r13
 push %r14
 push %r15
 sub $0x2e0,%rsp
 mov %rdi,%r11
 mov %rsi,-0x2c0(%rbp)
 mov 8(%rsi),%rax
 /* The body fragment expects rsp % 16 == 0 before its helper calls. */
 call *%r11
 add $0x2e0,%rsp
 pop %r15
 pop %r14
 pop %r13
 pop %r12
 pop %rbx
 pop %rbp
 ret
.size run_state_index_body,.-run_state_index_body
.section .note.GNU-stack,"",@progbits
