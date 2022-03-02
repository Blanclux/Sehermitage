/**
 * eccurve.c
 *  EC Curve Name Listing Program for OpenSSL
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <string.h>

#include "openssl/ec.h"
#include "openssl/evp.h"
#include "openssl/obj_mac.h"

void usage()
{
	printf("usage: eccurve {-i nID | -n curveName}\n");
}

int
main(int argc, char *argv[])
{
	size_t  crvLen = 0, n = 0;
	int     nid = 0;
	int     degree;
	EC_builtin_curve *curves = NULL;
	EC_GROUP *group;
	EC_KEY *eckey = NULL;
	const char *name;

	if (argc == 2 || argc > 3) {
		usage();
		return 1;
	}
	if (argc == 3) {
		if (strcmp(argv[1], "-i") == 0) {
			nid = atoi(argv[2]);
			if (nid == 0) {
				usage();
				return 1;
			}
			name = OBJ_nid2sn(nid);
			if (name == NULL) {
				fprintf(stderr, "No such EC curve.\n");
				return 1;
			}
			printf("NID = %2d,  Curve Name : %s\n", nid, name);
			return 0;
		}
		else if (strcmp(argv[1], "-n") == 0) {
			nid = OBJ_sn2nid(argv[2]);
			if (nid == 0) {
				fprintf(stderr, "No such EC curve.\n");
				return 1;
			}
			printf("NID = %2d,  Curve Name : %s\n", nid, argv[2]);
			return 0;
		} else {
			usage();
			return 1;
		}
	}
	printf("< EC Curve Name Listing (for OpenSSL) >\n");
	/* get a list of all internal curves */
	crvLen = EC_get_builtin_curves(NULL, 0);
	printf(" Number of EC curves = %d\n", crvLen);

	curves = OPENSSL_malloc(sizeof(EC_builtin_curve) * crvLen);
	if (curves == NULL) {
		return 1;
	}

	if (!EC_get_builtin_curves(curves, crvLen)) {
		fprintf(stderr, "Unable to get internal curves\n");
		return 1;
	}

	for (n = nid; n < crvLen; n++) {
		nid = curves[n].nid;

		/* create new ecdsa key */
		if ((eckey = EC_KEY_new()) == NULL) {
			return 1;
		}
		group = EC_GROUP_new_by_curve_name(nid);
		if (group == NULL) {
			return 1;
		}
		if (EC_KEY_set_group(eckey, group) == 0) {
			return 1;
		}
		EC_GROUP_free(group);

		degree = EC_GROUP_get_degree(EC_KEY_get0_group(eckey));
		printf("NID = %2d,  Curve Name : %s (degree = %d)\n",
			   OBJ_sn2nid(OBJ_nid2sn(nid)), OBJ_nid2sn(nid), degree);
		EC_KEY_free(eckey);
	}
	OPENSSL_free(curves);
	return 0;
}
