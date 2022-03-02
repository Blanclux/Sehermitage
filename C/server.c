/**
 * server.c
 *  Server sample program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

#define BUFSIZE 1024

int
main(int argc, char *argv[])
{
	int no;
	unsigned short port;		/* port number */
	int     ssp;				/* server socket */
	int     csp;				/* client socket */

	struct sockaddr_in srvAddr;
	struct sockaddr_in cliAddr;

	socklen_t cliAddrSize = sizeof(cliAddr);
	char    buf[BUFSIZE];
	int     rc;

	if (argc > 2) {
		printf("Usage: server [port_no]\n");
		exit(1);
	}
	port = (argc == 1) ? 5000 : atoi(argv[1]);

	memset(&srvAddr, 0, sizeof(srvAddr));
	srvAddr.sin_port = htons(port);
	srvAddr.sin_family = AF_INET;
	srvAddr.sin_addr.s_addr = htonl(INADDR_ANY);

	/* Create socket */
	ssp = socket(AF_INET, SOCK_STREAM, 0);

	/* Bind socket */
	bind(ssp, (struct sockaddr *) &srvAddr, sizeof(srvAddr));

	/* Listen socket */
	listen(ssp, 1);

	/* Accept */
	printf("Waiting for connection ...\n");
	if ((csp =
		 accept(ssp, (struct sockaddr *) &cliAddr, &cliAddrSize)) < 0) {
		perror("accept");
		return 1;
	}
	printf("Connected from %s\n", inet_ntoa(cliAddr.sin_addr));

	/* Receive data */
	no = 1;
	while (1) {
		rc = recv(csp, buf, BUFSIZE, 0);
		if (rc == 0) {
			printf ("client process end.\n");
			close(csp);
			break;
		}
		if (rc == -1) {
			perror("recv");
			close(csp);
			break;
		}
		printf("received %d: %s\n", no++, buf);
	}
	return 0;
}
