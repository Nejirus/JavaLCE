/* Analysis ABI bridge into the bounded original secondary-value body.
 * It enters after TLS/locks and returns at verified exits before TLS cleanup.
 * No original module startup or platform wrapper prologue executes. */
.text
.globl run_secondary_body
.type run_secondary_body,@function
run_secondary_body:
 push %rbx
 push %r12
 push %r14
 push %r15
 mov %rdi,%r11
 mov %rsi,%r14
 mov %edx,%r12d
 mov %ecx,%r15d
 mov %r8d,%ebx
 /* This is a body fragment expecting rsp % 16 == 0 before its helper
  * calls. Four pushes followed by CALL provide that fragment alignment. */
 call *%r11
 pop %r15
 pop %r14
 pop %r12
 pop %rbx
 ret
.size run_secondary_body,.-run_secondary_body
.section .note.GNU-stack,"",@progbits
