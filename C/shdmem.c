/**
 * shdmem.c
 *  Shared memory test program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */  
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <sys/shm.h>

int main(void) 
{
	int    shmid;
	int    child_cnt;

	/* Generate shared memory */
	if ((shmid = shmget(IPC_PRIVATE, 100, 0600)) == -1) {
		perror("shmget");
		exit(EXIT_FAILURE);
	}
	 
	/* Child process 1 */ 
	if (fork() == 0) {
		char  *shmaddr;
		printf("Child process 1 start.\n");
		 
		/* Attach shared memory */ 
		if ((shmaddr = shmat(shmid, NULL, 0)) == (void *) -1) {
			perror("Child process 1: shmat");
			exit(EXIT_FAILURE);
		}
		strcpy(shmaddr, "Shared memory test data");
		printf("Child process 1: Write shared memory.\n \"%s\"\n", shmaddr);
		 
		/* Detach shared memory */
		if (shmdt(shmaddr) == -1) {
			perror("Child process 1: shmdt");
			exit(EXIT_FAILURE);
		}
		printf("Child process 1 end.\n");
		exit(EXIT_SUCCESS);
	}

	/* Child process 2 */
	if (fork() == 0) {
		char  *shmaddr;
		printf("Child process 2 start.\n");

		/* Wait */ 
		sleep(1);

		/* Attach shared memory */ 
		if ((shmaddr =
				 shmat(shmid, NULL, SHM_RDONLY)) == (void *) -1) {
			perror("Child process 2: shmat");
			exit(EXIT_FAILURE);
		}
		printf("Child process 2: Read shared memory content.\n");
		printf(" \"%s\"\n", shmaddr);

		/* Detach shared memory */ 
		if (shmdt(shmaddr) == -1) {
			perror("Child process 2 : shmdt");
			exit(EXIT_FAILURE);
		}
		printf("Child process 2 end.\n");
		exit(EXIT_SUCCESS);
	}

	/* Wait process termination */ 
	for (child_cnt = 0; child_cnt < 2; ++child_cnt) {
		wait(NULL);
	}

	/* Destroy shared memory */ 
	if (shmctl(shmid, IPC_RMID, NULL) == -1) {
		perror("shmctl");
		exit(EXIT_FAILURE);
	}
	printf("Parent process end.\n");
	return EXIT_SUCCESS;
}
