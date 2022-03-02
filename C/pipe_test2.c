/* 
 * pipe_test2.c
 *  Pipe test program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int
main(int argc, char *argv[])
{
	int     pipefd[2];
	pid_t   pid;
	char    *s;
	char   *text = "Data from parent process.\n";

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
	if (pid < 0) {
		perror("fork");
		exit(EXIT_FAILURE);
	} else if (pid == 0) {		/* Chile process */
		close(pipefd[1]);		/* Close a pipe for writing */


		dup2(pipefd[0], STDIN_FILENO);
		close(pipefd[0]);		/* Close a pipe for reading */

		execl("/bin/cat", "/bin/cat", NULL);	/* execute cat command */
		perror("/bin/cat");
		exit(EXIT_FAILURE);
	} else {					/* Parent process */
		close(pipefd[0]);		/* Close a pipe for reading */

		printf("Parent sending data:  %s\n", s);
		write(pipefd[1], s, strlen(s));
		sleep(1);

		close(pipefd[1]);
		exit(EXIT_SUCCESS);
	}
}
