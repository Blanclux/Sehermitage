/**
 *  Fork and wait program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <sys/wait.h>

#define CHILD_NUM 2
#define COUNT 5

int main(void){
    pid_t pid;
    int i;

    for (i = 0; i < CHILD_NUM; i++) {
        pid = fork();

        switch (pid) {
        case -1:
          fprintf (stderr, "Can't fork, error %d\n", errno);
          exit(EXIT_FAILURE);
        case 0:
          fprintf (stdout,"Child process start (pid = %d).\n", getpid());
          sleep (i*3);
          break;
        default:
          break;
        }

        /* Child process */
        if (pid == 0){
            int j;
            for (j = 0; j < COUNT; j++) {
                printf(" # child (pid = %d): %d\n", getpid(), j);
                sleep (1);
            }
            _exit(0);
        }
    }


    /* Parent process */
    if (pid != 0) {
        int status;
        int child_pid;
        printf ("Parent process start (pid = %d).\n", getppid());
        for (i = 0; i < COUNT; i++) {
            printf(" # parent: %d\n", i);
            sleep(1);
        }
        i = 0;
        while (i < CHILD_NUM) {
            child_pid = waitpid(-1, &status, WNOHANG);
            if (child_pid > 0) {
                i++;
                fprintf (stdout,"PID %d done.\n", child_pid);
            }
            sleep(1);
        }
    }
    return 0;
}
