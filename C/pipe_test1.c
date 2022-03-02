/* 
 * pipe_test1.c
 *  Pipe test program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <sys/types.h>
#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

int
main(int argc, char *argv[])
{
	int     pipefd[2];
	pid_t   pid;
	char    buf, *s;
	char   *text = "Data from parent process.";


	if (argc > 2) {
		fprintf(stderr, "usage: %s [string]\n", argv[0]);
		exit(EXIT_FAILURE);
	}
	if (argc == 1) {
		s = text;
	} else {
		s = argv[1];
	}

	if (pipe(pipefd) == -1) {
		perror("pipe");
		exit(EXIT_FAILURE);
	}

	pid = fork();
	if (pid == -1) {
		perror("fork");
		exit(EXIT_FAILURE);
	}

	if (pid == 0) {				/* Child process */
		close(pipefd[1]);		/* Close a pipe for writing */

		printf("Child receiving data: ");
		fflush(stdout);
		while (read(pipefd[0], &buf, 1) > 0) {
			write(STDOUT_FILENO, &buf, 1);
		}
		write(STDOUT_FILENO, "\n", 1);
		close(pipefd[0]);
		_exit(EXIT_SUCCESS);

	} else {					/* Parent process */
		close(pipefd[0]);		/* Close a pipe for reading */
		printf("Parent sending data:  %s\n", s);
		write(pipefd[1], s, strlen(s));
		close(pipefd[1]);		/* Close a pipe for writing (End of writing) */
		wait(NULL);
		exit(EXIT_SUCCESS);
	}
}

