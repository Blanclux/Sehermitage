/**
 * client.c
 *  Client sample program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>
#include <string.h>

int
main(int argc, char *argv[])
{
	char   *target;
	unsigned short port = 5000;
	int     csp;
	int     no;
	int     rc;

	struct sockaddr_in addr;
	struct hostent *host;

	char   *text = "Test message from client.";
	if (argc == 2 || argc == 3) {
		target = argv[1];
		if (argc == 3) {
			port = atoi(argv[2]);
		}
	} else {
		printf("Usage: client host [port_no]\n");
		return 1;
	}

	/* Get hostname (server) */
	if ((host = gethostbyname(target)) == NULL) {
		fprintf(stderr, "Can not get address %s\n", target);
		return 1;
	}
	memset(&addr, 0, sizeof(addr));
	addr.sin_port = htons(port);
	addr.sin_family = AF_INET;
	memmove(&(addr.sin_addr), host->h_addr, host->h_length);

	/* Create socket */
	csp = socket(AF_INET, SOCK_STREAM, 0);

	/* Connect to the server */
	printf("Trying to connect to %s\n", target);
	rc = connect(csp, (struct sockaddr *) &addr, sizeof(addr));
	if (rc == -1) {
		perror("connect");
		return 1;
	}

	/* Send data */
	for (no = 1; no < 11; no++) {
		printf("sending data %d\n", no);
		send(csp, text, strlen(text) + 1, 0);
		sleep(1);
	}

	close(csp);
	return 0;
}
