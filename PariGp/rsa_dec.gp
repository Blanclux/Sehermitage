/* 
 * rsa_dec.gp
 * RSA decryption
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
/* Modular exponentiation using binary method */
/* a^b mod n */
modexp (a, b, n) = {
	local(d, bin);
	d = 1;
	bin = binary(b);
	for (i = 1, length(bin),
		d = Mod(d*d, n);
		if (bin[i] == 1,
			d = Mod(d*a, n);
		);
	);
	return (d);
}

rsadec(n, e, C) = {
	local(p, q, pq, r, M);

	print("n  = ", n);
	print("e  = ", e);
	print("C  = ", C);

	pq = factor(n);
	p = pq[1,1];
	q = pq[2,1];
	print("\np  = ", p);
	print("q  = ", q);

	r = lcm(p-1, q-1);
	print("r  = ", r);

	d = lift(Mod(1/e, r));
	print("d  = ", d);

	M = lift(modexp(C, d, n));
	print("\nM  = ", M);

	C = modexp(M, e, n);
	print("C  = ", lift(C));

	return(M);
}
